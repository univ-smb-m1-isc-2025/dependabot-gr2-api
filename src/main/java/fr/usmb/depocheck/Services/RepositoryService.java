package fr.usmb.depocheck.Services;

import fr.usmb.depocheck.Entities.Repository;
import fr.usmb.depocheck.Entities.User;
import fr.usmb.depocheck.Objects.DependencieObject;
import fr.usmb.depocheck.Repository.RepositoryRepository;
import fr.usmb.depocheck.Repository.UserRepository;
import fr.usmb.depocheck.RepoType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Console;
import java.io.IOException;
import java.util.List;

@Service
public class RepositoryService {
    @Autowired
    private RepositoryRepository repositoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MavenService mavenService;

    public List<Repository> getUserRepositories(Long userId) {
        return repositoryRepository.findByUserId(userId);
    }

    public Repository saveRepository(Repository repository, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        repository.setUser(user);
        return repositoryRepository.save(repository);
    }

    public List<DependencieObject> checkRepositoryDependencies(Long repoId, Long userId) throws IOException {
        Repository repository = repositoryRepository.findByIdAndUserId(repoId, userId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        switch (repository.getType()) {
            case MAVEN:
                return mavenService.getDependenciesMaven(
                        repository.getUsername(),
                        repository.getToken(),
                        repository.getName(),
                        repository.getBranch()
                );
            default:
                break;
        }

        return null;
    }

    public void deleteRepository(Long repoId, Long userId) {
        Repository repository = repositoryRepository.findByIdAndUserId(repoId, userId)
                .orElseThrow(() -> new RuntimeException("Repository not found or not owned by user"));

        repositoryRepository.delete(repository);
    }

    public List<DependencieObject> getRepositoryDependencies(Long repoId, Long userId) throws IOException {
        Repository repository = repositoryRepository.findByIdAndUserId(repoId, userId)
                .orElseThrow(() -> new RuntimeException("Repository not found or not owned by user"));
        System.out.println("Repository found: " + repository.getName());
        // TODO: get the url and extract the username and repo name from url
        switch (repository.getType()) {
            case MAVEN:
                return mavenService.getDependenciesMaven(
                        repository.getUsername(),
                        repository.getToken(),
                        repository.getName(),
                        repository.getBranch()
                );
            default:
                break;
        }

        return null;
    }
}