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
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import ensetproject.chatbotedu.database.DatabaseConnection;
import okhttp3.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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

    @FXML
    private TextArea documentTextArea;


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

        if (userMessage.isEmpty() && selectedFile == null && selectedImage == null) {
            chatMessages.add("Chatbot : Veuillez entrer un message ou sélectionner un fichier.");
            return;
        }

        // Ajouter le message utilisateur à la liste de chat
        String formattedUserMessage = formatMessageToMaxLength(userMessage);
        chatMessages.add("Vous : " + formattedUserMessage);

        // Sauvegarder le message utilisateur dans la base de données
        saveMessageToDatabase(userMessage, "user");
        RAGResponse ragResponse ;
        // Envoyer le fichier sélectionné (s'il existe) et le message
        if (selectedFile != null) {
            ragResponse = sendFileToServer(selectedFile, userMessage);
        } else if (selectedImage != null) {
            ragResponse = sendFileToServer(selectedImage, userMessage);
        }
        else { // Générer la réponse du chatbot via le serveur Python
             ragResponse = generateResponseFromServer(userMessage);
        }

        if (ragResponse != null) {
            // Récupérer la réponse et les documents consultés
            String chatbotResponse = ragResponse.getResponse();
            List<String> consultedDocuments = ragResponse.getConsultedDocuments();

            // Convertir la réponse en texte lisible
            chatbotResponse = convertUnicodeToReadable(chatbotResponse);

            // Formater la réponse pour qu'elle n'excède pas 100 caractères par ligne
            String formattedResponse = formatMessageToMaxLength(chatbotResponse);

            // Ajouter le message du chatbot à la liste
            chatMessages.add("Chatbot : " + formattedResponse);

            // Sauvegarder la réponse du chatbot dans la base de données
            saveMessageToDatabase(chatbotResponse, "chatbot");

            // Ajouter les documents consultés au TextArea
            if (!consultedDocuments.isEmpty()) {
                String documentsText = String.join("\n", consultedDocuments);
                documentTextArea.setText(documentsText); // Mettre à jour le TextArea
            } else {
                documentTextArea.setText("Aucun document consulté."); // Message par défaut
            }

            // Ajouter à l'historique
            String summary = createSummaryFromMessage(formattedUserMessage);
            historyMessages.add(summary); // Ajouter au message d'historique
        }
         else {
            chatMessages.add("Chatbot : Une erreur s'est produite lors de la génération de la réponse.");
        }

        // Réinitialiser les labels et les fichiers après l'envoi
        resetFileSelection();
        resetImageSelection();
        // Effacer l'aire de texte
        chatTextArea.clear();
    }


    // Méthode pour afficher un message de confirmation avant de supprimer l'historique
    public void handleDeleteHistory() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer l'historique ?");
        confirmationAlert.setContentText("Cette action est irréversible.");

        // Action si l'utilisateur confirme
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteChatHistory();
                historyMessages.clear();  // Vider la vue de l'historique dans l'interface utilisateur
                chatMessages.clear();
            }
        });
    }

    // Méthode pour supprimer l'historique de la base de données
    private void deleteChatHistory() {
        String query = "DELETE FROM chat_history";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class RAGResponse {
        private final String response;
        private final List<String> consultedDocuments;

        public RAGResponse(String response, List<String> consultedDocuments) {
            this.response = response;
            this.consultedDocuments = consultedDocuments;
        }

        public String getResponse() {
            return response;
        }

        public List<String> getConsultedDocuments() {
            return consultedDocuments;
        }
    }

    public RAGResponse generateResponseFromServer(String question) {
        try {
            // Configurer la connexion HTTP
            URL url = new URL("http://localhost:5000/rag"); // Adresse du serveur Python
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Préparer la requête JSON
            String jsonInput = "{\"query\": \"" + question + "\"}";

            // Envoyer les données
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lire la réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    StringBuilder responseBuilder = new StringBuilder();
                    while (scanner.hasNext()) {
                        responseBuilder.append(scanner.nextLine());
                    }

                    // Convertir la réponse JSON en objet RAGResponse
                    return parseResponse(responseBuilder.toString());
                }
            } else {
                System.err.println("Erreur serveur : " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private RAGResponse parseResponse(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);

        // Extraire la réponse principale
        String response = jsonObject.getString("response");

        // Extraire les documents consultés
        JSONArray consultedDocumentsArray = jsonObject.getJSONArray("consulted_documents");
        List<String> consultedDocuments = new ArrayList<>();
        for (int i = 0; i < consultedDocumentsArray.length(); i++) {
            consultedDocuments.add(consultedDocumentsArray.getString(i));
        }

        // Retourner un objet RAGResponse contenant les deux parties
        return new RAGResponse(response, consultedDocuments);
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
            System.out.println("Fichier sélectionné : " + selectedImage.getAbsolutePath());
            System.out.println("Taille du fichier : " + selectedImage.length());
        } else {
            System.err.println("Aucun fichier sélectionné.");
        }
    }

    public RAGResponse sendFileToServer(File file, String message) {
        if (file == null || !file.exists() || file.length() == 0) {
            System.err.println("Le fichier est introuvable ou vide.");
            return null;
        }

        // Créez un client OkHttp avec un délai de connexion et de lecture plus long
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Augmenter le délai de connexion
                .readTimeout(60, TimeUnit.SECONDS)     // Augmenter le délai de lecture
                .writeTimeout(60, TimeUnit.SECONDS)    // Augmenter le délai d'écriture
                .build();

        try {
            // Préparer le corps du fichier
            RequestBody fileBody = RequestBody.create(file, MediaType.parse(getMimeType(file)));

            // Construire le corps multipart/form-data
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.getName(), fileBody);

            // Ajouter le message si fourni
            if (message != null && !message.trim().isEmpty()) {
                multipartBuilder.addFormDataPart("message", message);
            }

            RequestBody requestBody = multipartBuilder.build();

            // Créer la requête HTTP
            Request request = new Request.Builder()
                    .url("http://localhost:5000/rag") // Remplacez par l'URL de votre API
                    .post(requestBody)
                    .build();

            // Envoyer la requête et traiter la réponse
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    return parseResponse(responseBody);
                } else {
                    System.err.println("Erreur serveur : " + response.code() + " - " + response.message());
                    if (response.body() != null) {
                        System.err.println("Détails : " + response.body().string());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtenir le type MIME du fichier
    private String getMimeType(File file) {
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    // Méthode pour envoyer l'image sélectionnée au serveur
    @FXML
    public void handleSendImage() {
        String message = chatTextArea.getText().trim();  // Récupère le message de l'utilisateur
        if (selectedImage != null) {  // Vérifie qu'un fichier a été sélectionné
            sendFileToServer(selectedImage, message);  // Envoie l'image avec le message
        } else {
            System.out.println("Aucune image sélectionnée.");
        }
    }


    // Méthode pour envoyer le fichier sélectionné au serveur
    @FXML
    public void handleSendFile() {
        String message = chatTextArea.getText().trim();  // Récupère le message de l'utilisateur
        if (selectedFile != null) {  // Vérifie qu'un fichier a été sélectionné
            sendFileToServer(selectedFile, message);  // Envoie le fichier avec le message
        } else {
            System.out.println("Aucun fichier sélectionné.");
        }
    }


    // Réinitialiser le label et la variable du fichier
    private void resetFileSelection() {
        selectedFile = null;
        fileLabel.setText("Aucun fichier sélectionné");
    }

    // Réinitialiser le label et la variable de l'image
    private void resetImageSelection() {
        selectedImage = null;
        imageLabel.setText("Aucune image sélectionnée");
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
