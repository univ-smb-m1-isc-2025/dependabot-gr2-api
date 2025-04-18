package fr.usmb.depocheck;

import fr.usmb.depocheck.Objects.DependencieObject;
import fr.usmb.depocheck.Services.GithubService;
import fr.usmb.depocheck.Services.MavenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MavenServiceTest {

    @Mock
    private GithubService githubService;

    @InjectMocks
    @Spy
    private MavenService mavenService;

    @Test
    public void testParseDependenciesMaven() {
        // Arrange
        String pomXml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.springframework.boot</groupId>\n" +
                "      <artifactId>spring-boot-starter-web</artifactId>\n" +
                "      <version>2.6.3</version>\n" +
                "    </dependency>\n" +
                "    <dependency>\n" +
                "      <groupId>org.junit.jupiter</groupId>\n" +
                "      <artifactId>junit-jupiter</artifactId>\n" +
                "      <version>5.8.2</version>\n" +
                "      <scope>test</scope>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "</project>";

        // Act
        List<DependencieObject> dependencies = mavenService.parseDependenciesMaven(pomXml);

        // Assert
        assertEquals(2, dependencies.size());
        assertEquals("org.springframework.boot:spring-boot-starter-web", dependencies.get(0).getName());
        assertEquals("2.6.3", dependencies.get(0).getVersion());
        assertEquals("org.junit.jupiter:junit-jupiter", dependencies.get(1).getName());
        assertEquals("5.8.2", dependencies.get(1).getVersion());
        assertEquals("test", dependencies.get(1).getScope());
    }

    @Test
    public void testParseDependenciesMaven_InvalidXml() {
        // Arrange
        String invalidPom = "Invalid XML content";

        // Act
        List<DependencieObject> dependencies = mavenService.parseDependenciesMaven(invalidPom);

        // Assert
        assertTrue(dependencies.isEmpty());
    }

    @Test
    public void testGetDependenciesMaven() throws IOException {
        // Arrange
        String username = "testUser";
        String token = "testToken";
        String repoName = "testRepo";
        String branch = "main";

        String pomXml = "<project>\n" +
                "  <dependencies>\n" +
                "    <dependency>\n" +
                "      <groupId>org.springframework.boot</groupId>\n" +
                "      <artifactId>spring-boot-starter</artifactId>\n" +
                "      <version>2.5.0</version>\n" +
                "    </dependency>\n" +
                "  </dependencies>\n" +
                "</project>";

        when(githubService.getPomXml(username, token, repoName, branch)).thenReturn(pomXml);

        // Faire un spy partiel pour éviter d'appeler la vraie méthode parseDependenciesMaven
        List<DependencieObject> expectedDeps = List.of(
                new DependencieObject("org.springframework.boot:spring-boot-starter", "2.5.0", null, "jar")
        );
        doReturn(expectedDeps).when(mavenService).parseDependenciesMaven(pomXml);

        // Act
        List<DependencieObject> result = mavenService.getDependenciesMaven(username, token, repoName, branch);

        // Assert
        verify(githubService).getPomXml(username, token, repoName, branch);
        verify(mavenService).parseDependenciesMaven(pomXml);
        assertEquals(expectedDeps, result);
    }

    @Test
    public void testGetUpdatedDependencies() {
        // Arrange
        List<DependencieObject> oldDeps = Arrays.asList(
                new DependencieObject("org.springframework:spring-core", "5.3.5", "compile", "jar"),
                new DependencieObject("org.mockito:mockito-core", "3.9.0", "test", "jar")
        );

        doReturn("5.3.20").when(mavenService).getLastMavenDependencyVersion("org.springframework:spring-core", "5.3.5");
        doReturn("3.9.0").when(mavenService).getLastMavenDependencyVersion("org.mockito:mockito-core", "3.9.0");

        // Act
        List<DependencieObject> updatedDeps = mavenService.getUpdatedDependencies(oldDeps);

        // Assert
        assertEquals(1, updatedDeps.size());
        assertEquals("org.springframework:spring-core", updatedDeps.get(0).getName());
        assertEquals("5.3.20", updatedDeps.get(0).getVersion());
    }

    @Test
    public void testGetLastMavenDependencyVersion_InvalidName() {
        // Arrange
        String invalidName = "invalid-name-format";
        String currentVersion = "1.0.0";

        // Act
        String result = mavenService.getLastMavenDependencyVersion(invalidName, currentVersion);

        // Assert
        assertEquals(currentVersion, result);
    }

    @Test
    public void testGetLastMavenDependencyVersion_ExceptionHandling() {
        // Arrange - utiliser directement le spy sans mock supplémentaire
        String name = "org.example:library";
        String currentVersion = "1.2.3";

        // Option 1: Utiliser un nom de package invalide qui provoque une exception interne
        String invalidFormat = "format-invalide-sans-separateur";

        // Act
        String result = mavenService.getLastMavenDependencyVersion(invalidFormat, currentVersion);

        // Assert - vérifier que la méthode renvoie la version actuelle en cas d'erreur
        assertEquals(currentVersion, result);

        // Option 2 (alternative) : tester avec un nom valide mais utiliser doReturn pour simuler
        // l'impossibilité d'exécuter la requête HTTP
        HttpClient mockClient = mock(HttpClient.class);
        // Cette solution nécessiterait une refactorisation pour injecter le client HTTP
    }
}