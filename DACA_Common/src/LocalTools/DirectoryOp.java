package LocalTools;

import java.io.*;


public class DirectoryOp {

    public static boolean removeDirectory(File directory) {

        if (directory == null) {
            return false;
        }
        if (!directory.exists()) {
            return true;
        }
        if (!directory.isDirectory()) {
            return false;
        }

        String[] list = directory.list();


        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(directory, list[i]);


                if (entry.isDirectory()) {
                    if (!removeDirectory(entry)) {
                        return false;
                    }
                } else {
                    if (!entry.delete()) {
                        return false;
                    }
                }
            }
        }

        return directory.delete();
    }


    public static void CreateDirectory(File tmpdir) {

        if (tmpdir.exists()) {
            if (!DirectoryOp.removeDirectory(tmpdir)) {
                System.err.println("ERROR COULD NOT CREATE DIRECTORY");
                System.exit(0);
            }
            tmpdir.mkdirs();
        } else
            tmpdir.mkdirs();
    }


    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void copyListFiles(File[] files, File dstdir) throws IOException {

        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i].getAbsolutePath());
            OutputStream out = new FileOutputStream(dstdir.getCanonicalPath() + "\\" + files[i].getName());

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }


}
