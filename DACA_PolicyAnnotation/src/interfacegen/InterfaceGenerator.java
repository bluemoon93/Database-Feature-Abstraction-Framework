package interfacegen;

import util.MyBoolean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Gera o código fonte de uma interface dado o seu objeto Class.
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class InterfaceGenerator {

    /**
     * Gera o código fonte de uma interface dado o seu objeto Class.
     *
     * @param c            O objeto class da classe a ser gerada.
     * @param otherClasses A lista de outras classes associadas (para facilitar os imports).
     * @param seqInfo      Informação sobre as sequencias configuradas.
     * @param seqActive    Se a orquestração está activa ou não.
     * @return Uma string contendo o código fonte da classe.
     */
    public static String genInterfaceSourceCode(Class c, List<Class> otherClasses, 
            Map<Integer, Map<Integer, String>> seqInfo, boolean seqActive) {
        StringBuilder isc = new StringBuilder();
        getPackage(isc, c);
        getImportsForChoreography(isc, c, seqInfo);
        getImportsForClass(isc, c, otherClasses);
        getInterfaceDeclaration(isc, c, otherClasses);
        getInterfaceFields(isc, c, otherClasses, seqInfo, seqActive);
        getMethods(isc, c, otherClasses);
        generateNextMethods(isc, c, seqInfo);
        isc.append("}");
        return isc.toString();
    }

    /**
     * Gera a declaração do pacote.
     *
     * @param c O objeto class da classe a ser gerada.
     * @return A declaração do pacote.
     */
    private static void getPackage(StringBuilder strb, Class c) {
        strb.append("package ");

        // Adicionar o parentPackage, se existir, no inicio.
        strb.append(c.getPackage().getName()).append(";\n\n");
    }

    /**
     * Determina os import necessários a efectuar.
     *
     * @param c            O objeto class da classe a ser gerada.
     * @param otherClasses A lista de outras classes associadas (para facilitar os imports).
     * @return Uma string com os imports necessários.
     */
    private static void getImportsForClass(StringBuilder strb, Class c, List<Class> otherClasses) {
        Set<String> imports = new HashSet<>();

        // Processa as interfaces da classe
        Class[] interfaces = c.getInterfaces();
        for (Class i : interfaces) {
            // Se a classe e a interface não pertencem ao mesmo pacote
            if (!c.getPackage().getName().equalsIgnoreCase(i.getPackage().getName())) {
                for (Class other : otherClasses) {
                    // E a lista de classes associadas contém essa interface
                    if (other.getSimpleName().equalsIgnoreCase(i.getSimpleName())) {
                        // Obter o pacote da interface e efectuar o import
                        imports.add("import " + other.getPackage().getName() + ".*;\n");
                        break;
                    }
                }
            }
        }

        // Processa as variáveis da interface
        for (Field f : c.getDeclaredFields()) {
            // Obtém a classe usada em tipos genéricos, se existir
            String gentype = f.getGenericType().toString();
            if (gentype.contains("<")) {
                gentype = gentype.substring(gentype.indexOf("<") + 1, gentype.indexOf(">"));
                String[] gtypes = gentype.split("[, ]");
                for (String gtype : gtypes) {
                    for (Class other : otherClasses) {
                        if (other.getSimpleName().equalsIgnoreCase(gtype.substring(gtype.lastIndexOf(".") + 1))) {
                            if (!other.getPackage().getName().equalsIgnoreCase(c.getPackage().getName())) {
                                // Obter o pacote da classe e efectuar o import
                                imports.add("import " + other.getPackage().getName() + ".*;\n");
                            }
                            break;
                        }
                    }
                }
            }

            // Procura na lista de classes associadas se a classe do tipo ou a classe usada em genéricos existe
            for (Class other : otherClasses) {
                if (other.getSimpleName().equalsIgnoreCase(f.getType().getSimpleName()) || other.getSimpleName().equalsIgnoreCase(gentype)) {
                    // Se a classe e a interface não pertencem ao mesmo pacote
                    if (!other.getPackage().getName().equalsIgnoreCase(c.getPackage().getName())) {
                        // Obter o pacote da classe e efectuar o import
                        imports.add("import " + other.getPackage().getName() + ".*;\n");
                    }
                    break;
                }
            }
        }

        // Processa os métodos da interface
        for (Method m : c.getDeclaredMethods()) {
            // Procura a classe do tipo de retorno na lista de classes associadas
            Class ret = m.getReturnType();
            for (Class other : otherClasses) {
                if (other.getSimpleName().equalsIgnoreCase(ret.getSimpleName())) {
                    // Se a classe e a interface não pertencem ao mesmo pacote
                    if (!other.getPackage().getName().equalsIgnoreCase(c.getPackage().getName())) {
                        // Obter o pacote da classe e efectuar o import
                        imports.add("import " + other.getPackage().getName() + ".*;\n");
                    }
                    break;
                }
            }

            // Procura a classe dos tipos dos parametros na lista de classes associadas
            for (Class param : m.getParameterTypes()) {
                for (Class other : otherClasses) {
                    if (other.getSimpleName().equalsIgnoreCase(param.getSimpleName())) {
                        // Se a classe e a interface não pertencem ao mesmo pacote
                        if (!other.getPackage().getName().equalsIgnoreCase(c.getPackage().getName())) {
                            // Obter o pacote da classe e efectuar o import
                            imports.add("import " + other.getPackage().getName() + ".*;\n");
                        }
                        break;
                    }
                }
            }
        }

        // Passar a lista de imports para string
        for (String s : imports) {
            strb.append(s);
        }
        strb.append("\n");
    }

    /**
     * Gera a declaração da interface.
     *
     * @param c            O objeto class da classe a ser gerada.
     * @param otherClasses A lista de outras classes associadas (para facilitar os imports).
     * @return Uma string com a declaração da interface.
     */
    private static void getInterfaceDeclaration(StringBuilder strb, Class c, List<Class> otherClasses) {
        // Obter os modificadores da interface (public / interface / etc)
        strb.append(Modifier.toString(c.getModifiers())).append(" ");

        // Obter o nome da interface
        strb.append(c.getSimpleName()).append(" ");

        // Obter a lista de extenções de outras interfaces
        Class[] interfaces = c.getInterfaces();
        if (interfaces.length != 0) {
            strb.append("extends ");
            for (int i = 0; i < interfaces.length; i++) {
                boolean added = false;
                for (Class other : otherClasses) {
                    if (other.getSimpleName().equalsIgnoreCase(interfaces[i].getSimpleName())) {
                        strb.append(interfaces[i].getSimpleName());
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    strb.append(interfaces[i].getCanonicalName());
                }
                strb.append((i == (interfaces.length - 1) ? " " : ", "));
            }
        }
        strb.append("{\n");
    }

    /**
     * Gera as variáveis da interface.
     *
     * @param c            O objeto class da classe a ser gerada.
     * @param otherClasses A lista de outras classes associadas (para facilitar os imports).
     * @return Uma string com as variáveis da interface.
     */
    private static void getInterfaceFields(StringBuilder strbf, Class c, List<Class> otherClasses, Map<Integer, Map<Integer, String>> orqinfo, boolean orqActive) {
        List<String> toExport = new ArrayList<>();
        for (Integer seq : orqinfo.keySet()) {
            toExport.add(orqinfo.get(seq).get(1));
        }

        // Obtém as variáveis da interface
        Field[] fs = c.getDeclaredFields();
        for (Field f : fs) {
            StringBuilder strbt = new StringBuilder();
            f.setAccessible(true);

            // Obtém os monificadores da variável (public / static / final / etc)
            strbt.append("    ").append(Modifier.toString(f.getModifiers())).append(" ");

            // Obtém o tipo da variável
            boolean added = false;
            for (Class other : otherClasses) {
                if (other.getSimpleName().equalsIgnoreCase(f.getType().getSimpleName())) {
                    strbt.append(f.getType().getSimpleName());
                    added = true;
                    break;
                }
            }
            if (!added) {
                strbt.append(f.getType().getCanonicalName());
            }

            // Determina se o tipo usa genéricos e quais são
            String gentype = f.getGenericType().toString();
            if (gentype.contains("<")) {
                strbt.append("<");
                gentype = gentype.substring(gentype.indexOf("<") + 1, gentype.indexOf(">"));
                String[] gtypes = gentype.split("[, ]");
                for (int i = 0; i < gtypes.length; i++) {
                    for (Class other : otherClasses) {
                        if (other.getSimpleName().equalsIgnoreCase(gtypes[i].substring(gtypes[i].lastIndexOf(".") + 1))) {

                            /*// Não exportar BEs que só sejam possiveis obter por sequencia.
                            if (orqActive.booleanValue() && c.getPackage().getName().equalsIgnoreCase("Classes")) {
                                stop = !toExport.contains(gtypes[i]);
                            }*/

                            gtypes[i] = gtypes[i].substring(gtypes[i].lastIndexOf(".") + 1);
                            strbt.append(gtypes[i]);
                            strbt.append((i == (gtypes.length - 1) ? ">" : ", "));
                            break;
                        }
                    }
                }
            }
            strbt.append(" ");

            // Obtém o nome da variável
            strbt.append(f.getName()).append(" = ");

            try {
                // Obtém o valor da variável.
                Object value = f.get(null);

                // Se o valor for do tipo Class
                if (value instanceof Class) {
                    Class c2 = (Class) value;
                    // Obtém-se o tipo da classe e acrescenta-se ".class"
                    strbt.append(c2.getSimpleName()).append(".class;\n");
                } else {
                    // Se for de outro tipo qualquer adiciona-se o valor. (Não processa "new"s)
                    strbt.append(value).append(";\n");
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                System.out.println(ex);
            }
            strbf.append(strbt);
        }
        strbf.append("\n");
    }

    /**
     * Gera os métodos da interface.
     *
     * @param c            O objeto class da classe a ser gerada.
     * @param otherClasses A lista de outras classes associadas (para facilitar os imports).
     * @return Uma string com os métodos da interface.
     */
    private static void getMethods(StringBuilder strb, Class c, List<Class> otherClasses) {
        boolean added;

        // Obtém os métodos da interface
        Method[] ms = c.getDeclaredMethods();
        for (Method m : ms) {
            m.setAccessible(true);

            // Obtém os modificadores do método (public / static / etc)
            strb.append("    ").append(Modifier.toString(m.getModifiers())).append(" ");

            // Obtém o tipo do valor de retorno
            added = false;
            for (Class other : otherClasses) {
                if (other.getSimpleName().equalsIgnoreCase(m.getReturnType().getSimpleName())) {
                    strb.append(m.getReturnType().getSimpleName()).append(" ");
                    added = true;
                    break;
                }
            }
            if (!added) {
                strb.append(m.getReturnType().getCanonicalName()).append(" ");
            }

            // Obtém o nome do método
            strb.append(m.getName()).append("(");

            // Obtém os parametros do método
            Class[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                added = false;
                for (Class other : otherClasses) {
                    if (other.getSimpleName().equalsIgnoreCase(params[i].getSimpleName())) {
                        strb.append(params[i].getSimpleName()).append(" arg").append(i);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    strb.append(params[i].getCanonicalName()).append(" arg").append(i);
                }
                strb.append((i == (params.length - 1) ? "" : ", "));
            }
            strb.append(")");

            // Obtém as excepções lançadas pelo método
            Class[] exs = m.getExceptionTypes();
            if (exs.length > 0) {
                strb.append(" throws ");
            }
            for (int i = 0; i < exs.length; i++) {
                strb.append(exs[i].getCanonicalName());
                strb.append((i == (exs.length - 1) ? "" : ", "));
            }
            strb.append(";\n");
        }
    }

    private static void generateNextMethods(StringBuilder strb, Class c, Map<Integer, Map<Integer, String>> orqinfo) {
        String beurl = c.getPackage().getName() + "." + c.getSimpleName();
        for (Integer seq : orqinfo.keySet()) {
            for (Integer pos : orqinfo.get(seq).keySet()) {
                if (orqinfo.get(seq).get(pos).equalsIgnoreCase(beurl)) {
                    if (orqinfo.get(seq).containsKey(pos + 1)) {
                        strb.append("    public ");
                        strb.append(orqinfo.get(seq).get(pos + 1)).append(" ");
                        strb.append("nextBE_S").append(seq);
                        strb.append("(int crud, ISession session) throws LocalTools.BTC_Exception;\n");
                    }
                }
            }
        }
    }

    private static void getImportsForChoreography(StringBuilder strb, Class c, Map<Integer, Map<Integer, String>> orqinfo) {
        String beurl = c.getPackage().getName() + "." + c.getSimpleName();
        for (Integer seq : orqinfo.keySet()) {
            for (Integer pos : orqinfo.get(seq).keySet()) {
                if (orqinfo.get(seq).get(pos).equalsIgnoreCase(beurl)) {
                    if (orqinfo.get(seq).containsKey(pos + 1)) {
                        strb.append("import BusinessManager.ISession;\n");
                        return;
                    }
                }
            }
        }
    }
}
