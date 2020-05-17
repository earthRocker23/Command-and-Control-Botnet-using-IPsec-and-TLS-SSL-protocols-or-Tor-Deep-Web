/**
 *    Ασφάλεια Δικτύων Υπολογιστών & Τεχνολογίες Προστασίας της Ιδιωτικότητας
 *                                   Άσκηση 2
 * Δημιουργία ασφαλούς διαύλου διαχείρισης botnet (C&C) με αξιοποίηση Tor και 
 * χρήση πρωτοκόλλου διασφάλισης εμπιστευτικότητας και ακεραιότητας
 * 
 *                      Πέππας Κωνσταντίνος 321/2011134
 *                      Σωτηρέλης Χρήστος   321/2012182
 *                      Χαϊκάλης Νικόλαος   321/2012200
 * 
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Class pou periexei oles tis methodous pou tha xriastoume gia to custom SSL
 * ektos apo to HMAC.
 */
public class MySSL implements Serializable{
    
    MySSL(){}

    /**
     * Methodos pou pairnw ta bytes tis CA pou diavazw apo to arxeio.
     * Auti i methodos xrisimopoiite mono apo to server.
     */
    public byte[] GetCABytes(){
        try {
            FileInputStream CaFile = new FileInputStream("C:\\Users\\Alcohealism\\Documents\\NetBeansProjects\\Botnet TOR\\CnC (Server)\\ca.crt");
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(CaFile);
            //write certificate bytes and read session key:
            byte[] cert_bytes = cert.getEncoded();
            System.out.println("Cert Bytes: " + cert_bytes);
            return cert_bytes;
            } catch (CertificateException | FileNotFoundException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    } 
    
    /**
     * Methodos pou epistrefei to KeyPair (private public keys) apo to
     * .jks file.
     * Auti i methodos xrisimopoiite mono apo ton Server.
     */
    public KeyPair GetKeyPair(){
        try {
            File jksFile = new File("C:\\Users\\Alcohealism\\Documents\\NetBeansProjects\\Botnet TOR\\CnC (Server)\\ca.jks");
            ExportPrivatePublicKeys keys = new ExportPrivatePublicKeys(jksFile, "JKS", "123456".toCharArray(), "1234".toCharArray());
            KeyPair keyPair = keys.MagicKeys();
            return keyPair;
        } catch (Exception ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Methodos pou dexetai os orisma byte[] kai ta metatrepepei se Certificate
     * apo to Certificate vriskei to public key kai kanei verify.
     * @param cert_byte
     * @return 
     */
    public PublicKey VerifyAndGetPubFromCA(byte[] cert_byte){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(cert_byte));
            PublicKey pubkey = (PublicKey) cert.getPublicKey();
            cert.verify(pubkey);
            System.out.println("++This certificate is VALID++");
            return pubkey;
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Dimiourgw ena CreateSecretKey.
     * Xrisimopoiite apo ta Bots.
     * @return 
     */
    public SecretKey CreateSecretKey(){
        try {
            KeyGenerator aes = KeyGenerator.getInstance("AES");
            aes.init(128);
            SecretKey SecKey = aes.generateKey();
            return SecKey;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Methodos gia Decrypt. Pairnw os orisma byte[] kai to PrivateKey k 
     * kanw decrypt ta byte[] k ta metatrepo se SecretKey.
     * Xrisimopoiite apo to Server.
     * @param encryptMSG
     * @param privKey
     * @return 
     */
    public SecretKey DecryptSecKey(byte[] encryptMSG, PrivateKey privKey){
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privKey);
            byte[] decrypted = cipher.doFinal(encryptMSG);
            SecretKey SecKey = new SecretKeySpec(decrypted, "AES");
            return SecKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }
    
    /**
     * Dexome os orisma tO SecretKey k to Public kai kriptografw to 
     * SecretKey me to Public kai ta metatrepo se byte[] ta byte auta ta kanw
     * return.
     * I methodos auti xrisimopoiite apo ta Bots.
     */
    public byte[] EncryptSecKeyWithPubKey(SecretKey SecKey, PublicKey pubkey){
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            byte[] cipherSecKey = cipher.doFinal(SecKey.getEncoded());
            return cipherSecKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    /**
     * Methodos pou dexetai os orisma ena String msg kai to SecretKey
     * kai kriprografw to String me to SecretKey kai epistrefw ta bytep[]
     * Xrisimopoiite kai apo ton Server kai apo tin Client.
     */
    public byte[] EncryptWithSecKey(String forEncrypt, SecretKey SecKey){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, SecKey);

            byte[] cipherMSG = cipher.doFinal(forEncrypt.getBytes());//encrypt with sec key
            return cipherMSG;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
     /**
     * Methodos pou dexetai os orisma ena Object msg kai to SecretKey
     * kai kriprografw to Object me to SecretKey kai epistrefw ta bytep[]
     * Xrisimopoiite kai apo ton Server kai apo tin Client.
     */
    public byte[] EncryptWithSecKey(Object obj, SecretKey SecKey){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, SecKey);
            byte[] tmp = serialize(obj);
            byte[] cipherMSG = cipher.doFinal(tmp);//encrypt with sec key
            return cipherMSG;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Dexomai os orisma byte[] kai to SecretKey kai me to SecretKey apokriptografw
     * ta byte[]. Ta byte auta meta ta metatrepw se Objects kai ta kanw return.
     */
    public Object DecryptWithSecKey(byte[] cipherMSG, SecretKey SecKey){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, SecKey);
            byte[] plainText = cipher.doFinal(cipherMSG);
//            String str = new String(plainText, "UTF-8");
//            System.out.println("Decrypt msg: " );
//            System.out.println(str);
            Object readObj = deserialize(plainText);
            return readObj;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        } 
//catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return null;
    }
    
    /**
     * Methodos pouy pairnw ena object kai to metatrepo se byte[]
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(obj);
        return out.toByteArray();
    }
    
    /**
     * Methodos pou metrapw ta byte[] se Object.
     */
    public static Object deserialize(byte[] data){
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
//            System.out.println("deserialize: " + in);
            ObjectInputStream ois = new ObjectInputStream(in);
//            System.out.println("deserialize, oos: " + ois);
            return (Object) ois.readObject();
        } catch (IOException ex) {

            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySSL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
