package BusinessManager.Utils;

import JavaTools.FileUtils;
import LocalTools.*;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenerateBE {

    // Source code that checks orchestration validity
    protected final String validationSourceCode;
    
    private final ArrayList<Interfaces> interfaces = new ArrayList<>();
    protected final BusinessEntity_Context BE;
    private final String path;
    private final String JarName;
    private final String Filename;
    private final File RandomDir;
    private final File MainInterfaceDir;
    private final File commonBEInterfaceDir;
    private final String Rnd;
    private final Map<Integer, Map<Integer, String>> controlinfo;

    public GenerateBE(BusinessEntity_Context BE, String path, String jarName, Map<Integer, Map<Integer, String>> controlinfo) throws BTC_Exception {
        this.BE = BE;
        this.path = path;
        this.JarName = jarName;
        this.Filename = "BE";
        this.controlinfo = controlinfo;
        this.commonBEInterfaceDir = new File(this.path + "BEInterfaces/");

        if (BE.getPackage().length() == 0) {
            throw new BTC_Exception("Interface needs to be in a Package.");
        }

        validationSourceCode = String.format(
                "\tif(!controller.validateExecution(activeSequence, \"%s.%s\")) {\n"
                + "\t\tthrow new SequenceException(\"%s was used out of order!\");\n"
                + "\t}\n", BE.getPackage(), BE.getName(), BE.getName()
        );

        Rnd = SessionIdentifierGenerator.nextSessionId();
        RandomDir = new File(this.path + Rnd + "/");
        this.MainInterfaceDir = new File(RandomDir.getAbsolutePath() + "/" + PackageNameUtils.getPackageNameDir(BE.getPackage()));

        File defaultInterfacesDir = new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/");
        if (!defaultInterfacesDir.mkdirs()) {
            throw new BTC_Exception("Could not create Temporary Directories");
        }
        GenerateDefaultInterfaces();

        GenerateSessionInterface();
        if (!this.MainInterfaceDir.exists()) {
            if (!this.MainInterfaceDir.mkdirs()) {
                throw new BTC_Exception("Could not create Temporary Directories");
            }
        }
    }

    // Generates the interfaces and main .java
    public void generate() {
        File BEInterfaceDir = new File(this.commonBEInterfaceDir, PackageNameUtils.getPackageNameDir(BE.getPackage()));
        FileUtils.deleteContents(BEInterfaceDir);

        GenerateOtherInterfaces();
        
        SelectWhichInterfaces();
        BEInterfaceDir.mkdirs();
        InterfaceGenerate.CreateSuperInterface(BEInterfaceDir, BE.getName(), this.interfaces, BE);

        GenerateMainClassBE(controlinfo);
    }

    // Generates the Read, Execute, Insert, Update and Set interfaces
    private void GenerateOtherInterfaces() throws BTC_Exception {
        if (BE.isRead()) {

            File ReadDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(BE.getReadPackage()));
            if (!ReadDirectory.exists()) {
                if (!ReadDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(ReadDirectory, "", Interfaces.IRead, BE);
            if (BE.ReadHaveAdditionalInterfaces()) {
                ArrayList<Class> readitfstemp = BE.getReadAdditionalInterfaces();
                for (int i = 0; i < readitfstemp.size(); i++) {
                    File ReadInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(PackageNameUtils.getPackage(readitfstemp.get(i))));
                    if (!ReadInterfaceDirectory.exists()) {
                        if (!ReadInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(ReadInterfaceDirectory, Interfaces.IRead, readitfstemp.get(i));
                }

            }
        }
        if (BE.isExecute()) {
            File ExecuteDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(BE.getExecutePackage()));
            if (!ExecuteDirectory.exists()) {
                if (!ExecuteDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(ExecuteDirectory, "", Interfaces.IExecute, BE);
            if (BE.ExecuteHaveAdditionalInterfaces()) {
                ArrayList<Class> executeitfstemp = BE.getExecuteInterfaces();
                for (int i = 0; i < executeitfstemp.size(); i++) {
                    File ExecuteInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(PackageNameUtils.getPackage(executeitfstemp.get(i))));
                    if (!ExecuteInterfaceDirectory.exists()) {
                        if (!ExecuteInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(ExecuteInterfaceDirectory, Interfaces.IRead, executeitfstemp.get(i));
                }

            }
        }
        if (BE.IsUpdate()) {
            File UpdateDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(BE.getUpdatePackage()));
            if (!UpdateDirectory.exists()) {
                if (!UpdateDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(UpdateDirectory, "", Interfaces.IUpdate, BE);

            if (BE.UpdateHaveAdditionalInterfaces()) {
                ArrayList<Class> updateitfstemp = BE.getUpdateAdditionalInterfaces();
                for (int i = 0; i < updateitfstemp.size(); i++) {
                    File UpdateInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(PackageNameUtils.getPackage(updateitfstemp.get(i))));
                    if (!UpdateInterfaceDirectory.exists()) {
                        if (!UpdateInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(UpdateInterfaceDirectory, Interfaces.IUpdate, updateitfstemp.get(i));
                }

            }
            if (BE.UpdateHaveAdditionalInterfacesInMethods()) {
                ArrayList<Class> updateitfstemp = BE.getUpdateInterfaces();
                for (int i = 0; i < updateitfstemp.size(); i++) {
                    File ExecuteInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(updateitfstemp.get(i).getName()));
                    if (!ExecuteInterfaceDirectory.exists()) {
                        if (!ExecuteInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(ExecuteInterfaceDirectory, Interfaces.IRead, updateitfstemp.get(i));
                }

            }

        }
        if (BE.IsInsert()) {
            File InsertDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(BE.getInsertPackage()));
            if (!InsertDirectory.exists()) {
                if (!InsertDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(InsertDirectory, "", Interfaces.IInsert, BE);

            if (BE.InsertHaveAdditionalInterfaces()) {
                ArrayList<Class> insertitfstemp = BE.getInsertAdditionalInterfaces();
                for (int i = 0; i < insertitfstemp.size(); i++) {
                    File InsertInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(PackageNameUtils.getPackage(insertitfstemp.get(i))));
                    if (!InsertInterfaceDirectory.exists()) {
                        if (!InsertInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(InsertInterfaceDirectory, Interfaces.IInsert, insertitfstemp.get(i));
                }

            }
            if (BE.InsertHaveAdditionalInterfacesInMethods()) {
                ArrayList<Class> insertitfstemp = BE.getInsertInterfaces();
                for (int i = 0; i < insertitfstemp.size(); i++) {
                    File ExecuteInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(insertitfstemp.get(i).getName()));
                    if (!ExecuteInterfaceDirectory.exists()) {
                        if (!ExecuteInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(ExecuteInterfaceDirectory, Interfaces.IRead, insertitfstemp.get(i));
                }

            }
        }

        if (BE.isSet()) {
            File SetDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackageNameDir(BE.getSetPackage()));
            if (!SetDirectory.exists()) {
                if (!SetDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(SetDirectory, "", Interfaces.ISet, BE);
            if (BE.SetHaveAdditionalInterfaces()) {
                ArrayList<Class> setitfstemp = BE.getSetInterfaces();
                for (int i = 0; i < setitfstemp.size(); i++) {
                    File SetInterfaceDirectory = new File(this.commonBEInterfaceDir + "/" + PackageNameUtils.getPackage(setitfstemp.get(i)));
                    if (!SetInterfaceDirectory.exists()) {
                        if (!SetInterfaceDirectory.mkdirs()) {
                            throw new BTC_Exception("Could not create Temporary Directories");
                        }
                    }
                    InterfaceGenerate.CreateAdditionalInterface(SetInterfaceDirectory, Interfaces.IRead, setitfstemp.get(i));
                }

            }
        }

    }

    private void GenerateSessionInterface() {
        try {
            File BusinessManDir = new File(commonBEInterfaceDir + "/BusinessManager/");
            BusinessManDir.mkdirs();

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "BTC_Exception.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("public class BTC_Exception extends Exception {\n");
                pw.write("\tpublic BTC_Exception(String exception) {\n");
                pw.write("\t\tsuper(exception);\n");
                pw.write("\t}\n");
                pw.write("}");
                pw.flush();
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "SequenceException.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("public class SequenceException extends RuntimeException {\n");
                pw.write("public SequenceException(String message) {\n");
                pw.write("\tsuper(message);\n");
                pw.write("}\n");
                pw.write("public SequenceException(String message, Throwable cause) {\n");
                pw.write("\tsuper(message, cause);\n");
                pw.write("}\n");
                pw.write("public SequenceException(Throwable cause) {\n");
                pw.write("\tsuper(cause);\n");
                pw.write("}\n");
                pw.write("public SequenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {\n");
                pw.write("\tsuper(message, cause, enableSuppression, writableStackTrace);\n");
                pw.write("}\n");
                pw.write("public SequenceException() {}\n");
                pw.write("}");
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "ITransaction.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("import java.sql.*;\n\n");
                pw.write("public interface ITransaction {\n");
                pw.write("\tvoid commit() throws SQLException;\n");
                pw.write("\tint getTransactionIsolationLevel() throws SQLException;\n");
                pw.write("\tvoid releaseSavepoint(Savepoint savepoint) throws SQLException;\n");
                pw.write("\tvoid rollBack() throws SQLException;\n");
                pw.write("\tvoid rollBack(Savepoint savepoint) throws SQLException;\n");
                pw.write("\tvoid setAutoCommit(boolean autoCommit) throws SQLException;\n");
                pw.write("\tSavepoint setSavepoint() throws SQLException;\n");
                pw.write("\tSavepoint setSaveSavepoint(String name) throws SQLException;\n");
                pw.write("\tvoid setTransactionIsolation(int level) throws SQLException;");
                pw.write("}");
                pw.flush();
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "IBusinessSchemaController.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("import java.util.List;\n\n");
                pw.write("public interface IBusinessSchemaController {\n");
                pw.write("\tpublic boolean validateExecution(int activeSequence, String beUrl);\n");
                pw.write("\tpublic boolean authorize(int activeSequence, String beUrl);\n");
                pw.write("\tpublic void addBEtoSequence(int seq, String beUrl, List<String> revokeList);\n");
                pw.write("\tpublic void removeSequence(int seq);\n");
                pw.write("\tpublic int requestNewActiveSequence();\n");
                pw.write("\tpublic void setControlStatus(boolean active);\n");
                pw.write("\tpublic void clearControl();");
                pw.write("}");
                pw.flush();
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "ISession.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("public interface ISession extends ITransaction, AutoCloseable {\n");
                pw.write("\tpublic void getConnection();\n");
                pw.write("}");
                pw.flush();
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "IAdaptation.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("public interface IAdaptation {\n");
                pw.write("\tpublic void removeBusinessSchema(Class bs) throws BTC_Exception;\n");
                pw.write("\tpublic void addBusinessSchema(Class bs) throws BTC_Exception;\n");
                pw.write("\tpublic void repository(String jarFile, boolean Rebuild);\n");
                pw.write("}");
                pw.flush();
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "DACAChangeListener.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("import java.util.HashMap;\n");
                pw.write("import java.util.Map;\n\n");
                pw.write("public interface DACAChangeListener {\n");
                pw.write("\tpublic void sequenceStatusChanged(boolean status);\n");
                pw.write("\tpublic void sequenceChanged(Map<Integer, Map<Integer, String>> sequences);\n");
                pw.write("\tpublic void policiesChanged(Map<String, HashMap<Integer, String>> businessSchemas);\n");
                pw.write("}\n");
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "IUser.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("import java.sql.*;\n\n");
                pw.write("public interface IUser {\n");
                pw.write("\tpublic void addAndCallDACAChangeListener(DACAChangeListener listener);\n");
                pw.write("\tpublic void addDACAChangeListener(DACAChangeListener listener);\n");
                pw.write("\tpublic void removeDACAChangeListener(DACAChangeListener listener);\n");
                pw.write("\tpublic <T> T instantiateBS(Class<T> bs, int crudId, ISession session) throws BTC_Exception;\n");
                pw.write("\tpublic <T> T instantiateBS(Class<T> bs, int crudId, ISession session, Integer activeSequence) throws BTC_Exception;\n");
                pw.write("\tpublic ISession getSession(String dburl) throws SQLException;\n");
                pw.write("}");
                pw.flush();
            }

            try (PrintWriter pw = new PrintWriter(new File(BusinessManDir, "IBusinessManager.java"))) {
                pw.write("package BusinessManager;\n\n");
                pw.write("public interface IBusinessManager extends IAdaptation, IUser, IBusinessSchemaController, AutoCloseable {\n");
                pw.write("}");
                pw.flush();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenerateBE.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void GenerateDefaultInterfaces() {
        InterfaceGenerate.CreateInterface(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IDelete, BE);
        InterfaceGenerate.CreateInterface(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IScrollable, BE);
        InterfaceGenerate.CreateInterface(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IResult, BE);
        InterfaceGenerate.CreateInterface(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IForwardOnly, BE);
        Compile(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/IDelete.java"), false, "");
        Compile(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/IScrollable.java"), false, "");
        Compile(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/IForwardOnly.java"), false, "");
        Compile(new File(RandomDir.getAbsolutePath() + "/BusinessInterfaces/IResult.java"), false, "");
    }

    public void Compile(File file, Boolean classpath, String classpathstr) {
        //System.out.println(System.getProperty("java.home"));
        //System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.8.0_20");
        //System.out.println(System.getProperty("java.home"));
        List<String> options = new ArrayList<>();
        if (classpath) {
            options.add("-classpath");
            StringBuilder sb = new StringBuilder();
            sb.append(classpathstr);
            options.add(sb.toString());
        }
        JavaCompiler compiler;
        if ((compiler = ToolProvider.getSystemJavaCompiler()) == null) {
            System.out.println("Null compiler!");
            System.exit(0);
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(file.getAbsolutePath()));
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        boolean sucess = task.call();

        if (!sucess) {
            System.err.println("Couldnt compile file " + file.getAbsolutePath());
            System.err.println("List of errors\n" + diagnostics.getDiagnostics().toString());
            System.exit(0);
        }
    }

    // Adds the interfaces to the list of interfaces of this BE
    private void SelectWhichInterfaces() {

        if (BE.IsUID()) {
            this.interfaces.add(Interfaces.IResult);
        }

        if (BE.isSet()) {
            this.interfaces.add(Interfaces.ISet);
        }

        this.interfaces.add(Interfaces.IExecute);

        if (BE.isScrollable()) {
            this.interfaces.add(Interfaces.IScrollable);
        }

        if (BE.isForwardOnly()) {
            this.interfaces.add(Interfaces.IForwardOnly);
        }

        if (this.BE.isRead()) {
            this.interfaces.add(Interfaces.IRead);
        }
        if (this.BE.IsUpdate()) {
            this.interfaces.add(Interfaces.IUpdate);
        }

        if (this.BE.IsInsert()) {
            this.interfaces.add(Interfaces.IInsert);
        }

        if (this.BE.IsDelete()) {
            this.interfaces.add(Interfaces.IDelete);
        }
    }

    public void BuildInterfaces() {
        for (int i = 0; i < this.interfaces.size(); i++) {
            InterfaceGenerate.CreateInterface(RandomDir, Filename, this.interfaces.get(i), BE);
        }
    }

    public void GenerateStateClass() {
        BufferedWriter file = null;
        try {
            File StateClass;
            StateClass = new File(this.MainInterfaceDir + "/" + "_stateop" + ".java");
            file = new BufferedWriter(new FileWriter(StateClass));
            file.append(CreateStateClass());
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String CreateStateClass() {
        StringBuilder stateop = new StringBuilder();
        stateop.append("package ").append(BE.getPackage()).append(";\n\n");
        stateop.append("public enum _stateop {idle,updating,inserting;}");
        return stateop.toString();
    }

    // Creates the main .java file
    private void GenerateMainClassBE(Map<Integer, Map<Integer, String>> controlinfo) throws BTC_Exception {
        BufferedWriter file = null;
        try {
            File MainClass;
            MainClass = new File(this.MainInterfaceDir + "/" + BE.getName().substring(1, BE.getName().length()) + ".java");
            file = new BufferedWriter(new FileWriter(MainClass));
            file.append(CreateMainClass(controlinfo));
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Creates the text for the main .java
    public String CreateMainClass(Map<Integer, Map<Integer, String>> controlinfo) throws BTC_Exception {
        StringBuilder str = new StringBuilder();

        str.append(getImports());
        str.append(getClassDecl());
        str.append(getCommonVariablesDecl());
        str.append(getVariablesDecl());
        str.append(getConstructor());
        
        // Execute (no if needed, always happens)
        str.append(getMethodsExecute());
        
        str.append(getSpecialMethod());
        str.append(getChoreographyMethod(controlinfo));

        // Probably these 3 interfaces represent the IRows interface
        if (interfaces.contains(Interfaces.IResult)) {
            str.append(getMethodResult());
        }
        if (interfaces.contains(Interfaces.IScrollable)) {
            str.append(getMethodsScroll());
        }
        if (interfaces.contains(Interfaces.IForwardOnly)) {
            str.append(getMethodsForwardOnly());
        }

        // If values need to be set at runtime for the query
        if (interfaces.contains(Interfaces.ISet)) {
            str.append(getMethodsSet());
        }

        // indirect access mode
        if (interfaces.contains(Interfaces.IRead)) {
            str.append(getMethodsRead());
        }
        if (interfaces.contains(Interfaces.IUpdate)) {
            str.append(getMethodsUpdate());
        }
        if (interfaces.contains(Interfaces.IInsert)) {
            str.append(getMethodsInsert());
        }
        if (interfaces.contains(Interfaces.IDelete)) {
            str.append(getMethodsDelete());
        }

        str.append("}");
        return str.toString();
    }

    public String getWritePath() {
        String locpath = this.BE.getReadClassPath();
        locpath = locpath.substring(1, locpath.length());
        try {
            locpath = URLDecoder.decode(locpath, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            System.err.println("ERROR COULDNT DECODE READ PATH!");
            System.exit(0);
        }
        String pathtmp = locpath.substring(0, locpath.lastIndexOf("build"));
        String pathtmp2 = BE.getReadPackage();
        if (pathtmp2.length() > 0) {
            if (pathtmp2.contains(".")) {
                pathtmp2 = pathtmp2.replaceAll("\\.", "/");
                pathtmp2 += "/";
            }

        }

        return pathtmp + "src/" + pathtmp2;
    }

    public String capitaliseFirstLetter(String type) {
        String teste;
        teste = type.substring(0, 1).toUpperCase() + type.substring(1);
        return teste;
    }

    // writes the constructor
    public final String getConstructor() {
        StringBuilder str = new StringBuilder();

        // changeConn
        str.append("\n\tpublic ").append(BE.getName().substring(1, BE.getName().length())).append("(IBusinessManager man, ObjectOutputStream oos, ObjectInputStream ois, String crud, Integer sessionID, Integer querySRID, IBusinessSchemaController controller, Integer activeSequence) throws SQLException {\n");
        str.append(validationSourceCode);
        //str.append("\t\tthis.sock=null;\n");
        //str.append("\t\tthis.conn=null;\n");
        str.append("\t\tthis.sessionID=sessionID;\n");
        str.append("\t\tthis.querySRID=querySRID;\n");
        str.append("\t\tthis.crud=crud;\n");
        str.append("\t\tthis.man=man;\n");
        str.append("\t\tthis.controller=controller;\n");
        str.append("\t\tthis.activeSequence=activeSequence;\n");
        
        str.append("\t\tthis.oos = oos;\n");
        str.append("\t\tthis.ois = ois;\n");
        
        str.append("\t\tthis.queryIdentifier = new Random().nextInt();");
        
        // Remove the params from the crud
        str.append(getAditionalConstructorOperations());

        str.append("\t}\n");

        return str.toString();
    }

    // writes the validateSchema method
    public String getSpecialMethod() {
        StringBuilder str = new StringBuilder();
        str.append("\tpublic boolean validateSchema(Class interfaceSchema) {\n");
        str.append("\t\tString myloc=this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();\n");
        str.append("\t\tFile jarIn = new File(myloc.substring(5));\n");
        str.append("\t\tURL url = null;\n");
        str.append("\t\tClass BTE = null;\n");
        str.append("\t\tConstructor c = null;\n");
        str.append("\t\tURLClassLoader cls=null;\n");
        str.append("\t\ttry {\n");
        str.append("\t\t\turl = jarIn.toURL();\n");
        str.append("\t\t\tURL[] urls = new URL[]{url};\n");

        str.append("\t\tcls = new URLClassLoader(urls);\n");
        str.append("\t\tClassLoader cl = cls;\n");
        str.append("\t\tString interfacestr=interfaceSchema.getInterfaces()[0].getName();\n");
        str.append("\t\tString st1=interfacestr.substring(0,interfacestr.lastIndexOf('.')+1);\n");
        str.append("\t\tString st2=interfacestr.substring(interfacestr.lastIndexOf('.')+2);\n");
        str.append("\t\tString mainesq=st1+st2;\n");
        str.append("\t\tBTE = cl.loadClass(mainesq);\n");
        str.append("\t\tString otherloc=BTE.getProtectionDomain().getCodeSource().getLocation().toString();\n");
        str.append("\t\tcls.close();\n");
        str.append("\t\tif(myloc.compareTo(otherloc)==0)\n");
        str.append("\t\t\treturn true;\n");
        str.append("\t\telse\n");
        str.append("\t\t\treturn false;\n");
        str.append("\t\t} catch (Exception ex) {\n");
        str.append("\t\t\ttry {\n");
        str.append("\t\t\t\tcls.close();\n");
        str.append("\t\t\t\treturn false;\n");
        str.append("\t\t\t} catch (Exception ex2) {\n");
        str.append("\t\t\treturn false;\n");
        str.append("\t\t}");

        str.append("\t\t}\n");

        str.append("\t}\n");
        return str.toString();
    }

    public String getType(String type) {
        if (type.compareTo("Integer") == 0) {
            return "int";
        }
        if (type.compareTo("Float") == 0) {
            return "float";
        }

        return type;
    }

    // writes the nextBE_S() method, for choreography
    private String getChoreographyMethod(Map<Integer, Map<Integer, String>> controlinfo) {
        StringBuilder strb = new StringBuilder();
        for (Integer seq : controlinfo.keySet()) {
            Map<Integer, String> poss = controlinfo.get(seq);

            for (Integer pos : poss.keySet()) {
                String pbeurl = poss.get(pos);
                String tbeurl = BE.getPackage() + "." + BE.getName();

                if (tbeurl.equalsIgnoreCase(pbeurl)) {
                    if (poss.containsKey(pos + 1)) {
                        String nbeurl = poss.get(pos + 1);
                        strb.append("\tpublic ").append(nbeurl).append(" nextBE_S").append(seq).append("(int crud, ISession session) throws BusinessManager.BTC_Exception {\n");
                        strb.append(validationSourceCode);
                        strb.append("\t\treturn man.instantiateBS(").append(nbeurl).append(".class, crud, session, activeSequence);\n");
                        strb.append("\t}\n");
                    }
                }
            }
        }

        return strb.toString();
    }

    //compiles the generated BE
    public void compile() {
        try {
            Path src = FileSystems.getDefault().getPath(this.commonBEInterfaceDir.getCanonicalPath());
            Path dest = FileSystems.getDefault().getPath(this.RandomDir.getCanonicalPath());
            FileUtils.copyDirectory(src, dest);
            System.out.println("Copying files from " + src + " to " + dest + " with main interface "+BE.getName().substring(1, BE.getName().length()));
            PrintWriter pw = new PrintWriter(new File(dest+"/"+BE.getName().substring(1, BE.getName().length())+".lol"));
            pw.write(""); pw.close();
            Compile(new File(this.MainInterfaceDir + "/" + BE.getName().substring(1, BE.getName().length()) + ".java"), true, this.RandomDir.getAbsolutePath() + getClasspath());
            CreateJarFile.addtoJar(this.RandomDir, this.path, this.JarName, this.Rnd, PackageNameUtils.getPackageName(BE.getPackage()), this.BE);
        } catch (IOException ex) {
            Logger.getLogger(GenerateBE.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    // declares variables
    private String getCommonVariablesDecl() {
        StringBuilder str = new StringBuilder();
        //changeConn
        //str.append("\tprivate Connection conn;\n");
       // str.append("\tprivate Socket sock;\n");
        str.append("\tprivate String crud;\n");
        str.append("\tprivate Integer sessionID;\n");
        str.append("\tprivate Integer querySRID;\n");
        str.append("\tprivate String params;\n");
        str.append("\tprivate IBusinessSchemaController controller;\n");
        str.append("\tprivate Integer activeSequence;\n");
        str.append("\tprivate IBusinessManager man;\n");
        str.append("\tprivate int queryIdentifier;\n");
        

        if (this.BE.IsUID()) {
            str.append("\tprivate int nrows;\n");
        } else {
            //str.append("\tprivate ResultSet rs;\n");
        }

        //str.append("\tprivate PreparedStatement ps;\n");
        str.append("\tprivate ObjectOutputStream oos;\n");
        str.append("\tprivate ObjectInputStream ois;\n");

        return str.toString();
    }

    /**
     * Retrieves specific operations to place in the constructor.
     *
     * @return The class concurrency policy.
     */
    protected abstract String getAditionalConstructorOperations();

    /**
     * Retrieves the imports for the class.
     *
     * @return The imports for the class.
     */
    protected abstract String getImports();

    /**
     * Retrieves the class declaration.
     *
     * @return The class declaration for the class.
     */
    protected abstract String getClassDecl();

    /**
     * Retrieves the variables to be used for the class.
     *
     * @return The class variables declaration.
     */
    protected abstract String getVariablesDecl();

    /**
     * Retrieves the execute method.
     *
     * @return The class execute method.
     */
    protected abstract String getMethodsExecute();

    /**
     * Retrieves the result method.
     *
     * @return The class result method.
     */
    protected abstract String getMethodResult();

    /**
     * Retrieves the methods for the scroll policy.
     *
     * @return The class methods for the scroll policy.
     */
    protected abstract String getMethodsScroll();

    /**
     * Retrieves the methods for the forward policy.
     *
     * @return The class forward policy.
     */
    protected abstract String getMethodsForwardOnly();

    /**
     * Retrieves the set methods.
     *
     * @return The class set methods.
     */
    protected abstract String getMethodsSet();

    /**
     * Retrieves the read methods.
     *
     * @return The class read methods.
     */
    protected abstract String getMethodsRead();

    /**
     * Retrieves the update methods.
     *
     * @return The update set methods.
     */
    protected abstract String getMethodsUpdate();

    /**
     * Retrieves the insert methods.
     *
     * @return The class insert methods.
     */
    protected abstract String getMethodsInsert();

    /**
     * Retrieves the delete methods.
     *
     * @return The class delete methods.
     */
    protected abstract String getMethodsDelete();

    /**
     * Retrieves the scroll policy.
     *
     * @return The class scroll policy.
     */
    protected abstract String getScrollType();

    /**
     * Retrieves the concurrency policy.
     *
     * @return The class concurrency policy.
     */
    protected abstract String getRsConcur();

    /**
     * Retrieves the classpath.
     *
     * @return The class classpath needed.
     */
    protected abstract String getClasspath();
}
