package ensetproject.chatbotedu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/chatbot_edu";
    private static final String USER = "root"; // Par défaut sur WAMP
    private static final String PASSWORD = ""; // Par défaut vide sur WAMP

    public static Connection getConnection() {
        try {
            // Charger le driver MySQL (optionnel avec versions récentes de JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connexion à la base de données
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
