package org.example.client;

import org.example.util.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

public class MessageListener implements Runnable {

    private ObjectInputStream objectInputFromServer;

    private PacketCallback packetCallback;

    private boolean running = true;

    private static final Logger logger = Logger.getLogger(MessageListener.class.getName());

    public MessageListener(ObjectInputStream objectInputFromServer, PacketCallback packetCallback) {
        this.objectInputFromServer = objectInputFromServer;
        this.packetCallback = packetCallback;
    }

    public void run() {
        try{
            while (running) {
                Packet packet = (Packet) objectInputFromServer.readObject();
                packetCallback.onPacketReceived(packet);
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            logger.warning("Erreur de connexion");
            running = false;
        }

    }

    public void stop() {
        running = false;
    }
}
