package fr.usmb.depocheck;

import fr.usmb.depocheck.DTO.RepositoryDTO;
import fr.usmb.depocheck.Entities.Repository;
import fr.usmb.depocheck.RepoType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryDTOTest {

    @Test
    public void testFromEntity() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Repository repository = new Repository();
        repository.setId(1L);
        repository.setName("test-repo");
        repository.setUrl("https://github.com/user/repo");
        repository.setBranch("main");
        repository.setUsername("testUser");
        repository.setType(RepoType.MAVEN);
        repository.setLastVerificationDate(now);
        repository.setPendingUpdatesCount(5);
        repository.setNumberOfDependencies(10);

        // Act
        RepositoryDTO dto = RepositoryDTO.fromEntity(repository);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("test-repo", dto.getName());
        assertEquals("https://github.com/user/repo", dto.getUrl());
        assertEquals("main", dto.getBranch());
        assertEquals("testUser", dto.getUsername());
        assertEquals(RepoType.MAVEN, dto.getType());
        assertEquals(now, dto.getLastVerificationDate());
        assertEquals(5, dto.getPendingUpdatesCount());
        assertEquals(10, dto.getNumberOfDependencies());
    }

    @Test
    public void testFromEntityList() {
        // Arrange
        Repository repo1 = new Repository();
        repo1.setId(1L);
        repo1.setName("repo1");

        Repository repo2 = new Repository();
        repo2.setId(2L);
        repo2.setName("repo2");

        List<Repository> repositories = Arrays.asList(repo1, repo2);

        // Act
        List<RepositoryDTO> dtos = RepositoryDTO.fromEntityList(repositories);

        // Assert
        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals("repo1", dtos.get(0).getName());
        assertEquals(2L, dtos.get(1).getId());
        assertEquals("repo2", dtos.get(1).getName());
    }
}