package org.example.server;

import org.example.dao.MessageDao;
import org.example.dao.MessageDaoImpl;
import org.example.dao.UserDao;
import org.example.dao.UserDaoImpl;
import org.example.model.Message;
import org.example.model.User;
import org.example.util.Packet;
import org.example.util.PasswordUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserDao userDao;
    private MessageDao messageDao;
    private User currentUser;
    private SessionManager sessionManager = SessionManager.getInstance();
    private boolean running = true;
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.userDao = new UserDaoImpl();
        this.messageDao = new MessageDaoImpl();
    }

    public void sendPacket(Packet packet) throws IOException {
        out.writeObject(packet);
        out.flush();
    }

    @Override
    public void run() {
        try {
            while (running) {
                Packet packet = (Packet) in.readObject();
                switch (packet.getType()) {
                    case LOGIN           -> handleLogin(packet);
                    case REGISTER        -> handleRegister(packet);
                    case MESSAGE         -> handleMessage(packet);
                    case LOGOUT          -> handleLogout();
                    case GET_USERS       -> handleGetUsers();
                    case GET_ONLINE_USERS -> handleGetUsers();
                    case GET_HISTORY     -> handleGetHistory(packet);
                    case GET_ALL_MEMBERS -> handleGetAllMembers();
                }
            }
        } catch (Exception e) {
            handleDisconnection();
        } finally {
            closeResources();
        }
    }

    private void handleLogin(Packet packet) throws Exception {
        User user = (User) packet.getData();

        if (sessionManager.isConnected(user.getUserName())) {
            sendPacket(new Packet(Packet.Type.ERROR, "Déjà connecté"));
            return;
        }

        this.currentUser = userDao.findByUsername(user.getUserName());
        if (this.currentUser == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur introuvable"));
            return;
        }

        if (PasswordUtil.verifyPassword(user.getHashPassword(), currentUser.getHashPassword())) {
            currentUser.setStatus(User.Status.ONLINE);
            userDao.update(currentUser);
            sessionManager.addClient(currentUser.getUserName(), this);
            sendPacket(new Packet(Packet.Type.SUCCESS, currentUser));
            logger.info("Connexion : " + currentUser.getUserName());

            for (Message message : messageDao.findPendingMessage(currentUser)) {
                sendPacket(new Packet(Packet.Type.MESSAGE, message));
                messageDao.updateStatut(message);
            }
        } else {
            sendPacket(new Packet(Packet.Type.ERROR, "Mot de passe incorrect"));
        }
    }

    private void handleRegister(Packet packet) throws Exception {
        User user = (User) packet.getData();

        if (userDao.existsByUsername(user.getUserName())) {
            sendPacket(new Packet(Packet.Type.ERROR, "Nom d'utilisateur déjà pris"));
            return;
        }

        userDao.save(user);
        sendPacket(new Packet(Packet.Type.SUCCESS, "Inscription réussie"));
        logger.info("Inscription : " + user.getUserName());
    }

    private void handleMessage(Packet packet) throws Exception {
        if (currentUser == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Non authentifié"));
            return;
        }

        Message message = (Message) packet.getData();

        if (!userDao.existsByUsername(message.getReceiver().getUserName())) {
            sendPacket(new Packet(Packet.Type.ERROR, "Destinataire introuvable"));
            return;
        }

        messageDao.save(message);
        logger.info("Message de " + currentUser.getUserName()
                + " vers " + message.getReceiver().getUserName());

        if (sessionManager.isConnected(message.getReceiver().getUserName())) {
            sessionManager.getClient(message.getReceiver().getUserName())
                    .sendPacket(new Packet(Packet.Type.MESSAGE, message));
        }
    }

    private void handleLogout() throws Exception {
        if (currentUser == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Non authentifié"));
            return;
        }

        currentUser.setStatus(User.Status.OFFLINE);
        userDao.update(currentUser);
        sessionManager.removeClient(currentUser.getUserName());
        sendPacket(new Packet(Packet.Type.SUCCESS, "Déconnexion réussie"));
        logger.info("Déconnexion : " + currentUser.getUserName());
        running = false;
    }

    private void handleGetUsers() throws Exception {
        if (currentUser == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Non authentifié"));
            return;
        }

        List<User> connectedUsers = new ArrayList<>();
        for (ClientHandler handler : sessionManager.getClients()) {
            connectedUsers.add(handler.getCurrentUser());
        }

        sendPacket(new Packet(Packet.Type.USER_LIST, connectedUsers));
    }

    private void handleGetHistory(Packet packet) throws Exception {
        if (currentUser == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Non authentifié"));
            return;
        }

        Message msg = (Message) packet.getData();
        User receiver = userDao.findByUsername(msg.getReceiver().getUserName());

        if (receiver == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur introuvable"));
            return;
        }

        List<Message> historique = messageDao.findConversation(currentUser, receiver);
        sendPacket(new Packet(Packet.Type.USER_LIST, historique));
    }

    private void handleGetAllMembers() throws Exception {
        if (currentUser == null) {
            sendPacket(new Packet(Packet.Type.ERROR, "Non authentifié"));
            return;
        }

        if (currentUser.getRole() != User.Role.ORGANISATEUR) {
            sendPacket(new Packet(Packet.Type.ERROR, "Accès refusé — ORGANISATEUR uniquement"));
            return;
        }

        List<User> membres = userDao.findAll();
        sendPacket(new Packet(Packet.Type.USER_LIST, membres));
        logger.info("Liste membres demandée par : " + currentUser.getUserName());
    }

    private void handleDisconnection() {
        try {
            if (currentUser != null) {
                currentUser.setStatus(User.Status.OFFLINE);
                userDao.update(currentUser);
                sessionManager.removeClient(currentUser.getUserName());
                logger.info("Déconnexion brutale : " + currentUser.getUserName());
            }
        } catch (Exception e) {
            logger.warning("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    private void closeResources() {
        try { in.close(); }     catch (IOException e) { logger.warning("Erreur fermeture in : "     + e.getMessage()); }
        try { out.close(); }    catch (IOException e) { logger.warning("Erreur fermeture out : "    + e.getMessage()); }
        try { socket.close(); } catch (IOException e) { logger.warning("Erreur fermeture socket : " + e.getMessage()); }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
