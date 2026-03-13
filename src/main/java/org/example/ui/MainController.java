package org.example.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.client.ServerConnection;

public class MainController {

    @FXML private Label labelErreurReseau;

    @FXML
    public void initialize() {
        // Vérifier la connexion au serveur (RG10)
        ServerConnection conn = ServerConnection.getInstance();
        if (!conn.isConnected()) {
            if (!conn.connect()) {
                labelErreurReseau.setText("❌ Serveur non disponible. Veuillez réessayer plus tard.");
            }
        }

        // Gérer la déconnexion brutale (RG10)
        conn.setOnDisconnect(() -> Platform.runLater(() ->
                labelErreurReseau.setText("❌ Connexion perdue avec le serveur.")));
    }

    @FXML
    public void allerLogin() {
        changerScene("/org/example/ui/LoginView.fxml", 500, 520);
    }

    @FXML
    public void allerRegister() {
        changerScene("/org/example/ui/RegisterView.fxml", 500, 560);
    }

    private void changerScene(String chemin, int largeur, int hauteur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(chemin));
            Scene scene = new Scene(loader.load(), largeur, hauteur);
            Stage stage = (Stage) labelErreurReseau.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}