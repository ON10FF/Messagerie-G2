package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server {

    private ServerSocket serverSocket;

    private int port = 5000;

    private static final Logger logger  = Logger.getLogger(Server.class.getName());

    public Server() throws IOException {
            this.serverSocket = new ServerSocket(port);
            logger.info("Le server a bien demarre");
    }

    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            logger.info("La connexion a ete effectuee avec succes");

            try {
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }catch (IOException e){
                logger.warning("Erreur de connexion" + e.getMessage());
            }


        }
    }

    public void stop() throws IOException {
        serverSocket.close();
        logger.info("Serveur arrêté");
    }
}
