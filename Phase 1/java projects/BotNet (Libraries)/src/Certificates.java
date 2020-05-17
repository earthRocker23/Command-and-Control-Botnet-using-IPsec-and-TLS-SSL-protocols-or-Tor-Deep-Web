/**
 *    Ασφάλεια Δικτύων Υπολογιστών & Τεχνολογίες Προστασίας της Ιδιωτικότητας
 *                                   Άσκηση 1
 * Δημιουργία ασφαλούς διαύλου διαχείρισης botnet (C&C) με χρήση IPsec & TLS/SSL πρωτοκόλλων.
 * 
 *                      Πέππας Κωνσταντίνος 321/2011134
 *                      Σωτηρέλης Χρήστος   321/2012182
 *                      Χαϊκάλης Νικόλαος   321/2012200
 * 
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class Certificates {
    
    private String jks;

    public Certificates() {
    }

    public Certificates(String jks) {
        this.jks = jks;
    }

    public KeyManagerFactory FindPubKey(){
        KeyManagerFactory serverKeyManager = null;
        try {
            //load server private key
            KeyStore serverKeys = KeyStore.getInstance("JKS","SUN");
            serverKeys.load(new FileInputStream(jks + ".jks"),"123456".toCharArray());
            serverKeyManager = KeyManagerFactory.getInstance("SunX509");
            serverKeyManager.init(serverKeys,"1234".toCharArray());
            return serverKeyManager;
        } catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | IOException | CertificateException | UnrecoverableKeyException ex) {

        }
        return null;
    }
    
    public TrustManagerFactory FindCaKey(){
        TrustManagerFactory trustManager = null;
        try {
            //load ca
            KeyStore caPubKey = KeyStore.getInstance("JKS","SUN");
            caPubKey.load(new FileInputStream("ca.jks"),"123456".toCharArray());
            trustManager = TrustManagerFactory.getInstance("SunX509");
            trustManager.init(caPubKey);
            return trustManager;
        } catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | IOException | CertificateException ex) {
            return null;
        } 
    }
      
}
