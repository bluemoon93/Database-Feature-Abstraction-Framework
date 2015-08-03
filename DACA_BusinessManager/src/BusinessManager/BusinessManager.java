/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import Client.ClientHandler;
import Client.ClientOperations;
import JavaTools.BECruds;
import JavaTools.JarClassLoader;
import LocalTools.BTC_Exception;
import LocalTools.CreateJarFile;
import LocalTools.PackageNameUtils;
import LocalTools.PackageUtils;
import Security.AuthenticationMethod;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BusinessManager implements IBusinessManager {

    private static BusinessManager instance = null;
    private final Map<String, HashMap<Integer, String>> bss = new HashMap<>();
    private Map<Integer, Map<Integer, String>> orqseqs;
    private ArrayList<DACAChangeListener> listeners;
    private Map<Integer, Integer> crudIdToSRId;
    private int sessionID;
    private String jarpath;
    private String jarfile;
    private ClientHandler CH;
    private String username;
    private String password;
    private String appname;
    private String Serverip;
    private int Serverport;
    private String myIP;
    private int myport;
    private ArrayList<BECruds> array = null;
    private File jarIn;

    public String getServerip() {
        return Serverip;
    }

    public int getServerport() {
        return Serverport;
    }

    public String getAppname() {
        return appname;
    }

    public String getMyIP() {
        return myIP;
    }

    public int getMyport() {
        return myport;
    }

    public String getUsername() {
        return username;
    }

    public void setBes(ArrayList<BECruds> tmp) {
        this.array = tmp;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getQuerySRID(int crudid) {
        return crudIdToSRId.get(crudid);
    }

    public void addCrudIdToSRId(int crudid, int SRId) {
        crudIdToSRId.put(crudid, SRId);
    }

    // Constructor
    private BusinessManager(String psusername, String pspassword, String psurl, int psport, String app, AuthenticationMethod authMethod, String certPath, String certPwd, boolean rebuild) throws BTC_Exception, IOException, SQLException {
        this.lock = new ReentrantReadWriteLock();
        this.sequences = new HashMap<>();
        this.activeSequences = new HashMap<>();
        this.crudIdToSRId = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.active = true;
        this.activeSequenceCounter = 0;

        repository(psusername + app + ".jar", rebuild);
        System.out.println("After rep");
        if (rebuild) {
            System.out.println("cfgServer");
            cfgServer(psusername, pspassword, app, authMethod, certPath, certPwd, psurl, psport, true);
            System.out.println("compile");
            Generator.compileGeneratedBEs();
        }
        System.out.println("After generate");
    }

    // Configures the current BusinessManager
    public static void configure(String psusername, String pspassword, String psurl, int psport, String app, AuthenticationMethod authMethod, String certPath, String certPwd, boolean rebuild) throws SQLException, IOException {
        assert psusername != null : "Username reference was null.";
        assert pspassword != null : "Password reference was null.";
        assert psurl != null : "Url reference was null.";
        assert app != null : "App name reference was null.";
        assert psport > 0 && psport < 65536 : "Invalid port number.";

        if (instance != null) {
            try {
                instance.close();
            } catch (Exception ex) {
            }
        }
        System.out.println("Creating new instance");
        instance = new BusinessManager(psusername, pspassword, psurl, psport, app, authMethod, certPath, certPwd, rebuild);
        System.out.println("created");
    }

    /**
     * Adds a DACAChangeListener and calls its methods with the current values.
     *
     * @param listener
     */
    @Override
    public void addAndCallDACAChangeListener(DACAChangeListener listener) {
        this.listeners.add(listener);
        listener.sequenceStatusChanged(active);
        listener.sequenceChanged(orqseqs);
        listener.policiesChanged(bss);
    }

    /**
     * Adds a DACAChangeListener.
     *
     * @param listener
     */
    @Override
    public void addDACAChangeListener(DACAChangeListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a DACAChangeListener.
     *
     * @param listener
     */
    @Override
    public void removeDACAChangeListener(DACAChangeListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Gets the Business Manager instance.
     * 
     * Requires it to have been configured.
     *
     * @return
     */
    public static IBusinessManager getInstance() {
        assert instance != null : "Business Manager is not configured.";
        return instance;
    }

    @Override
    public ISession getSession(String dburl) throws SQLException {
        assert dburl != null : "Url reference was null.";
        return new Session(CH.getSocket(), dburl);
    }

    public void addCRUD(int crudId, String crud, Class bs) {
        String _url = bs.getName();
        if (!bss.containsKey(_url)) {
            bss.put(_url, new HashMap<>());
        }
        if (!bss.get(_url).containsKey(crudId)) {
            bss.get(_url).put(crudId, crud);
        }
        listeners.stream().forEach((l) -> {
            l.policiesChanged(bss);
        });
    }

    public void removeCRUD(int crudId, Class bs) throws BTC_Exception {
        String _url = bs.getName();
        if (!bss.containsKey(_url)) {
            throw new BTC_Exception("Business Service does not exist.");
        }
        if (!bss.get(_url).containsKey(crudId)) {
            throw new BTC_Exception("This crudId is not attached to this Business Service.");
        }
        bss.get(_url).remove(crudId);
        listeners.stream().forEach((l) -> {
            l.policiesChanged(bss);
        });
    }

    // adds a new BE to the list to be compiled 
    @Override
    public void addBusinessSchema(Class bs) throws BTC_Exception {
        String _url = bs.getName();
        if (!bss.containsKey(_url)) {
            bss.put(_url, new HashMap<>());
            Class nbs = null;
            try {
                nbs = loadClassFromJar(_url, jarIn);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace(System.out);
            }
            Generator.AddClassBe(this.jarpath, this.jarfile, nbs, orqseqs);
            listeners.stream().forEach((l) -> {
                l.policiesChanged(bss);
            });
        }
    }

    public Class loadClass(String urlstr) throws ClassNotFoundException {
        ClassLoader classLoader = BusinessManager.class.getClassLoader();
        return classLoader.loadClass(urlstr);
    }

    @Override
    public void repository(String jarFile, boolean Rebuild) {
        System.out.println("at repository");
        this.jarpath = "./lib/";
        File directory = new File(this.jarpath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        this.jarfile = jarFile;
        File _jarIn = new File(this.jarpath + this.jarfile);
        if (Rebuild) {
            if (_jarIn.exists()) {
                boolean deleted = false;
                while (!deleted && _jarIn.exists()) {
                    deleted = _jarIn.delete();
                }
            }
        }
        System.out.println("out of repository");
    }

    @Override
    public void removeBusinessSchema(Class bs) throws BTC_Exception {
        String _url = bs.getName();
        bss.remove(_url);
        try {
            System.gc();
            CreateJarFile.DeleteBusinessEntity(this.jarpath, this.jarfile, bs);
        } catch (IOException ex) {
            Logger.getLogger(BusinessManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        listeners.stream().forEach((l) -> {
            l.policiesChanged(bss);
        });
    }

    @Override
    public <T> T instantiateBS(Class<T> bs, int crudId, ISession session) throws BTC_Exception {
        return instantiateBS(bs, crudId, session, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T instantiateBS(Class<T> bs, int crudId, ISession session, Integer activeSequence) throws BTC_Exception {
        String BS_Url = bs.getName();
        System.out.println("comparing "+BS_Url+" and key "+crudId);
        if (!bss.containsKey(BS_Url)) {
            throw new BTC_Exception("Business Service does not exist.");
        }
        
        HashMap<Integer,String> a = bss.get(BS_Url);
        System.out.println("hashmap has "+a.size()+" elements. The first one is "+a.keySet().toArray()[0]);
        
        if (!bss.get(BS_Url).containsKey(crudId)) {
            throw new BTC_Exception("This crudId is not attached to this Business Service.");
        }
        if (!this.crudIdToSRId.containsKey(crudId)) {
            throw new BTC_Exception("No Session Remote Query ID for specified crud ID");
        }

        boolean newSequence = (activeSequence == null);

        if (newSequence) {
            activeSequence = requestNewActiveSequence();
        }

        if (!authorize(activeSequence, BS_Url)) {
            throw new SequenceException("The Business Service " + BS_Url + " is not authorized to be instatiated at this point.");
        }
        // ... instantiate the BS

        File _jarIn = new File(this.jarpath + this.jarfile);
        Class<T> BTE;
        URL _url;
        Constructor c;
        try {
            _url = _jarIn.toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new BTC_Exception("MalformedURL, please check jar path!");
        }
        URL[] urls = new URL[]{_url};
        
        try (URLClassLoader cls = new URLClassLoader(urls)) {

            cls.setClassAssertionStatus(bs.getName(), true);
            BTE = (Class<T>) cls.loadClass(
                    PackageUtils.getCorrectClassloadPath(PackageNameUtils.getPackageNameDir(bs.getPackage().getName())
                            + bs.getSimpleName().substring(1, bs.getSimpleName().length()) + ".class")
            );
            Class types[] = new Class[8];
            types[0] = IBusinessManager.class;
            types[1] = ObjectOutputStream.class; //Socket.class; //Connection.class;
            types[2] = ObjectInputStream.class;
            types[3] = String.class;
            types[4] = Integer.class;
            types[5] = Integer.class;
            types[6] = IBusinessSchemaController.class;
            types[7] = Integer.class;
            
            try {
                c = BTE.getConstructor(types);
            } catch (NoSuchMethodException ex) {
                throw new BTC_Exception("Method not found, make sure, its the correct jar!");
            } catch (SecurityException ex) {
                throw new BTC_Exception("Security error " + ex.getMessage());
            }

            if (this.bss.get(BS_Url).get(crudId) == null) {
                throw new BTC_Exception("INVALID QUERY");
            }

            Object arglist[] = new Object[8];
            arglist[0] = this;
            arglist[1] = session.getOOS(); //session.getConnection();
            arglist[2] = session.getOIS();
            arglist[3] = this.bss.get(BS_Url).get(crudId);
            arglist[4] = getSessionID();
            arglist[5] = getQuerySRID(crudId);
            arglist[6] = this;
            arglist[7] = activeSequence;
            
            return (T) c.newInstance(arglist);//DebugProxy.newInstance(c.newInstance(arglist));
        } catch (ClassNotFoundException ex) {
            throw new BTC_Exception("Class not found, make sure its the correct jar!");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new BTC_Exception("Error, could not instantiate proxy!");
        } catch (IOException e) {
            throw new BTC_Exception("Error, could not close jar!");
        }
    }

    private void cfgServer(String username, String password, String AppName, AuthenticationMethod authMethod, String certPath, String certPwd, String ip, int port, boolean ReadFile) throws IOException, BTC_Exception {
        System.out.println("at cfgServer");
        this.username = username;
        this.password = password;
        this.appname = AppName;
        this.Serverip = ip;
        this.Serverport = port;
        System.out.println("at repository1");
        try {
            this.myIP = ClientOperations.getIp();
        } catch (UnknownHostException ex) {
            throw new BTC_Exception("Could not get an IP for this host, Host Exception!");
        }
        System.out.println("at repository2");
        try {
            this.myport = ClientOperations.findFreePort();
        } catch (IOException ex) {
            throw new BTC_Exception("Could not get an Port for this host, IO Exception!");
        }
        System.out.println("at repository3");
        this.CH = new ClientHandler(this, authMethod, certPath, certPwd);
        System.out.println("at repository4");
        this.jarIn = CH.getJar();
        System.out.println("at repository5");
        try {
            loadClassFromJar("BusinessInterfaces.IScrollable", this.jarIn);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.out);
        }
        System.out.println("at repository6");
        try {
            getSqlConfig();
        } catch (BTC_Exception ex) {
            Logger.getLogger(BusinessManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("at datamode");
        this.CH.GoToDataMode();
        System.out.println("at start");
        CH.start();
        System.out.println("at end of cfgServer");
    }

    public String getPassword() {
        return password;
    }

    private void getSqlConfig() throws BTC_Exception {
        Class bs = null;
        for (BECruds beCruds : array) {
            try {
                bs = loadClassFromJar(beCruds.getUrl(), jarIn);
                this.addBusinessSchema(bs);
            } catch (ClassNotFoundException ex) {
                System.err.println("Class :" + beCruds.getUrl() + " Not found please update jar.");
            }
            for (int j = 0; j < beCruds.getCruds().size(); j++) {
                this.addCRUD(beCruds.getCruds().get(j).getId(), beCruds.getCruds().get(j).getCruds(), bs);
            }
        }
    }

    private Class loadClassFromJar(String Classname, File jar) throws ClassNotFoundException {
        return (new JarClassLoader(jar)).loadClass(Classname);
    }

    public void setControlInfo(Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo) {
        orqseqs = new HashMap<>();
        lock.writeLock().lock();
        try {
            clearControl();
            for (Integer seq : controlinfo.keySet()) {
                for (int pos = 0; pos < controlinfo.get(seq).size(); pos++) {
                    Map.Entry<String, List<String>> entry = controlinfo.get(seq).get(pos);
                    addBEtoSequence(seq, entry.getKey(), entry.getValue());

                    if (!orqseqs.containsKey(seq)) {
                        orqseqs.put(seq, new HashMap<>());
                    }

                    orqseqs.get(seq).put(pos, entry.getKey());
                }
            }

            listeners.stream().forEach((l) -> {
                l.sequenceChanged(orqseqs);
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void policiesChanged() {
        listeners.stream().forEach((l) -> {
            l.policiesChanged(bss);
        });
    }

    public void updateControlInfo() {
        orqseqs = new HashMap<>();
        lock.writeLock().lock();
        try {
            for (Integer seq : sequences.keySet()) {
                for (int pos = 0; pos < sequences.get(seq).size(); pos++) {
                    SequenceEntry entry = sequences.get(seq).get(pos);

                    if (!orqseqs.containsKey(seq)) {
                        orqseqs.put(seq, new HashMap<>());
                    }

                    orqseqs.get(seq).put(pos, entry.getAuthorizedBS());
                }
            }

            listeners.stream().forEach((l) -> {
                l.sequenceChanged(orqseqs);
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Sequence Control
    /**
     * Lock object. Changes made to the choreography must lock as writers, other operations can use read locks.
     */
    private final ReentrantReadWriteLock lock;

    /**
     * The complete list of sequences.
     */
    private final Map<Integer, List<SequenceEntry>> sequences;

    /**
     * The list of active sequences.
     */
    private final Map<Integer, ActiveSequence> activeSequences;
    /**
     * The counter used to create new active sequences.
     */
    private int activeSequenceCounter;

    /**
     * Indicates if the authorization/validation is to be executed.
     */
    private boolean active;

    @Override
    public void setControlStatus(boolean active) {
        lock.writeLock().lock();
        try {
            System.out.println("BusinessSchemaController set to " + (active ? "active!" : "inactive!"));
            this.active = active;

            // Clear the active sequences so that previous BS can't be used.
            if (!active) {
                activeSequences.clear();
            }

            listeners.stream().forEach((l) -> {
                l.sequenceStatusChanged(active);
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes every sequence from the authorization chain.
     */
    @Override
    public void clearControl() {
        lock.writeLock().lock();
        try {
            System.out.println("Orchestration cleared!");
            sequences.clear();
            activeSequences.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean validateExecution(int activeSequence, String beUrl) {
        lock.readLock().lock();
        try {
            return !active || (activeSequences.containsKey(activeSequence) && activeSequences.get(activeSequence).isBEAlive(beUrl));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean authorize(int activeSequence, String beUrl) {
        lock.readLock().lock();
        try {
            if (!active) {
                return true;
            }

            boolean ret = false;
            ActiveSequence seq = activeSequences.get(activeSequence);

            // Initialize a list of sequences to stop following
            List<Integer> toRemove = new ArrayList<>();

            // For each possible BS sequences
            for (Integer i : seq.getCurrentBranches()) {
                List<SequenceEntry> sequence = sequences.get(i);

                // If the sequence as ended or the next BS is not the BS being authorized
                if (sequence.size() <= seq.getNextPosition() || !sequence.get(seq.getNextPosition()).getAuthorizedBS().equalsIgnoreCase(beUrl)) {
                    // Flag the sequence to be removed from the list of sequences to follow
                    toRemove.add(i);
                } else {
                    // if a sequence has the BS being authorized as the next BS then authorize
                    ret = true;
                }
            }

            // If the BE is authorized
            if (ret) {
                // Adds this BE to the list of BEs that can be executed and removes the BEs from the revoke list
                seq.updateAliveBEs(sequences, beUrl);
                // Removes the sequences previously determined to be removed.
                seq.removeBranches(toRemove);
                // Increments the position in the sequence.
                seq.incrementNextPosition();
            }

            // Return whether this BS was authorized.
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addBEtoSequence(int seq, String beUrl, List<String> revokeList) {
        assert seq > 0;
        assert beUrl != null;

        lock.writeLock().lock();
        try {
            System.out.println("Adding BE to SequenceEntry: " + seq + " " + beUrl + " " + revokeList);

            if (revokeList == null) {
                revokeList = new ArrayList<>();
            }

            if (!sequences.containsKey(seq)) {
                sequences.put(seq, new ArrayList<>());
            }

            List<SequenceEntry> sequence = this.sequences.get(seq);
            sequence.add(new SequenceEntry(beUrl, revokeList, null));

            for (Integer iseq : sequences.keySet()) {
                if (iseq != seq) {
                    List<SequenceEntry> tseqs = sequences.get(iseq);

                    for (int i = 0; i < tseqs.size() && i < sequence.size(); i++) {
                        String tbeurl = tseqs.get(i).getAuthorizedBS();
                        String nbeurl = sequence.get(i).getAuthorizedBS();

                        if (!tbeurl.equalsIgnoreCase(nbeurl)) {
                            break;
                        }

                        List<String> trevlist = tseqs.get(i).getRevokeList();
                        List<String> nrevlist = sequence.get(i).getRevokeList();

                        if (!trevlist.containsAll(nrevlist) || !nrevlist.containsAll(trevlist)) {
                            sequence.remove(sequence.size() - 1);
                            throw new SequenceException("Replicated sequence with different revoke lists.");
                        }
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeSequence(int seq) {
        assert seq > 0;

        lock.writeLock().lock();
        try {
            System.out.println("Remove SequenceEntry: " + seq);
            sequences.remove(seq);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int requestNewActiveSequence() {
        lock.writeLock().lock();
        try {
            activeSequences.put(activeSequenceCounter, new ActiveSequence(sequences.keySet()));
            return this.activeSequenceCounter++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Compiles added Business Schemas.
     */
    public void compileAddedBusinessSchemas() {
        Generator.compileGeneratedBEs();
    }

    /**
     * Closes this resource, relinquishing any underlying resources. This method is invoked automatically on objects managed by the {@code try}-with-resources statement.
     * 
     * <p>
     * While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to declare concrete implementations of the {@code close} method to throw more specific exceptions, or to throw no exception at all if
     * the close operation cannot fail.
     * 
     * <p>
     * <em>Implementers of this interface are also strongly advised to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * 
     * This exception interacts with a thread's interrupted status, and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * 
     * More generally, if it would cause problems for an exception to be suppressed, the {@code AutoCloseable.close} method should not throw it.
     * 
     * <p>
     * Note that unlike the {@link java.io.Closeable#close close} method of {@link java.io.Closeable}, this {@code close} method is <em>not</em> required to be idempotent. In other words, calling this
     * {@code close} method more than once may have some visible side effect, unlike {@code Closeable.close} which is required to have no effect if called more than once.
     * 
     * However, implementers of this interface are strongly encouraged to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        CH.close();
        instance = null;
        System.gc();
    }
}
