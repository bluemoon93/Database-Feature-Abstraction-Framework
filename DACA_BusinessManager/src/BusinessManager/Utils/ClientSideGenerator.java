package BusinessManager.Utils;

import LocalTools.BTC_Exception;
import LocalTools.BusinessEntity_Context;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class ClientSideGenerator extends GenerateBE {

    public ClientSideGenerator(BusinessEntity_Context BE, String path, String jarName, Map<Integer, Map<Integer, String>> controlinfo) throws BTC_Exception {
        super(BE, path, jarName, controlinfo);
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
        str.append("import java.io.File;\n");
        str.append("import java.util.Random;\n");

        str.append("import java.net.Socket;\n");
        str.append("import java.io.ObjectInputStream;\nimport java.io.ObjectOutputStream;\n");
       // str.append("import static LocalTools.MessageTypes.*;");

        // changeConn
        str.append("import java.net.MalformedURLException;\n");
        str.append("import java.lang.reflect.Constructor;\n");
        str.append("import java.util.jar.*;\n");
        str.append("import java.lang.reflect.Method;\n\n");

        str.append(BE.getExecuteAdditionalImports());
        if (BE.IsUpdate()) {
            str.append(BE.getUpdateAdditionalInterfaceImports());
        }
        if (BE.IsInsert()) {
            str.append(BE.getInsertAdditionalInterfaceImports());
        }

        // why not request additional delete imports?
        str.append("\n\n//This is a modded generator!\n\n");
        return str.toString();
    }

    @Override
    public String getAditionalConstructorOperations() {
        StringBuilder str = new StringBuilder();

        str.append("\t\tint idx;\n");
        str.append("\t\tif((idx = crud.indexOf(\"@Params\")) != -1) {\n");
        str.append("\t\t\tint pi = crud.indexOf('\\'', idx) + 1;\n");
        str.append("\t\t\tint pf = crud.indexOf('\\'', pi);\n");
        str.append("\t\t\tthis.params = crud.substring(pi, pf);\n");
        str.append("\t\t\tthis.crud = crud.substring(0, pi-1) + \"?\";\n");
        str.append("\t\t} else this.params = null;\n");

        /*if (BE.IsUID()) {
         str.append("\tps=conn.prepareStatement(this.crud);\n");
         } else {
         str.append("\tps=conn.prepareStatement(this.crud,").append(getScrollType()).append(",").append(getRsConcur()).append(");\n");
         str.append("\t_state=idle;\n");
         }*/
        if (!BE.IsUID()) {
            //str.append("\t\tps=null;\n");
            str.append("\t\t_state=idle;\n");
        }

        return str.toString();
    }

    @Override
    public String getMethodsForwardOnly() {
        StringBuilder str = new StringBuilder();
        str.append("\t@Override\n");
        str.append("\tpublic boolean moveNext() throws SQLException{\n");
        str.append(validationSourceCode);
        //str.append("\t\treturn rs.next();\n");
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(NEXT); oos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\treturn ois.readBoolean();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t\treturn false;\n");
        str.append("\t}\n");
        return str.toString();
    }

    @Override
    public String getMethodResult() {
        StringBuilder str = new StringBuilder();
        str.append("\t@Override\n");
        str.append("\tpublic int getNAffectedRows(){\n");
        str.append(validationSourceCode);
        str.append("\t\treturn nrows;\n");
        str.append("\t}\n");
        return str.toString();
    }

    @Override
    public String getMethodsSet() { // todo
        StringBuilder str = new StringBuilder();
        ArrayList<Method> al2 = BE.GetParamISet();
        if (al2.size() > 0) {
            for (Method method : al2) {
                Class[] tmp = method.getParameterTypes();
                str.append("\t@Override\n");
                str.append("\tpublic void set(");
                for (int i = 0; i < tmp.length; i++) {
                    if (i < tmp.length - 1) {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i).append(", ");
                    } else {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i);
                    }
                }
                str.append(") throws SQLException {\n");

                str.append(validationSourceCode);

                if (tmp.length > 0 && tmp[0] != null) {
                    str.append("\tString execParams = this.params;\n");

                    // Regex for matching a single question mark: "\\?"
                    String qmRegex = "\"\\\\?\"";
                    // String escaped double quote escape: "\""
                    String dqstr = "\"\\\"\"";

                    if (tmp[0].equals(Object[].class)) {
                        str.append("\tfor (int i = 0; i < args0.length; i++) {\n");
                        str.append(String.format("\t\texecParams = execParams.replaceFirst(%s, %s+args0[i]+%s);\n", qmRegex, dqstr, dqstr));
                        //str.append("ps.setObject(i+1,args0[i]);\n");
                        str.append("\t}\n");

                    } else {
                        for (int i = 0; i < tmp.length; i++) {
                            if (tmp[i].isInterface()) {
                                //str.append("if(!validateSchema(args").append(i).append(".getClass()))");
                                //str.append("throw new SQLException(\"Invalid Schema!\");");
                                Method mtd = tmp[i].getDeclaredMethods()[0];

                                str.append(String.format("\t\t\texecParams = execParams.replaceFirst(%s,%s+args%d.%s()+%s);\n", qmRegex, dqstr, i, mtd.getName(), dqstr));
                                //str.append("ps.set" + capitaliseFirstLetter(getType(mtd.getReturnType().getSimpleName())) + "(" + (i + 1) + ",args" + i + "." + mtd.getName() + "());\n");
                            } else {
                                str.append(String.format("\t\t\texecParams = execParams.replaceFirst(%s,%s+args%d+%s);\n", qmRegex, dqstr, i, dqstr));
                                //str.append("ps.set" + capitaliseFirstLetter(getType(tmp[i].getSimpleName())) + "(" + (i + 1) + ",args" + i + ");\n");
                            }
                        }
                    }
                    // str.append("\tps.setString(3, execParams);\n");

                    str.append("\t\ttry{\n\t\t\toos.writeInt(SET);\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeObject(execParams);\n\t\t\toos.flush();");
                    str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                    str.append("\t\t\tois.readObject();\n");
                    str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
                }


                /*
                 if (tmp.length > 0) {
                 if (tmp[0] != null) {
                 if (tmp[0].equals(Object[].class)) {
                 str.append("\t\tfor (int i = 0; i < args0.length; i++) {");
                 str.append("\t\t\tps.setObject(i+1,args0[i]);\n");
                 str.append("\t\t}\n");

                 } else {
                 for (int i = 0; i < tmp.length; i++) {
                 if (tmp[i].isInterface()) {
                 Method mtd = tmp[i].getDeclaredMethods()[0];

                 str.append("\t\tps.set").append(capitaliseFirstLetter(getType(mtd.getReturnType().getSimpleName()))).append("(").append(i + 1).append(",args").append(i).append(".").append(mtd.getName()).append("()").append(");\n");
                 } else {
                 str.append("\t\tps.set").append(capitaliseFirstLetter(getType(tmp[i].getSimpleName()))).append("(").append(i + 1).append(",args").append(i).append(");\n");
                 }

                 }
                 }
                 }
                 }
                 */
                str.append("}\n");

            }
        }

        return str.toString();
    }

    @Override
    public String getMethodsRead() {
        StringBuilder str = new StringBuilder();
        Method[] mtds = this.BE.getReadAllMethods();
        if (mtds.length > 0) {
            int i = 1;
            for (Method mtd : mtds) {
                str.append("\t@Override\n");
                str.append("\tpublic ").append(mtd.getReturnType().getSimpleName()).append(" ").append(mtd.getName()).append("() ").append("throws SQLException{\n");
                str.append(validationSourceCode);
                str.append("\t\ttry{\n");
                str.append("\t\t\toos.writeInt(GET);\n\t\t\toos.flush();\n");
                str.append("\t\t\toos.writeUTF(\"").append(mtd.getName()).append("\");\n\t\t\toos.flush();\n");
                //str.append("\t\t\toos.writeInt("+i+");\n\t\t\toos.flush();\n");
                //System.out.println("Writing "+mtd.getName()+" at position "+i);
                str.append("\t\t\toos.writeUTF(\"").append(mtd.getReturnType().getSimpleName()).append("\");\n\t\t\toos.flush();\n");
                str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                str.append("\t\t\treturn (").append(mtd.getReturnType().getSimpleName()).append(") ois.readObject();\n");
                str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
                if (mtd.getReturnType().getSimpleName().equals("boolean")) {
                    str.append("\t\treturn false;\n");
                } else if (Character.isLowerCase(mtd.getReturnType().getSimpleName().charAt(0))) {
                    str.append("\t\treturn 0;\n");
                } else {
                    str.append("\t\treturn null;\n");
                }
                //str.append("\t\t\treturn rs.get").append(capitaliseFirstLetter(mtd.getReturnType().getSimpleName())).append("(").append("\"").append(mtd.getName()).append("\"").append(");\n");
                str.append("\t}\n");
                i++;
            }

        }
        return str.toString();

    }

    @Override
    public String getMethodsScroll() {
        StringBuilder str = new StringBuilder();
        str.append("\t@Override\n");
        str.append("\tpublic boolean moveNext() throws SQLException{\n");
        str.append(validationSourceCode);
        //str.append("\treturn rs.next();\n");
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(NEXT);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\treturn ois.readBoolean();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t\treturn false;\n");
        str.append("\t}\n\n");

        str.append("\t@Override\n");
        str.append("\tpublic boolean moveAbsolute(int pos) throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(ABSOLUTE);\n\t\t\toos.flush();\n\t\t\toos.writeInt(pos);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\treturn ois.readBoolean();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t\treturn false;\n");
        //str.append("\treturn rs.absolute(pos);\n");
        str.append("\t}\n\n");

        str.append("\t@Override\n");
        str.append("\tpublic boolean moveRelative(int offset) throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(RELATIVE);\n\t\t\toos.flush();\n\t\t\toos.writeInt(offset);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\treturn ois.readBoolean();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t\treturn false;\n");
        //str.append("\treturn rs.relative(offset);\n");
        str.append("\t}\n\n");

        str.append("\t@Override\n");
        str.append("\tpublic void moveBeforeFirst() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(BEFORE_FIRST);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\trs.beforeFirst();\n");
        str.append("\t}\n\n");

        str.append("\t@Override\n");
        str.append("\tpublic boolean moveFirst() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(FIRST);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\treturn ois.readBoolean();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t\treturn false;\n");
        //str.append("\treturn rs.first();\n");
        str.append("\t}\n\n");

        str.append("\t@Override\n");
        str.append("\tpublic void moveAfterLast() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(AFTER_LAST);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\trs.afterLast();\n");
        str.append("\t}\n\n");

        str.append("\t@Override\n");
        str.append("\tpublic boolean moveLast() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(LAST);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\treturn ois.readBoolean();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t\treturn false;\n");
        //str.append("\treturn rs.last();\n");
        str.append("\t}\n\n");

        return str.toString();
    }

    @Override
    public String getMethodsExecute() {
        StringBuilder str = new StringBuilder();
        ArrayList<Method> al2 = BE.GetParamIExecute();
        if (al2.size() > 0) {
            for (Method method : al2) {
                Class[] tmp = method.getParameterTypes();
                str.append("\t@Override\n");
                str.append("\tpublic void execute(");
                for (int i = 0; i < tmp.length; i++) {
                    if (i < tmp.length - 1) {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i).append(", ");
                    } else {
                        str.append(getType(tmp[i].getSimpleName())).append(" args").append(i);
                    }
                }
                str.append(") throws SQLException {\n");

                str.append(validationSourceCode);

                /*str.append("\tps.setInt(1, this.sessionID);\n");
                 str.append("\tps.setInt(2, this.querySRID);\n\n");
                 */
                if (tmp.length > 0 && tmp[0] != null) {
                    str.append("\tString execParams = this.params;\n");

                    // Regex for matching a single question mark: "\\?"
                    String qmRegex = "\"\\\\?\"";
                    // String escaped double quote escape: "\""
                    String dqstr = "\"\\\"\"";

                    if (tmp[0].equals(Object[].class)) {
                        str.append("\tfor (int i = 0; i < args0.length; i++) {\n");
                        
                        str.append(String.format("\t\texecParams = execParams.replaceFirst(%s, %s+args0[i]+%s);\n", qmRegex, dqstr, dqstr)); //if type != string, no '''
                        //str.append("ps.setObject(i+1,args0[i]);\n");
                        str.append("\t}\n");

                    } else {
                        for (int i = 0; i < tmp.length; i++) {
                            if (tmp[i].isInterface()) {
                                //str.append("if(!validateSchema(args").append(i).append(".getClass()))");
                                //str.append("throw new SQLException(\"Invalid Schema!\");");
                                Method mtd = tmp[i].getDeclaredMethods()[0];
                                
                                if(!mtd.getReturnType().getSimpleName().equals("int")&&!mtd.getReturnType().getSimpleName().equals("double"))
                                    str.append(String.format("\t\t\texecParams = execParams.replaceFirst(%s,%s+args%d.%s()+%s);\n", qmRegex, dqstr, i, mtd.getName(), dqstr));
                                else
                                    str.append(String.format("\t\t\texecParams = execParams.replaceFirst(%s,\"\"+args%d.%s());\n", qmRegex, i, mtd.getName()));
                                
                                //str.append("ps.set" + capitaliseFirstLetter(getType(mtd.getReturnType().getSimpleName())) + "(" + (i + 1) + ",args" + i + "." + mtd.getName() + "());\n");
                            } else {
                                System.out.println("yolooooo"+tmp[i].getSimpleName());
                                if(!tmp[i].getSimpleName().equals("int")&&!tmp[i].getSimpleName().equals("double"))
                                    str.append(String.format("\t\t\texecParams = execParams.replaceFirst(%s,%s+args%d+%s);\n", qmRegex, dqstr, i, dqstr));
                                else
                                    str.append(String.format("\t\t\texecParams = execParams.replaceFirst(%s,\"\"+args%d);\n", qmRegex, i));
                                //str.append("ps.set" + capitaliseFirstLetter(getType(tmp[i].getSimpleName())) + "(" + (i + 1) + ",args" + i + ");\n");
                            }
                        }
                    }
                    // str.append("\tps.setString(3, execParams);\n");
                }

                if (tmp.length > 0 && tmp[0] != null) {
                    str.append("\t\ttry{\n\t\t\toos.writeInt(3);\n\t\t\toos.flush();\n\t\t\toos.writeBoolean(").append(!this.BE.IsUID()).append(");\n\t\t\toos.flush();\n");
                } else {
                    str.append("\t\ttry{\n\t\t\toos.writeInt(2);\n\t\t\toos.flush();\n\t\t\toos.writeBoolean(").append(!this.BE.IsUID()).append(");\n\t\t\toos.flush();\n");
                }

                str.append("\t\t\toos.writeObject(this.sessionID);\n\t\t\toos.flush();\n\t\t\toos.writeObject(this.querySRID);\n\t\t\toos.flush();\n");

                if (tmp.length > 0 && tmp[0] != null) {
                    str.append("\t\t\toos.writeObject(execParams);\n\t\t\toos.flush();");
                }
                str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                if (this.BE.IsUID()) {
                    //str.append("\tnrows=ps.executeUpdate();\n");
                    str.append("\t\t\tnrows=ois.readInt();\n");
                } else {
                    //str.append("\trs=ps.executeQuery();\n");
                    str.append("\t\t\tois.readObject();\n");
                }
                str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
                str.append("\t}\n");

            }
        }

        return str.toString();
    }

    @Override
    public String getScrollType() {
        if (this.BE.isScrollable()) {
            if (this.BE.ReadOnly()) {
                return "ResultSet.TYPE_SCROLL_INSENSITIVE";
            }
            return "ResultSet.TYPE_SCROLL_SENSITIVE";
        }
        return "ResultSet.TYPE_FORWARD_ONLY";
        //return "ResultSet.TYPE_SCROLL_INSENSITIVE";
    }

    @Override
    public String getRsConcur() {
        if (this.BE.ReadOnly()) {
            return "ResultSet.CONCUR_READ_ONLY";
        } else {
            return "ResultSet.CONCUR_UPDATABLE";
        }
        //return "ResultSet.CONCUR_UPDATABLE";
    }

    @Override
    public String getClassDecl() {
        StringBuilder str = new StringBuilder();
        str.append("public class ").append(BE.getName().substring(1, BE.getName().length())).append(" implements ").append(this.BE.getName());
        str.append("{\n");

        //to do
        str.append("public static final int NEXT=-1, ABSOLUTE=12, RELATIVE=-13, PREVIOUS=-14, \n"
                + "            BEFORE_FIRST=-15, AFTER_LAST=-16, FIRST=-18, LAST=-19,\n"
                + "            GET=-2, SET=-3, EXEC=-17, DELETE_ROW=-20,\n"
                + "            BEGIN_UPDATE=-4, CANCEL_UPDATE=-5, UPDATE_ROW=-6, UPDATE_VAL=-7, \n"
                + "            BEGIN_INSERT=-8, CANCEL_INSERT=-9, INSERT_ROW=-10, INSERT_VAL=-11;\n\n");

        str.append("public static final int COMMIT=-21, RB=-23, AUTOCOMMIT=-26,\n"
                + "            RELSP=-22, RBSP=-24, SP=-25, SPNAME=-27;");

        if (!BE.IsUID()) {
            str.append("\tpublic final int idle=0;\n");
            str.append("\tpublic final int updating=1;\n");
            str.append("\tpublic final int inserting=2;\n");
            str.append("\tpublic int _state=idle;\n");
        }

        return str.toString();
    }

    @Override
    public String getVariablesDecl() {
        return "";
    }

    @Override
    public String getMethodsUpdate() {
        StringBuilder str = new StringBuilder();
        str.append("@Override\n");
        str.append("public void beginUpdate() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\tif(_state==inserting){\n");
        str.append("\t\tthrow new SQLException(\"Cant Update, While Inserting!\");\n");
        str.append("\t} else {\n");
        str.append("\t\t_state=updating;\n");
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(BEGIN_UPDATE);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        str.append("\t}\n");
        str.append("}\n\n");

        str.append("@Override\n");
        str.append("public void updateRow() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\tif(_state!=updating){\n");
        str.append("\t\tthrow new SQLException(\"Cant Update, While not updating!\");\n");
        str.append("\t}else{\n");
        str.append("\t\t_state=idle;\n");
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(UPDATE_ROW);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\t\trs.updateRow();\n");
        str.append("\t}\n");
        str.append("}\n");

        str.append("@Override\n");
        str.append("public void cancelUpdate() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(CANCEL_UPDATE);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\trs.cancelRowUpdates();\n");
        str.append("\t_state=idle;\n");
        str.append("}\n");

        Method[] mtds = this.BE.getAllUpdateMethods();
        if (mtds.length > 0) {
            for (Method mtd1 : mtds) {
                str.append("@Override");
                str.append("\n").append("public void").append(" ").append(mtd1.getName()).append("(").append(mtd1.getParameterTypes()[0].getSimpleName()).append(" ").append(mtd1.getName().substring(1, mtd1.getName().length())).append(")").append(" throws SQLException{\n");
                str.append(validationSourceCode);
                str.append("\tif(_state!=updating)\n");
                str.append("\t\tthrow new SQLException(\"Cant Update, While Inserting, or not BeginUpdating!\");\n");
                str.append("\telse{\n");
                if (mtd1.getParameterTypes()[0].isInterface()) {
                    Method mtd = mtd1.getParameterTypes()[0].getDeclaredMethods()[0];
                    str.append("\t\ttry{\n");
                    str.append("\t\t\toos.writeInt(UPDATE_VAL);\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeUTF(\"").append(mtd.getName()).append("\");\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeObject(").append(mtd.getName()).append(".").append(mtd.getName()).append("());\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                    str.append("\t\t\tois.readObject();\n");
                    str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n\t}\n");
                    //str.append("\t\trs.update").append(capitaliseFirstLetter(mtd.getReturnType().getSimpleName())).append("(").append("\"").append(mtd.getName()).append("\"").append(",").append(mtd.getName()).append(".").append(mtd.getName()).append("()").append(");}\n");
                } else {
                    str.append("\t\ttry{\n");
                    str.append("\t\t\toos.writeInt(UPDATE_VAL);\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeUTF(\"").append(mtd1.getName().substring(1, mtd1.getName().length())).append("\");\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeObject(").append(mtd1.getName().substring(1, mtd1.getName().length())).append(");\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                    str.append("\t\t\tois.readObject();\n");
                    str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n\t}\n");
                    //str.append("\t\trs.update").append(capitaliseFirstLetter(mtd1.getParameterTypes()[0].getSimpleName())).append("(").append("\"").append(mtd1.getName().substring(1, mtd1.getName().length())).append("\"").append(",").append(mtd1.getName().substring(1, mtd1.getName().length())).append(");}\n");
                }
                str.append("}\n");
            }
        }
        return str.toString();
    }

    @Override
    public String getMethodsInsert() {
        StringBuilder str = new StringBuilder();
        str.append("@Override\n");
        str.append("public void beginInsert() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\tif(_state==updating)\n");
        str.append("\t\tthrow new SQLException(\"Cant Insert, While Updating!\");\n");
        str.append("\telse{\n");
        str.append("\t\t_state=inserting;}\n");
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(BEGIN_INSERT);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\trs.moveToInsertRow();\n");
        str.append("}\n");

        str.append("public void endInsert(boolean moveToPreviousRow) throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\tif(_state!=inserting)\n");
        str.append("\t\tthrow new SQLException(\"Cant Insert, While not inserting!\");\n");
        str.append("\telse{\n");
        str.append("\t\t_state=idle;\n");
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(INSERT_ROW);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeBoolean(moveToPreviousRow);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n\t}");
        //str.append("\t\trs.insertRow();}\n");
        //str.append("\tif(moveToPreviousRow)\n");
        //str.append("\t\trs.moveToCurrentRow();\n");
        str.append("}\n");

        str.append("@Override\n");
        str.append("public void cancelInsert() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(CANCEL_INSERT);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\trs.moveToCurrentRow();\n");
        str.append("\t_state=idle;\n");
        str.append("}\n");

        Method[] mtds = this.BE.getAllInsertMethods();
        if (mtds.length > 0) {
            for (Method mtd1 : mtds) {
                str.append("@Override");
                str.append("\n").append("public void").append(" ").append(mtd1.getName()).append("(").append(mtd1.getParameterTypes()[0].getSimpleName()).append(" ").append(mtd1.getName().substring(1, mtd1.getName().length())).append(")").append(" throws SQLException{\n");
                str.append(validationSourceCode);
                str.append("\tif(_state!=inserting)\n");
                str.append("\t\tthrow new SQLException(\"Cant insert, While updating or not BeginInsert!\");\n");
                str.append("\telse{\n");
                if (mtd1.getParameterTypes()[0].isInterface()) {
                    Method mtd = mtd1.getParameterTypes()[0].getDeclaredMethods()[0];
                    str.append("\t\ttry{\n");
                    str.append("\t\t\toos.writeInt(INSERT_VAL);\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeUTF(\"").append(mtd.getName()).append("\");\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeObject(").append(mtd.getName()).append(".").append(mtd.getName()).append("());\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                    str.append("\t\t\tois.readObject();\n");
                    str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n\t}\n");
                    //str.append("\t\trs.update").append(capitaliseFirstLetter(mtd.getReturnType().getSimpleName())).append("(").append("\"").append(mtd.getName()).append("\"").append(",").append(mtd.getName()).append(".").append(mtd.getName()).append("()").append(");}\n");
                } else {
                    str.append("\t\ttry{\n");
                    str.append("\t\t\toos.writeInt(INSERT_VAL);\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeUTF(\"").append(mtd1.getName().substring(1, mtd1.getName().length())).append("\");\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeObject(").append(mtd1.getName().substring(1, mtd1.getName().length())).append(");\n\t\t\toos.flush();\n");
                    str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
                    str.append("\t\t\tois.readObject();\n");
                    str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n\t}\n");
                    //str.append("\t\trs.update").append(capitaliseFirstLetter(mtd1.getParameterTypes()[0].getSimpleName())).append("(").append("\"").append(mtd1.getName().substring(1, mtd1.getName().length())).append("\"").append(",").append(mtd1.getName().substring(1, mtd1.getName().length())).append(");}\n");
                }
                str.append("}\n");
            }
        }
        return str.toString();
    }

    @Override
    public String getMethodsDelete() {
        StringBuilder str = new StringBuilder();
        str.append("@Override\n");
        str.append("public void deleteRow() throws SQLException {\n");
        str.append(validationSourceCode);
        str.append("\t\ttry{\n");
        str.append("\t\t\toos.writeInt(DELETE_ROW);\n\t\t\toos.flush();\n");
        str.append("\t\t\toos.writeInt(queryIdentifier);oos.flush();\n");
        str.append("\t\t\tois.readObject();\n");
        str.append("\t\t}catch(Exception ex){ ex.printStackTrace(); }\n");
        //str.append("\trs.deleteRow();\n");
        str.append("}\n");
        return str.toString();
    }

    @Override
    protected String getClasspath() {
        return "";
    }
}
