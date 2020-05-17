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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class pou tin xrisimopoiume gia tin dimiourgia twn 2 chat. 1 antikeimeno
 * gia to chat me ton BotMaster kai 1 antikeimeno gia to chat gia kathe
 * bot.
 */
public class Chat implements Serializable{
    
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String User;
    private Messages msgs = new Messages();

    /**
     * @param oos
     * @param ois 
     * Pairnw ta oos k ois tou current user.
     */
    public Chat(ObjectOutputStream oos, ObjectInputStream ois, String User) {
        this.oos = oos;
        this.ois = ois;
        this.User = User;
    }

    //Default Constructor:
    public Chat(){}
    
    //Methodos gia na stelnw minimata ston Client.
    public void SendMessage(Object msg) {
        try {
            oos.writeObject(msg);
            oos.flush();
            System.out.println("C&C Server> " + msg.toString());
        } catch (IOException ex) {
//            System.out.println("implements Serializable");
        }
    }
      
    public void SendMessageWithOOS(Object msg, ObjectOutputStream oos) {
        try {
            oos.writeObject(msg);
            oos.flush();
            System.out.println("C&C Server> " + msg.toString());
        } catch (IOException ex) {
//            System.out.println("implements Serializable");
        }
    }
    
    public void SendBytesWithOOS(byte[] msg, ObjectOutputStream oos){
        try {
            oos.writeObject(msg);
            oos.flush();
            System.out.println("C&C Server> " + msg.toString());
        } catch (IOException ex) {
//            System.out.println("implements Serializable");
        }
    }
    
    public void SendBytes(byte[] bytes){
        try {
            //System.out.println("Paw na steilw Bytes stin SendBytes(byte[] bytes) : ");
            //System.out.println(bytes);
            oos.writeObject(bytes);
            oos.flush();
            //System.out.println("Ta esteila");
            System.out.println("C&C Server> " + bytes);
        } catch (IOException ex) {
//            System.out.println("implements Serializable");
        }
    }
    
    //Methodos gia na diavazw.
    public Object readMessage(){
        try {
            Object readObj = ois.readObject();
            System.out.println(User+"> " + readObj.toString());
            return readObj;
        } catch (IOException ex) {
            Object readObj = msgs.StopConnection();
            return readObj;
        }   catch (ClassNotFoundException ex) {
            
        }
        return null;
    }  
    
    public byte[] ReadBytes(){
        try {
            byte[] tmp = (byte[]) ois.readObject();
            System.out.println(User+"> " + tmp);
            return (byte[]) tmp;
        } catch (IOException | ClassNotFoundException ex) {
            
        }
        return null;
    }
    
    //GETS:
    public ObjectOutputStream getOos() {
        return oos;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public String getUser() {
        return User;
    }
    
    
    
    //SETS:
    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public void setUser(String User) {
        this.User = User;
    }
}
