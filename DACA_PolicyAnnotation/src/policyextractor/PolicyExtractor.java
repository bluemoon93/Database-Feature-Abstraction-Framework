package policyextractor;

import LocalTools.*;
import Security.AuthenticationMethod;
import Security.Authenticators.Client.DacaClientAuthenticator;
import Security.Authenticators.Client.DacaClientCRAuthenticator;
import Security.Authenticators.Client.DacaClientPlainAuthenticator;
import Security.Authenticators.Client.DacaClientPSKSSLAuthenticator;
import Security.Authenticators.Client.DacaClientSSLAuthenticator;
import util.MyBoolean;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.BuildPath;

/**
 * @author Diogo Regateiro
 * diogoregateiro@ua.pt
 */
public class PolicyExtractor {

    /**
     * Creates new form MainGui
     */
    private File itfsjar;
    private File tmpdir;
    private Socket socket;
    private Map<String, String> roles;
    private final ArrayList<String> javapaths = new ArrayList<>();

    public boolean ObtainInfo(String usr, String pwd, String url, int port, String app, String jarpath, AuthenticationMethod authMethod, String keyStorePath, String keyStorePwd, boolean updateJar, Map<Integer, Map<Integer, String>> orqinfo, MyBoolean orqActive) throws BTC_Exception {

        try {
            DacaClientAuthenticator auth;

            switch (authMethod) {
                case PLAIN:
                    auth = new DacaClientPlainAuthenticator(url, port);
                    break;
                case ChallengeResponse:
                    auth = new DacaClientCRAuthenticator(url, port);
                    break;
                case PSKSSL:
                    auth = new DacaClientPSKSSLAuthenticator(url, port);
                    break;
                case SSL:
                default:
                    auth = new DacaClientSSLAuthenticator(url, port, keyStorePath, keyStorePwd);
            }

            socket = auth.authenticate(app, usr, pwd);

            if (updateJar) {
                File finaljar = new File(BuildPath.DIR, jarpath);

                Filename jarfile = new Filename(finaljar.getAbsolutePath(), '/', '.');
                File dir = new File(jarfile.path());
                System.out.println("Going for: "+dir.getAbsolutePath() + "/" + SessionIdentifierGenerator.nextSessionId() + "/");
                this.tmpdir = new File(dir.getAbsolutePath() + "/" + SessionIdentifierGenerator.nextSessionId() + "/");
                if (tmpdir.exists()) {
                    DirectoryOp.removeDirectory(tmpdir);
                }

                DirectoryOp.CreateDirectory(tmpdir);

                if (getJar(app)) {
                    roles = getRoles(app);
                    createRoles();
                    Compile(true, "");
                    CreateJarFile.addClassesToJar(finaljar, this.itfsjar, this.tmpdir);
                } else {
                    throw new BTC_Exception("Couldn't get JAR.");
                }
            } else {
                roles = getRoles(app);
            }

            getOrqInfo(roles, orqinfo, orqActive);

        } finally {
            try {
                if (socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
            }
        }
        return true;
    }

    boolean getJar(String appref) {
        File tmp;

        FileOutputStream fout;
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out.println("getJar " + appref);
            out.flush();
            
            tmp = new File(this.tmpdir + "/" + "temp.jar");
            fout = new FileOutputStream(tmp);
            
            int size = Integer.parseInt(in.readLine());
            int byteCount = 0;
            byte[] buffer = new byte[size];
            
            while (byteCount < size) {
                byteCount += this.socket.getInputStream().read(buffer, byteCount, size - byteCount);
            }
            
            fout.write(buffer);
            
            this.itfsjar = tmp;
            return true;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
                throw new RuntimeException(ex);
        }
    }

