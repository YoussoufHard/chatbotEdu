package ensetproject.chatbotedu.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
import org.commonmark.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class ChatController {

    @FXML
    private ListView<String> historyListView;

    @FXML
    private TextArea chatTextArea;

    @FXML
    private Label fileLabel;

    @FXML
    private Label imageLabel;

    @FXML
    private TextArea documentTextArea;

    @FXML
    private WebView chatWebView; // Pour format html
    private WebEngine chatEngine;

    @FXML
    private ProgressIndicator progressIndicator;

    // Stocker le contenu HTML du chat
    private StringBuilder chatContent ;

    private ObservableList<String> historyMessages = FXCollections.observableArrayList();
    private ObservableList<String> chatMessages = FXCollections.observableArrayList();

    private File selectedFile;
    private File selectedImage;

    @FXML
    public void initialize() {
        historyListView.setItems(historyMessages);

        chatEngine = chatWebView.getEngine();

        // Préparer le contenu HTML
        chatContent = new StringBuilder();
        chatContent.append("<html><head>")
                .append("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'>")
                .append("</head><body style='font-family: Arial; font-size: 14px;'>");

        // Charger le contenu initial
        chatEngine.loadContent(chatContent.toString() + "</body></html>");
        // Charger l'historique des messages depuis la base de données
        loadChatHistory();
    }

    // clas
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

        // Ajouter le message de l'utilisateur dans le chat
        addMessageToChat(userMessage, true);

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

        // Lorsque la tâche est terminée, masquer l'indicateur
        if (ragResponse != null) {
            // Récupérer la réponse et les documents consultés
            String chatbotResponse = ragResponse.getResponse();
            List<String> consultedDocuments = ragResponse.getConsultedDocuments();

            // Convertir le Markdown en HTML
            chatbotResponse = convertMarkdownToHtml(chatbotResponse);

            // Convertir la réponse en texte lisible
    //        chatbotResponse = convertUnicodeToReadable(chatbotResponse);

            // Formater la réponse pour qu'elle n'excède pas 100 caractères par ligne
            String formattedResponse = formatMessageToMaxLength(chatbotResponse);

            // Ajouter le message du chatbot à la liste
            chatMessages.add("Chatbot : " + formattedResponse);

            // Sauvegarder la réponse du chatbot dans la base de données
            saveMessageToDatabase(chatbotResponse, "chatbot");

            // Mettre à jour le WebView avec la réponse HTML
            addMessageToChat(chatbotResponse, false);

            // Lorsque la tâche est terminée, masquer l'indicateur
        //    Platform.runLater(() -> progressIndicator.setVisible(false));

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
                chatEngine.loadContent("<html><body></body></html>"); // Vider le WebView si nécessaire
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
            // Afficher l'indicateur de chargement
            progressIndicator.setVisible(true);
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

        // Initialiser un StringBuilder pour construire le contenu HTML
        StringBuilder historyContent = new StringBuilder("<html><head>")
                .append("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'>")
                .append("</head><body style='font-family: Arial; font-size: 14px;'>");

        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");

                // Formater le message pour ne pas dépasser 100 caractères par ligne
                String formattedMess = formatMessageToMaxLength(message);

                // Ajouter le message formaté au chat
                if (sender.equals("user")) {
                    // Message de l'utilisateur avec icône et style
                    historyContent.append("<p style='color: rgba(0, 0, 255, 0.7); font-style: italic; text-align: right;'>")
                            .append("<i class='fa fa-user' style='margin-right: 8px; color: rgba(0, 0, 255, 0.7);'></i>")
                            .append(formattedMess)
                            .append("</p>");
                } else {
                    // Message du chatbot avec icône et style
                    historyContent.append("<p>")
                            .append("<i class='fa fa-robot' style='margin-right: 8px; color: green;'></i>")
                            .append(formattedMess)
                            .append("</p>");
                }

                // Extraire un résumé pour l'historique
                String summary = createSummaryFromMessage(message);
                if (sender.equals("user")) {
                    historyMessages.add(summary);  // Ajouter au message d'historique
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Terminer le contenu HTML et le charger dans le WebView
        historyContent.append("</body></html>");
        chatEngine.loadContent(historyContent.toString());
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

    // Méthode pour convertir le Markdown en HTML
    public String convertMarkdownToHtml(String markdown) {
        // Créer un parser pour le Markdown
        Parser parser = Parser.builder().build();

        // Convertir le texte Markdown en un arbre de nœuds
        org.commonmark.node.Node document = parser.parse(markdown);

        // Créer un renderer pour convertir le document en HTML
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        // Convertir le document en HTML
        return renderer.render(document);
    }

    // Fonction pour ajouter un message au WebView
    public void addMessageToChat(String message, boolean isUser) {
        // Style du message pour l'utilisateur
        String formattedMessage;
        if (isUser) {
            formattedMessage = "<p style='color: rgba(0, 0, 255, 0.7); font-style: italic; text-align: right;'>"
                    + "<i class='fa fa-user' style='margin-right: 8px; color: rgba(0, 0, 255, 0.7);'></i>"
                    + message + "</p>";
        } else {
            // Style du message pour le chatbot
            formattedMessage = "<p>"
                    + "<i class='fa fa-robot' style='margin-right: 8px; color: green;'></i>"
                    + message + "</p>";
        }

        // Ajouter le message au contenu
        chatContent.append(formattedMessage);

        // Mettre à jour le contenu dans le WebView
        chatEngine.loadContent(chatContent.toString() + "</body></html>");
    }


}
