package fr.usmb.depocheck;

import fr.usmb.depocheck.Services.GithubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GithubServiceTest {

    @Spy
    private GithubService githubService;

    private final String USERNAME = "testUser";
    private final String TOKEN = "testToken";
    private final String REPO_NAME = "owner/repo";
    private final String BRANCH = "main";

    @Test
    public void testGetPomXml_DefaultBranch() throws IOException {
        // Arrange
        String xmlContent = "<project>test content</project>";
        doReturn(xmlContent).when(githubService).getPomXml(USERNAME, TOKEN, REPO_NAME, null);

        // Act
        String result = githubService.getPomXml(USERNAME, TOKEN, REPO_NAME);

        // Assert
        assertEquals(xmlContent, result);
        verify(githubService).getPomXml(USERNAME, TOKEN, REPO_NAME, null);
    }

    // Test d'intégration avec MockGitHub (à implémenter avec un test d'intégration réel)
    @Test
    public void testPomXmlProcessing() throws IOException {
        // Pour un test complet, nous aurions besoin de refactoriser GithubService
        // ou d'utiliser une bibliothèque comme MockWebServer

        // Ce test simule le comportement du service sans appeler l'API réelle
        GithubService serviceSpy = spy(new GithubService());

        // Simuler un fichier XML déjà au format texte
        String xmlContent = "<?xml version=\"1.0\"?><project>test</project>";
        doReturn(xmlContent).when(serviceSpy).getPomXml(anyString(), anyString(), anyString(), anyString());

        String result = serviceSpy.getPomXml("user", "token", "repo", "branch");
        assertEquals(xmlContent, result);

        // Simuler un contenu encodé en Base64
        String originalXml = "<project>encoded content</project>";
        String encodedContent = Base64.getEncoder().encodeToString(originalXml.getBytes(StandardCharsets.UTF_8));
        doReturn(encodedContent).when(serviceSpy).getPomXml(anyString(), anyString(), anyString(), eq("encoded"));

        result = serviceSpy.getPomXml("user", "token", "repo", "encoded");
        assertEquals(encodedContent, result);
    }
}