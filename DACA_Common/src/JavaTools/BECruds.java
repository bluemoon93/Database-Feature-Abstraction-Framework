/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaTools;

import java.util.ArrayList;

/**
 * @author DIOGO
 */
public class BECruds {
    int id;
    String bename;
    String url;
    ArrayList<Cruds> cruds;

    public BECruds(int id, String bename, String url) {
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

    public void addCrud(int id, String crud) {
        cruds.add(new Cruds(id, crud));
    }

    public ArrayList<Cruds> getCruds() {
        return this.cruds;
    }

    public Cruds getCrud(int i) {
        return this.cruds.get(i);
    }
}
