package Security;

import LocalTools.BTC_Exception;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;
import sun.security.provider.certpath.OCSP;

public class CCHelper {

    private static Provider p = null;

    /**
     * @param userCert
     * @return
     */
    public static boolean validate(X509Certificate userCert) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List mylist = new ArrayList();
            String aucurl = "http://ocsp.auc.cartaodecidadao.pt/publico/ocsp";

            String issuerCertName = userCert.getIssuerX500Principal().getName();
            int cnIdx = issuerCertName.indexOf("CN=");
            if (cnIdx == -1) {
                return false;
            }
            int endIdx = issuerCertName.indexOf(",", cnIdx);
            if (endIdx == -1) {
                endIdx = issuerCertName.length();
            }

            issuerCertName = issuerCertName.substring(cnIdx + 3, endIdx);
            issuerCertName = issuerCertName.substring(issuerCertName.lastIndexOf(" ") + 1);
            String subCAFile = String.format("./certificates/%s.cer", issuerCertName);

            FileInputStream isCert = new FileInputStream(subCAFile);
            X509Certificate certSUBCA = (X509Certificate) cf.generateCertificate(isCert);

            issuerCertName = certSUBCA.getIssuerX500Principal().getName();
            cnIdx = issuerCertName.indexOf("CN=");
            if (cnIdx == -1) {
                return false;
            }
            endIdx = issuerCertName.indexOf(",", cnIdx);
            if (endIdx == -1) {
                endIdx = issuerCertName.length();
            }

            issuerCertName = issuerCertName.substring(cnIdx + 3, endIdx);
            issuerCertName = issuerCertName.substring(issuerCertName.lastIndexOf(" ") + 1);
            String caFile = String.format("./certificates/CC%s.cer", issuerCertName);

            FileInputStream isCertCA = new FileInputStream(caFile);
            X509Certificate certCA = (X509Certificate) cf.generateCertificate(isCertCA);

            mylist.add(userCert);
            //mylist.add(certSUBCA);

            CertPath cp = cf.generateCertPath(mylist);
       
            Certificate trust = certSUBCA;
            TrustAnchor anchor = new TrustAnchor((X509Certificate) trust, null);
            PKIXParameters params = new PKIXParameters(Collections.singleton(anchor));
            params.setRevocationEnabled(true);

            // enable OCSP
            Security.setProperty("ocsp.enable", "true");
            Security.setProperty("ocsp.responderURL", aucurl);

            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
            cpv.validate(cp, params);
            return true;
        } catch (CertificateException | FileNotFoundException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | CertPathValidatorException ex) {
            Logger.getLogger(CCHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static X509Certificate getCCAuthenticationCert() {
        Certificate cert = null;
        try {
            KeyStore ks = loadPkcs11();
            cert = ks.getCertificate("CITIZEN AUTHENTICATION CERTIFICATE");
        } catch (KeyStoreException e) {
            throw new BTC_Exception("Can't construct X509 Certificate. "
                    + e.getMessage());
        }
        return (X509Certificate) cert;

    }

    public static Provider getProvider() {
        if (p == null) {
            String[] t = System.getProperty("os.name").split(" ");

            String pkcs11ConfigSettings;
            if (t[0].equals("Windows")) {
                pkcs11ConfigSettings = "name = SmartCard\n" + "library = C:/WINDOWS/system32/pteidpkcs11.dll";
            } else {
                pkcs11ConfigSettings = "name = SmartCard\n" + "library = /usr/local/lib/libpteidpkcs11.so";
            }

            byte[] pkcs11configBytes = pkcs11ConfigSettings.getBytes();
            ByteArrayInputStream configStream = new ByteArrayInputStream(pkcs11configBytes);
            p = new sun.security.pkcs11.SunPKCS11(configStream);
            Security.addProvider(p);
        }

        return p;
    }

    /**
     * Loads the keystore from the smart card using its PKCS#11 implementation library and the Sun PKCS#11 security provider.
     *
     * @return
     */
    public static KeyStore loadPkcs11() {
        KeyStore ks;

        try {
            CallbackHandler cmdLineHdlr = new com.sun.security.auth.callback.TextCallbackHandler();
            KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11", getProvider(),
                    new KeyStore.CallbackHandlerProtection(cmdLineHdlr));
            ks = builder.getKeyStore();

            return ks;
        } catch (KeyStoreException e1) {
            throw new BTC_Exception(e1);
        }
    }
}
