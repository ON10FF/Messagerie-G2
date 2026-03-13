package org.example.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.client.PacketCallback;
import org.example.client.ServerConnection;
import org.example.model.User;
import org.example.util.Packet;
import org.example.util.PasswordUtil;

public class LoginController {

    @FXML private TextField     champUsername;
    @FXML private PasswordField champMotDePasse;
    @FXML private Label         labelErreur;
    @FXML private Button        boutonConnecter;

    @FXML
    public void initialize() {
        ServerConnection conn = ServerConnection.getInstance();
        if (!conn.isConnected()) {
            if (!conn.connect()) {
                labelErreur.setText("❌ Impossible de joindre le serveur.");
                boutonConnecter.setDisable(true);
                return;
            }
        }
        conn.setOnMessage(packet -> Platform.runLater(() -> {
            switch (packet.getType()) {
                case SUCCESS -> allerChat(packet);
                case ERROR   -> labelErreur.setText("❌ " + packet.getData());
            }
        }));
    }

    @FXML
    public void seConnecter() {
        String username = champUsername.getText().trim();
        String password = champMotDePasse.getText();
        labelErreur.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        User user = new User(username, PasswordUtil.getHash(password.toCharArray()), User.Role.MEMBRE);
        ServerConnection.getInstance().send(new Packet(Packet.Type.LOGIN, user));
    }

    @FXML
    public void allerInscription() {
        changerScene("/org/example/ui/RegisterView.fxml", 500, 560);
    }

    private void allerChat(Packet packet) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/ChatView.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            ChatController controller = loader.getController();
            User user = (User) packet.getData();
            controller.init(user.getUserName(), user.getRole().toString());
            Stage stage = (Stage) champUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("💬 Messagerie — " + user.getUserName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changerScene(String chemin, int largeur, int hauteur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(chemin));
            Scene scene = new Scene(loader.load(), largeur, hauteur);
            Stage stage = (Stage) champUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
