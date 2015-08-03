/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaTools;

public class BeInfo {
    private int crudid;
    private String crud_ref;
    private String crud_str;

    public BeInfo(int crudid, String crud_ref, String crud_str) {
        this.crudid = crudid;
        this.crud_ref = crud_ref;
        this.crud_str = crud_str;
    }

    public String getCrud_ref() {
        return crud_ref;
    }

    public void setCrud_ref(String crud_ref) {
        this.crud_ref = crud_ref;
    }

    public String getCrud_str() {
        return crud_str;
    }

    public void setCrud_str(String crud_str) {
        this.crud_str = crud_str;
    }

    public int getCrudid() {
        return crudid;
    }

    public void setCrudid(int crudid) {
        this.crudid = crudid;
    }

}
