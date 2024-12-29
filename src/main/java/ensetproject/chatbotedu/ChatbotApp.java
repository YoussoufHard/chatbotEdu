package ensetproject.chatbotedu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;

public class ChatbotApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Vérification du chemin du fichier FXML
        String fxmlPath = "/ensetproject/chatbotedu/fxml/chat-view.fxml";
        URL fxmlLocation = getClass().getResource(fxmlPath);

        if (fxmlLocation == null) {
            System.err.println("Erreur : Fichier FXML introuvable à l'emplacement : " + fxmlPath);
            return;
        } else {
            System.out.println("Fichier FXML trouvé à : " + fxmlLocation);
        }

        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        AnchorPane root = loader.load();

        // Créer la scène et l'ajouter au stage
        Scene scene = new Scene(root);
        // Appliquer le CSs
        scene.getStylesheets().add(getClass().getResource("/ensetproject/chatbotedu/styles/chat.css").toExternalForm());
        primaryStage.setTitle("Chatbot Educatif");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
