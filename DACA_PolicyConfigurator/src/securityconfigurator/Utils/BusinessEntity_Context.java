package securityconfigurator.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
            itfIReadArray = new ArrayList<Class>();
            for (int i = 0; i < itfs.length; i++) {
                itfIReadArray.add(itfs[i]);
            }
        }
    }

    public void Update_Class(Class itf) {
        this.itfUpdate = itf;
    }

    public void GetOther_Update_Class(Class itf) {
        Class[] itfs = itf.getInterfaces();
        if (itfs.length > 0) {
            itfIUpdateArray = new ArrayList<Class>();
            for (int i = 0; i < itfs.length; i++) {
                itfIUpdateArray.add(itfs[i]);
            }
        }
    }

    public void Insert_Class(Class itf) {
        this.itfInsert = itf;
    }

    public void GetOther_Insert_Class(Class itf) {
        Class[] itfs = itf.getInterfaces();
        if (itfs.length > 0) {
            itfIInsertArray = new ArrayList<Class>();
            for (int i = 0; i < itfs.length; i++) {
                itfIInsertArray.add(itfs[i]);
            }
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
        for (int i = 0; i < itfs.length; i++) {
            if (itfs[i].getName().contains("IRead")) {
                Read_Class(itfs[i]);
                GetOther_Read_Class(itfs[i]);
            }
            if (itfs[i].getName().contains("IUpdate")) {
                Update_Class(itfs[i]);
                GetOther_Update_Class(itfs[i]);
            }
            if (itfs[i].getName().contains("IInsert")) {
                Insert_Class(itfs[i]);
                GetOther_Insert_Class(itfs[i]);
            }
            if (itfs[i].getName().contains("IDelete")) {
                Delete_Class(itfs[i]);
            }
            if (itfs[i].getName().contains("IExecute")) {
                Execute_Class(itfs[i]);
            }
            if (itfs[i].getName().contains("IResult")) {
                Result_Class(itfs[i]);
            }

            if (itfs[i].getName().contains("IScrollable")) {
                Scrollable_Class(itfs[i]);
            }

            if (itfs[i].getName().contains("IForwardOnly")) {
                ForwardOnly_Class(itfs[i]);
            }
            if (itfs[i].getName().contains("ISet")) {
                Set_Class(itfs[i]);
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
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].getName().startsWith("u") && tmp[i].getName().compareTo("updateRow") != 0) {
                tmp2[size] = tmp[i];
                size++;
            }
        }
        return tmp2;
    }

    public Method[] getAllUpdateMethods() {
        Method[] tmp = this.itfUpdate.getMethods();

        Method[] tmp2 = new Method[tmp.length - 3];
        int size = 0;
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].getName().startsWith("u") && tmp[i].getName().compareTo("updateRow") != 0) {
                tmp2[size] = tmp[i];
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
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].getName().startsWith("i")) {
                tmp2[size] = tmp[i];
                size++;
            }
        }
        return tmp2;
    }

    public Method[] getAllInsertMethods() {
        Method[] tmp = this.itfInsert.getMethods();

        Method[] tmp2 = new Method[tmp.length - 3];
        int size = 0;
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].getName().startsWith("i")) {
                tmp2[size] = tmp[i];
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
        if (this.itfUpdate != null || this.itfInsert != null || this.itfDelete != null) {
            return false;
        }
        return true;
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
        ArrayList<Method> tmp = new ArrayList<Method>();
        Method[] mtds = this.itfExecute.getMethods();
        for (int i = 0; i < mtds.length; i++) {
            tmp.add(mtds[i]);
        }
        return tmp;

    }

    public ArrayList<Method> GetParamIUpdate() {
        ArrayList<Method> tmp = new ArrayList<Method>();
        Method[] mtds = this.itfUpdate.getMethods();
        for (int i = 0; i < mtds.length; i++) {
            tmp.add(mtds[i]);
        }
        return tmp;

    }

    public ArrayList<Method> GetParamIInsert() {
        ArrayList<Method> tmp = new ArrayList<Method>();
        Method[] mtds = this.itfInsert.getMethods();
        for (int i = 0; i < mtds.length; i++) {
            tmp.add(mtds[i]);
        }
        return tmp;

    }

    public String getReadClassPath() {
        return this.itfRead.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    public String getReadPackage() {
        return this.itfRead.getPackage().getName();
    }

    public String getExecutePackage() {
        return this.itfExecute.getPackage().getName();
    }

    public String getSetPackage() {
        return this.itfSet.getPackage().getName();
    }

    public String getUpdatePackage() {
        return this.itfUpdate.getPackage().getName();
    }

    public String getInsertPackage() {
        return this.itfInsert.getPackage().getName();
    }

    public String getReadAdditionalImports() {
        String imports = "";
        for (int i = 0; i < this.itfIReadArray.size(); i++) {
            imports += "import " + this.itfIReadArray.get(i).getName() + ";\n";
        }
        return imports;
    }

    public boolean ReadHaveAdditionalInterfaces() {
        if (this.itfIReadArray != null && this.itfIReadArray.size() > 0) {
            return true;
        }
        return false;
    }

    public ArrayList<Class> getReadAdditionalInterfaces() {
        return this.itfIReadArray;
    }

    public ArrayList<String> getReadAdditionalInterfacesAsString() {
        ArrayList<String> extds = new ArrayList<String>();
        for (int i = 0; i < this.itfIReadArray.size(); i++) {
            extds.add(this.itfIReadArray.get(i).getSimpleName());
        }

        return extds;
    }

    public boolean UpdateHaveAdditionalInterfaces() {
        if (this.itfIUpdateArray != null && this.itfIUpdateArray.size() > 0) {
            return true;
        }
        return false;
    }

    public ArrayList<String> getUpdateAdditionalInterfacesAsString() {
        ArrayList<String> extds = new ArrayList<String>();
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

    public String getUpdateAdditionalInterfaceImports() {
        String imports = "";
        ArrayList<Method> al2 = GetParamIUpdate();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface())
                        imports += "import " + tt[j].getName() + ";\n";
                }
            }
        }
        return imports;
    }

    public ArrayList<Class> getUpdateAdditionalInterfaces() {
        return this.itfIUpdateArray;
    }

    public boolean InsertHaveAdditionalInterfaces() {
        if (this.itfIInsertArray != null && this.itfIInsertArray.size() > 0) {
            return true;
        }
        return false;
    }

    public ArrayList<Class> getInsertAdditionalInterfaces() {
        return this.itfIInsertArray;
    }

    public ArrayList<String> getInsertAdditionalInterfacesAsString() {
        ArrayList<String> extds = new ArrayList<String>();
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
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface())
                        imports += "import " + tt[j].getName() + ";\n";
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
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface())
                        imports += "import " + tt[j].getName() + ";\n";
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
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        return true;
                }
            }
        }
        return false;
    }


    public boolean UpdateHaveAdditionalInterfacesInMethods() {
        ArrayList<Method> al2 = GetParamIUpdate();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Method> GetParamISet() {
        ArrayList<Method> tmp = new ArrayList<Method>();
        Method[] mtds = this.itfSet.getMethods();
        for (int i = 0; i < mtds.length; i++) {
            tmp.add(mtds[i]);
        }
        return tmp;

    }


    public boolean SetHaveAdditionalInterfaces() {
        ArrayList<Method> al2 = GetParamISet();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        return true;
                }
            }
        }
        return false;
    }


    public ArrayList<Class> getSetInterfaces() {

        ArrayList<Class> al = new ArrayList<>();

        ArrayList<Method> al2 = GetParamISet();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        al.add(tt[j]);
                }
            }
        }
        HashSet<Class> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
        return al;
    }

    public ArrayList<Class> getExecuteInterfaces() {

        ArrayList<Class> al = new ArrayList<>();

        ArrayList<Method> al2 = GetParamIExecute();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        al.add(tt[j]);
                }
            }
        }
        HashSet<Class> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
        return al;
    }

    public ArrayList<Class> getUpdateInterfaces() {

        ArrayList<Class> al = new ArrayList<>();

        ArrayList<Method> al2 = GetParamIUpdate();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        al.add(tt[j]);
                }
            }
        }
        HashSet<Class> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
        return al;
    }


    public String getInsertAdditionalInterfaceImports() {
        String imports = "";
        ArrayList<Method> al2 = GetParamIInsert();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface())
                        imports += "import " + tt[j].getName() + ";\n";
                }
            }
        }
        return imports;
    }

    public boolean InsertHaveAdditionalInterfacesInMethods() {
        ArrayList<Method> al2 = GetParamIInsert();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        return true;
                }
            }
        }
        return false;
    }


    public ArrayList<Class> getInsertInterfaces() {

        ArrayList<Class> al = new ArrayList<>();

        ArrayList<Method> al2 = GetParamIInsert();
        if (al2.size() > 0) {
            for (int i = 0; i < al2.size(); i++) {

                Class[] tt = al2.get(i).getParameterTypes();
                for (int j = 0; j < tt.length; j++) {
                    if (tt[j].isInterface() && tt[j].getSimpleName().startsWith("IR"))
                        al.add(tt[j]);
                }
            }
        }
        HashSet<Class> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
        return al;
    }
}

