package ensetproject.chatbotedu.service;

import ensetproject.chatbotedu.model.ChatMessage;
import ensetproject.chatbotedu.model.RAGResponse;
import ensetproject.chatbotedu.utils.MessageFormatter;
import ensetproject.chatbotedu.database.DatabaseConnection;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    // Charger l'historique des messages depuis la base de données
    public List<ChatMessage> loadChatHistory(ObservableList<String> historyMessages, ObservableList<String> chatMessages) {
        List<ChatMessage> messages = new ArrayList<>();
        String query = "SELECT sender, message, timestamp FROM chat_history ORDER BY timestamp ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");

                ChatMessage chatMessage = new ChatMessage(sender, message, timestamp);
                messages.add(chatMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // Ajouter un message de l'utilisateur
    public void addUserMessage(String userMessage, ObservableList<String> chatMessages, ObservableList<String> historyMessages) {
        String timestamp = MessageFormatter.formatTimestamp(System.currentTimeMillis()); // Utiliser une méthode de formatage pour l'heure
        ChatMessage chatMessage = new ChatMessage("Vous", userMessage, timestamp);
        saveMessage(chatMessage);
    }

    // Ajouter la réponse du chatbot
    public void addChatbotResponse(String chatbotResponse) {
        String timestamp = MessageFormatter.formatTimestamp(System.currentTimeMillis());
        ChatMessage chatMessage = new ChatMessage("Chatbot", chatbotResponse, timestamp);
        saveMessage(chatMessage);
    }

    // Sauvegarder un message dans la base de données
    private void saveMessage(ChatMessage message) {
        String query = "INSERT INTO chat_history (sender, message, timestamp) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, message.getSender());
            stmt.setString(2, message.getMessage());
            stmt.setString(3, message.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Traiter le message de l'utilisateur et générer une réponse
    public RAGResponse processMessage(String userMessage, File selectedFile, File selectedImage) {
       return null ;
    }

    // Confirmer la suppression de l'historique
    public boolean confirmDeleteHistory() {
        // Logique de confirmation (tu peux personnaliser cela en fonction de l'UI)
        return true;  // On assume que l'utilisateur confirme directement la suppression
    }

    // Supprimer tous les messages de l'historique de la base de données
    public void deleteChatHistory() {
        String query = "DELETE FROM chat_history";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addChatbotResponse(RAGResponse ragResponse, ObservableList<String> chatMessages, TextArea documentTextArea) {
    }
}
