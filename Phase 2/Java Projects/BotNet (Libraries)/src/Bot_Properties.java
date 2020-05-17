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

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import javax.crypto.SecretKey;


public class Bot_Properties implements Serializable{
    private String BotIP, BotPCname; //Ip tou bot k to PC name tou Bot antistoixa.
    private Socket socket;
    private ObjectOutputStream oos;
    private SecretKey secKey;

    public Bot_Properties(String BotIP, String BotPCname) {
        this.BotIP = BotIP;
        this.BotPCname = BotPCname;
    }
    
    public Bot_Properties(){}

    
    //GETS:
    public String getBotIP() {
        return BotIP;
    }

    public String getBotPCname() {
        return BotPCname;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public SecretKey getSecKey() {
        return secKey;
    }
    
    
    
    
    //SETS:
    public void setBotPCname(String BotPCname) {
        this.BotPCname = BotPCname;
    }

    public void setBotIP(String BotIP) {
        this.BotIP = BotIP;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public void setSecKey(SecretKey secKey) {
        this.secKey = secKey;
    }
    
    
    
    
    
}//End class
