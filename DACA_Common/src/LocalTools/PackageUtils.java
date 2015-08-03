package LocalTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that handle package operations
 */
public class PackageUtils {

    /**
     * Method that gets the names of the classes in package
     *
     * @param jarName     the path to the jar
     * @param packageName the name of the package
     * @return
     */
    public static List<String> getClasseNamesInPackage(String jarName, String packageName) {
        ArrayList<String> classes = new ArrayList<>();

        packageName = packageName.replaceAll("\\.", "/");
        boolean debug = true;
        if (debug) {
            System.out.println("Jar " + jarName + " looking for " + packageName);
        }
        try {
            JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;

            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if ((jarEntry.getName().startsWith(packageName))
                        && (jarEntry.getName().endsWith(".class"))) {
                    if (debug) {
                        System.out.println("Found " + jarEntry.getName().replaceAll("/", "\\."));
                    }
                    classes.add(jarEntry.getName().replaceAll("/", "\\."));
                }
            }
        } catch (Exception ignored) {
        }
        return classes;
    }

    /**
     * Method that checks is business exist
     *
     * @param jarName the path of the jar
     * @param Dir     the business to check if exists
     * @return
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    public static boolean CheckBusinessExists(String jarName, String Dir) {
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                return false;
            }

            while (jarEntry != null) {
                if (jarEntry.isDirectory()) {
                    if (jarEntry.getName().compareTo(Dir) == 0) {
                        return true;
                    }
                }
                jarEntry = jarFile.getNextJarEntry();
            }


        } catch (IOException ex) {
            Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jarFile.close();
            } catch (IOException ex) {
                Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /**
     * Method to check if _RsOther exists
     *
     * @param jarName the path of the jarfile
     * @return true if _RsOther exists
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    public static boolean CheckOtherExists(String jarName) {
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                return false;
            }

            while (jarEntry != null) {
                if (jarEntry.getName().contains("_RsOther")) {
                    return true;
                }
                jarEntry = jarFile.getNextJarEntry();
            }


        } catch (IOException ex) {
            Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jarFile.close();
            } catch (IOException ex) {
                Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /**
     * Method that get the services in a jar file
     *
     * @param jarName the path of the jar
     * @return services in a jar file
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    public static List<String> getServices(String jarName) {
        List<String> list = new ArrayList<>();
        JarInputStream jarFile = null;
        File JarFiletst = new File(jarName);
        if (!JarFiletst.exists()) {
            return list;
        }
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                return list;
            }
            String splitstr[];

            while (jarEntry != null) {
                if (jarEntry.getName().contains("/")) {
                    splitstr = jarEntry.getName().split("/");
                    if (splitstr.length == 2 && splitstr[0].compareTo("BusinessInterface") == 0) {
                        list.add(splitstr[1]);
                    }
                }
                jarEntry = jarFile.getNextJarEntry();
            }


            return list;

        } catch (IOException ex) {
            Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jarFile.close();
            } catch (IOException ex) {
                Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }

    /**
     * Method that get the main interface of a jar file
     *
     * @param PackageName the packagename
     * @param jarName     the path to the jar
     * @return main interface of a jar file
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    public static String getMainInterface(String PackageName, String jarName) {

        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                System.err.println("Erro Jar vazio");
                System.exit(0);
            }
            String splitstr[];

            while (jarEntry != null) {
                if (jarEntry.getName().contains("/")) {
                    splitstr = jarEntry.getName().split("/");
                    if (splitstr.length == 3 && splitstr[0].compareTo("BusinessInterface") == 0 && splitstr[1].compareTo(PackageName) == 0 && (jarEntry.getName().endsWith(".class"))) {
                        jarFile.close();
                        return jarEntry.getName();
                    }
                }
                jarEntry = jarFile.getNextJarEntry();
            }
            System.err.println("Erro Jar Invalido");
            System.exit(0);
            return null;

        } catch (IOException ex) {
            Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jarFile.close();
            } catch (IOException ex) {
                Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * Method that gets the correct classload of a given path
     *
     * @param path the path of the class
     * @return correct classload of a given path
     */
    public static String getCorrectClassloadPath(String path) {
        String tmp[] = path.split("/");
        tmp[tmp.length - 1] = tmp[tmp.length - 1].substring(0, tmp[tmp.length - 1].indexOf('.'));

        String tmpstr2 = "";
        for (int i = 0; i < tmp.length; i++) {
            if (i == 0) {
                tmpstr2 += tmp[i];
            } else {
                tmpstr2 += "." + tmp[i];
            }

        }
        return tmpstr2;
    }

    /**
     * Method that gets the sub interfaces of a jarfile
     *
     * @param packageName the name of the package
     * @param jarName     the path of the jarfile
     * @return sub interfaces of a jarfile
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    public static List<String> getSubInterfaces(String packageName, String jarName) {
        List<String> list = new ArrayList<>();

        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                System.err.println("Erro Jar vazio");
                System.exit(0);
            }
            String splitstr[];

            while (jarEntry != null) {
                if (jarEntry.getName().contains("/")) {
                    splitstr = jarEntry.getName().split("/");
                    if (splitstr.length == 4 && splitstr[0].compareTo("BusinessInterface") == 0
                            && splitstr[1].compareTo(packageName) == 0
                            && splitstr[2].compareTo("I" + packageName) == 0
                            && (jarEntry.getName().endsWith(".class"))) {
                        list.add(jarEntry.getName());
                    }
                }
                jarEntry = jarFile.getNextJarEntry();
            }

            if (list.isEmpty()) {
                System.err.println("Erro Jar Invalido");
                System.exit(0);
            }

            return list;

        } catch (IOException ex) {
            Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jarFile.close();
            } catch (IOException ex) {
                Logger.getLogger(PackageUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return list;
    }
}
