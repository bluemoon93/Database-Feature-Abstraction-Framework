/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securityconfigurator;

public class CrudInfo {
    String crudref;
    String crud;

    public CrudInfo(String crudref, String crud) {
        this.crudref = crudref;
        this.crud = crud;
    }

    public String getCrud() {
        return crud;
    }

    public String getCrudref() {
        return crudref;
    }


}
