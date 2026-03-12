package org.example.client;

import org.example.util.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerConnection {

    private Socket clientSocket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private static final Logger logger = Logger.getLogger(ServerConnection.class.getName());

    public ServerConnection(){
        try {
            this.clientSocket = new Socket("localhost", 5000);
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        }catch (IOException e){
            logger.warning("Erreur de connexion" + e.getMessage());
        }

    }

    public void sendPacket(Packet packet){
        try {
            objectOutputStream.writeObject(packet);
            objectOutputStream.flush();
        }catch (IOException e){
            logger.warning("Erreur de connexion" + e.getMessage());
        }
    }

    public ObjectInputStream getObjectInputStream(){
        return objectInputStream;
    }

    public void disconnect(){
        try {objectOutputStream.close();} catch (IOException e) {logger.warning("Erreur de fermeture de outputStream" + e.getMessage());}
        try {objectInputStream.close();} catch (IOException e) {logger.warning("Erreur de fermeture de InputStream" + e.getMessage());}
        try {clientSocket.close();} catch (IOException e) {logger.warning("Erreur de fermeture de clientSocket" + e.getMessage());}

    }
}
