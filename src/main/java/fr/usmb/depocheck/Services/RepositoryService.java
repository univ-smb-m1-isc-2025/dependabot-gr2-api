package fr.usmb.depocheck.Services;

import fr.usmb.depocheck.DTO.UpdateRepoDependencies;
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
import java.util.*;

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
        Repository repository = getRepositoryById(repoId, userId);

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

    public  List<UpdateRepoDependencies> getRepositoryDependencies(Long repoId, Long userId) throws IOException {
        Repository repository = getRepositoryById(repoId, userId);

        String repoUrl = extractGitHubInfoFromURL(repository.getUrl());

        switch (repository.getType()) {
            case MAVEN:
                List<DependencieObject> deps = mavenService.getDependenciesMaven(
                        repository.getUsername(),
                        repository.getToken(),
                        repoUrl,
                        repository.getBranch()
                );

                List<UpdateRepoDependencies> updateRepoDependencies = new ArrayList<>();

                for (DependencieObject dep : deps) {
                    String lastVersion = mavenService.getLastMavenDependencyVersion(dep.getName(), dep.getVersion());

                    updateRepoDependencies.add(new UpdateRepoDependencies(
                            dep.getName(),
                            dep.getVersion(),
                            lastVersion
                    ));
                }
                return updateRepoDependencies;

            default:
                break;
        }

        return null;
    }

    private String extractGitHubInfoFromURL(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Depot URL can't be null or empty");
        }

        String path = url.replaceFirst("https?://github\\.com/", "");

        String[] segments = path.split("/");

        if (segments.length < 2) {
            throw new IllegalArgumentException("Invalid github URL format: " + url);
        }

        return path;
    }

    public Repository getRepositoryById(Long id, Long userId) {
        return repositoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Repository not found or not owned by user"));
    }
}