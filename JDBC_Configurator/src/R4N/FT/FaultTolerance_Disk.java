/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author bluemoon
 */
public class FaultTolerance_Disk implements FaultTolerance {

    public FaultTolerance_Disk(String f){
        folder = f;
    }
    
    public FaultTolerance_Disk(){
        folder = "Failures/";
    }
    
    public static String folder;

    @Override
    public void createFile(String id) throws FileNotFoundException {
        File f = new File(folder + id);
        PrintWriter out = new PrintWriter(f);
        out.close();
        if (!id.contains(".temp")) {
            printFile(id);
        }
    }

    @Override
    public void deleteFile(String id) throws IOException {
        Files.delete(new File(folder + id).toPath());
    }

    @Override
    public void clearStates(String id) throws IOException {
        File f = new File(folder + id);
        File fOld = new File(folder + id + ".old");
        File fTemp = new File(folder + id + ".temp");

        // we have new
        createFile(id + ".temp");                                   // we have new and (temp)
        Files.copy(f.toPath(), fOld.toPath(), REPLACE_EXISTING);    // we have new, (old) and temp
        Files.delete(fTemp.toPath());                               // we have new, old and (temp)
        // we have new and old
        PrintWriter out = new PrintWriter(f);
        out.close();

        Files.delete(fOld.toPath());
        printFile(id);
    }

    private void printFile(String id) throws FileNotFoundException {
        /*System.out.println("\tFile " + id);
        try (Scanner in = new Scanner(new File(folder + id))) {
            while (in.hasNextLine()) {
                System.out.println("\t" + in.nextLine());
            }
        }
        System.out.println("\tEOF --\n");*/
    }

    @Override
    public void setNewState(String id, String state) throws IOException {
        File f = new File(folder + id);
        File fOld = new File(folder + id + ".old");
        File fTemp = new File(folder + id + ".temp");

        // we have new
        createFile(id + ".temp");                                   // we have new and (temp)
        Files.copy(f.toPath(), fOld.toPath(), REPLACE_EXISTING);    // we have new, (old) and temp
        Files.delete(fTemp.toPath());                               // we have new, old and (temp)
        // we have new and old
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(folder + id, true)))) {
            out.println(state);
        }

        Files.delete(fOld.toPath());
        printFile(id);
    }

    @Override
    public void removeLastStateReverser(String id) throws FileNotFoundException, IOException {
        File f = new File(folder + id);
        File fOld = new File(folder + id + ".old");
        File fTemp = new File(folder + id + ".temp");

        // we have new
        createFile(id + ".temp");                                   // we have new and (temp)
        Files.copy(f.toPath(), fOld.toPath(), REPLACE_EXISTING);    // we have new, (old) and temp
        Files.delete(fTemp.toPath());                               // we have new, old and (temp)
        // we have new and old
        try (PrintWriter out = new PrintWriter(f)) {
            try (Scanner in = new Scanner(fOld)) {
                String previousLine = "";
                if (in.hasNextLine()) {
                    previousLine = in.nextLine();
                }

                while (in.hasNextLine()) {
                    out.println(previousLine);
                    previousLine = in.nextLine();
                }

                String[] fields = previousLine.split(";;");
                for (int i = 0; i < fields.length - 1; i++) {
                    out.print(fields[i] + ";;");
                }
                out.println();
            }

        }

        Files.delete(fOld.toPath());
        printFile(id);
    }

    @Override
    public void removeLastState(String id) throws FileNotFoundException, IOException {
        File f = new File(folder + id);
        File fOld = new File(folder + id + ".old");
        File fTemp = new File(folder + id + ".temp");

        // we have new
        createFile(id + ".temp");                                   // we have new and (temp)
        Files.copy(f.toPath(), fOld.toPath(), REPLACE_EXISTING);    // we have new, (old) and temp
        Files.delete(fTemp.toPath());                               // we have new, old and (temp)
        // we have new and old
        try (PrintWriter out = new PrintWriter(f)) {
            try (Scanner in = new Scanner(fOld)) {
                String previousLine = "";
                if (in.hasNextLine()) {
                    previousLine = in.nextLine();
                }

                while (in.hasNextLine()) {
                    out.println(previousLine);
                    previousLine = in.nextLine();
                }
            }

        }

        Files.delete(fOld.toPath());
        printFile(id);
    }

    @Override
    public boolean checkIfRecoveryIsNecessary() {
        File folder2 = new File(folder);
        File[] listOfFiles = folder2.listFiles();

        return listOfFiles.length != 0;
    }

    @Override
    public ArrayList<FailureRecovery.DBStatement> getStatements() {
        File folder2 = new File(folder);
        File[] listOfFiles = folder2.listFiles();
        ArrayList<FailureRecovery.DBStatement> list = new ArrayList();
        
        for (File fi : listOfFiles) {
            try {
                Scanner fin = new Scanner(fi);
                while (fin.hasNextLine()) {
                    String line = fin.nextLine();
                    if (line.equals("")) {
                        break;
                    }
                    String a = line.substring(5).split(";;")[0];
                    FailureRecovery.DBStatement temp = new FailureRecovery.DBStatement(a, fi.getName(), line.substring(7 + a.length()).split(";;"));
                    if (fin.hasNextLine()) {
                        if (fin.nextLine().equals("Done;")) {
                            temp.done = true;
                        }
                    }
                    list.add(temp);
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

        }
        
        return list;
    }

    @Override
    public void reconnect() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
