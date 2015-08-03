/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;

/**
 * @author OMP
 */
public interface ISession extends ITransaction, AutoCloseable {
    /**
     * Retuns the connection for this session.
     *
     * @return The connection.
     */
    //public Connection getConnection();
    //public Socket getConnection();
    
    public ObjectOutputStream getOOS();
    public ObjectInputStream getOIS();
}
