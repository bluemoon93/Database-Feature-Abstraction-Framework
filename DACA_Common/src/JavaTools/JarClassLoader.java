/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaTools;

import LocalTools.PackageNameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author DIOGO
 */
public class JarClassLoader extends ClassLoader {

    private final File jarFile;
    private final HashMap<String, Class> classes = new HashMap<>(); //used to cache already defined classes  

    public JarClassLoader(File jar) {
        super(JarClassLoader.class.getClassLoader()); //calls the parent class loader's constructor
        this.jarFile = jar;
    }

    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        return findClass(className);
    }

    @Override
    public URL findResource(String name) {
        try {
            String url = jarFile.toURI().toURL().toString();
            return new URL("jar:" + url + "!/" + name);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Class findClass(String className) {
        byte classByte[];
        Class result;

        result = classes.get(className); //checks in cached classes  
        if (result != null) {
            return result;
        }

        if (className.startsWith("java") || className.compareTo("BusinessInterfaces.IScrollable") == 0 || className.compareTo("BusinessInterfaces.IDelete") == 0 || className.compareTo("BusinessInterfaces.IForwardOnly") == 0
                || className.compareTo("BusinessInterfaces.IResult") == 0 || className.compareTo("BusinessInterfaces.Common") == 0) {
            try {
                return findSystemClass(className);
            } catch (Exception ignored) {
            }
        }

        try {
            JarFile jar = new JarFile(jarFile);
            JarEntry entry = jar.getJarEntry(PackageNameUtils.getPackageNameDir(className).substring(0, PackageNameUtils.getPackageNameDir(className).length() - 1) + ".class");
            InputStream is = jar.getInputStream(entry);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            int nextValue = is.read();
            while (-1 != nextValue) {
                byteStream.write(nextValue);
                nextValue = is.read();
            }
            classByte = byteStream.toByteArray();
            result = defineClass(className, classByte, 0, classByte.length);
            classes.put(className, result);
            return result;
        } catch (Exception e) {
            System.err.println("ERRO " + e);
            return result;
        }
    }
}
