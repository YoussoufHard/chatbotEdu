package ensetproject.chatbotedu.controller;

import ensetproject.chatbotedu.model.ChatMessage;
import ensetproject.chatbotedu.model.RAGResponse;
import ensetproject.chatbotedu.service.ChatService;
import ensetproject.chatbotedu.service.FileService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class m__controller {

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

    private final ChatService chatService = new ChatService();
    private final FileService fileService = new FileService();

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
                    setStyle(item.startsWith("Vous : ") ? "-fx-text-fill: blue; -fx-alignment: CENTER-RIGHT;" : "-fx-text-fill: green; -fx-alignment: CENTER-LEFT;");
                }
            }
        });

        chatService.loadChatHistory(historyMessages, chatMessages);
    }

    @FXML
    public void handleSendMessage() {
        String userMessage = chatTextArea.getText().trim();

        if (userMessage.isEmpty() && selectedFile == null && selectedImage == null) {
            chatMessages.add("Chatbot : Veuillez entrer un message ou sélectionner un fichier.");
            return;
        }

        chatService.addUserMessage(userMessage, chatMessages, historyMessages);

        RAGResponse ragResponse = chatService.processMessage(userMessage, selectedFile, selectedImage);

        if (ragResponse != null) {
            chatService.addChatbotResponse(ragResponse, chatMessages, documentTextArea);
        } else {
            chatMessages.add("Chatbot : Une erreur s'est produite lors de la génération de la réponse.");
        }

        resetFileSelection();
        resetImageSelection();
        chatTextArea.clear();
    }

    @FXML
    public void handleDeleteHistory() {
        if (chatService.confirmDeleteHistory()) {
            chatService.deleteChatHistory();
            historyMessages.clear();
            chatMessages.clear();
        }
    }

    @FXML
    public void handleChooseFile() {
        selectedFile = fileService.chooseFile(new Stage());
        if (selectedFile != null) {
            fileLabel.setText("Fichier sélectionné : " + selectedFile.getName());
        }
    }

    @FXML
    public void handleChooseImage() {
        selectedImage = fileService.chooseImage(new Stage());
        if (selectedImage != null) {
            imageLabel.setText("Image sélectionnée : " + selectedImage.getName());
        }
    }

    private void resetFileSelection() {
        selectedFile = null;
        fileLabel.setText("");
    }

    private void resetImageSelection() {
        selectedImage = null;
        imageLabel.setText("");
    }
}
