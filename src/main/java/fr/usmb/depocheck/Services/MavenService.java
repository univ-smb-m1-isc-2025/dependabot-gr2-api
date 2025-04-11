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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MavenService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GithubService githubService;

    public List<DependencieObject> getDependenciesMaven(String username, String token, String repoName, String targetBranch) throws IOException {
        logger.info("Récupération des dépendances Maven pour le dépôt {}/{}, branche {}", username, repoName, targetBranch);
        String pomXml = githubService.getPomXml(username, token, repoName, targetBranch);
        return parseDependenciesMaven(pomXml);
    }

    public List<DependencieObject> parseDependenciesMaven(String pomXmlContent) {
        logger.debug("Analyse du contenu du fichier pom.xml");
        List<DependencieObject> dependencies = new ArrayList<>();

        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new StringReader(pomXmlContent));

            if (model.getDependencies() != null) {
                logger.debug("Nombre de dépendances trouvées: {}", model.getDependencies().size());
                for (org.apache.maven.model.Dependency dependency : model.getDependencies()) {
                    String name = dependency.getGroupId() + ":" + dependency.getArtifactId();
                    String version = dependency.getVersion();
                    String scope = dependency.getScope();
                    String type = dependency.getType() != null ? dependency.getType() : "jar";

                    dependencies.add(new DependencieObject(name, version, scope, type));
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'analyse du pom.xml", e);
            return dependencies;
        }

        logger.info("Analyse terminée avec {} dépendances trouvées", dependencies.size());
        return dependencies;
    }

    public List<DependencieObject> getUpdatedDependencies(List<DependencieObject> oldDependencies) {
        logger.info("Vérification des mises à jour pour {} dépendances", oldDependencies.size());
        List<DependencieObject> updatedDependencies = new ArrayList<>();

        for (DependencieObject oldDep : oldDependencies) {
            String name = oldDep.getName();
            String version = oldDep.getVersion();

            // Check if the dependency is up-to-date
            String updatedVersion = getLastMavenDependencyVersion(name, version);
            if (!updatedVersion.equals(version)) {
                logger.debug("Mise à jour disponible pour {} : {} -> {}", name, version, updatedVersion);
                updatedDependencies.add(new DependencieObject(name, updatedVersion, oldDep.getScope(), oldDep.getType()));
            }
        }

        logger.info("{} dépendances ont des mises à jour disponibles", updatedDependencies.size());
        return updatedDependencies;
    }

    public String getLastMavenDependencyVersion(String name, String currentVersion) {
        logger.debug("Recherche de la dernière version pour {}, version actuelle: {}", name, currentVersion);
        try {
            // Split dependency name into groupId and artifactId
            String[] parts = name.split(":");
            if (parts.length < 2) {
                logger.warn("Format de dépendance invalide: {}", name);
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

            logger.debug("URL de requête Maven Central: {}", url);

            // Create client and request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Send request and parse response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("Réponse Maven Central: {} {}", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                // Parse JSON response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode docs = root.path("response").path("docs");

                // Check if the response contains any documents
                if (docs.isArray() && docs.size() > 0) {
                    String latestVersion = docs.get(0).path("latestVersion").asText();
                    logger.info("Dernière version pour {}: {}", name, latestVersion);
                    return latestVersion;
                } else {
                    logger.warn("Aucune version trouvée pour {}", name);
                    return currentVersion; // No versions found
                }
            } else {
                logger.error("Erreur lors de l'appel à Maven Central: {}", response.statusCode());
                return currentVersion; // Error in response
            }
        } catch (Exception e) {
            logger.error("Exception lors de la recherche de la dernière version pour {}", name, e);
            return currentVersion;
        }
    }
}
