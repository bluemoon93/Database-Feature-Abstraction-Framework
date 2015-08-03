/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaTools;

/**
 * @author DIOGO
 */
public class Cruds {
    int id;
    String cruds;

    public Cruds(int id, String cruds) {
        this.id = id;
        this.cruds = cruds;
    }

    public String getCruds() {
        return cruds;
    }

    public void setCruds(String cruds) {
        this.cruds = cruds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
