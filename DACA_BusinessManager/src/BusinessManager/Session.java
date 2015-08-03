/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import LocalTools.BTC_Exception;
import static LocalTools.MessageTypes.*;
import Security.ReflectionUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author OMP
 */
class Session implements ISession {

    //private final Connection conn;
    //private final Socket sock;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Session(Socket wrapperSocket, String url) throws SQLException {
        //        try {
        //System.out.println("url is " + url);
        //String connectionUrl = "jdbc:" + url + ";user=dummy;password=dummy;";
        //            Class.forName("org.apache.hive.jdbc.HiveDriver");
        //            String connectionUrl = "jdbc:hive2://10.0.0.3:10000/default;";

        //conn = DriverManager.getConnection(connectionUrl);//Obtém conecção á base de dados
        //changeConnectionSocket(conn, wrapperSocket);
        //        } catch (ClassNotFoundException ex) {
        //            throw new RuntimeException(ex);
        //        }
        try {
            oos = new ObjectOutputStream(wrapperSocket.getOutputStream());
            ois = new ObjectInputStream(wrapperSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //sock=wrapperSocket;
        //conn=null;
    }

    @Override
    public void commit() throws SQLException {
        try {
            oos.writeInt(COMMIT);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // this.conn.commit();
    }

    /*@Override
     public int getTransactionIsolationLevel() throws SQLException {
     return conn.getTransactionIsolation();
     }*/
    @Override
    public void releaseSavepoint(String savepoint) throws SQLException {
        try {
            oos.writeInt(RELSP);
            oos.flush();
            oos.writeUTF(savepoint);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //conn.releaseSavepoint(savepoint);
    }

    @Override
    public void rollBack() throws SQLException {
        try {
            oos.writeInt(RB);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //conn.rollback();
    }

    @Override
    public void rollBack(String savepoint) throws SQLException {
        try {
            oos.writeInt(RBSP);
            oos.flush();
            oos.writeUTF(savepoint);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //conn.rollback(savepoint);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        try {
            oos.writeInt(AUTOCOMMIT);
            oos.flush();
            oos.writeBoolean(autoCommit);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //conn.setAutoCommit(autoCommit);
    }

    @Override
    public void setSavepoint() throws SQLException {
        try {
            oos.writeInt(SP);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return conn.setSavepoint();
    }

    @Override
    public void setSaveSavepoint(String name) throws SQLException {
        try {
            oos.writeInt(SPNAME);
            oos.flush();
            oos.writeUTF(name);
            oos.flush();
            ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return conn.setSavepoint(name);
    }

    /*@Override
     public void setTransactionIsolation(int level) throws SQLException {
     //conn.setTransactionIsolation(level);
     throw new SQLException("Not implemented in this prototype");
     }*/

    /*@Override
     public Connection getConnection() {
     return conn;
     }*/
    /*@Override
     public Socket getConnection() {
     return sock;
     }*/
    @Override
    public ObjectOutputStream getOOS() {
        return oos;
    }

    @Override
    public ObjectInputStream getOIS() {
        return ois;
    }

    /**
     * Closes this resource, relinquishing any underlying resources. This method
     * is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>
     * While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to declare
     * concrete implementations of the {@code close} method to throw more
     * specific exceptions, or to throw no exception at all if the close
     * operation cannot fail.
     *
     * <p>
     * <em>Implementers of this interface are also strongly advised to not have
     * the {@code close} method throw {@link
     * InterruptedException}.</em>
     *
     * This exception interacts with a thread's interrupted status, and runtime
     * misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     *
     * More generally, if it would cause problems for an exception to be
     * suppressed, the {@code AutoCloseable.close} method should not throw it.
     *
     * <p>
     * Note that unlike the {@link java.io.Closeable#close close} method of
     * {@link java.io.Closeable}, this {@code close} method is <em>not</em>
     * required to be idempotent. In other words, calling this {@code close}
     * method more than once may have some visible side effect, unlike
     * {@code Closeable.close} which is required to have no effect if called
     * more than once.
     *
     * However, implementers of this interface are strongly encouraged to make
     * their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        //conn.close();
        oos.writeInt(-100);
        oos.flush();
    }

    /**
     * Changes the connection to use a given socket.
     *
     * @param conn The connection to change.
     * @param mySocket The socket to use.
     * @throws BTC_Exception If something throws an exception.
     */
    private static void changeConnectionSocket(Connection conn, Socket mySocket) throws BTC_Exception {
        try {
            Object fieldValue = ReflectionUtils.getFieldValue(conn, "tdsChannel");
            Field declaredField = fieldValue.getClass().getDeclaredField("channelSocket");
            declaredField.setAccessible(true);
            declaredField.set(fieldValue, mySocket);

            declaredField = fieldValue.getClass().getDeclaredField("tcpSocket");
            declaredField.setAccessible(true);
            declaredField.set(fieldValue, mySocket);

            declaredField = fieldValue.getClass().getDeclaredField("tcpInputStream");
            declaredField.setAccessible(true);
            declaredField.set(fieldValue, mySocket.getInputStream());

            declaredField = fieldValue.getClass().getDeclaredField("inputStream");
            declaredField.setAccessible(true);
            declaredField.set(fieldValue, mySocket.getInputStream());

            declaredField = fieldValue.getClass().getDeclaredField("tcpOutputStream");
            declaredField.setAccessible(true);
            declaredField.set(fieldValue, mySocket.getOutputStream());

            declaredField = fieldValue.getClass().getDeclaredField("outputStream");
            declaredField.setAccessible(true);
            declaredField.set(fieldValue, mySocket.getOutputStream());
        } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            throw new BTC_Exception(ex);
        }
    }

}
