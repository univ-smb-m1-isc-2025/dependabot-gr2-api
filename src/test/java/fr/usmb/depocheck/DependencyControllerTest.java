package fr.usmb.depocheck;

import fr.usmb.depocheck.DTO.*;
import fr.usmb.depocheck.Entities.Repository;
import fr.usmb.depocheck.Entities.User;
import fr.usmb.depocheck.Objects.DependencieObject;
import fr.usmb.depocheck.Objects.RepoObject;
import fr.usmb.depocheck.Repository.UserRepository;
import fr.usmb.depocheck.RepoType;
import fr.usmb.depocheck.Services.MavenService;
import fr.usmb.depocheck.Services.RepositoryService;
import fr.usmb.depocheck.adapters.api.DependencyController;
import fr.usmb.depocheck.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DependencyControllerTest {

    @Mock
    private MavenService mavenService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DependencyController dependencyController;

    @Test
    public void testCheckDependencies() throws Exception {
        // Arrange
        RepoObject repo = new RepoObject();
        repo.setName("owner/repo");
        repo.setUsername("user");
        repo.setToken("token");
        repo.setBranch("main");
        repo.setType(RepoType.MAVEN);

        List<DependencieObject> dependencies = Arrays.asList(
                new DependencieObject("group:artifact1", "1.0.0", "compile", "jar"),
                new DependencieObject("group:artifact2", "2.0.0", "test", "jar")
        );

        when(mavenService.getDependenciesMaven("user", "token", "owner/repo", "main"))
                .thenReturn(dependencies);

        // Act
        ResponseEntity<?> response = dependencyController.checkDependencies(repo);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof ResponseDependecyRequest);
        ResponseDependecyRequest result = (ResponseDependecyRequest) response.getBody();
        assertEquals("Dependencies checked", result.getResponse());
        assertEquals(2, result.getDependencies().size());
    }

    @Test
    public void testUpdateDependencies() throws Exception {
        // Arrange
        List<DependencieObject> dependencies = Arrays.asList(
                new DependencieObject("group:artifact1", "1.0.0", "compile", "jar")
        );

        UpdateDependenciesRequest request = new UpdateDependenciesRequest();
        request.setRepoType(RepoType.MAVEN);
        request.setDependencies(dependencies);

        when(mavenService.getUpdatedDependencies(dependencies))
                .thenReturn(Arrays.asList(
                        new DependencieObject("group:artifact1", "1.5.0", "compile", "jar")
                ));

        // Act
        ResponseEntity<?> response = dependencyController.updateDependencies(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof ResponseDependecyRequest);
        ResponseDependecyRequest result = (ResponseDependecyRequest) response.getBody();
        assertEquals("Dependencies updated", result.getResponse());
        assertEquals("1.5.0", result.getDependencies().get(0).getVersion());
    }

    @Test
    public void testGetUserRepositories() throws Exception {
        // Arrange
        String authHeader = "Bearer valid-token";
        User user = new User(1L, "testUser", "test@example.com", "password");
        Repository repo = new Repository();
        repo.setId(1L);
        repo.setName("test-repo");

        when(jwtUtil.getUsernameFromToken("valid-token")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(repositoryService.getUserRepositories(1L)).thenReturn(Collections.singletonList(repo));

        // Act
        ResponseEntity<?> response = dependencyController.getUserRepositories(authHeader);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof ResponseRepository);
        ResponseRepository result = (ResponseRepository) response.getBody();
        assertEquals("User repositories retrieved", result.getResponse());
        assertEquals(1, result.getRepositories().size());
    }

    @Test
    public void testAddRepository() throws Exception {
        // Arrange
        String authHeader = "Bearer valid-token";
        AddRepositoryRequest request = new AddRepositoryRequest();
        request.setName("test-repo");
        request.setUrl("https://github.com/user/repo");
        request.setBranch("main");
        request.setType(RepoType.MAVEN);

        User user = new User(1L, "testUser", "test@example.com", "password");
        Repository savedRepo = new Repository();
        savedRepo.setId(1L);
        savedRepo.setName("test-repo");

        when(jwtUtil.getUsernameFromToken("valid-token")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(repositoryService.saveRepository(any(Repository.class), eq(1L))).thenReturn(savedRepo);

        // Act
        ResponseEntity<?> response = dependencyController.addRepository(authHeader, request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof ResponseRepository);
        ResponseRepository result = (ResponseRepository) response.getBody();
        assertEquals("Repository added successfully", result.getResponse());
        assertEquals(1, result.getRepositories().size());
    }
}