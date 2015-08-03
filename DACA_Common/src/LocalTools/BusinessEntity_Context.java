package LocalTools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BusinessEntity_Context {

    Class BI;
    String name;
    String Package;
    Class itfRead = null;
    ArrayList<Class> itfIReadArray = null;
    ArrayList<Class> itfIUpdateArray = null;
    ArrayList<Class> itfIInsertArray = null;
    Class itfUpdate = null;
    Class itfInsert = null;
    Class itfDelete = null;
    Class itfExecute = null;
    Class itfResult = null;
    Class itfScrollable = null;
    Class itfSet = null;
    Class itfForwardOnly = null;

    public BusinessEntity_Context(Class BI) {
        this.BI = BI;
        this.name = BI.getSimpleName();

        this.Package = BI.getName().substring(0, BI.getName().lastIndexOf("."));
        getInterfaces(BI);
    }

    public void Read_Class(Class itf) {
        this.itfRead = itf;
    }

    public void GetOther_Read_Class(Class itf) {
        Class[] itfs = itf.getInterfaces();
        if (itfs.length > 0) {
            itfIReadArray = new ArrayList<>();
            itfIReadArray.addAll(Arrays.asList(itfs));
        }
    }

    public void Update_Class(Class itf) {
        this.itfUpdate = itf;
    }

    public void GetOther_Update_Class(Class itf) {
        Class[] itfs = itf.getInterfaces();
        if (itfs.length > 0) {
            itfIUpdateArray = new ArrayList<>();
            itfIUpdateArray.addAll(Arrays.asList(itfs));
        }
    }

    public void Insert_Class(Class itf) {
        this.itfInsert = itf;
    }

    public void GetOther_Insert_Class(Class itf) {
        Class[] itfs = itf.getInterfaces();
        if (itfs.length > 0) {
            itfIInsertArray = new ArrayList<>();
            itfIInsertArray.addAll(Arrays.asList(itfs));
        }
    }

    public void Delete_Class(Class itf) {
        this.itfDelete = itf;
    }

    public void Execute_Class(Class itf) {
        this.itfExecute = itf;
    }

    public void Result_Class(Class itf) {
        this.itfResult = itf;
    }

    public void Scrollable_Class(Class itf) {
        this.itfScrollable = itf;
    }

    public void ForwardOnly_Class(Class itf) {
        this.itfForwardOnly = itf;
    }

    public void Set_Class(Class itf) {
        this.itfSet = itf;
    }

    private void getInterfaces(Class itf) {
        Class[] itfs = itf.getInterfaces();
        for (Class itf1 : itfs) {
            if (itf1.getName().contains("IRead")) {
                Read_Class(itf1);
                GetOther_Read_Class(itf1);
            }
            if (itf1.getName().contains("IUpdate")) {
                Update_Class(itf1);
                GetOther_Update_Class(itf1);
            }
            if (itf1.getName().contains("IInsert")) {
                Insert_Class(itf1);
                GetOther_Insert_Class(itf1);
            }
            if (itf1.getName().contains("IDelete")) {
                Delete_Class(itf1);
            }
            if (itf1.getName().contains("IExecute")) {
                Execute_Class(itf1);
            }
            if (itf1.getName().contains("IResult")) {
                Result_Class(itf1);
            }
            if (itf1.getName().contains("IScrollable")) {
                Scrollable_Class(itf1);
            }
            if (itf1.getName().contains("IForwardOnly")) {
                ForwardOnly_Class(itf1);
            }
            if (itf1.getName().contains("ISet")) {
                Set_Class(itf1);
            }
        }
    }

    public Method[] getReadMethods() {
        return this.itfRead.getDeclaredMethods();
    }

    public Method[] getReadAllMethods() {
        return this.itfRead.getMethods();
    }

    public Method[] getUpdateMethods() {
        Method[] tmp = this.itfUpdate.getDeclaredMethods();

        Method[] tmp2 = new Method[tmp.length - 3];
        int size = 0;
        for (Method tmp1 : tmp) {
            if (tmp1.getName().startsWith("u") && tmp1.getName().compareTo("updateRow") != 0) {
                tmp2[size] = tmp1;
                size++;
            }
        }
        return tmp2;
    }

    public Method[] getAllUpdateMethods() {
        Method[] tmp = this.itfUpdate.getMethods();

        Method[] tmp2 = new Method[tmp.length - 3];
        int size = 0;
        for (Method tmp1 : tmp) {
            if (tmp1.getName().startsWith("u") && tmp1.getName().compareTo("updateRow") != 0) {
                tmp2[size] = tmp1;
                size++;
            }
        }
        return tmp2;
    }

    public Method[] getSetMethods() {
        return this.itfSet.getMethods();
    }

    public Method[] getInsertMethods() {
        Method[] tmp = this.itfInsert.getDeclaredMethods();

        Method[] tmp2 = new Method[tmp.length - 3];
        int size = 0;
        for (Method tmp1 : tmp) {
            if (tmp1.getName().startsWith("i")) {
                tmp2[size] = tmp1;
                size++;
            }
        }
        return tmp2;
    }

    public Method[] getAllInsertMethods() {
        Method[] tmp = this.itfInsert.getMethods();

        Method[] tmp2 = new Method[tmp.length - 3];
        int size = 0;
        for (Method tmp1 : tmp) {
            if (tmp1.getName().startsWith("i")) {
                tmp2[size] = tmp1;
                size++;
            }
        }
        return tmp2;
    }

    public boolean isScrollable() {
        return this.itfScrollable != null;
    }

    public boolean isForwardOnly() {
        return this.itfForwardOnly != null;
    }

    public boolean isSet() {
        return this.itfSet != null;
    }

    public boolean ReadOnly() {
        return this.itfUpdate == null && this.itfInsert == null && this.itfDelete == null;
    }

    public boolean isRead() {
        return this.itfRead != null;
    }

    public boolean isExecute() {
        return this.itfExecute != null;
    }

    public String getName() {
        return this.name;
    }

    public String getPackage() {
        return this.Package;
    }

    public Boolean IsUID() {
        return (this.itfDelete == null && this.itfUpdate == null && this.itfInsert == null && this.itfRead == null);
    }

    public Boolean IsUpdate() {
        return this.itfUpdate != null;
    }

    public Boolean IsDelete() {
        return this.itfDelete != null;
    }

    public Boolean IsInsert() {
        return this.itfInsert != null;
    }

    public ArrayList<Method> GetParamIExecute() {
        ArrayList<Method> tmp = new ArrayList<>();
        Method[] mtds = this.itfExecute.getMethods();
        tmp.addAll(Arrays.asList(mtds));
        return tmp;

    }

    public String getReadClassPath() {
        return this.itfRead.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public String getReadPackage() {
        return PackageNameUtils.getPackage(this.itfRead);
    }

    public String getExecutePackage() {
        return PackageNameUtils.getPackage(this.itfExecute);
    }

    public String getSetPackage() {
        return PackageNameUtils.getPackage(this.itfSet);
    }

    public String getUpdatePackage() {
        return PackageNameUtils.getPackage(this.itfUpdate);
    }

    public String getInsertPackage() {
        return PackageNameUtils.getPackage(this.itfInsert);
    }

    public String getReadAdditionalImports() {
        String imports = "";
        for (int i = 0; i < this.itfIReadArray.size(); i++) {
            imports += "import " + this.itfIReadArray.get(i).getName() + ";\n";
        }
        return imports;
    }

    public boolean ReadHaveAdditionalInterfaces() {
        return this.itfIReadArray != null && this.itfIReadArray.size() > 0;
    }

    public ArrayList<Class> getReadAdditionalInterfaces() {
        return this.itfIReadArray;
    }

    public ArrayList<String> getReadAdditionalInterfacesAsString() {
        ArrayList<String> extds = new ArrayList<>();
        for (int i = 0; i < this.itfIReadArray.size(); i++) {
            extds.add(this.itfIReadArray.get(i).getSimpleName());
        }

        return extds;
    }

    public boolean UpdateHaveAdditionalInterfaces() {
        return this.itfIUpdateArray != null && this.itfIUpdateArray.size() > 0;
    }

    public ArrayList<String> getUpdateAdditionalInterfacesAsString() {
        ArrayList<String> extds = new ArrayList<>();
        for (int i = 0; i < this.itfIUpdateArray.size(); i++) {
            extds.add(this.itfIUpdateArray.get(i).getSimpleName());
        }

        return extds;
    }

    public String getUpdateAdditionalImports() {
        String imports = "";
        for (int i = 0; i < this.itfIUpdateArray.size(); i++) {
            imports += "import " + this.itfIUpdateArray.get(i).getName() + ";\n";
        }
        return imports;
    }

    public ArrayList<Class> getUpdateAdditionalInterfaces() {
        return this.itfIUpdateArray;
    }

    public boolean InsertHaveAdditionalInterfaces() {
        return this.itfIInsertArray != null && this.itfIInsertArray.size() > 0;
    }

    public ArrayList<Class> getInsertAdditionalInterfaces() {
        return this.itfIInsertArray;
    }

    public ArrayList<String> getInsertAdditionalInterfacesAsString() {
        ArrayList<String> extds = new ArrayList<>();
        for (int i = 0; i < this.itfIInsertArray.size(); i++) {
            extds.add(this.itfIInsertArray.get(i).getSimpleName());
        }

        return extds;
    }

    public String getInsertAdditionalImports() {
        String imports = "";
        for (int i = 0; i < this.itfIInsertArray.size(); i++) {
            imports += "import " + this.itfIInsertArray.get(i).getName() + ";\n";
        }
        return imports;
    }

    public String getExecuteAdditionalImports() {
        String imports = "";
        ArrayList<Method> al2 = GetParamIExecute();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        imports += "import " + tt1.getName() + ";\n";
                    }
                }
            }
        }
        return imports;
    }

    public String getSetAdditionalImports() {
        String imports = "";
        ArrayList<Method> al2 = GetParamISet();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        imports += "import " + tt1.getName() + ";\n";
                    }
                }
            }
        }
        return imports;
    }

    public boolean ExecuteHaveAdditionalInterfaces() {
        ArrayList<Method> al2 = GetParamIExecute();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<Method> GetParamISet() {
        ArrayList<Method> tmp = new ArrayList<>();
        Method[] mtds = this.itfSet.getMethods();
        tmp.addAll(Arrays.asList(mtds));
        return tmp;

    }

    public boolean SetHaveAdditionalInterfaces() {
        ArrayList<Method> al2 = GetParamISet();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<Class> getSetInterfaces() {

        HashSet<Class> hs = new HashSet<>();

        ArrayList<Method> al2 = GetParamISet();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        hs.add(tt1);
                    }
                }
            }
        }

        return new ArrayList<>(hs);
    }

    public ArrayList<Class> getExecuteInterfaces() {

        HashSet<Class> hs = new HashSet<>();

        ArrayList<Method> al2 = GetParamIExecute();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        hs.add(tt1);
                    }
                }
            }
        }

        return new ArrayList<>(hs);
    }

    public String getUpdateAdditionalInterfaceImports() {
        String imports = "";
        ArrayList<Method> al2 = GetParamIUpdate();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface()) {
                        imports += "import " + tt1.getName() + ";\n";
                    }
                }
            }
        }
        return imports;
    }

    public ArrayList<Method> GetParamIUpdate() {
        ArrayList<Method> tmp = new ArrayList<>();
        Method[] mtds = this.itfExecute.getMethods();
        tmp.addAll(Arrays.asList(mtds));
        return tmp;

    }

    public boolean UpdateHaveAdditionalInterfacesInMethods() {
        ArrayList<Method> al2 = GetParamIUpdate();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<Class> getUpdateInterfaces() {

        HashSet<Class> hs = new HashSet<>();

        ArrayList<Method> al2 = GetParamIUpdate();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        hs.add(tt1);
                    }
                }
            }
        }

        return new ArrayList<>(hs);
    }

    public String getInsertAdditionalInterfaceImports() {
        String imports = "";
        ArrayList<Method> al2 = GetParamIInsert();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface()) {
                        imports += "import " + tt1.getName() + ";\n";
                    }
                }
            }
        }
        return imports;
    }

    public ArrayList<Method> GetParamIInsert() {
        ArrayList<Method> tmp = new ArrayList<>();
        Method[] mtds = this.itfInsert.getMethods();
        tmp.addAll(Arrays.asList(mtds));
        return tmp;

    }

    public boolean InsertHaveAdditionalInterfacesInMethods() {
        ArrayList<Method> al2 = GetParamIInsert();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ArrayList<Class> getInsertInterfaces() {

        HashSet<Class> hs = new HashSet<>();

        ArrayList<Method> al2 = GetParamIInsert();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (Class tt1 : tt) {
                    if (tt1.isInterface() && tt1.getSimpleName().startsWith("IR")) {
                        hs.add(tt1);
                    }
                }
            }
        }

        return new ArrayList<>(hs);
    }
}
