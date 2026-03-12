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
        this.in =  new ObjectInputStream(socket.getInputStream());
        this.userDao = new UserDaoImpl();
        this.messageDao = new MessageDaoImpl();
    }

    public void sendPacket(Packet packet) throws IOException {
        out.writeObject(packet);
        out.flush();
    }

    @Override
    public void run(){
        try{
            while (running){
                Packet packet = (Packet) in.readObject();
                switch (packet.getType()){

                    case LOGIN     -> handleLogin(packet);
                    case REGISTER  -> handleRegister(packet);
                    case MESSAGE   -> handleMessage(packet);
                    case LOGOUT    -> handleLogout();
                    case GET_USERS -> handleGetUsers();
                }
            }

        } catch (Exception e) {
            handleDisconnection();
        }finally {
            closeResources();
        }
    }

    private void handleLogin(Packet packet) throws Exception{
         User user = (User) packet.getData();

        if (sessionManager.isConnected(user.getUserName()))
        {
            sendPacket(new Packet(Packet.Type.ERROR, "Deja connecte"));
            return;
        }

        this.currentUser = userDao.findByUsername(user.getUserName());
        if(this.currentUser == null){
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur Introuvable"));
            return;
        }

        if(PasswordUtil.verifyPassword(user.getHashPassword(), currentUser.getHashPassword()))
        {
            sendPacket(new Packet(Packet.Type.SUCCESS, "Connexion reussie"));
            currentUser.setStatus(User.Status.ONLINE);
            userDao.update(currentUser);
            sessionManager.addClient(currentUser.getUserName(), this);

            for (Message message : messageDao.findPendingMessage(currentUser)) {
                sendPacket(new Packet(Packet.Type.MESSAGE, message));
                messageDao.updateStatut(message);
            }
            
        }else {
            sendPacket(new Packet(Packet.Type.ERROR, "Mot de passe incorrect"));
        }
    }

    private void handleRegister(Packet packet) throws Exception{

        User user  = (User) packet.getData();

        if(userDao.existsByUsername(user.getUserName())){
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur existe deja"));
            return;
        }

        userDao.save(user);

        sendPacket(new Packet(Packet.Type.SUCCESS, "Utilisateur cree avec succes"));
    }

    private void handleMessage(Packet packet) throws Exception{

        if(this.currentUser == null){
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur Non Authentifie"));
            return;
        }

        Message message = (Message) packet.getData();

        if(!userDao.existsByUsername(message.getReceiver().getUserName()))
        {
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur inexistant"));
            return;
        }

        messageDao.save(message);

        if(sessionManager.isConnected(message.getReceiver().getUserName()))
        {
            sessionManager.getClient(message.getReceiver().getUserName()).sendPacket(new Packet(Packet.Type.MESSAGE, message));
        }
    }

    private void handleLogout() throws Exception{
        if(this.currentUser == null){
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur Non Authentifie"));
            return;
        }

        currentUser.setStatus(User.Status.OFFLINE);
        userDao.update(currentUser);
        sessionManager.removeClient(currentUser.getUserName());
        sendPacket(new Packet(Packet.Type.SUCCESS, "Utilisateur deconnecte avec succes"));
        running = false;
    }

    private void handleGetUsers() throws Exception{
        if(this.currentUser == null){
            sendPacket(new Packet(Packet.Type.ERROR, "Utilisateur Non Authentifie"));
            return;
        }

        List<User> connectedUsers = new ArrayList<>();

        for (ClientHandler handler : sessionManager.getClients()){
            connectedUsers.add(handler.getCurrentUser());
        }

        sendPacket(new Packet(Packet.Type.USER_LIST, connectedUsers));
    }

    private void handleDisconnection(){

        try{
            if (this.currentUser != null) {
                this.currentUser.setStatus(User.Status.OFFLINE);
                userDao.update(this.currentUser);
                sessionManager.removeClient(this.currentUser.getUserName());
                logger.info("Deconnexion brutale avec succes");
            }
        }catch (Exception e){
            logger.warning("Erreur de connexion");
        }

    }

    private void closeResources(){
        try { in.close(); } catch (IOException e) { logger.warning("erreur lors de la fermeture du inputStream"); }
        try { out.close(); } catch (IOException e) { logger.warning("erreur lors de la fermeture du ouputStream"); }
        try { socket.close(); } catch (IOException e) { logger.warning("erreur lors de la fermeture du socket"); }
    }

    public User getCurrentUser() {
        return currentUser;
    }

}

