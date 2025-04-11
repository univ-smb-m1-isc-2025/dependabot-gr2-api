package fr.usmb.depocheck.Services;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class GithubService {

    public String getPomXml(String username, String token, String repoName, String targetBranch) throws IOException {
        GitHub github = new GitHubBuilder()
                .withOAuthToken(token, username)
                .build();

        GHRepository repo = github.getRepository(repoName);
        GHContent content;

        if (targetBranch != null && !targetBranch.isEmpty()) {
            // Get content from the specified branch
            content = repo.getFileContent("pom.xml", targetBranch);
        } else {
            // Get content from the default branch
            content = repo.getFileContent("pom.xml");
        }

        if (content.isFile()) {
            String rawContent = content.getContent();

            // First check if content looks like XML
            if (rawContent.trim().startsWith("<?xml") || rawContent.trim().startsWith("<project")) {
                return rawContent; // Already plain text
            }

            // Try to decode as base64
            try {
                byte[] contentBytes = Base64.getDecoder().decode(rawContent);
                return new String(contentBytes, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                // If decoding fails, return the raw content
                return rawContent;
            }
        }

        throw new IOException("Could not find pom.xml in repository");
    }

    public String getPomXml(String username, String token, String repoName) throws IOException {
        return getPomXml(username, token, repoName, null);
    }


}