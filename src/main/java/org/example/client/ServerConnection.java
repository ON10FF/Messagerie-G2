package org.example.client;

import org.example.util.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerConnection {

    private static ServerConnection instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private PacketCallback onMessage;
    private Runnable onDisconnect;

    private boolean connected = false;

    private MessageListener messageListener;
    private Thread listenerThread;

    private static final Logger logger = Logger.getLogger(ServerConnection.class.getName());

    private ServerConnection() {}

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public boolean connect() {
        try {
            this.socket = new Socket("localhost", 5000);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            this.messageListener = new MessageListener(in, packet -> {
                if (onMessage != null) onMessage.onPacketReceived(packet);
            });

            this.listenerThread = new Thread(messageListener);
            this.listenerThread.setDaemon(true);
            this.listenerThread.start();

            this.connected = true;
            logger.info("Connexion au serveur établie");
            return true;

        } catch (IOException e) {
            logger.warning("Impossible de se connecter au serveur : " + e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void send(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            logger.warning("Erreur lors de l'envoi du Packet : " + e.getMessage());
            connected = false;
            if (onDisconnect != null) onDisconnect.run();
        }
    }

    public void setOnMessage(PacketCallback onMessage) {
        this.onMessage = onMessage;
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public void disconnect() {
        connected = false;
        if (messageListener != null) messageListener.stop();
        try { if (out != null) out.close(); } catch (IOException e) { logger.warning(e.getMessage()); }
        try { if (in != null) in.close(); } catch (IOException e) { logger.warning(e.getMessage()); }
        try { if (socket != null) socket.close(); } catch (IOException e) { logger.warning(e.getMessage()); }
        logger.info("Déconnexion du serveur");
    }
}
