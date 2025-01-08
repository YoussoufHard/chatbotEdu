package ensetproject.chatbotedu.model;

import java.util.List;

public class RAGResponse {
    private String response;
    private List<String> consultedDocuments;

    public RAGResponse(String response, List<String> consultedDocuments) {
        this.response = response;
        this.consultedDocuments = consultedDocuments;
    }

    public String getResponse() { return response; }
    public List<String> getConsultedDocuments() { return consultedDocuments; }
}
