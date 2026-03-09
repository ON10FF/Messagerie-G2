package org.example.client;

import org.example.model.Packet;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ServerConnection {

    private static final Logger logger = Logger.getLogger(ServerConnection.class.getName());

    // ⚠️ Changer l'IP si le serveur est sur une autre machine
    private static final String SERVER_IP   = "localhost";
    private static final int    SERVER_PORT = 5000;

    private Socket               socket;
    private ObjectOutputStream   sortie;
    private ObjectInputStream    entree;
    private Consumer<Packet>     onMessage;
    private Runnable             onDisconnect;
    private boolean              actif = false;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            sortie = new ObjectOutputStream(socket.getOutputStream());
            entree = new ObjectInputStream(socket.getInputStream());
            actif  = true;
            startListening();
            logger.info("Connecté au serveur " + SERVER_IP + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            logger.warning("Impossible de se connecter : " + e.getMessage());
            return false;
        }
    }

    private void startListening() {
        Thread thread = new Thread(() -> {
            try {
                while (actif) {
                    Packet packet = (Packet) entree.readObject();
                    if (onMessage != null) onMessage.accept(packet);
                }
            } catch (EOFException | java.net.SocketException e) {
                actif = false;
                if (onDisconnect != null) onDisconnect.run(); // RG10
            } catch (Exception e) {
                logger.warning("Erreur réception : " + e.getMessage());
                actif = false;
                if (onDisconnect != null) onDisconnect.run();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void send(Packet packet) {
        try {
            sortie.writeObject(packet);
            sortie.flush();
            sortie.reset();
        } catch (IOException e) {
            logger.warning("Erreur envoi : " + e.getMessage());
        }
    }

    public void disconnect() {
        actif = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    public boolean isConnected()                     { return actif && socket != null && !socket.isClosed(); }
    public void setOnMessage(Consumer<Packet> c)     { this.onMessage = c; }
    public void setOnDisconnect(Runnable r)           { this.onDisconnect = r; }


    private static ServerConnection instance;
    private ServerConnection() {}
    public static ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }
}