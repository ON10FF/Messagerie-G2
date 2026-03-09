package org.example.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.client.ServerConnection;
import org.example.model.Packet;

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
        conn.setOnMessage(this::traiterReponse);
    }

    private void traiterReponse(Packet packet) {
        Platform.runLater(() -> {
            switch (packet.getType()) {
                case "LOGIN_OK"   -> allerChat(packet.getUsername(), packet.getRole());
                case "LOGIN_FAIL" -> labelErreur.setText("❌ " + packet.getMessage());
            }
        });
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

        Packet packet = new Packet("LOGIN");
        packet.setUsername(username);
        packet.setMessage(password);
        ServerConnection.getInstance().send(packet);
    }

    @FXML
    public void allerInscription() {
        changerScene("/org/example/ui/RegisterView.fxml", 500, 560);
    }

    private void allerChat(String username, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/ChatView.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            ChatController controller = loader.getController();
            controller.init(username, role);
            Stage stage = (Stage) champUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("💬 Messagerie — " + username);
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