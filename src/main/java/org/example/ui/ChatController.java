package org.example.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.example.client.ServerConnection;
import org.example.model.Packet;

import java.util.List;

public class ChatController {

    @FXML private Label            labelUsername;
    @FXML private Label            labelRole;
    @FXML private Button           boutonTousMembres;
    @FXML private ListView<String> listeMembres;
    @FXML private Label            labelChatAvec;
    @FXML private Label            labelStatut;
    @FXML private VBox             zoneMessages;
    @FXML private ScrollPane       scrollPane;
    @FXML private TextField        champMessage;

    private String currentUser;
    private String currentRole;
    private String selectedMember = null;

    public void init(String username, String role) {
        this.currentUser = username;
        this.currentRole = role;
        labelUsername.setText(username);
        labelRole.setText(role);

        // RG13 : bouton visible uniquement pour ORGANISATEUR
        if ("ORGANISATEUR".equals(role)) {
            boutonTousMembres.setVisible(true);
            boutonTousMembres.setManaged(true);
        }

        ServerConnection conn = ServerConnection.getInstance();
        conn.setOnMessage(this::traiterReponse);
        conn.setOnDisconnect(() -> Platform.runLater(this::gererDeconnexion)); // RG10

        // Clic sur un membre
        listeMembres.setOnMouseClicked(e -> {
            String selection = listeMembres.getSelectionModel().getSelectedItem();
            if (selection != null && !selection.equals(currentUser)) {
                selectionnerMembre(selection.replace(" 🔴", ""));
            }
        });

        // Demander la liste des membres en ligne
        conn.send(new Packet("GET_ONLINE_USERS"));
    }

    @SuppressWarnings("unchecked")
    private void traiterReponse(Packet packet) {
        Platform.runLater(() -> {
            switch (packet.getType()) {
                case "ONLINE_USERS"  -> majListeMembres((List<String>) packet.getData());
                case "RECEIVE_MSG"   -> afficherMessageRecu(packet);
                case "HISTORY"       -> chargerHistorique(packet);
                case "ALL_MEMBERS"   -> afficherTousMembres((List<String>) packet.getData());
            }
        });
    }

    private void majListeMembres(List<String> membres) {
        listeMembres.getItems().clear();
        for (String m : membres) {
            if (!m.equals(currentUser)) {
                listeMembres.getItems().add(m);
            }
        }
    }

    private void selectionnerMembre(String username) {
        selectedMember = username;
        labelChatAvec.setText(username);
        labelStatut.setText("● en ligne");
        zoneMessages.getChildren().clear();

        Packet p = new Packet("GET_HISTORY");
        p.setUsername(currentUser);
        p.setReceiver(username);
        ServerConnection.getInstance().send(p);
    }

    @SuppressWarnings("unchecked")
    private void chargerHistorique(Packet packet) {
        // L'historique sera géré par le serveur
        List<String[]> historique = (List<String[]>) packet.getData();
        zoneMessages.getChildren().clear();
        if (historique != null) {
            for (String[] msg : historique) {
                boolean estMoi = msg[0].equals(currentUser);
                ajouterBulle(msg[0], msg[1], msg[2], estMoi);
            }
        }
        defilerVersLeBas();
    }

    private void afficherMessageRecu(Packet packet) {
        String expediteur = packet.getUsername();
        if (expediteur.equals(selectedMember)) {
            ajouterBulle(expediteur, packet.getMessage(),
                    java.time.LocalDateTime.now().toString(), false);
            defilerVersLeBas();
        } else {
            // Notification
            listeMembres.getItems().replaceAll(u -> {
                String clean = u.replace(" 🔴", "");
                return clean.equals(expediteur) ? expediteur + " 🔴" : u;
            });
        }
    }

    // RG13 : Voir tous les membres
    @FXML
    public void voirTousMembres() {
        ServerConnection.getInstance().send(new Packet("GET_ALL_MEMBERS"));
    }

    private void afficherTousMembres(List<String> membres) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Liste complète des membres");
        dialog.setHeaderText("👥 Tous les membres inscrits (" + membres.size() + ")");
        ListView<String> liste = new ListView<>();
        liste.getItems().addAll(membres);
        liste.setPrefSize(420, 300);
        dialog.getDialogPane().setContent(liste);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    public void envoyerMessage() {
        if (selectedMember == null) {
            afficherAlerte("Sélectionnez d'abord un contact.");
            return;
        }
        String contenu = champMessage.getText().trim();
        if (contenu.isEmpty() || contenu.length() > 1000) return;

        Packet p = new Packet("SEND_MSG");
        p.setUsername(currentUser);
        p.setReceiver(selectedMember);
        p.setMessage(contenu);
        ServerConnection.getInstance().send(p);

        ajouterBulle(currentUser, contenu,
                java.time.LocalDateTime.now().toString(), true);
        champMessage.clear();
        defilerVersLeBas();
    }

    private void ajouterBulle(String expediteur, String contenu, String heure, boolean estMoi) {
        VBox bulle = new VBox(4);
        bulle.setMaxWidth(420);
        bulle.setPadding(new Insets(10, 14, 10, 14));
        bulle.setStyle(estMoi
                ? "-fx-background-color: #e94560; -fx-background-radius: 18 18 4 18;"
                : "-fx-background-color: #0f3460; -fx-background-radius: 18 18 18 4;");

        Text texte = new Text(contenu);
        texte.setFill(Color.WHITE);
        texte.setStyle("-fx-font-size: 14px;");

        String heureFormatee = heure.length() > 16
                ? heure.substring(0, 16).replace("T", " ") : heure;
        Label labelHeure = new Label(heureFormatee);
        labelHeure.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.6);");

        bulle.getChildren().addAll(new TextFlow(texte), labelHeure);
        HBox ligne = new HBox(bulle);
        ligne.setPadding(new Insets(2, 8, 2, 8));
        ligne.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        zoneMessages.getChildren().add(ligne);
    }

    @FXML
    public void seDeconnecter() {
        Packet p = new Packet("LOGOUT");
        p.setUsername(currentUser);
        ServerConnection.getInstance().send(p);
        ServerConnection.getInstance().disconnect();
        allerLogin();
    }

    private void gererDeconnexion() {
        afficherAlerte("❌ Connexion perdue avec le serveur. (RG10)");
        allerLogin();
    }

    private void allerLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/ui/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 500, 520);
            Stage stage = (Stage) zoneMessages.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void defilerVersLeBas() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void afficherAlerte(String message) {
        Alert alerte = new Alert(Alert.AlertType.WARNING);
        alerte.setTitle("Avertissement");
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.showAndWait();
    }
}