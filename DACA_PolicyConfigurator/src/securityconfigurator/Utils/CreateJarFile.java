package securityconfigurator.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

import securityconfigurator.SCException;

public class CreateJarFile {

    public static int BUFFER_SIZE = 10240;

    public static void addtoJar(File TmpDir, String path, String jarName, String Rnd, String Filename, BusinessEntity_Context BE) {
        File finaljar = new File(path + jarName);
        File tempjar = new File(path + Rnd + ".jar");
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(tempjar);
            JarOutputStream outjar = new JarOutputStream(stream, manifest);

            if (finaljar.exists()) {
                FileInputStream streamin2 = new FileInputStream(finaljar);
                JarInputStream injar2 = new JarInputStream(streamin2);
                JarEntry jaren = (JarEntry) injar2.getNextEntry();

                while (jaren != null) {//add business services to new temporary file

                    outjar.putNextEntry(jaren);

                    while (true) {
                        int nRead = injar2.read(buffer, 0, buffer.length);
                        if (nRead <= 0) {
                            break;
                        }
                        outjar.write(buffer, 0, nRead);


                    }


                    jaren = (JarEntry) injar2.getNextEntry();
                }


                injar2.close();
            } else {
                JarEntry newentry;
                //add business entity main interface file
                newentry = new JarEntry("BusinessInterfaces/");
                outjar.putNextEntry(newentry);
                FileInputStream rfile = null;
                File[] mainfiles = new File(TmpDir + "/BusinessInterfaces/").listFiles();
                for (int i = 0; i < mainfiles.length; i++) {
                    if (mainfiles[i].isFile() && mainfiles[i].getName().contains(".class")) {
                        newentry = new JarEntry("BusinessInterfaces/" + mainfiles[i].getName());
                        outjar.putNextEntry(newentry);
                        rfile = new FileInputStream(mainfiles[i]);
                        while (true) {
                            int nRead = rfile.read(buffer, 0, buffer.length);
                            if (nRead <= 0) {
                                break;
                            }
                            outjar.write(buffer, 0, nRead);

                        }
                        rfile.close();
                    }
                }
            }


            JarEntry newentry;
            File InterfacePackageteste = new File(TmpDir.getAbsolutePath());

            ArrayList<File> files = new ArrayList<File>();
            getAllClassFilesInDirectory(files, InterfacePackageteste);

            File InterfacePackage = new File(TmpDir.getAbsolutePath() + "/" + PackageNameUtils.getPackageNameDir(BE.getPackage()));


            if (!CreateInterfaceDirPackage(finaljar, outjar, BE.getPackage())) {
                DirectoryOp.removeDirectory(new File(path + Rnd + "/"));
                outjar.close();
                tempjar.delete();
                throw new BTC_Exception("This Interface Package Already Exists");
            }


            String teststr = PackageNameUtils.getPackageNameDir(BE.getPackage());
            FileInputStream rfile = null;
            for (int i = 0; i < files.size(); i++) {
                String file = getCorrectDirectory(files.get(i), TmpDir);

                try {
                    newentry = new JarEntry(file);
                    outjar.putNextEntry(newentry);
                    rfile = new FileInputStream(files.get(i));
                    while (true) {
                        int nRead = rfile.read(buffer, 0, buffer.length);
                        if (nRead <= 0) {
                            break;
                        }
                        outjar.write(buffer, 0, nRead);

                    }
                    rfile.close();

                } catch (ZipException ze) {

                }


            }


            outjar.close();
            String pathnew = finaljar.getAbsolutePath();

            boolean deleted = false;
            while (!deleted && finaljar.exists()) {
                deleted = finaljar.delete();
            }

            tempjar.renameTo(new File(pathnew));

            DirectoryOp.removeDirectory(new File(path + Rnd + "/"));

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error: " + ex.getMessage());
        }

    }

    public static void getAllClassFilesInDirectory(ArrayList<File> files, File Directory) {

        File[] tmp = Directory.listFiles();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].isDirectory()) {
                getAllClassFilesInDirectory(files, tmp[i]);
            } else {
                if (tmp[i].isFile() && tmp[i].getName().contains(".class"))
                    files.add(tmp[i]);
            }
        }

    }

    public static String getCorrectDirectory(File file, File tmpdir) throws IOException {
        String tmpstr = file.getCanonicalPath().substring(file.getCanonicalPath().lastIndexOf(tmpdir.getName()) + tmpdir.getName().length() + 1);
        tmpstr = tmpstr.replace('\\', '/');
        return tmpstr;
    }


    public static boolean CreateInterfaceDirPackage(File finaljar, JarOutputStream outjar, String Package) throws BTC_Exception, IOException, SCException {
        if (finaljar.exists()) {
            if (PackageUtils.CheckBusinessExists(finaljar.getAbsolutePath(), PackageNameUtils.getPackageNameDir(Package))) {
                return false;
            }
            String dirtmp = "";
            String[] dirs = Package.split("\\.");
            for (int i = 0; i < dirs.length; i++) {
                dirtmp += dirs[i] + "/";
                JarEntry newentry;
                newentry = new JarEntry(dirtmp);
                if (!PackageUtils.CheckBusinessExists(finaljar.getAbsolutePath(), dirtmp)) {
                    outjar.putNextEntry(newentry);
                }
            }
        } else {
            String dirtmp = "";
            String[] dirs = Package.split("\\.");
            for (int i = 0; i < dirs.length; i++) {
                dirtmp += dirs[i] + "/";
                JarEntry newentry;
                newentry = new JarEntry(dirtmp);
                if (dirtmp.compareTo("BusinessInterfaces/") != 0) {
                    outjar.putNextEntry(newentry);
                }
            }
        }
        return true;
    }

    public static void DeleteBusinessEntity(String jarPath, String jarFile, Class bs) throws BTC_Exception, FileNotFoundException, IOException, SCException {
        File finaljar = new File(jarPath + jarFile);
        if (PackageUtils.CheckBusinessExists(finaljar.getAbsolutePath(), PackageNameUtils.getPackageNameDir(bs.getPackage().getName()))) {
            SessionIdentifierGenerator SIG = new SessionIdentifierGenerator();
            String Rnd = SIG.nextSessionId();
            File tempjar = new File(jarPath + Rnd + ".jar");
            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(tempjar);
            JarOutputStream outjar = new JarOutputStream(stream, manifest);

            if (finaljar.exists()) {
                FileInputStream streamin2 = new FileInputStream(finaljar);
                JarInputStream injar2 = new JarInputStream(streamin2);
                JarEntry jaren = (JarEntry) injar2.getNextEntry();

                while (jaren != null) {//add business services to new temporary file
                    if (!jaren.getName().contains(PackageNameUtils.getPackageNameDir(bs.getPackage().getName()))) {
                        outjar.putNextEntry(jaren);

                        while (true) {
                            int nRead = injar2.read(buffer, 0, buffer.length);
                            if (nRead <= 0) {
                                break;
                            }
                            outjar.write(buffer, 0, nRead);


                        }


                    }

                    jaren = (JarEntry) injar2.getNextEntry();

                }

                injar2.close();

                outjar.close();
                String pathnew = finaljar.getAbsolutePath();

                finaljar.delete();
                tempjar.renameTo(new File(pathnew));
            }

        } else {
            throw new BTC_Exception("This Interface Package Does Not Exist");
        }
    }

    public static void addCrudToJar(File jarfile, File tmpfile) {
        try {
            String temppath = tmpfile.getAbsolutePath().substring(0, tmpfile.getAbsolutePath().lastIndexOf("\\"));
            File tempjar = new File(temppath + "/" + "temp.jar");
            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(tempjar);
            JarOutputStream outjar = new JarOutputStream(stream, manifest);

            if (jarfile.exists()) {
                FileInputStream streamin2 = new FileInputStream(jarfile);
                JarInputStream injar2 = new JarInputStream(streamin2);
                JarEntry jaren = (JarEntry) injar2.getNextEntry();

                while (jaren != null && jaren.getName().compareTo("crud/") != 0 && jaren.getName().compareTo("crud/Cruds.class") != 0) {//add business services to new temporary file

                    outjar.putNextEntry(jaren);

                    while (true) {
                        int nRead = injar2.read(buffer, 0, buffer.length);
                        if (nRead <= 0) {
                            break;
                        }
                        outjar.write(buffer, 0, nRead);


                    }
                    jaren = (JarEntry) injar2.getNextEntry();
                }


                injar2.close();
            }
            outjar.putNextEntry(new JarEntry("crud/"));
            outjar.putNextEntry(new JarEntry("crud/" + tmpfile.getName()));
            FileInputStream rfile = new FileInputStream(tmpfile);
            while (true) {
                int nRead = rfile.read(buffer, 0, buffer.length);
                if (nRead <= 0) {
                    break;
                }
                outjar.write(buffer, 0, nRead);

            }
            rfile.close();

            outjar.close();
            String pathnew = jarfile.getAbsolutePath();

            boolean deleted = false;
            while (!deleted && jarfile.exists()) {
                deleted = jarfile.delete();
            }


            tempjar.renameTo(new File(pathnew));

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error: " + ex.getMessage());
        }
    }
}

