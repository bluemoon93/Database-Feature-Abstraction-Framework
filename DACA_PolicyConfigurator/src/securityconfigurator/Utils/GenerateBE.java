package securityconfigurator.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import securityconfigurator.SCException;

public class GenerateBE {

    BusinessEntity_Context BE;
    String path;
    String JarName;
    String Filename;
    File TmpDir;
    ArrayList<Interfaces> interfaces = new ArrayList<Interfaces>();
    File DefaultInterfacesDir;
    File MainInterfaceDir;

    public GenerateBE(BusinessEntity_Context BE, String path, String jarName) throws BTC_Exception, SCException {
        this.BE = BE;
        this.path = path;
        this.JarName = jarName;
        this.Filename = "BE";
        if (BE.getPackage().length() == 0) {
            throw new BTC_Exception("Interface needs to be in a Package.");
        }

        SessionIdentifierGenerator SIG = new SessionIdentifierGenerator();
        String Rnd = SIG.nextSessionId();

        TmpDir = new File(this.path + Rnd + "/");
        this.DefaultInterfacesDir = new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/");
        if (!this.DefaultInterfacesDir.mkdirs()) {
            throw new BTC_Exception("Could not create Temporary Directories");
        }
        GenerateDefaultInterfaces();
        this.MainInterfaceDir = new File(TmpDir.getAbsolutePath() + "/" + PackageNameUtils.getPackageNameDir(BE.getPackage()));
        if (!this.MainInterfaceDir.exists()) {
            if (!this.MainInterfaceDir.mkdirs()) {
                throw new BTC_Exception("Could not create Temporary Directories");
            }
        }
        GenerateOtherInterfaces();
        SelectWhichInterfaces();
        InterfaceGenerate.CreateSuperInterface(MainInterfaceDir, BE.getName(), this.interfaces, BE);
        Compile(new File(MainInterfaceDir + "/" + BE.getName() + ".java"), true, this.TmpDir.getAbsolutePath());
        CreateJarFile.addtoJar(TmpDir, this.path, jarName, Rnd, PackageNameUtils.getPackageName(BE.getPackage()), this.BE);

    }

