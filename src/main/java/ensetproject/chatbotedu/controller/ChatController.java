package ensetproject.chatbotedu.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import ensetproject.chatbotedu.database.DatabaseConnection;
import org.apache.commons.lang3.StringEscapeUtils;

public class ChatController {

    @FXML
    private ListView<String> historyListView;

    @FXML
    private ListView<String> chatListView;

    @FXML
    private TextArea chatTextArea;

    @FXML
    private Label fileLabel;

    @FXML
    private Label imageLabel;

    private ObservableList<String> historyMessages = FXCollections.observableArrayList();
    private ObservableList<String> chatMessages = FXCollections.observableArrayList();

    private File selectedFile;
    private File selectedImage;

    @FXML
    public void initialize() {
        historyListView.setItems(historyMessages);
        chatListView.setItems(chatMessages);

        // Custom cell factory for better message styling
        chatListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("Vous : ")) {
                        setStyle("-fx-text-fill: blue; -fx-alignment: CENTER-RIGHT;");
                    } else {
                        setStyle("-fx-text-fill: green; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

        // Charger l'historique des messages depuis la base de données
        loadChatHistory();
    }

    @FXML
    public void handleSendMessage() {
        String userMessage = chatTextArea.getText().trim();

        if (userMessage.isEmpty()) {
            chatMessages.add("Chatbot : Veuillez entrer un message valide.");
            return;
        }

        // Ajouter le message utilisateur à la liste de chat
        String formattedUserMessage = formatMessageToMaxLength(userMessage);
        chatMessages.add("Vous : " + formattedUserMessage);

        // Sauvegarder le message utilisateur dans la base de données
        saveMessageToDatabase(userMessage, "user");

        // Générer la réponse du chatbot via le serveur Python
        String response = generateResponseFromServer(userMessage);

        if (response != null) {
            // Convertir la réponse en texte lisible
            response = convertUnicodeToReadable(response);

            // Formater la réponse pour qu'elle n'excède pas 100 caractères par ligne
            String formattedResponse = formatMessageToMaxLength(response);

            // Ajouter le message du chatbot à la liste
            chatMessages.add("Chatbot : " + formattedResponse);

            // Sauvegarder la réponse du chatbot dans la base de données
            saveMessageToDatabase(response, "chatbot");

            // Ajouter à l'historique
            // Extraire un résumé ou des mots-clés pour l'historique
            String summary = createSummaryFromMessage(formattedUserMessage);
            historyMessages.add(summary);  // Ajouter au message d'historique
        } else {
            chatMessages.add("Chatbot : Une erreur s'est produite lors de la génération de la réponse.");
        }

        // Effacer l'aire de texte
        chatTextArea.clear();
    }

    private String generateResponseFromServer(String question) {
        try {
            // Configurer la connexion HTTP
            URL url = new URL("http://localhost:5000/generate"); // Adresse du serveur Python
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Préparer la requête JSON
            String jsonInput = "{\"prompt\": \"" + question + "\"}";

            // Envoyer les données
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lire la réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    // Extraire et retourner la réponse du chatbot
                    return parseResponse(response.toString());
                }
            } else {
                System.err.println("Erreur serveur : " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseResponse(String jsonResponse) {
        // Extraire la réponse du JSON retourné
        if (jsonResponse.contains("\"response\"")) {
            int startIndex = jsonResponse.indexOf("\"response\":") + 12;
            int endIndex = jsonResponse.lastIndexOf("\"");
            return jsonResponse.substring(startIndex, endIndex);
        }
        return "Erreur dans le traitement de la réponse.";
    }

    // Convertir les réponses Unicode en texte lisible
    private String convertUnicodeToReadable(String text) {
        // Utilisation de StringEscapeUtils pour convertir les séquences Unicode en texte lisible
        return StringEscapeUtils.unescapeJava(text);
    }

    // Méthode pour sauvegarder les messages dans la base de données
    private void saveMessageToDatabase(String message, String sender) {
        String query = "INSERT INTO chat_history (message, sender) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, message);
            stmt.setString(2, sender);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour charger l'historique des messages depuis la base de données
    private void loadChatHistory() {
        String query = "SELECT * FROM chat_history ORDER BY timestamp ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                // Formater la réponse pour qu'elle n'excède pas 100 caractères par ligne
                String formattedMess = formatMessageToMaxLength(message);
                chatMessages.add((sender.equals("user") ? "Vous : " : "Chatbot : ") + formattedMess);

                // Extraire un résumé ou des mots-clés pour l'historique
                String summary = createSummaryFromMessage(message);
                if(sender.equals("user"))
                  historyMessages.add(summary);  // Ajouter au message d'historique
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String createSummaryFromMessage(String message) {
        // Si vous voulez juste les 5 premiers mots du message comme résumé :
        String[] words = message.split("\\s+");
        StringBuilder summary = new StringBuilder();

        // Limiter à un nombre de mots (par exemple, les 4 premiers mots)
        int maxWords = 4;  // Limite de mots pour le résumé
        for (int i = 0; i < Math.min(words.length, maxWords); i++) {
            summary.append(words[i]).append(" ");
        }

        // Ajouter "..." à la fin pour indiquer qu'il s'agit d'un résumé
        if (words.length > maxWords) {
            summary.append("...");
        }

        return summary.toString().trim();
    }


    // Méthode pour sélectionner un fichier
    @FXML
    public void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            fileLabel.setText("Fichier sélectionné : " + selectedFile.getName());
        }
    }

    // Méthode pour sélectionner une image
    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedImage = fileChooser.showOpenDialog(new Stage());
        if (selectedImage != null) {
            imageLabel.setText("Image sélectionnée : " + selectedImage.getName());
        }
    }

    private String formatMessageToMaxLength(String message) {
        StringBuilder formattedMessage = new StringBuilder();
        int maxLength = 100;

        while (message.length() > maxLength) {
            // Chercher la dernière occurrence d'un espace avant la limite
            int lastSpaceIndex = message.lastIndexOf(' ', maxLength);

            // Si aucun espace trouvé, couper strictement à 100 caractères
            if (lastSpaceIndex == -1) {
                lastSpaceIndex = maxLength;
            }

            // Ajouter la portion coupée du message
            formattedMessage.append(message, 0, lastSpaceIndex).append("\n");
            message = message.substring(lastSpaceIndex).trim();  // Enlever l'espace et continuer avec le reste du message
        }

        // Ajouter le reste du message (si existant)
        formattedMessage.append(message);

        return formattedMessage.toString();
    }

}
