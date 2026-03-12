package org.example.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static SessionManager instance;

    private ConcurrentHashMap<String, ClientHandler> clients =  new ConcurrentHashMap<>();

    private SessionManager(){}

    public static synchronized SessionManager getInstance(){
        if(instance==null){
            instance=new SessionManager();
        }
        return instance;
    }

    public void addClient(String username,ClientHandler client){
        clients.put(username, client);
    }

    public void removeClient(String username){
        clients.remove(username);
    }

    public ClientHandler getClient(String username){
        return clients.get(username);
    }

    public boolean isConnected(String username){
        return clients.containsKey(username);
    }

    public Collection<ClientHandler> getClients(){
        return clients.values();
    }

}
