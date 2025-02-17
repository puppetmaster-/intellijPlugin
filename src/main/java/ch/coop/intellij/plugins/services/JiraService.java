package ch.coop.intellij.plugins.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JiraService {
    private final Project project;
    private final String apiUrl;
    private final String apiToken;

    public JiraService(Project project, String apiUrl, String apiToken) {
        this.project = project;
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
    }

    /**
     * Sucht nach Jira-Issues basierend auf einer Suchanfrage.
     *
     * @param query Die Suchanfrage.
     * @return Eine Liste von Jira-Issue-IDs.
     */
    public List<String> searchJiraIssues(String query) {
        List<String> issues = new ArrayList<>();

        if (apiUrl.isEmpty() || apiToken.isEmpty()) {
            return issues;
        }

        try {
            URL url = new URL(apiUrl + "/rest/api/2/search?jql=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                issues = parseJiraIssues(response.toString());
            } else {
                Messages.showErrorDialog(project, "Failed to search Jira issues: " + responseCode, "Error");
            }
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to search Jira issues: " + e.getMessage(), "Error");
        }

        return issues;
    }

    /**
     * Parst die JSON-Antwort von Jira und extrahiert die Issue-IDs.
     *
     * @param jsonResponse Die JSON-Antwort von Jira.
     * @return Eine Liste von Jira-Issue-IDs.
     */
    private List<String> parseJiraIssues(String jsonResponse) {
        // Hier könntest du die JSON-Antwort parsen und eine Liste von Jira-Issue-IDs zurückgeben
        // Dies ist ein vereinfachtes Beispiel, das die tatsächliche Implementierung erfordert
        List<String> issues = new ArrayList<>();
        issues.add("JIRA-1234");
        issues.add("JIRA-5678");
        return issues;
    }
}