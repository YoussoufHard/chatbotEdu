package ensetproject.chatbotedu.utils;

public class MessageFormatter {
    public static String formatMessage(String message, int maxLength) {
        StringBuilder formatted = new StringBuilder();
        int lineLength = 0;

        for (String word : message.split(" ")) {
            if (lineLength + word.length() > maxLength) {
                formatted.append("\n");
                lineLength = 0;
            }
            formatted.append(word).append(" ");
            lineLength += word.length() + 1;
        }
        return formatted.toString().trim();
    }

    public static String formatTimestamp(long l) {
        // le code ici
        return null;
    }
}
