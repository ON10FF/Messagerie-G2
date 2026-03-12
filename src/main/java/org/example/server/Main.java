package org.example.server;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try{
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }

}
