package fr.usmb.depocheck;

import fr.usmb.depocheck.DTO.UpdateRepoDependencies;
import fr.usmb.depocheck.Entities.Repository;
import fr.usmb.depocheck.Entities.User;
import fr.usmb.depocheck.Objects.DependencieObject;
import fr.usmb.depocheck.Repository.RepositoryRepository;
import fr.usmb.depocheck.Repository.UserRepository;
import fr.usmb.depocheck.RepoType;
import fr.usmb.depocheck.Services.MavenService;
import fr.usmb.depocheck.Services.RepositoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepositoryServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MavenService mavenService;

    @InjectMocks
    private RepositoryService repositoryService;

    @Test
    public void testGetUserRepositories() {
        // Arrange
        Long userId = 1L;
        List<Repository> expectedRepos = Arrays.asList(new Repository(), new Repository());
        when(repositoryRepository.findByUserId(userId)).thenReturn(expectedRepos);

        // Act
        List<Repository> result = repositoryService.getUserRepositories(userId);

        // Assert
        assertEquals(expectedRepos, result);
        verify(repositoryRepository).findByUserId(userId);
    }

    @Test
    public void testSaveRepository() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Repository repository = new Repository();
        repository.setName("test-repo");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repositoryRepository.save(any(Repository.class))).thenReturn(repository);

        // Act
        Repository result = repositoryService.saveRepository(repository, userId);

        // Assert
        assertEquals(user, result.getUser());
        verify(userRepository).findById(userId);
        verify(repositoryRepository).save(repository);
    }

    @Test
    public void testSaveRepository_UserNotFound() {
        // Arrange
        Long userId = 1L;
        Repository repository = new Repository();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            repositoryService.saveRepository(repository, userId);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testCheckRepositoryDependencies_Maven() throws IOException {
        // Arrange
        Long repoId = 1L;
        Long userId = 1L;

        Repository repository = new Repository();
        repository.setType(RepoType.MAVEN);
        repository.setUsername("testUser");
        repository.setToken("testToken");
        repository.setName("owner/repo");
        repository.setBranch("main");

        List<DependencieObject> expectedDeps = Arrays.asList(
                new DependencieObject("group", "artifact", "1.0.0", "compile"),
                new DependencieObject("group", "artifact2", "2.0.0", "compile")
        );

        when(repositoryRepository.findByIdAndUserId(repoId, userId)).thenReturn(Optional.of(repository));
        when(mavenService.getDependenciesMaven(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedDeps);

        // Act
        List<DependencieObject> result = repositoryService.checkRepositoryDependencies(repoId, userId);

        // Assert
        assertEquals(expectedDeps, result);
    }

    @Test
    public void testDeleteRepository() {
        // Arrange
        Long repoId = 1L;
        Long userId = 1L;
        Repository repository = new Repository();

        when(repositoryRepository.findByIdAndUserId(repoId, userId)).thenReturn(Optional.of(repository));

        // Act
        repositoryService.deleteRepository(repoId, userId);

        // Assert
        verify(repositoryRepository).delete(repository);
    }

    @Test
    public void testDeleteRepository_NotFound() {
        // Arrange
        Long repoId = 1L;
        Long userId = 1L;

        when(repositoryRepository.findByIdAndUserId(repoId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            repositoryService.deleteRepository(repoId, userId);
        });
        assertEquals("Repository not found or not owned by user", exception.getMessage());
    }

    @Test
    public void testGetRepositoryDependencies() throws IOException {
        // Arrange
        Long repoId = 1L;
        Long userId = 1L;

        Repository repository = new Repository();
        repository.setType(RepoType.MAVEN);
        repository.setUsername("testUser");
        repository.setToken("testToken");
        repository.setUrl("https://github.com/owner/repo");
        repository.setBranch("main");

        List<DependencieObject> deps = Arrays.asList(
                new DependencieObject("group", "artifact1", "1.0.0", "compile"),
                new DependencieObject("group", "artifact2", "2.0.0", "compile")
        );

        when(repositoryRepository.findByIdAndUserId(repoId, userId)).thenReturn(Optional.of(repository));
        when(mavenService.getDependenciesMaven(anyString(), anyString(), anyString(), anyString())).thenReturn(deps);

        // Correction des mocks pour correspondre à l'appel réel
        when(mavenService.getLastMavenDependencyVersion("group", "artifact1")).thenReturn("1.1.0");
        when(mavenService.getLastMavenDependencyVersion("group", "artifact2")).thenReturn("2.0.0");

        when(repositoryRepository.save(any(Repository.class))).thenReturn(repository);

        // Act
        List<UpdateRepoDependencies> result = repositoryService.getRepositoryDependencies(repoId, userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("1.1.0", result.get(0).getNew_version());
        assertEquals("2.0.0", result.get(1).getNew_version());
        verify(repositoryRepository).save(repository);
    }

    @Test
    public void testGetRepositoryById() {
        // Arrange
        Long repoId = 1L;
        Long userId = 1L;
        Repository expected = new Repository();

        when(repositoryRepository.findByIdAndUserId(repoId, userId)).thenReturn(Optional.of(expected));

        // Act
        Repository result = repositoryService.getRepositoryById(repoId, userId);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void testGetRepositoryById_NotFound() {
        // Arrange
        Long repoId = 1L;
        Long userId = 1L;

        when(repositoryRepository.findByIdAndUserId(repoId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            repositoryService.getRepositoryById(repoId, userId);
        });
        assertEquals("Repository not found or not owned by user", exception.getMessage());
    }
}