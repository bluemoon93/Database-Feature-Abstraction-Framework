package Security;

import LocalTools.BTC_Exception;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DacaSSLSocketFactoryGenerator {

    private DacaSSLSocketFactoryGenerator() {
    }

    private static TrustManager[] getTrustManagers(String certPath, String pwd) throws IOException, GeneralSecurityException {
        String alg = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmFact = TrustManagerFactory.getInstance(alg);
        KeyStore ks;
        try (FileInputStream fis = new FileInputStream(certPath)) {
            ks = KeyStore.getInstance("jks");
            ks.load(fis, pwd.toCharArray());
        }
        tmFact.init(ks);
        return tmFact.getTrustManagers();
    }

    public static SSLSocketFactory getSSLSocketFactory(String certPath, String pwd) {
        try {
            TrustManager[] tms = getTrustManagers(certPath, pwd);
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, tms, null);
            return context.getSocketFactory();
        } catch (IOException | GeneralSecurityException ex) {
            throw new BTC_Exception(ex);
        }
    }
}