    Map<String, String> getRoles(String appref) {
        Map<String, String> troles = new HashMap<>();

        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out.println("getRoles " + appref);
            out.flush();

            String line;
            while (!(line = in.readLine()).equalsIgnoreCase("END")) {
                String[] resp = line.split("[ ]");
                if (resp.length == 2) {
                    troles.put(resp[0], resp[1]);
                } else {
                    troles.put(resp[0], null);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PolicyExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return troles;
    }

    void createRoles() throws BTC_Exception {
        for (String key : roles.keySet()) {
            ArrayList<String> ext = getExtensions(key);
            Map<String, HashMap<Integer, String>> beinfo = getBeInfo(key);

            try {
                File Interface = new File(tmpdir.getAbsolutePath() + "/Role_" + key + ".java");
                this.javapaths.add(tmpdir.getAbsolutePath() + "/Role_" + key + ".java");
                try (BufferedWriter file = new BufferedWriter(new FileWriter(Interface))) {
                    file.append(CreateJava(key, ext, beinfo));
                }
            } catch (IOException ex) {
                throw new BTC_Exception("IOException : " + ex.getMessage());
            }
        }
    }

    Map<String, HashMap<Integer, String>> getBeInfo(String key) {
        Map<String, HashMap<Integer, String>> beinfo = new HashMap<>();
        ArrayList<Integer> beids = getBesids(key);
        for (int i = 0; i < beids.size(); i++) {
            beinfo.put(getBeUrl(beids.get(i)), new HashMap<Integer, String>());
            putBeSqls(beids.get(i), beinfo.get(getBeUrl(beids.get(i))));
        }
        return beinfo;
    }

    void putBeSqls(int beid, HashMap<Integer, String> hm) {
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out.println("getCRUDs " + beid);
            out.flush();

            String line;
            while (!(line = in.readLine()).equalsIgnoreCase("END")) {
                String[] resp = line.split("[ ]");
                if (hm != null) {
                    hm.put(Integer.parseInt(resp[0]), resp[1]);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PolicyExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String getBeUrl(int beid) {
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out.println("getBEUrl " + beid);
            out.flush();

            return in.readLine();
        } catch (IOException ex) {
            Logger.getLogger(PolicyExtractor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    ArrayList<Integer> getBesids(String key) {
        ArrayList<Integer> beids = new ArrayList<>();
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out.println("getBEsIDs " + key);
            out.flush();

            String line;
            while (!(line = in.readLine()).equalsIgnoreCase("END")) {
                beids.add(Integer.parseInt(line));
            }
        } catch (IOException ex) {
            Logger.getLogger(PolicyExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return beids;
    }

    ArrayList<String> getExtensions(String role) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : roles.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                if (value.toString().compareTo(role) == 0 && key.compareTo(role) != 0) {
                    list.add(key);
                }
            }
        }
        return list;

    }

    private void Compile(Boolean classpath, String classpathstr) {
        
        List<String> options = new ArrayList<>();
        if (classpath) {
            options.add("-classpath");
            StringBuilder sb = new StringBuilder();
            sb.append(classpathstr);
            sb.append(this.itfsjar);
            options.add(sb.toString());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("NULL COMPILER3");
            System.exit(0);
            return;
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(this.javapaths);
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        boolean sucess = task.call();

        if (!sucess) {
            System.err.println("List of errors\n" + diagnostics.getDiagnostics().toString());
            System.exit(0);
        }
    }

    private String CreateJava(Object key, ArrayList<String> ext, Map<String, HashMap<Integer, String>> beinfo) {
        String cls = "";

        cls += getImports(beinfo);
        cls += getDeclaration(key, ext);
        cls += getBody(beinfo);
        cls += "}\n";
        return cls;
    }

    private String getImports(Map<String, HashMap<Integer, String>> beinfo) {
        String imports = "";
        imports += "package Classes;\n\n";
        for (String key : beinfo.keySet()) {
            imports += "import " + key + ";\n";
        }
        return imports;
    }

    private String getDeclaration(Object key, ArrayList<String> ext) {
        String dec = "";
        dec += "public interface Role_" + key.toString();
        if (ext.size() > 0) {
            dec += " extends ";
            for (int i = 0; i < ext.size(); i++) {
                if (i < ext.size() - 1) {
                    dec += "Role_" + ext.get(i) + ",";
                } else {
                    dec += "Role_" + ext.get(i);
                }
            }
        }
        dec += "{\n\n";
        return dec;
    }

    private String getBody(Map<String, HashMap<Integer, String>> beinfo) {
        String body = "";

        for (String key : beinfo.keySet()) {
            String name = key.substring(key.lastIndexOf(".") + 1, key.length());
            body += " public final Class<" + name + ">" + name.substring(1, name.length()).toLowerCase() + "=" + name + ".class;\n";
            HashMap<Integer, String> hm = beinfo.get(key);

            for (Map.Entry<Integer, String> entry : hm.entrySet()) {
                Integer key2 = entry.getKey();
                Object value = entry.getValue();
                body += "public final int " + name.substring(1, name.length()).toLowerCase() + "_" + value + "=" + key2 + ";\n";

            }
        }
        return body;
    }

    private void getOrqInfo(Map<String, String> roles, Map<Integer, Map<Integer, String>> orqinfo, MyBoolean orqActive) {
        try {
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out.println("getSeqStatus");
            out.flush();
            orqActive.setValue(Boolean.parseBoolean(in.readLine()));

            for (String role : roles.keySet()) {
                out.println("getSeqInfo " + role);
                out.flush();

                String line;
                while (!(line = in.readLine()).equalsIgnoreCase("END")) {
                    String[] resp = line.split("[ ]");
                    Integer seq = Integer.parseInt(resp[0]);
                    Integer pos = Integer.parseInt(resp[1]);
                    String beurl = resp[2];

                    if (!orqinfo.containsKey(seq)) {
                        orqinfo.put(seq, new HashMap<Integer, String>());
                    }

                    orqinfo.get(seq).put(pos, beurl);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PolicyExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
