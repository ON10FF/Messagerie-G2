package org.example.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.client.ServerConnection;
import org.example.model.User;
import org.example.util.Packet;
import org.example.util.PasswordUtil;

public class RegisterController {

    @FXML private TextField        champUsername;
    @FXML private PasswordField    champPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private PasswordField    champCodeSecret;
    @FXML private Label            labelErreur;

    @FXML
    public void initialize() {
        comboRole.getItems().addAll("MEMBRE", "BENEVOLE", "ORGANISATEUR");
        comboRole.setValue("MEMBRE");

        comboRole.setOnAction(e -> {
            boolean estOrga = "ORGANISATEUR".equals(comboRole.getValue());
            champCodeSecret.setVisible(estOrga);
            champCodeSecret.setManaged(estOrga);
            if (!estOrga) champCodeSecret.clear();
        });

        ServerConnection.getInstance().setOnMessage(packet -> Platform.runLater(() -> {
            switch (packet.getType()) {
                case SUCCESS -> {
                    labelErreur.setStyle("-fx-text-fill: #4caf50;");
                    labelErreur.setText("✅ " + packet.getData());
                }
                case ERROR -> {
                    labelErreur.setStyle("-fx-text-fill: #e94560;");
                    labelErreur.setText("❌ " + packet.getData());
                }
            }
        }));
    }

    @FXML
    public void sInscrire() {
        String username = champUsername.getText().trim();
        String password = champPassword.getText();
        String role     = comboRole.getValue();
        labelErreur.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            labelErreur.setStyle("-fx-text-fill: #e94560;");
            labelErreur.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (password.length() < 4) {
            labelErreur.setStyle("-fx-text-fill: #e94560;");
            labelErreur.setText("Mot de passe trop court (minimum 4 caractères).");
            return;
        }

        // SHA-256 côté client — jamais le mot de passe en clair sur le réseau
        String sha256 = PasswordUtil.getSHA256(password.toCharArray());
        User user = new User(username, sha256, User.Role.valueOf(role));
        ServerConnection.getInstance().send(new Packet(Packet.Type.REGISTER, user));
    }

    @FXML
    public void allerLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 500, 520);
            Stage stage = (Stage) champUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}