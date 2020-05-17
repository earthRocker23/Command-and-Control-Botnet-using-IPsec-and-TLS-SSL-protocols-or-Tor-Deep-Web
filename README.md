# Command-and-Control-Botnet-using-IPsec-and-TLS-SSL-protocols-or-Tor-Deep-Web

Command and Control Botnet using IPsec and TLS/SSL protocols or Tor Deep Web - University Assignment

Distributed System with a GUI written in Java.
Wrote a Python script using Scapy to DDoS the victims.
Used TLS/SSL, IPsec and Tor Network.

* Phase 1

The Botmaster is using SSL/TLS in order to communicate with the Command and Control Server (C&C).
The C&C forwarding the Botmaster’s requests to the Bots using SSL/TLS.
At the end the Bots are receiving the requests, and they start attacking to the victims using a python script in order to DDoS the victims.
As an extra layer of security the Botmaster is using IPsec to communicate with the C&C in order to hide his real IP address.
The SSL/TLS built it using OpenSSL.

* Phase 2

The Botmaster is using TOR dark web in order to communicate with the Command and Control Server (C&C). The C&C forwarding the Botmaster’s requests to the Bots using a custom SSL/TLS channel. At the end the Bots are receiving the requests, and they start attacking to the victims using a python script in order to DDoS the victims.

The custom SSL/TLS built using these steps:  

C&C:  
The server owns a self-signed Certificate  
Step 1:  
Bot: Hello message  
Step 2:  
C&C: Send certificate  
Step 3:  
Bot: Verify the certificate  
Step 4:  
Bot: Create a session key, Encrypt the Session Key with the Server’s Public Key  
Step 5:  
C&C:  
Decrypt Session Key with the Private Key  
Step 6:  
Bot: Encrypt(Data), HMAC(Encrypt(Data))  
Step 7:  
C&C: Verify(HMAC(Encrypted_Data)), Decrypt(Data)