    public void GenerateOtherInterfaces() throws BTC_Exception, SCException {
        if (BE.isRead()) {

            File ReadDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(BE.getReadPackage()));
            if (!ReadDirectory.exists()) {
                if (!ReadDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(ReadDirectory, "", Interfaces.IRead, BE);
            if (BE.ReadHaveAdditionalInterfaces()) {
                ArrayList<Class> readitfstemp = BE.getReadAdditionalInterfaces();
                for (int i = 0; i < readitfstemp.size(); i++) {
                    File ReadInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(readitfstemp.get(i).getPackage().getName()));
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
            File ExecuteDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(BE.getExecutePackage()));
            if (!ExecuteDirectory.exists()) {
                if (!ExecuteDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(ExecuteDirectory, "", Interfaces.IExecute, BE);
            if (BE.ExecuteHaveAdditionalInterfaces()) {
                ArrayList<Class> executeitfstemp = BE.getExecuteInterfaces();
                for (int i = 0; i < executeitfstemp.size(); i++) {
                    File ExecuteInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(executeitfstemp.get(i).getPackage().getName()));
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
            File UpdateDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(BE.getUpdatePackage()));
            if (!UpdateDirectory.exists()) {
                if (!UpdateDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(UpdateDirectory, "", Interfaces.IUpdate, BE);

            if (BE.UpdateHaveAdditionalInterfaces()) {
                ArrayList<Class> updateitfstemp = BE.getUpdateAdditionalInterfaces();
                for (int i = 0; i < updateitfstemp.size(); i++) {
                    File UpdateInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(updateitfstemp.get(i).getPackage().getName()));
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
                    File ExecuteInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(updateitfstemp.get(i).getPackage().getName()));
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
            File InsertDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(BE.getInsertPackage()));
            if (!InsertDirectory.exists()) {
                if (!InsertDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(InsertDirectory, "", Interfaces.IInsert, BE);

            if (BE.InsertHaveAdditionalInterfaces()) {
                ArrayList<Class> insertitfstemp = BE.getInsertAdditionalInterfaces();
                for (int i = 0; i < insertitfstemp.size(); i++) {
                    File InsertInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(insertitfstemp.get(i).getPackage().getName()));
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
                    File ExecuteInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(insertitfstemp.get(i).getPackage().getName()));
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
            File SetDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(BE.getSetPackage()));
            if (!SetDirectory.exists()) {
                if (!SetDirectory.mkdirs()) {
                    throw new BTC_Exception("Could not create Temporary Directories");
                }
            }
            InterfaceGenerate.CreateInterface(SetDirectory, "", Interfaces.ISet, BE);
            if (BE.SetHaveAdditionalInterfaces()) {
                ArrayList<Class> setitfstemp = BE.getSetInterfaces();
                for (int i = 0; i < setitfstemp.size(); i++) {
                    File SetInterfaceDirectory = new File(this.TmpDir + "/" + PackageNameUtils.getPackageNameDir(setitfstemp.get(i).getPackage().getName()));
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

    public void GenerateDefaultInterfaces() {
        InterfaceGenerate.CreateInterface(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IDelete, BE);
        InterfaceGenerate.CreateInterface(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IScrollable, BE);
        InterfaceGenerate.CreateInterface(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IResult, BE);
        InterfaceGenerate.CreateInterface(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/"), "", Interfaces.IForwardOnly, BE);
        Compile(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/IDelete.java"), false, "");
        Compile(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/IScrollable.java"), false, "");
        Compile(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/IForwardOnly.java"), false, "");
        Compile(new File(TmpDir.getAbsolutePath() + "/BusinessInterfaces/IResult.java"), false, "");
    }

    public void Compile(File file, Boolean classpath, String classpathstr) {

        
        List<String> options = new ArrayList<String>();
        if (classpath) {
            options.add("-classpath");
            StringBuilder sb = new StringBuilder();
            sb.append(classpathstr);
            options.add(sb.toString());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("NULL COMPILER1");
            System.exit(0);
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(file.getAbsolutePath()));
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        boolean sucess = task.call();

        if (sucess == false) {
            System.err.println("Couldnt compile file " + file.getAbsolutePath());
            System.err.println("List of errors\n" + diagnostics.getDiagnostics().toString());
            System.exit(0);
        }
    }

    private void Compile2(File file, Boolean classpath, String classpathstr) {

        List<String> options = new ArrayList<String>();
        if (classpath) {
            options.add("-classpath");
            StringBuilder sb = new StringBuilder();
            sb.append(classpathstr);
            options.add(sb.toString());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("NULL COMPILER0");
            System.exit(0);
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(file.getAbsolutePath()));
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

        boolean sucess = task.call();

        if (sucess == false) {
            System.err.println("Couldnt compile file " + file.getAbsolutePath());
            System.err.println("List of errors\n" + diagnostics.getDiagnostics().toString());
            System.exit(0);
        }
    }

    public void SelectWhichInterfaces() {

        if (BE.IsUID()) {
            this.interfaces.add(Interfaces.IResult);
            //this.interfaces.add(Interfaces.ISet);
        }

        if (BE.isSet())
            this.interfaces.add(Interfaces.ISet);

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
            InterfaceGenerate.CreateInterface(TmpDir, Filename, this.interfaces.get(i), BE);
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
                file.close();
            } catch (IOException ex) {
                Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String CreateStateClass() {
        String stateop = "";
        stateop += "package " + BE.getPackage() + ";\n\n";
        stateop += "public enum _stateop {idle,updating,inserting;}";
        return stateop;
    }

    public void GenerateMainClassBE() throws BTC_Exception {
        BufferedWriter file = null;
        try {
            File MainClass;
            MainClass = new File(this.MainInterfaceDir + "/" + BE.getName().substring(1, BE.getName().length()) + ".java");
            file = new BufferedWriter(new FileWriter(MainClass));
            file.append(CreateMainClass());
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                file.close();
            } catch (IOException ex) {
                Logger.getLogger(InterfaceGenerate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String CreateMainClass() throws BTC_Exception {
        String str = "";
        str += getImports();
        str += getClassDecl();
        str += getVariablesDecl();
        str += getConstructor();
        str += getMethodsExecute();

        if (interfaces.contains(Interfaces.IResult)) {
            str += getMethodResult();
        }
        if (interfaces.contains(Interfaces.IScrollable)) {
            str += getMethodsScroll();
        }

        if (interfaces.contains(Interfaces.IForwardOnly)) {
            str += getMethodsForwardOnly();
        }

        if (interfaces.contains(Interfaces.ISet)) {
            str += getMethodsSet();
        }


        if (interfaces.contains(Interfaces.IRead)) {
            str += getMethodsRead();
        }
        if (interfaces.contains(Interfaces.IUpdate)) {
            str += getMethodsUpdate();
        }
        if (interfaces.contains(Interfaces.IInsert)) {
            str += getMethodsInsert();
        }
        if (interfaces.contains(Interfaces.IDelete)) {
            str += getMethodsDelete();
        }

        str += "}";
        return str;
    }

    public String getWritePath() {
        String path = this.BE.getReadClassPath();
        path = path.substring(1, path.length());
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            System.err.println("ERROR COULDNT DECODE READ PATH!");
            System.exit(0);
        }
        String pathtmp = path.substring(0, path.lastIndexOf("build"));
        String pathtmp2 = BE.getReadPackage();
        if (pathtmp2.length() > 0) {
            if (pathtmp2.contains(".")) {
                pathtmp2 = pathtmp2.replaceAll("\\.", "/");
                pathtmp2 += "/";
            }

        }


        return pathtmp + "src/" + pathtmp2;
    }

    public String getImports() {
        String str = "";
        str += "package " + this.BE.getPackage() + ";\n";
        str += "import BusinessInterfaces.*;\n";
        str += "import java.math.*;\n";
        str += "import java.sql.*;\n\n";
        str += BE.getExecuteAdditionalImports();
        return str;
    }

    public String getMethodsForwardOnly() {
        String str = "";
        str += "@Override\n";
        str += "public boolean moveNext() throws SQLException{\n";
        str += "return rs.next();\n";
        str += "}";
        return str;
    }

    public String getMethodResult() {
        String str = "";
        str += "@Override\n";
        str += "public int getNAffectedRows(){\n";
        str += "return nrows;\n";
        str += "}";
        return str;
    }


    public String getMethodsSet() {
        String str = "";
        ArrayList<Method> al2 = BE.GetParamIExecute();
        if (al2.size() > 0) {
            for (int j = 0; j < al2.size(); j++) {
                Class[] tmp = al2.get(j).getParameterTypes();
                str += "@Override\n";
                str += "public void set(";
                for (int i = 0; i < tmp.length; i++) {
                    if (i < tmp.length - 1) {
                        str += getType(tmp[i].getSimpleName()) + " args" + i + ",";
                    } else {
                        str += getType(tmp[i].getSimpleName()) + " args" + i;
                    }
                }
                str += ") throws SQLException {\n";
                if (tmp.length > 0) {
                    if (tmp[0] != null) {
                        if (tmp[0].equals(Object[].class)) {
                            str += "for (int i = 0; i < args0.length; i++) {";
                            str += "ps.setObject(i+1,args0[i]);\n";
                            str += "}\n";

                        } else {
                            for (int i = 0; i < tmp.length; i++) {
                                str += "ps.set" + capitaliseFirstLetter(getType(tmp[i].getSimpleName())) + "(" + (i + 1) + ",args" + i + ");\n";

                            }
                        }
                    }
                }

                str += "}\n";

            }
        }


        return str;
    }


    public String getMethodsRead() {
        String med = "";
        Method[] mtds = this.BE.getReadAllMethods();
        if (mtds.length > 0) {
            for (int i = 0; i < mtds.length; i++) {
                med += "@Override";
                med += "\n public " + mtds[i].getReturnType().getSimpleName() + " " + mtds[i].getName() + "() " + "throws SQLException{\n";
                med += "return rs.get" + capitaliseFirstLetter(mtds[i].getReturnType().getSimpleName()) + "(" + "\"" + mtds[i].getName() + "\"" + ");}\n";
            }

        }
        return med;


    }

    public String capitaliseFirstLetter(String type) {
        String teste;
        teste = type.substring(0, 1).toUpperCase() + type.substring(1);
        return teste;
    }

    public String getMethodsScroll() {
        String str = "";
        str += "@Override\n";
        str += "public boolean moveNext() throws SQLException{\n";
        str += "return rs.next();\n";
        str += "}\n";
        str += "@Override\n";
        str += "public boolean moveAbsolute(int pos) throws SQLException {\n";
        str += "return rs.absolute(pos);\n";
        str += "}\n";
        str += "@Override\n";
        str += "public boolean moveRelative(int offset) throws SQLException {\n";
        str += "return rs.relative(offset);\n";
        str += "}\n";
        str += "@Override\n";
        str += "public void moveBeforeFirst() throws SQLException {\n";
        str += "rs.beforeFirst();\n";
        str += "}\n";
        str += "@Override\n";
        str += "public boolean moveFirst() throws SQLException {\n";
        str += "return rs.first();\n";
        str += "}\n";
        str += "@Override\n";
        str += "public void moveAfterLast() throws SQLException {\n";
        str += "rs.afterLast();\n";
        str += "}\n";
        str += "@Override\n";
        str += " public boolean moveLast() throws SQLException {\n";
        str += "return rs.last();\n";
        str += "}\n";


        return str;
    }

    public String getConstructor() {
        String str = "";
        if (BE.IsUID()) {
            str += "public " + BE.getName().substring(1, BE.getName().length()) + "(Connection conn,String crud) throws SQLException {\n";
            str += "this.conn=conn;\n";
            str += "this.crud=crud;\n";
            str += "ps=conn.prepareStatement(crud);\n";
            str += "}\n";
        } else {
            str += "public " + BE.getName().substring(1, BE.getName().length()) + "(Connection conn,String crud) throws SQLException {\n";
            str += "this.conn=conn;\n";
            str += "this.crud=crud;\n";
            str += "ps=conn.prepareStatement(crud," + getScrollType() + "," + getRsConcur() + ");\n";
            str += "_state=idle;\n";
            str += "}\n";

        }
        return str;
    }

    public String getMethodsExecute() throws BTC_Exception {
        String str = "";
        ArrayList<Method> al2 = BE.GetParamIExecute();
        if (al2.size() > 0) {
            for (int j = 0; j < al2.size(); j++) {
                Class[] tmp = al2.get(j).getParameterTypes();
                str += "@Override\n";
                str += "public void execute(";
                for (int i = 0; i < tmp.length; i++) {
                    if (i < tmp.length - 1) {
                        str += getType(tmp[i].getSimpleName()) + " args" + i + ",";
                    } else {
                        str += getType(tmp[i].getSimpleName()) + " args" + i;
                    }
                }
                str += ") throws SQLException {\n";
                if (tmp.length > 0) {
                    if (tmp[0] != null) {
                        if (tmp[0].equals(Object[].class)) {
                            str += "for (int i = 0; i < args0.length; i++) {";
                            str += "ps.setObject(i+1,args0[i]);\n";
                            str += "}\n";

                        } else {
                            for (int i = 0; i < tmp.length; i++) {
                                if (tmp[i].isInterface()) {
                                    Method mtd = tmp[i].getDeclaredMethods()[0];

                                    str += "ps.set" + capitaliseFirstLetter(getType(mtd.getReturnType().getSimpleName())) + "(" + (i + 1) + ",args" + i + "." + mtd.getName() + "()" + ");\n";
                                } else {

                                    str += "ps.set" + capitaliseFirstLetter(getType(tmp[i].getSimpleName())) + "(" + (i + 1) + ",args" + i + ");\n";
                                }
                            }
                        }
                    }
                }


                if (this.BE.IsUID()) {
                    str += "nrows=ps.executeUpdate();\n";
                } else {
                    str += "rs=ps.executeQuery();\n";
                }

                str += "}\n";

            }
        }


        return str;
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

    public String getScrollType() {
        if (this.BE.isScrollable()) {
            if (this.BE.ReadOnly())
                return "ResultSet.TYPE_SCROLL_INSENSITIVE";
            return "ResultSet.TYPE_SCROLL_SENSITIVE";
        }
        return "ResultSet.TYPE_FORWARD_ONLY";
    }

    public String getRsConcur() {
        if (this.BE.ReadOnly()) {
            return "ResultSet.CONCUR_READ_ONLY";
        } else {
            return "ResultSet.CONCUR_UPDATABLE";
        }
    }

    public String getClassDecl() {
        String str = "";
        str += "public class " + BE.getName().substring(1, BE.getName().length()) + " implements " + this.BE.getName();


        str += "{\n";

        if (!BE.IsUID()) {
            str += "public final int idle=0;\n";
            str += "public final int updating=1;\n";
            str += "public final int inserting=2;\n";
            str += "public int _state=idle;\n";
        }

        return str;
    }

    public String getVariablesDecl() {
        String str = "";
        str += "private Connection conn;\n";
        str += "private String crud;\n";
        if (this.BE.IsUID()) {
            str += "private int nrows;\n";
        } else {
            str += "ResultSet rs;\n";
        }
        str += "private PreparedStatement ps;\n";
        return str;
    }

    public String getMethodsUpdate() {
        String str = "";
        str += "@Override\n";
        str += "public void beginUpdate() throws SQLException {\n";
        str += "if(_state==inserting)\n";
        str += "throw new SQLException(\"Cant Update, While Inserting!\");\n";
        str += "else{\n";
        str += "_state=updating;}\n";
        str += "}\n";
        str += "@Override\n";
        str += "public void updateRow() throws SQLException {\n";
        str += "if(_state!=updating)\n";
        str += "throw new SQLException(\"Cant Update, While not updating!\");\n";
        str += "else{\n";
        str += "_state=idle;\n";
        str += "rs.updateRow();}\n";
        str += "}\n";
        str += "@Override\n";
        str += "public void cancelUpdate() throws SQLException {\n";
        str += "rs.cancelRowUpdates();\n";
        str += "_state=idle;\n";
        str += "}\n";
        Method[] mtds = this.BE.getAllUpdateMethods();
        if (mtds.length > 0) {
            for (int i = 0; i < mtds.length; i++) {
                str += "@Override";
                str += "\n" + "public void" + " " + mtds[i].getName() + "(" + mtds[i].getParameterTypes()[0].getSimpleName() + " " + mtds[i].getName().substring(1, mtds[i].getName().length()) + ")" + " throws SQLException{\n";
                str += "if(_state!=updating)\n";
                str += "throw new SQLException(\"Cant Update, While Inserting, or not BeginUpdating!\");\n";
                str += "else{\n";
                str += "rs.update" + capitaliseFirstLetter(mtds[i].getParameterTypes()[0].getSimpleName()) + "(" + "\"" + mtds[i].getName().substring(1, mtds[i].getName().length()) + "\"" + "," + mtds[i].getName().substring(1, mtds[i].getName().length()) + ");}\n";
                str += "}\n";
            }
        }
        return str;
    }

    public String getMethodsInsert() {
        String str = "";
        str += "@Override\n";
        str += "public void beginInsert() throws SQLException {\n";
        str += "if(_state==updating)\n";
        str += "throw new SQLException(\"Cant Insert, While Updating!\");\n";
        str += "else{\n";
        str += "_state=inserting;}\n";
        str += "rs.moveToInsertRow();\n";
        str += "}\n";
        str += "public void endInsert(boolean moveToPreviousRow) throws SQLException {\n";
        str += "if(_state!=inserting)\n";
        str += "throw new SQLException(\"Cant Insert, While not inserting!\");\n";
        str += "else{\n";
        str += "_state=idle;\n";
        str += "rs.insertRow();}\n";
        str += "if(moveToPreviousRow)\n";
        str += "rs.moveToCurrentRow();\n";
        str += "}\n";
        str += "@Override\n";
        str += "public void cancelInsert() throws SQLException {\n";
        str += "rs.moveToCurrentRow();\n";
        str += "_state=idle;\n";
        str += "}\n";
        Method[] mtds = this.BE.getAllInsertMethods();
        if (mtds.length > 0) {
            for (int i = 0; i < mtds.length; i++) {
                str += "@Override";
                str += "\n" + "public void" + " " + mtds[i].getName() + "(" + mtds[i].getParameterTypes()[0].getSimpleName() + " " + mtds[i].getName().substring(1, mtds[i].getName().length()) + ")" + " throws SQLException{\n";
                str += "if(_state!=inserting)\n";
                str += "throw new SQLException(\"Cant insert, While updating or not BeginInsert!\");\n";
                str += "else{\n";
                str += "rs.update" + capitaliseFirstLetter(mtds[i].getParameterTypes()[0].getSimpleName()) + "(" + "\"" + mtds[i].getName().substring(1, mtds[i].getName().length()) + "\"" + "," + mtds[i].getName().substring(1, mtds[i].getName().length()) + ");}\n";
                str += "}\n";
            }
        }
        return str;
    }

    public String getMethodsDelete() {
        String str = "";
        str += "@Override\n";
        str += "public void deleteRow() throws SQLException {\n";
        str += "rs.deleteRow();\n";
        str += "}\n";
        return str;
    }

}
