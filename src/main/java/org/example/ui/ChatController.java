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
import org.example.model.Message;
import org.example.model.User;
import org.example.util.Packet;

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

    private User currentUser;
    private String selectedMember = null;

    public void init(String username, String role) {
        currentUser = new User(username, null, User.Role.valueOf(role));
        labelUsername.setText(username);
        labelRole.setText(role);

        if ("ORGANISATEUR".equals(role)) {
            boutonTousMembres.setVisible(true);
            boutonTousMembres.setManaged(true);
        }

        ServerConnection conn = ServerConnection.getInstance();
        conn.setOnMessage(packet -> Platform.runLater(() -> traiterReponse(packet)));
        conn.setOnDisconnect(() -> Platform.runLater(this::gererDeconnexion));

        listeMembres.setOnMouseClicked(e -> {
            String selection = listeMembres.getSelectionModel().getSelectedItem();
            if (selection != null && !selection.equals(username)) {
                // Nettoie le 🔴 et le (non lu)
                String clean = selection.replace(" 🔴", "").trim();
                selectionnerMembre(clean);
            }
        });

        conn.send(new Packet(Packet.Type.GET_USERS, null));
    }

    @SuppressWarnings("unchecked")
    private void traiterReponse(Packet packet) {
        switch (packet.getType()) {
            case USER_LIST       -> majListeMembres((List<User>) packet.getData());
            case MESSAGE         -> afficherMessageRecu((Message) packet.getData());
            case HISTORY         -> chargerHistorique((List<Message>) packet.getData());
            case GET_ALL_MEMBERS -> afficherTousMembres((List<User>) packet.getData());
            case ERROR           -> afficherAlerte((String) packet.getData());
        }
    }

    private void majListeMembres(List<User> membres) {
        listeMembres.getItems().clear();
        for (User u : membres) {
            if (!u.getUserName().equals(currentUser.getUserName())) {
                listeMembres.getItems().add(u.getUserName());
            }
        }
    }

    private void selectionnerMembre(String username) {
        selectedMember = username;
        labelChatAvec.setText(username);
        labelStatut.setText("● en ligne");
        zoneMessages.getChildren().clear();

        // Retire le 🔴 quand on ouvre la conversation
        listeMembres.getItems().replaceAll(u -> {
            String clean = u.replace(" 🔴", "").trim();
            return clean.equals(username) ? clean : u;
        });

        User receiver = new User(username, null, User.Role.MEMBRE);
        Message msg = new Message(currentUser, receiver, "");
        ServerConnection.getInstance().send(new Packet(Packet.Type.GET_HISTORY, msg));
    }

    private void chargerHistorique(List<Message> historique) {
        zoneMessages.getChildren().clear();
        if (historique != null) {
            for (Message msg : historique) {
                boolean estMoi = msg.getSender().getUserName()
                        .equals(currentUser.getUserName());
                ajouterBulle(msg.getSender().getUserName(), msg.getMessage(),
                        msg.getDateHeureEnvoi().toString(), estMoi);
            }
        }
        defilerVersLeBas();
    }

    private void afficherMessageRecu(Message message) {
        String expediteur = message.getSender().getUserName();

        if (expediteur.equals(selectedMember)) {
            //  La conversation est ouverte → affiche directement
            ajouterBulle(expediteur, message.getMessage(),
                    message.getDateHeureEnvoi().toString(), false);
            defilerVersLeBas();
        } else {
            //  Conversation pas ouverte → affiche 🔴 dans la liste
            boolean dejaPresent = listeMembres.getItems().stream()
                    .anyMatch(u -> u.replace(" 🔴", "").trim().equals(expediteur));

            if (!dejaPresent) {
                // Ajoute l'expéditeur s'il n'est pas encore dans la liste
                listeMembres.getItems().add(expediteur + " 🔴");
            } else {
                listeMembres.getItems().replaceAll(u -> {
                    String clean = u.replace(" 🔴", "").trim();
                    return clean.equals(expediteur) ? expediteur + " 🔴" : u;
                });
            }
        }
    }

    @FXML
    public void voirTousMembres() {
        ServerConnection.getInstance().send(new Packet(Packet.Type.GET_ALL_MEMBERS, null));
    }

    private void afficherTousMembres(List<User> membres) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Liste complète des membres");
        dialog.setHeaderText("👥 Tous les membres inscrits (" + membres.size() + ")");
        ListView<String> liste = new ListView<>();
        for (User u : membres) {
            String statut = u.getStatus() == User.Status.ONLINE ? " 🟢 En ligne" : " 🔴 Hors ligne";
            liste.getItems().add(u.getUserName() + statut);
        }
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

        User receiver = new User(selectedMember, null, User.Role.MEMBRE);
        Message message = new Message(currentUser, receiver, contenu);
        ServerConnection.getInstance().send(new Packet(Packet.Type.MESSAGE, message));

        ajouterBulle(currentUser.getUserName(), contenu,
                java.time.LocalDateTime.now().toString(), true);
        champMessage.clear();
        defilerVersLeBas();
    }

    private void ajouterBulle(String expediteur, String contenu,
                              String heure, boolean estMoi) {
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
        ServerConnection.getInstance().send(new Packet(Packet.Type.LOGOUT, null));
        ServerConnection.getInstance().disconnect();
        allerLogin();
    }

    private void gererDeconnexion() {
        afficherAlerte("❌ Connexion perdue avec le serveur.");
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