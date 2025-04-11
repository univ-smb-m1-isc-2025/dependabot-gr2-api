package fr.usmb.depocheck.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.usmb.depocheck.Objects.DependencieObject;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MavenService {
    @Autowired
    private GithubService githubService;

    public List<DependencieObject> getDependenciesMaven(String username, String token, String repoName, String targetBranch) throws IOException {
        String pomXml = githubService.getPomXml(username, token, repoName, targetBranch);
        return parseDependenciesMaven(pomXml);
    }

    public List<DependencieObject> parseDependenciesMaven(String pomXmlContent) {
        List<DependencieObject> dependencies = new ArrayList<>();

        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new StringReader(pomXmlContent));

            if (model.getDependencies() != null) {
                for (org.apache.maven.model.Dependency dependency : model.getDependencies()) {
                    String name = dependency.getGroupId() + ":" + dependency.getArtifactId();
                    String version = dependency.getVersion();
                    String scope = dependency.getScope();
                    String type = dependency.getType() != null ? dependency.getType() : "jar";

                    dependencies.add(new DependencieObject(name, version, scope, type));
                }
            }
        } catch (Exception e) {
            return dependencies;
        }

        return dependencies;
    }

    public List<DependencieObject> getUpdatedDependencies(List<DependencieObject> oldDependencies) {
        List<DependencieObject> updatedDependencies = new ArrayList<>();
        for (DependencieObject oldDep : oldDependencies) {
            String name = oldDep.getName();
            String version = oldDep.getVersion();

            // Check if the dependency is up-to-date
            String updatedVersion = getLastMavenDependencyVersion(name, version);
            if (!updatedVersion.equals(version)) {
                updatedDependencies.add(new DependencieObject(name, updatedVersion, oldDep.getScope(), oldDep.getType()));
            }
        }
        return updatedDependencies;
    }

    public String getLastMavenDependencyVersion(String name, String currentVersion) {
        try {
            // Split dependency name into groupId and artifactId
            String[] parts = name.split(":");
            if (parts.length < 2) {
                return currentVersion;
            }

            String groupId = parts[0];
            String artifactId = parts[1];

            // Build URL with query parameters
            String baseUrl = "https://search.maven.org/solrsearch/select";
            String query = String.format("g:%s+AND+a:%s",
                    URLEncoder.encode(groupId, StandardCharsets.UTF_8),
                    URLEncoder.encode(artifactId, StandardCharsets.UTF_8));
            String url = baseUrl + "?q=" + query + "&rows=1&wt=json";

            // Create client and request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Send request and parse response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse JSON response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode docs = root.path("response").path("docs");

                if (docs.isArray() && docs.size() > 0) {
                    return docs.get(0).path("latestVersion").asText();
                } else {
                    return currentVersion; // No versions found
                }
            } else {
                return currentVersion; // Error in response
            }
        } catch (Exception e) {
            // Return the current version if any error occurs
            return currentVersion;
        }
    }
}
