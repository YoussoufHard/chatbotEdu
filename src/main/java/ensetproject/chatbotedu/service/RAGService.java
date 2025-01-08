package ensetproject.chatbotedu.service;

import ensetproject.chatbotedu.model.RAGResponse;
import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RAGService {
    private static final String SERVER_URL = "http://localhost:5000/rag";

    public RAGResponse getResponseFromServer(String userQuery) throws IOException {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("query", userQuery);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(SERVER_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            String chatbotResponse = jsonResponse.getString("response");
            JSONArray documentsArray = jsonResponse.getJSONArray("consulted_documents");
            List<String> documents = new ArrayList<>();
            for (int i = 0; i < documentsArray.length(); i++) {
                documents.add(documentsArray.getString(i));
            }
            return new RAGResponse(chatbotResponse, documents);
        }
    }
}
