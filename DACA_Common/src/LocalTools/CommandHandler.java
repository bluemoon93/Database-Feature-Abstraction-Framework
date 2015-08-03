/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LocalTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandHandler {

    LocalHandler handler;
    private Map<String, String> chelp = new HashMap<>();
    File configFile;

    public CommandHandler(File configFile) {
        this.configFile = configFile;
        handler = new LocalHandler();
        try {
            handler.ConfigFile(configFile);
        } catch (FileNotFoundException ex) {
            System.err.println("ERROR CONFIG FILE NOT FOUND!");
            System.exit(0);
        } catch (IOException ex) {
            System.err.println("ERROR READING CONFIG FILE!");
            System.exit(0);
        } catch (SQLException ex) {
            System.err.println("ERROR CREATING CONNECTION! Msg:" + ex);
            System.exit(0);
        }
        try {
            if (!handler.TableExist()) {
                System.err.println("ERROR Tables not exist in database!");
                System.exit(0);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CommandHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void CreateHelp() {
        chelp.put("adduser", "adds a new user to database");
    }

    public void ListHelp() {
        System.out.println("Command      |     Description");
        Iterator iterator = chelp.entrySet().iterator();
        while (iterator.hasNext()) {
            String[] strtmp = iterator.next().toString().split("=");
            System.out.println(strtmp[0] + "    " + strtmp[1]);
        }
    }

    public void getCommands() {
        CreateHelp();
        System.out.println("ServerSide Application Running, type help for more help");
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.print(">");
            String opt = sc.nextLine();
            HandleCommand(opt);
        }
    }

    public void listUsers() {
        ArrayList<String> users = handler.getUsers();
        for (int i = 0; i < users.size(); i++) {
            System.out.println(users.get(i));
        }
    }

    public void listInterfaces() {
        ArrayList<String> interfaces = handler.getInterfaces();
        for (int i = 0; i < interfaces.size(); i++) {
            System.out.println(interfaces.get(i));
        }
    }

    public void HandleCommand(String command) {
        boolean commandfound = false;
        if (command.startsWith("adduser")) {
            commandfound = true;
            String[] commandstr = command.split(" ");
            if (commandstr.length != 3) {
                System.out.println("ERROR IN NUMBER OF COMMAND ARGUMENTS!");
            } else {
                if (handler.CheckUserExists(commandstr[1])) {
                    System.out.println("ERROR User already Exists!");
                } else {
                    handler.AddUser(commandstr[1], commandstr[2]);
                }
            }

        }
        if (command.startsWith("list")) {
            String[] commandstr = command.split(" ");
            if (commandstr.length < 2) {
                System.out.println("ERROR IN NUMBER OF COMMAND ARGUMENTS!");
            } else {
                if (commandstr[1].toLowerCase().contains("User".toLowerCase())) {
                    commandfound = true;
                    listUsers();
                }
                if (commandstr[1].toLowerCase().contains("Interfaces".toLowerCase())) {
                    commandfound = true;
                    listInterfaces();
                }

            }
        }

        if (command.startsWith("help")) {
            commandfound = true;
            ListHelp();

        }

        if (command.startsWith("addinterface")) {
            commandfound = true;
            String[] commandstr = command.split(" ");
            if (commandstr.length < 3) {
                System.out.println("ERROR IN NUMBER OF COMMAND ARGUMENTS!");
            } else {
                String tmp = this.configFile.getAbsolutePath().substring(0, this.configFile.getAbsolutePath().lastIndexOf("\\"));
                File filetmp = new File(tmp + "/" + commandstr[2]);
                if (!filetmp.exists()) {
                    System.out.println(filetmp.getAbsolutePath());
                    System.out.println("ERROR JAR FILE DO NOT EXISTS!");
                } else {
                    if (handler.CheckJarExists(commandstr[1])) {
                        System.out.println("ERROR Interface already Exists!");
                    }
                }
            }
        }

        if (!commandfound) {
            System.out.println("Unrecognized Command");
        }
    }
}
