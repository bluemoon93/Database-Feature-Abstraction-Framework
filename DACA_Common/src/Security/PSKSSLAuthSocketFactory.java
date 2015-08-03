package Security;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class PSKSSLAuthSocketFactory {

    private static final ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
    private static final SocketFactory csf = SSLSocketFactory.getDefault();

    public static Socket createServerSocketAndAccept(int port, byte[] secret) {
        try {
            // Connect using SSL
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
            serverSocket.setEnabledCipherSuites(new String[]{"TLS_DH_anon_WITH_AES_128_CBC_SHA"});
            SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
            Object handshaker = ReflectionUtils.getFieldValue(clientSocket, "handshaker");
            clientSocket.startHandshake();
            changeSSLConnectionKeys(clientSocket, handshaker, secret);
            return clientSocket;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static Socket createSocket(String dest, int port, byte[] secret) {
        try {
            // Connect using SSL
            SSLSocket socket = (SSLSocket) csf.createSocket(dest, port);
            socket.setEnabledCipherSuites(new String[]{"TLS_DH_anon_WITH_AES_128_CBC_SHA"});
            Object handshaker = ReflectionUtils.getFieldValue(socket, "handshaker");
            socket.startHandshake();
            changeSSLConnectionKeys(socket, handshaker, secret);
            return socket;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void changeSSLConnectionKeys(SSLSocket socket, Object handshaker, byte[] secret) {
        try {
            // Retrieve the previous connection state
            Field CSF = socket.getClass().getDeclaredField("connectionState");
            CSF.setAccessible(true);
            int prevState = CSF.getInt(socket);

            // Retrieve the master secret
            Field MSF = socket.getSession().getClass().getDeclaredField("masterSecret");
            MSF.setAccessible(true);
            SecretKey masterSecret = (SecretKey) MSF.get(socket.getSession());

            // Hash the current master secret with the shared secret
            MessageDigest md = MessageDigest.getInstance("SHA-512", "SUN");
            md.update(masterSecret.getEncoded());
            md.update(secret);
            byte[] newMasterSecretBytes = Arrays.copyOfRange(md.digest(), 0, masterSecret.getEncoded().length);

            // Set the newly created secret in the master secret
            Field KF = masterSecret.getClass().getDeclaredField("key");
            KF.setAccessible(true);
            KF.set(masterSecret, newMasterSecretBytes);

            // Invoke the method to calculate the connection keys
            Method CCK = handshaker.getClass().getSuperclass().getDeclaredMethod("calculateConnectionKeys", SecretKey.class);
            CCK.setAccessible(true);
            CCK.invoke(handshaker, masterSecret);

            // Set the handshaker with the new connections keys in the SSL Socket
            Field HF = socket.getClass().getDeclaredField("handshaker");
            HF.setAccessible(true);
            HF.set(socket, handshaker);

            // Set the current socket state to cs_HANDSHAKE
            CSF.set(socket, ReflectionUtils.getFieldInt(socket, "cs_HANDSHAKE"));

            // Invoke the method to change the read cipher
            Method CRC = socket.getClass().getDeclaredMethod("changeReadCiphers");
            CRC.setAccessible(true);
            CRC.invoke(socket);

            // Invoke the method to change the write cipher
            Method CWC = socket.getClass().getDeclaredMethod("changeWriteCiphers");
            CWC.setAccessible(true);
            CWC.invoke(socket);

            // Put the socket back to its original state and set the handshaker to null
            CSF.set(socket, prevState);
            HF.set(socket, null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchMethodException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

    }
}
