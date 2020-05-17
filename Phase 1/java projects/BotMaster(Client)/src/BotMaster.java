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

public class BotMaster {

    public static void main(String[] args) throws InterruptedException {
        
        ChatWithCC_Server BotMaster = new ChatWithCC_Server();
        boolean start = BotMaster.Start_Connection(); 
        if(start == true){
            GUI gui = new GUI(BotMaster);
            gui.createGUI();
        }
    }
    
}
