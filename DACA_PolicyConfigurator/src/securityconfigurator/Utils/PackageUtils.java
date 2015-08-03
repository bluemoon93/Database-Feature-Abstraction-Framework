package securityconfigurator.Utils;

import java.util.jar.*;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PackageUtils {

    private static boolean debug = true;

    public static List getClasseNamesInPackage(String jarName, String packageName) {
        ArrayList<String> classes = new ArrayList<>();

        packageName = packageName.replaceAll("\\.", "/");
        if (debug) System.out.println
                ("Jar " + jarName + " looking for " + packageName);
        try {
            JarInputStream jarFile = new JarInputStream
                    (new FileInputStream(jarName));
            JarEntry jarEntry;

            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if ((jarEntry.getName().startsWith(packageName)) &&
                        (jarEntry.getName().endsWith(".class"))) {
                    if (debug) System.out.println
                            ("Found " + jarEntry.getName().replaceAll("/", "\\."));
                    classes.add(jarEntry.getName().replaceAll("/", "\\."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }


    public static boolean CheckBusinessExists(String jarName, String Dir) {
        List list = new ArrayList();
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                return false;
            }

            while (jarEntry != null) {
                if (jarEntry.isDirectory())
                    if (jarEntry.getName().compareTo(Dir) == 0)
                        return true;
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


    public static boolean CheckOtherExists(String jarName) {
        List list = new ArrayList();
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                return false;
            }
            String splitstr[] = jarEntry.getName().split("/");

            while (jarEntry != null) {
                if (jarEntry.getName().contains("_RsOther") == true)
                    return true;
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


    public static List getServices(String jarName) {
        List<String> list = new ArrayList<>();
        JarInputStream jarFile = null;
        File JarFiletst = new File(jarName);
        if (!JarFiletst.exists())
            return list;
        try {
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                return list;
            }
            String splitstr[] = jarEntry.getName().split("/");

            while (jarEntry != null) {
                if (jarEntry.getName().contains("/") == true) {
                    splitstr = jarEntry.getName().split("/");
                    if (splitstr.length == 2 && splitstr[0].compareTo("BusinessInterface") == 0)
                        list.add(splitstr[1]);
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
                if (jarEntry.getName().contains("/") == true) {
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

    public static String getCorrectClassloadPath(String path) {
        String tmpstr = path;
        String tmp[] = tmpstr.split("/");
        tmp[tmp.length - 1] = tmp[tmp.length - 1].substring(0, tmp[tmp.length - 1].indexOf('.'));

        String tmpstr2 = "";
        for (int i = 0; i < tmp.length; i++) {
            if (i == 0)
                tmpstr2 += tmp[i];
            else
                tmpstr2 += "." + tmp[i];

        }
        return tmpstr2;
    }

    public static List getSubInterfaces(String packageName, String jarName) {
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
                if (jarEntry.getName().contains("/") == true) {
                    splitstr = jarEntry.getName().split("/");
                    if (splitstr.length == 4 && splitstr[0].compareTo("BusinessInterface") == 0
                            && splitstr[1].compareTo(packageName) == 0
                            && splitstr[2].compareTo("I" + packageName) == 0
                            && (jarEntry.getName().endsWith(".class")))
                        list.add(jarEntry.getName());
                }
                jarEntry = jarFile.getNextJarEntry();
            }

            if (list.size() == 0) {
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




