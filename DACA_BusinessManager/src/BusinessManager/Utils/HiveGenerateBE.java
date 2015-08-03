package BusinessManager.Utils;

import LocalTools.BTC_Exception;
import LocalTools.BusinessEntity_Context;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class HiveGenerateBE extends GenerateBE {

    private final String structName;
    private String returnaux = "";

    public HiveGenerateBE(BusinessEntity_Context BE, String path, String jarName, Map<Integer, Map<Integer, String>> controlinfo) throws BTC_Exception {
        super(BE, path, jarName, controlinfo);
        structName = "Structure" + BE.getName().substring(3, BE.getName().length());

    }

    @Override
    public String getImports() {

        StringBuilder str = new StringBuilder();
        str.append("package ").append(BE.getPackage()).append(";\n");
        str.append("import BusinessInterfaces.*;\n");
        str.append("import BusinessManager.*;\n");
        str.append("import java.math.*;\n");
        str.append("import java.sql.*;\n");
        str.append("import java.net.URL;\n");
        str.append("import java.net.URLClassLoader;\n");
        str.append("import java.net.UnknownHostException;\n");
        str.append("import java.io.File.*;\n");
        str.append("import java.lang.reflect.Method;\n");

        str.append("import java.net.MalformedURLException;\n");
        str.append("import java.lang.reflect.Constructor;\n");
        str.append("import java.util.jar.*;\n");
        str.append("import java.sql.SQLException;\n");
        str.append("import java.util.Map;\n");
        str.append("import java.util.TreeMap;\n");
        str.append("import java.io.PrintWriter;\n");
        str.append("import java.sql.Statement;\n");
        str.append("import java.util.Scanner;\n");
        str.append("import java.net.Socket;\n\n");
        str.append("import java.util.Collection;\n");
        str.append("import java.util.Arrays;\n");
        str.append("import java.util.Iterator;\n");
        str.append("import java.util.Set;\n");

        //flume
        str.append("import java.io.File;");
        str.append("import com.google.common.collect.Lists;\n");
        str.append("import java.io.BufferedReader;\n");
        str.append("import java.io.FileReader;\n");
        str.append("import java.io.IOException;\n");
        str.append("import java.io.InputStreamReader;\n");
        str.append("import java.nio.charset.Charset;\n");
        str.append("import java.util.HashMap;\n");
        str.append("import java.util.List;\n");
        str.append("import java.util.Map;\n");
        str.append("import org.apache.flume.Event;\n");
        str.append("import org.apache.flume.FlumeException;\n");
        str.append("import org.apache.flume.EventDeliveryException;\n");
        str.append("import org.apache.flume.api.RpcClient;\n");
        str.append("import org.apache.flume.api.RpcClientFactory;\n");
        str.append("import org.apache.flume.event.EventBuilder;\n");

        str.append(BE.getExecuteAdditionalImports());

        if (BE.IsUpdate()) {
            str.append(BE.getUpdateAdditionalInterfaceImports());
        }

        if (BE.IsInsert()) {
            str.append(BE.getInsertAdditionalInterfaceImports());
        }

        if (BE.IsDelete()) {
            str.append(BE.getInsertAdditionalInterfaceImports());
        }

        str.append("class ").append(structName).append(" implements Cloneable {\n");
        if (this.BE.isRead()) {
            Method[] mtds = this.BE.getReadAllMethods();
            //declarar atributos da class

            str.append("static public int idToSend=1;\n");
            str.append("static public Map<String, Integer> map = new TreeMap<String, Integer>();\n");
            str.append("Object toArray[];\n");
            str.append("private Collection values;\n");

            if (mtds.length > 0) {
                for (Method mtd : mtds) {

                    if (mtd.getReturnType().getSimpleName().equals("date") || mtd.getReturnType().getSimpleName().equals("money")) { //melhor if != dos que existem

                        str.append("private ").append("String").append(" " + mtd.getName()).append(";\n");
                    } else {
                        str.append("private ").append(mtd.getReturnType().getSimpleName()).
                                append(" " + mtd.getName()).append(";\n");
                    }
                }
            }
            // metodos da class
            mtds = this.BE.getReadAllMethods();
            if (mtds.length > 0) {
                for (Method mtd : mtds) {

                    if (mtd.getReturnType().getSimpleName().equals("date") || mtd.getReturnType().getSimpleName().equals("money")) {
                        str.append("\n public ").append("void ").append(" set").append(mtd.getName().toLowerCase()).append("( ").append("String").append(" ").append(mtd.getName()).append(") ").append("{\n");
                        str.append("this.").append(mtd.getName()).append("=").append(mtd.getName()).append(";\n");
                        str.append("}\n");
                        str.append("\n public ").append("String").append(" get").append(mtd.getName().toLowerCase()).append("() ").append("{\n");
                        str.append("return this.").append(mtd.getName()).append(";\n");
                        str.append("}\n");
                    } else {
                        str.append("\n public ").append("void ").append(" set").append(mtd.getName().toLowerCase()).append("( ").append(mtd.getReturnType().getSimpleName()).append(" ").append(mtd.getName()).append(") ").append("{\n");
                        str.append("this.").append(mtd.getName()).append("=").append(mtd.getName()).append(";\n");
                        str.append("}\n");
                        str.append("\n public ").append(mtd.getReturnType().getSimpleName()).append(" get").append(mtd.getName().toLowerCase()).append("() ").append("{\n");
                        str.append("return this.").append(mtd.getName()).append(";\n");
                        str.append("}\n");
                    }
                }

                //clone()
                str.append("public ").append(structName).append(" myclone(){\n");
                str.append("try {\n");
                str.append(structName).append(" ret = " + "(").append(structName).append(")super.clone();\n");
                str.append("ret.toArray = (toArray == null ? null : toArray.clone());\n");
                str.append("ret.map = new HashMap<>(map);\n");
                str.append("ret.values = null;\n");
                str.append("return ret;");
                str.append("}catch(CloneNotSupportedException e){");
                str.append("throw  new RuntimeException(e);");
                str.append("}");
                str.append("}");

                //tostring()
                str.append("@Override \n");
                str.append("public String toString(){\n");
                //  String auxreturn = "";
                //  String auxreturnf = "";

                mtds = this.BE.getReadAllMethods();
                if (mtds.length > 0) {
//
                    str.append("String auxreturn = \"\";\n");
                    str.append("String auxreturnf = \"\";\n");
                    str.append(" values=map.values();\n").append("toArray=values.toArray();\n").append("Arrays.sort(toArray);\n");
                    str.append("for (int i=0;i< toArray.length;i++){\n");
                    str.append("Set<Map.Entry<String,Integer>> entrySet= map.entrySet();\n");
                    str.append("Iterator<Map.Entry<String,Integer>> iterator =entrySet.iterator();\n");
                    str.append("while(iterator.hasNext()){\n");
                    str.append("Map.Entry<String,Integer> next =iterator.next();\n");
                    str.append("if(toArray[i]==next.getValue()){\n");
                    str.append("try{\n");
                    str.append("auxreturn += getClass().getDeclaredMethod(\"get\" + next.getKey()).invoke(this) + \",\";\n");
                    str.append("} catch(Exception ex) {System.err.println(\"Error invoking methods in toString(): + \" + ex);}\n");
                    str.append("}}}\n");
                    str.append("auxreturnf = idToSend + \",\" + auxreturn.substring(0, (auxreturn.length() - 1));");
                    //
                }
                str.append("return ").append("auxreturnf").append(";\n");
                str.append("}\n");
            }
        }
        str.append("}\n");
        //STRUCT GENERAT CLASS over
        return str.toString();
    }

    @Override
    public String getAditionalConstructorOperations() {
        StringBuilder str = new StringBuilder();

        if (BE.IsUID()) {
            str.append("ps=conn.prepareStatement(this.crud);\n");
        } else {
            str.append("ps=conn.prepareStatement(this.crud);\n");
            str.append("_state=idle;\n");
        }
        return str.toString();
    }

    @Override
    public String getMethodsForwardOnly() {
        StringBuilder str = new StringBuilder();
        str.append("@Override\n");
        str.append("public boolean moveNext() throws SQLException{\n");
        str.append(validationSourceCode);
        //R 
        str.append("if(rs.next()){\n");

        str.append("struct = new ").append(structName).append("();\n");

        Method[] mtds = this.BE.getReadAllMethods();
        if (mtds.length > 0) {
            for (Method mtd : mtds) {
                if (mtd.getReturnType().getSimpleName().equals("date") || mtd.getReturnType().getSimpleName().equals("money")) {
                    str.append("struct.set" + mtd.getName().toLowerCase() + "(").append("rs.getString(").append("struct.map.get(").append("\"").append(mtd.getName().toLowerCase()).append("\"").append(")));\n");

                } else {
                    str.append("struct.set" + mtd.getName().toLowerCase() + "(").append("rs.get").append(capitaliseFirstLetter(mtd.getReturnType().getSimpleName())).append("(struct.map.get(").append("\"").append(mtd.getName().toLowerCase()).append("\"").append(")));\n");
                }
            }
        }
        //-R
        str.append("}");
        str.append("return rs.next();\n");
        str.append("}");
        return str.toString();
    }

    @Override
    public String getMethodResult() {
        StringBuilder str = new StringBuilder();
        str.append("@Override\n");
        str.append("public int getNAffectedRows(){\n");
        str.append(validationSourceCode);
        str.append("return nrows;\n");
        str.append("}");
        return str.toString();
    }

    @Override
    public String getMethodsSet() {
        StringBuilder str = new StringBuilder();
        ArrayList<Method> al2 = BE.GetParamISet();
        if (al2.size() > 0) {
            for (int j = 0; j < al2.size(); j++) {
                Class[] tmp = al2.get(j).getParameterTypes();
                str.append("@Override\n");
                str.append("public void set(");
                for (int i = 0; i < tmp.length; i++) {
                    if (i < tmp.length - 1) {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i).append(",");
                    } else {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i);
                    }
                }
                str.append(") throws SQLException {\n");
                str.append(validationSourceCode);
                if (tmp.length > 0) {
                    if (tmp[0] != null) {
                        if (tmp[0].equals(Object[].class)) {
                            str.append("for (int i = 0; i < args0.length; i++) {");
                            str.append("ps.setObject(i+1,args0[i]);\n");
                            str.append("}\n");
                        } else {
                            for (int i = 0; i < tmp.length; i++) {
                                if (tmp[i].isInterface()) {
                                    //str.append("if(!validateSchema(args").append(i).append(".getClass()))");
                                    //str.append("throw new SQLException(\"Invalid Schema!\");");
                                    Method mtd = tmp[i].getDeclaredMethods()[0];

                                    str.append("ps.set").append(capitaliseFirstLetter(getType(mtd.getReturnType().getSimpleName()))).append("(").append(i + 1).append(",args").append(i).append(".").append(mtd.getName()).append("()").append(");\n");
                                } else {
                                    str.append("ps.set").append(capitaliseFirstLetter(getType(tmp[i].getSimpleName()))).append("(").append(i + 1).append(",args").append(i).append(");\n");
                                }
                            }
                        }
                    }
                }
                str.append("}\n");
            }
        }
        return str.toString();
    }

    @Override
    public String getMethodsRead() {
        StringBuilder med = new StringBuilder();
        Method[] mtds = this.BE.getReadAllMethods();
        if (mtds.length > 0) {
            for (Method mtd : mtds) {

                if (mtd.getReturnType().getSimpleName().equals("date") || mtd.getReturnType().getSimpleName().equals("money")) {
                    med.append("@Override");
                    med.append("\n public ").append("String").append(" ").append(mtd.getName()).append("() ").append("throws SQLException{\n");
                    med.append(validationSourceCode);
                    med.append("return struct.get").append(mtd.getName().toLowerCase()).append("(").append(");}\n");

                } else {
                    med.append("@Override");
                    med.append("\n public ").append(mtd.getReturnType().getSimpleName()).append(" ").append(mtd.getName()).append("() ").append("throws SQLException{\n");
                    med.append(validationSourceCode);
                    med.append("return struct.get").append(mtd.getName().toLowerCase()).append("(").append(");}\n");
                }
            }
        }
        return med.toString();
    }

    @Override
    public String getMethodsScroll() {
        StringBuilder str = new StringBuilder();
        str.append("@Override\n");
        str.append("public boolean moveNext() throws SQLException{\n");
        str.append(validationSourceCode);
        str.append("if(rs.next()){\n");

        str.append("struct = new ").append(structName).append("();\n");

        Method[] mtds = this.BE.getReadAllMethods();
        if (mtds.length > 0) {
            for (Method mtd : mtds) {
                if (mtd.getReturnType().getSimpleName().equals("date") || mtd.getReturnType().getSimpleName().equals("money")) {
                    str.append("struct.set" + mtd.getName().toLowerCase() + "(").append("rs.getString").append("(struct.map.get(").append("\"").append(mtd.getName().toLowerCase()).append("\"").append(")));\n");
                } else {
                    str.append("struct.set" + mtd.getName().toLowerCase() + "(").append("rs.get").append(capitaliseFirstLetter(mtd.getReturnType().getSimpleName())).append("(struct.map.get(").append("\"").append(mtd.getName().toLowerCase()).append("\"").append(")));\n");
                }
            }
        }
        str.append("}");
        str.append("return rs.next();\n");
        str.append("}");

        str.append("@Override\n");
        str.append("public boolean moveAbsolute(int pos) throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("throw new UnsupportedOperationException(\"unsupported by hive\");\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public boolean moveRelative(int offset) throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("throw new UnsupportedOperationException(\"unsupported by hive\");\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public void moveBeforeFirst() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("throw new UnsupportedOperationException(\"unsupported by hive\");\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public boolean moveFirst() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("throw new UnsupportedOperationException(\"unsupported by hive\");\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public void moveAfterLast() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("throw new UnsupportedOperationException(\"unsupported by hive\");\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public boolean moveLast() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("throw new UnsupportedOperationException(\"unsupported by hive\");\n");
        str.append("}\n");

        return str.toString();
    }

    @Override
    public String getMethodsExecute() {
        StringBuilder str = new StringBuilder();
        ArrayList<Method> al2 = BE.GetParamIExecute();
        if (al2.size() > 0) {
            for (Method method : al2) {
                Class[] tmp = method.getParameterTypes();
                str.append("@Override\n");
                str.append("public void execute(");
                for (int i = 0; i < tmp.length; i++) {
                    if (i < tmp.length - 1) {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i).append(",");
                    } else {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i);
                    }
                }
                str.append(") throws SQLException {\n");

                str.append(validationSourceCode);
                str.append("count=1;\n");

                if (tmp.length > 0) {
                    if (tmp[0] != null) {
                        if (tmp[0].equals(Object[].class)) {
                            str.append("for (int i = 0; i < args0.length; i++) {");
                            str.append("ps.setObject(i+1,args0[i]);\n");
                            str.append("}\n");

                        } else {
                            for (int i = 0; i < tmp.length; i++) {
                                if (tmp[i].isInterface()) {
//                                    str.append("if(!validateSchema(args" + i + ".getClass()))");
//                                    str.append("throw new SQLException(\"Invalid Schema!\");");
                                    Method mtd = tmp[i].getDeclaredMethods()[0];

                                    if (mtd.getReturnType().getSimpleName().equals("date") || mtd.getReturnType().getSimpleName().equals("money")) {
                                        str.append("ps.setString" + "(" + (i + 1) + ",args" + i + "." + mtd.getName() + "().toString()" + ");\n");
                                    } else {
                                        str.append("ps.set" + capitaliseFirstLetter(getType(mtd.getReturnType().getSimpleName())) + "(" + (i + 1) + ",args" + i + "." + mtd.getName() + "()" + ");\n");
                                    }
                                } else {
                                    if (getType(tmp[i].getSimpleName()).toLowerCase().equals("date") || getType(tmp[i].getSimpleName()).toLowerCase().equals("money")) {
                                        str.append("ps.setString" + "(" + (i + 1) + ",args" + i + ".toString());\n");

                                    } else {
                                        str.append("ps.set" + capitaliseFirstLetter(getType(tmp[i].getSimpleName())) + "(" + (i + 1) + ",args" + i + ");\n");
                                    }
                                }
                            }
                        }
                    }
                }
                if (this.BE.IsUID()) {
                    str.append("nrows=ps.executeUpdate();\n");
                } else {
                    str.append("rs=ps.executeQuery();\n");
                    str.append("stmt = conn.createStatement();\n");
                    str.append("res = stmt.executeQuery(\"describe " + this.BE.getName().substring(3, BE.getName().length()) + "\");\n");

                    str.append(" while (res.next()) {\n");
                    str.append("");
                    str.append("struct.map.put(res.getString(1).trim().toLowerCase(), count);");
                    str.append("count++;");
                    str.append("}\n");
//

                }
                //--r DESCRIBE
                str.append("}\n");
            }

        }

        return str.toString();
    }

    @Override
    public String getScrollType() {
        if (this.BE.isScrollable()) {
            if (this.BE.ReadOnly()) {
                throw new UnsupportedOperationException("unsupported by hive");
            }
            throw new UnsupportedOperationException("unsupported by hive");
        }
        throw new UnsupportedOperationException("unsupported by hive");
    }

    @Override
    public String getRsConcur() {
        if (this.BE.ReadOnly()) {
            throw new UnsupportedOperationException("unsupported by hive");
        } else {
            throw new UnsupportedOperationException("unsupported by hive");
        }
    }

    @Override
    public String getClassDecl() {
        StringBuilder str = new StringBuilder();
        str.append("public class ").append(BE.getName().substring(1, BE.getName().length())).append(" implements ").append(this.BE.getName());
        str.append("{\n");

        if (!BE.IsUID()) {
            str.append("public final int idle=0;\n");
            str.append("public final int updating=1;\n");
            str.append("public final int inserting=2;\n");
            str.append("public int _state=idle;\n");
        }
        return str.toString();
    }

    @Override
    public String getVariablesDecl() { //check
        StringBuilder str = new StringBuilder();

        //str.append("Structure struct = new Structure();\n");
        //socket
        str.append("private static Scanner in;\n"
                + " private static PrintWriter out;");
        //loadData
        str.append("private int count=1;\n");
        str.append("private Statement stmt;\n");
        str.append("private ResultSet res;\n");
        //flume
        str.append(" private static final int BATCH_SIZE = 5;\n");
        str.append("private  String hostname = \"10.0.0.3\";\n");
        str.append("private  int port= 41414;\n");
        str.append("private  String fileName = \"insert.txt\";\n");
        str.append("private Map<String, String> headers = new HashMap<String, String>();\n");
        str.append("private int sent;\n");

        str.append(structName).append(" struct = new ").append(structName).append("();\n");

        //struct para o update
        str.append(structName).append(" structupdate = new ").append(structName).append("();\n");

//R
// o statment ps vai estar encarregue de executar os meus .executeQuery(). Este vai estar encarregue do meu resultSet so de leitura     
// necessito de criar um ps2 para executar os meus .executeUpdate() [insert,update,delete].
        //criei tambem um int nrows2 para ter o retorno do ps2
//NOTA: faria sentido colocar a criação desta variavél so quando fossem permitidas as opções de insert, update, delete 
        str.append("private PreparedStatement ps2;\n");
        str.append("private int nrows2;\n");
//-R
        return str.toString();
    }

    @Override
    public String getMethodsUpdate() {
        StringBuilder str = new StringBuilder();
        //--
        Method[] mtds = this.BE.getReadAllMethods();

        String l = "";

        str.append("public void deleteRow() throws SQLException {\n");
        str.append(validationSourceCode);

        ArrayList<Method> order = new ArrayList<>();
        if (mtds.length > 0) {
            for (Method mtd : mtds) {
                l += mtd.getName() + " != (?) or ";
                order.add(mtd);
            }
        }
        l = l.substring(0, l.length() - 3);

        str.append(" ps2=conn.prepareStatement(\"").append("INSERT overwrite TABLE " + BE.getName().substring(3, BE.getName().length())).append(" select * from " + BE.getName().substring(3, BE.getName().length())).append(" where (").append(l).append(")\");\n");

        String aux = "";
        if (order.size() > 0) {
            for (int i = 1; i <= order.size(); i++) {//sets
                Method mtd = order.get(i - 1);
                aux = mtd.getReturnType().getSimpleName().toLowerCase();

                if (aux.equals("date") || aux.equals("money")) {
                    str.append("ps2.setString" + "(" + i + ",struct.get" + mtd.getName().toLowerCase() + "().toString());\n");

                } //    str.append("ps2.set(" + mtd.getName() + ",struct.get" + mtd.getName() + "());\n");
                else {
                    str.append("ps2.set" + capitaliseFirstLetter(aux) + "(" + i + ",struct.get" + mtd.getName().toLowerCase() + "());\n");
                }
            }
        }
        str.append(" nrows2=  ps2.executeUpdate();");
        str.append("}\n");
        //--

        str.append("@Override\n");
        str.append("public void beginUpdate() throws SQLException {\n");
        str.append(validationSourceCode);

        str.append("if(_state==inserting)\n");
        str.append("throw new SQLException(\"Cant Update, While Inserting!\");\n");
        str.append("else{\n");
        str.append("structupdate=struct.myclone();\n");
        str.append("_state=updating;}\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public void updateRow() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("if(_state!=updating)\n");
        str.append("throw new SQLException(\"Cant Update, While not updating!\");\n");
        str.append("else{\n");
        str.append("_state=idle;\n");
        //verificar se cada um dos metodos faz mesmo o que fazia anteriormente
        str.append("deleteRow();\n").
                append("beginInsert();\n").
                append("struct=structupdate.myclone();").
                append("insertRow();\n");

        //-- str.append("rs.updateRow();}\n");
        str.append("}}\n");

        str.append("@Override\n");
        str.append("public void cancelUpdate() throws SQLException {\n");
        str.append(validationSourceCode);
        //str.append("rs.cancelRowUpdates();\n");
        str.append("_state=idle;\n");
        str.append("}\n");
        mtds = this.BE.getAllUpdateMethods();
        if (mtds.length > 0) {
            for (Method mtd1 : mtds) {
                str.append("@Override");
                str.append("\n").append("public void").append(" ").append(mtd1.getName()).append("(").append(mtd1.getParameterTypes()[0].getSimpleName()).append(" ").append(mtd1.getName().substring(1, mtd1.getName().length())).append(")").append(" throws SQLException{\n");
                str.append(validationSourceCode);
                str.append("if(_state!=updating)\n");
                str.append("throw new SQLException(\"Cant Update, While Inserting, or not BeginUpdating!\");\n");
                str.append("else{\n");
                if (mtd1.getParameterTypes()[0].isInterface()) {
                    Method mtd = mtd1.getParameterTypes()[0].getDeclaredMethods()[0];
                    str.append("structupdate.set").append(mtd.getName().toLowerCase()).append("").append("(").append(mtd.getName()).append(".").append(mtd.getName()).append("()").append(");}\n");
                } else {
                    str.append("structupdate.set").append(mtd1.getName().toLowerCase().substring(1, mtd1.getName().length())).append("").append("(").append(mtd1.getName().substring(1, mtd1.getName().length())).append(");}\n");
                }
                str.append("}\n");
            }
        }
        return str.toString();
    }

    //comment the delete file lol.java
    @Override
    public String getMethodsInsert() { //metodo vai ficar incompleto.falta o load() e o write num ficheiro
        StringBuilder str = new StringBuilder();

        str.append("public void runFlume() throws IOException, FlumeException,EventDeliveryException {\n"
                + "        BufferedReader reader = null;\n"
                + "        RpcClient rpcClient = RpcClientFactory.getDefaultInstance(hostname, port, BATCH_SIZE);\n"
                + "        try {\n"
                + "            List<Event> eventBuffer = Lists.newArrayList();\n"
                + "            if (fileName != null) {\n"
                + "                reader = new BufferedReader(new FileReader(new File(fileName)));\n"
                + "            } else {\n"
                + "                reader = new BufferedReader(new InputStreamReader(System.in));\n"
                + "            }\n"
                + "            String line;\n"
                + "            long lastCheck = System.currentTimeMillis();\n"
                + "            long sentBytes = 0;\n"
                + "            int batchSize = rpcClient.getBatchSize();\n"
                + "            while ((line = reader.readLine()) != null) {\n"
                + "                int size = eventBuffer.size();\n"
                + "                if (size == batchSize) {\n"
                + "                    rpcClient.appendBatch(eventBuffer);\n"
                + "                    eventBuffer.clear();\n"
                + "                }\n"
                + "                Event event = EventBuilder.withBody(line, Charset.forName(\"UTF8\"));\n"
                + "                event.setHeaders(headers);\n"
                + "                eventBuffer.add(event);\n"
                + "                sentBytes += event.getBody().length;\n"
                + "                sent++;\n"
                + "                long now = System.currentTimeMillis();\n"
                + "                if (now >= lastCheck + 5000) {\n"
                + "                    lastCheck = now;\n"
                + "                }\n"
                + "            }\n"
                + "            if (!eventBuffer.isEmpty()) {\n"
                + "                rpcClient.appendBatch(eventBuffer);\n"
                + "            }\n"
                + "        } finally {\n"
                + "            if (reader != null) {\n"
                + "                reader.close();\n"
                + "            }\n"
                + "            rpcClient.close();\n"
                + "        }\n"
                + "    }\n");
        str.append("\n");
        str.append("public void loadData() {\n");
        //RUNFLUME 
        //PRIMEIRO É FEITO O ENVIO DO FICHEIRO, DEPOIS, É FEITO O LOAD DO FICHEIRO PARA A TABELA
        //COMO FICHEIRO DE TESTE, UMA FEZ QUE A RECEÇÃO DO FLUME AINDA NÃO ESTA FEITA, VOU AO FICHEIRO AUX.TXT
        str.append("try{");
        str.append("runFlume();");

        str.append("\n"
                + "        Socket socket = new Socket(hostname, 6060);\n"
                + "\n"
                + "        //Crio o fluxo para o envio de dados\n"
                + "        out = new PrintWriter(socket.getOutputStream()); //escrever linha\n"
                + "        in = new Scanner(socket.getInputStream()); //ler linha\n"
                + "\n"
                + "        out.println(\"autenticacao\");\n"
                + "        out.println(struct.idToSend);\n"
                + "        out.flush();\n"
                + "\n"
                + "        if (in.nextLine().trim().equals(\"autok\")) {\n"
                + "            //envia flume...\n"
                + "            \n"
                + "            System.err.println(\"autok\");\n"
                + "            out.println(\"le\");\n"
                + "            out.flush();\n"
                + "        }\n"
                + "\n"
                + "        if (in.nextLine().trim().equals(\"fim\")) {\n"
                + "\n"
                + "            System.err.println(\"fim\");\n"
                + "        struct.idToSend++;\n"
                + "        }\n"
                + "\n"
                + "        out.close();\n"
                + "        in.close();\n"
                + "        socket.close();");
        str.append("stmt =conn.createStatement();\n "
                + "stmt.execute(\"load data local inpath '/home/hadoop/BDProj/aux.txt' into table aux" + BE.getName().substring(3, BE.getName().length()) + "\");\n");
        str.append("} catch (SQLException ex) {System.out.println(\"Error running flume. \" + ex);");
        str.append("} catch (FlumeException | EventDeliveryException | IOException ex) {System.out.println(\"Error running flume.\" + ex);}\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public void beginInsert() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("if(_state==updating)\n");
        str.append("throw new SQLException(\"Cant Insert, While Updating!\");\n");
        str.append("else{\n");
        str.append("_state=inserting;\n").append("struct = new ").append(structName).append("();\n").append("}\n");
        // str.append("rs.moveToInsertRow();\n");
        str.append("}\n");
        str.append("@Override\n");
        str.append("public void endInsert(boolean moveToPreviousRow) throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("if(_state!=inserting)\n");
        str.append("throw new SQLException(\"Cant Insert, While not inserting!\");\n");
        str.append("else{\n");
        str.append("insertRow();\n");
        str.append("_state=idle;}}\n");

        str.append("public void insertRow() throws SQLException{\n");
        str.append("if(_state==inserting){\n");

        str.append("File f = new File(\"insert.txt\");\n");
        str.append("try {\n");
        str.append("PrintWriter pw = new PrintWriter(f)\n;");
        str.append("pw.println(struct);\n  pw.flush();\n pw.close();\n loadData();\n");
        str.append("} catch (java.io.FileNotFoundException ex) {System.out.println(\"IO error.\");}\n");
        str.append(" ps2=conn.prepareStatement(").append(" \"INSERT into TABLE " + this.BE.getName().substring(3, BE.getName().length())).append(" select * from aux" + BE.getName().substring(3, BE.getName().length()) + "\"); \n");
        str.append(" nrows2=  ps2.executeUpdate();");

        str.append("}\n");
        str.append("}\n");

        //INSERT into TABLE BDCidade SELECT * FROM aux
        //MUITO INCOMPLETO
        //--str.append("rs.insertRow();}\n");
        // str.append("if(moveToPreviousRow)\n");
        //str.append("rs.moveToCurrentRow();\n");
        //        str.append("}\n");
        //       str.append("}\n");
        //str.append("@Override\n");
        str.append("public void cancelInsert() throws SQLException {\n");
        str.append(validationSourceCode);
        // str.append("rs.moveToCurrentRow();\n");
        str.append("_state=idle;\n");
        str.append("}\n");
        Method[] mtds = this.BE.getAllInsertMethods();
        if (mtds.length > 0) {
            for (Method mtd1 : mtds) {
                str.append("@Override");
                str.append("\n").append("public void").append(" ").append(mtd1.getName()).append("(").append(mtd1.getParameterTypes()[0].getSimpleName()).append(" ").append(mtd1.getName().substring(1, mtd1.getName().length())).append(")").append(" throws SQLException{\n");
                str.append(validationSourceCode);
                str.append("if(_state!=inserting)\n");
                str.append("throw new SQLException(\"Cant insert, While updating or not BeginInsert!\");\n");
                str.append("else{\n");
                if (mtd1.getParameterTypes()[0].isInterface()) {
                    Method mtd = mtd1.getParameterTypes()[0].getDeclaredMethods()[0];
                    str.append("struct.set").append(mtd.getName().toLowerCase()).append("").append("(").append(mtd.getName()).append(".").append(mtd.getName()).append("()").append(");}\n");
                } else {
                    str.append("struct.set").append(mtd1.getName().toLowerCase().substring(1, mtd1.getName().length())).append("").append("(").append(mtd1.getName().substring(1, mtd1.getName().length())).append(");}\n");
                }
                str.append("}\n");
            }
        }
        return str.toString();
    }

    @Override
    public String getMethodsDelete() { //tudo alterado
        //POSSO APAGAR, JA TA A SER CARREGADO NO UPDATE
        StringBuilder str = new StringBuilder();
        //--

        return "";
    }

    @Override
    protected String getClasspath() {
        StringBuilder strb = new StringBuilder();
        File hivedir = new File("lib/hive");
        File[] listFiles = hivedir.listFiles((File dir, String name) -> name.contains(".jar"));
        for (File f : listFiles) {
            strb.append(";").append(f.getAbsolutePath());
        }
        return strb.toString();
    }
}
