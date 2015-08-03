/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaTools;

import java.util.ArrayList;

/**
 * @author DIOGO
 */
public class BE {

    int id;
    String bename;
    String url;
    ArrayList<String> cruds;

    public BE(int id, String bename, String url) {
        this.id = id;
        this.bename = bename;
        this.url = url;
        cruds = new ArrayList<>();
    }

    public String getBename() {
        return bename;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return bename + " " + url;
    }

    public void addCrud(String crud) {
        cruds.add(crud);
    }

    public ArrayList<String> getCruds() {
        return this.cruds;
    }

    public String getCrud(int i) {
        return this.cruds.get(i);
    }
}
