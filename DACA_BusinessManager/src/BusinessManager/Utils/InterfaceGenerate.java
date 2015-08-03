package BusinessManager.Utils;

import LocalTools.BusinessEntity_Context;
import LocalTools.PackageNameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterfaceGenerate {

    public enum ImportStatus {
        iread, iwrite, iset, iupdate, iinsert
    }

    private static String getImports(ImportStatus sts) {
        String importStr = "";

        if (sts.equals(ImportStatus.iread)) {
            importStr += "import java.math.*;\n";
            importStr += "import java.sql.*;\n\n";
        }
        if (sts.equals(ImportStatus.iwrite)) {
            importStr += "import java.math.*;\n";
            importStr += "import java.sql.*;\n\n";
        }
        if (sts.equals(ImportStatus.iset)) {
            importStr += "import java.math.*;\n";
            importStr += "import java.sql.*;\n\n";
        }

        return importStr;
    }

    public static void CreateInterface(File dir, String Name, Interfaces inter, BusinessEntity_Context BE) {
        System.out.println("Creating interface "+dir);
        BufferedWriter file = null;
        try {
            File Interface;
            Interface = new File(dir.getAbsolutePath() + "/" + inter.name() + ".java");
            file = new BufferedWriter(new FileWriter(Interface));
            file.append(CreateInterface(inter, BE, Name));
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

    public static void CreateAdditionalInterface(File dir, Interfaces inter, Class itf) {
        System.out.println("Creating additional interface "+dir);
        BufferedWriter file = null;
        try {
            File Interface;
            Interface = new File(dir.getAbsolutePath() + "/" + itf.getSimpleName() + ".java");
            file = new BufferedWriter(new FileWriter(Interface));
            file.append(CreateAdditionalInterface(inter, itf));
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

    private static String CreateAdditionalInterface(Interfaces inter, Class itf) {
        String interstr = "";
        switch (inter) {
            case IRead:
                interstr += "package " + PackageNameUtils.getPackage(itf) + ";\n";
                interstr += getImports(ImportStatus.iread);
                interstr += getInterfaceDeclaration(itf.getSimpleName(), null);
                interstr += getAdditionalInterfaceMethods(ImportStatus.iread, itf);
                interstr += "}";
                break;
            case IUpdate:
                interstr += "package " + PackageNameUtils.getPackage(itf) + ";\n";
                interstr += getImports(ImportStatus.iread);
                interstr += getInterfaceDeclaration(itf.getSimpleName(), null);
                interstr += getAdditionalInterfaceMethods(ImportStatus.iupdate, itf);
                interstr += "}";
                break;
            case IInsert:
                interstr += "package " + PackageNameUtils.getPackage(itf) + ";\n";
                interstr += getImports(ImportStatus.iread);
                interstr += getInterfaceDeclaration(itf.getSimpleName(), null);
                interstr += getAdditionalInterfaceMethods(ImportStatus.iinsert, itf);
                interstr += "}";
                break;
            case IExecute:
                interstr += "package " + PackageNameUtils.getPackage(itf) + ";\n";
                interstr += getImports(ImportStatus.iread);
                interstr += getInterfaceDeclaration(itf.getSimpleName(), null);
                interstr += getAdditionalInterfaceMethods(ImportStatus.iread, itf);
                interstr += "}";
                break;
        }

        return interstr;
    }

    private static String CreateInterface(Interfaces inter, BusinessEntity_Context BE, String Name) {
        String interstr = "";

        switch (inter) {
            case Common: {
                interstr += "package BusinessInterfaces;\n";
                interstr += "public class Common {\n";
                interstr += "public enum scrollability {forwardOnly, scrollable};\n";
                interstr += "public enum updatability {readOnly, updatable}\n;";
                interstr += "public Common() {}\n";
                interstr += "}\n";
                break;
            }
            case IRead:
                interstr += "package " + BE.getReadPackage() + ";\n";
                interstr += getImports(ImportStatus.iread);
                ArrayList exttemp = null;
                if (BE.ReadHaveAdditionalInterfaces()) {
                    interstr += BE.getReadAdditionalImports();
                    exttemp = BE.getReadAdditionalInterfacesAsString();
                }

                interstr += getInterfaceDeclaration("IRead", exttemp);
                interstr += getMethods(ImportStatus.iread, BE);
                interstr += "}";
                break;
            case IResult:
                interstr += "package " + "BusinessInterfaces" + ";\n";
                interstr += getInterfaceDeclaration("IResult", null);
                interstr += "int getNAffectedRows();\n";
                interstr += "}";
                break;
            case IExecute:
                interstr += "package " + BE.getExecutePackage() + ";\n";
                interstr += getImports(ImportStatus.iset);
                interstr += BE.getExecuteAdditionalImports();
                interstr += getInterfaceDeclaration("IExecute", null);
                ArrayList<Method> al2 = BE.GetParamIExecute();
                if (al2.size() > 0) {
                    for (int i = 0; i < al2.size(); i++) {

                        Class[] tt = al2.get(i).getParameterTypes();
                        interstr += " void execute(";
                        for (int j = 0; j < tt.length; j++) {
                            if (j < tt.length - 1) {
                                interstr += getType(tt[j].getSimpleName()) + " args" + j + ",";
                            } else {
                                interstr += getType(tt[j].getSimpleName()) + " args" + j;
                            }
                        }
                        interstr += ") throws SQLException;\n";

                    }
                }
                interstr += "}";
                break;
            case IScrollable:
                interstr += "package " + "BusinessInterfaces" + ";\n";
                interstr += getImports(ImportStatus.iset);
                interstr += getInterfaceDeclaration("IScrollable", null);
                interstr += " boolean moveNext() throws SQLException;\n";
                interstr += " boolean moveAbsolute(int pos) throws SQLException;\n";
                interstr += " boolean moveRelative(int offset) throws SQLException;\n";
                interstr += " void moveBeforeFirst() throws SQLException;\n";
                interstr += " boolean moveFirst() throws SQLException;\n";
                interstr += " void moveAfterLast() throws SQLException;\n";
                interstr += " boolean moveLast() throws SQLException;\n";
                interstr += "}";
                break;
            case IForwardOnly:
                interstr += "package " + "BusinessInterfaces" + ";\n";
                interstr += getImports(ImportStatus.iset);
                interstr += getInterfaceDeclaration("IForwardOnly", null);
                interstr += "  boolean moveNext() throws SQLException;\n";
                interstr += "}";
                break;
            case ISet:
                interstr += "package " + BE.getExecutePackage() + ";\n";
                interstr += getImports(ImportStatus.iset);
                interstr += BE.getSetAdditionalImports();
                interstr += getInterfaceDeclaration("ISet", null);
                ArrayList<Method> al3 = BE.GetParamIExecute();
                if (al3.size() > 0) {
                    for (int i = 0; i < al3.size(); i++) {

                        Class[] tt = al3.get(i).getParameterTypes();
                        interstr += " void set(";
                        for (int j = 0; j < tt.length; j++) {
                            if (j < tt.length - 1) {
                                interstr += getType(tt[j].getSimpleName()) + " args" + j + ",";
                            } else {
                                interstr += getType(tt[j].getSimpleName()) + " args" + j;
                            }
                        }
                        interstr += ") throws SQLException;\n";

                    }
                }
                interstr += "}";
                break;
            case IUpdate:
                interstr += "package " + BE.getUpdatePackage() + ";\n";
                interstr += getImports(ImportStatus.iset);
                ArrayList exttempupd = null;
                interstr += BE.getUpdateAdditionalInterfaceImports();
                if (BE.UpdateHaveAdditionalInterfaces()) {
                    interstr += BE.getUpdateAdditionalImports();
                    exttempupd = BE.getUpdateAdditionalInterfacesAsString();
                }
                interstr += getInterfaceDeclaration("IUpdate", exttempupd);
                interstr += " void beginUpdate() throws SQLException;\n";
                interstr += " void updateRow() throws SQLException;\n";
                interstr += " void cancelUpdate() throws SQLException;\n";
                interstr += getMethods(ImportStatus.iupdate, BE);
                interstr += "}";
                break;
            case IInsert:
                interstr += "package " + BE.getInsertPackage() + ";\n";
                interstr += getImports(ImportStatus.iset);
                ArrayList exttempisr = null;
                interstr += BE.getInsertAdditionalInterfaceImports();
                if (BE.InsertHaveAdditionalInterfaces()) {
                    interstr += BE.getInsertAdditionalImports();
                    exttempisr = BE.getInsertAdditionalInterfacesAsString();
                }
                interstr += getInterfaceDeclaration("IInsert", exttempisr);
                interstr += " void beginInsert() throws SQLException;\n";
                interstr += " void endInsert(boolean moveToPreviousRow) throws SQLException;\n";
                interstr += " void cancelInsert() throws SQLException;\n";
                interstr += getMethods(ImportStatus.iinsert, BE);
                interstr += "}";
                break;
            case IDelete:
                interstr += "package " + "BusinessInterfaces" + ";\n";
                interstr += getImports(ImportStatus.iset);
                interstr += getInterfaceDeclaration("IDelete", null);
                interstr += " void deleteRow() throws SQLException;\n";
                interstr += "}";
                break;

        }
        return interstr;
    }

    private static String getType(String type) {
        if (type.compareTo("Integer") == 0) {
            return "int";
        }
        if (type.compareTo("Float") == 0) {
            return "float";
        }

        return type;
    }

    private static String getMethods(ImportStatus exsts, BusinessEntity_Context BE) {
        String med = "";
        if (exsts.equals(ImportStatus.iread)) {
            Method[] mtds = BE.getReadMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + mtd.getReturnType().getSimpleName() + " " + mtd.getName() + "() " + "throws SQLException;\n";
                }
            }
        }
        if (exsts.equals(ImportStatus.iwrite)) {
            Method[] mtds = BE.getReadMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + "void" + " " + mtd.getName() + "(" + mtd.getReturnType().getSimpleName() + " " + mtd.getName() + ")" + " throws SQLException;\n";
                }
            }
        }

        if (exsts.equals(ImportStatus.iupdate)) {
            Method[] mtds = BE.getUpdateMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + "void" + " " + mtd.getName() + "(" + mtd.getParameterTypes()[0].getSimpleName() + " " + mtd.getName().substring(1, mtd.getName().length()) + ")" + " throws SQLException;\n";
                }
            }
        }

        if (exsts.equals(ImportStatus.iinsert)) {
            Method[] mtds = BE.getInsertMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + "void" + " " + mtd.getName() + "(" + mtd.getParameterTypes()[0].getSimpleName() + " " + mtd.getName().substring(1, mtd.getName().length()) + ")" + " throws SQLException;\n";
                }
            }
        }

        return med;
    }

    private static String getAdditionalInterfaceMethods(ImportStatus exsts, Class itf) {
        String med = "";
        if (exsts.equals(ImportStatus.iread)) {
            Method[] mtds = itf.getDeclaredMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + mtd.getReturnType().getSimpleName() + " " + mtd.getName() + "() " + "throws SQLException;\n";
                }
            }
        }
        if (exsts.equals(ImportStatus.iupdate)) {
            Method[] mtds = itf.getDeclaredMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + "void" + " " + mtd.getName() + "(" + mtd.getParameterTypes()[0].getSimpleName() + " " + mtd.getName().substring(1, mtd.getName().length()) + ")" + " throws SQLException;\n";
                }
            }
        }

        if (exsts.equals(ImportStatus.iinsert)) {
            Method[] mtds = itf.getDeclaredMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {
                    med += "\n" + "void" + " " + mtd.getName() + "(" + mtd.getParameterTypes()[0].getSimpleName() + " " + mtd.getName().substring(1, mtd.getName().length()) + ")" + " throws SQLException;\n";
                }
            }
        }

        return med;
    }

    public static void CreateSuperInterface(File TmpDir, String Filename, ArrayList<Interfaces> interfaces, BusinessEntity_Context BE) {
        String SI = "";
        SI += "package " + BE.getPackage() + ";\n";
        SI += "import BusinessInterfaces.*;\n";
        ArrayList<String> ExtendList = new ArrayList<>();
        for (int i = 0; i < interfaces.size(); i++) {
            ExtendList.add(interfaces.get(i).name());
        }
        SI += getInterfaceDeclaration(BE.getName(), ExtendList);
        SI += "}\n";
        BufferedWriter file = null;
        try {
            File Interface;
            Interface = new File(TmpDir.getAbsolutePath() + "/" + BE.getName() + ".java");
            file = new BufferedWriter(new FileWriter(Interface));
            file.append(SI);
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

    private static String getInterfaceDeclaration(String interfacename, ArrayList extendList) {
        String IntDec = "";

        IntDec += "public interface " + interfacename;
        if (extendList != null) {
            if (extendList.size() > 0) {
                IntDec += " extends ";
                for (int i = 0; i < extendList.size(); i++) {
                    if (i != extendList.size() - 1) {
                        IntDec += extendList.get(i) + ",";
                    } else {
                        IntDec += extendList.get(i);
                    }
                }
            }
        }
        IntDec += " {\n";
        return IntDec;
    }
}
