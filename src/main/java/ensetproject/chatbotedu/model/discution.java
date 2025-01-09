package ensetproject.chatbotedu.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class discution {
    private int id;
    private String title;
    private LocalDateTime createdAt;

    // Constructeur, getters et setters
    public discution(int id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title + " (" + createdAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + ")";
    }
}
