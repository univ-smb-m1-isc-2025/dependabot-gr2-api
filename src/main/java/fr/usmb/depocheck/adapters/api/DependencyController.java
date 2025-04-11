package fr.usmb.depocheck.adapters.api;

import fr.usmb.depocheck.DTO.*;
import fr.usmb.depocheck.Entities.Repository;
import fr.usmb.depocheck.Entities.User;
import fr.usmb.depocheck.Objects.DependencieObject;
import fr.usmb.depocheck.Objects.RepoObject;
import fr.usmb.depocheck.Repository.UserRepository;
import fr.usmb.depocheck.Services.MavenService;
import fr.usmb.depocheck.Services.RepositoryService;
import fr.usmb.depocheck.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deps")
public class DependencyController {
    @Autowired
    private MavenService mavenService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/check")
    public ResponseEntity<?> checkDependencies(@RequestBody RepoObject repo) {
        List<DependencieObject> dependencies;
        try {
            switch (repo.getType()) {
                case MAVEN:
                    dependencies = mavenService.getDependenciesMaven(repo.getUsername(), repo.getToken(), repo.getName(), repo.getBranch());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Error: Unsupported repository type");
            }

            return ResponseEntity.ok(new ResponseDependecyRequest("Dependencies checked", dependencies));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateDependencies(@RequestBody UpdateDependenciesRequest request) {
        List<DependencieObject> updatedDependencies;
        try {
            switch (request.getRepoType()) {
                case MAVEN:
                    updatedDependencies = mavenService.getUpdatedDependencies(request.getDependencies());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Error: Unsupported repository type");
            }
            return ResponseEntity.ok(new ResponseDependecyRequest("Dependencies updated", updatedDependencies));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/repositories")
    public ResponseEntity<?> getUserRepositories(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = getUserIdFromToken(token);

            List<Repository> repositories = repositoryService.getUserRepositories(userId);
            List<RepositoryDTO> repositoryDTOs = RepositoryDTO.fromEntityList(repositories);

            return ResponseEntity.ok(new ResponseRepository("User repositories retrieved", repositoryDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/add-repository")
    public ResponseEntity<?> addRepository(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AddRepositoryRequest request) {
        try {
            // Extract user ID from JWT token
            String token = authHeader.replace("Bearer ", "");
            Long userId = getUserIdFromToken(token);

            // Create repository from request
            Repository repository = new Repository();
            repository.setName(request.getName());
            repository.setUrl(request.getUrl());
            repository.setBranch(request.getBranch());
            repository.setUsername(request.getUsername());
            repository.setType(request.getType());
            repository.setToken(request.getToken());

            // Modify the return statement
            Repository savedRepository = repositoryService.saveRepository(repository, userId);
            RepositoryDTO repositoryDTO = RepositoryDTO.fromEntity(savedRepository);

            return ResponseEntity.ok(new ResponseRepository("Repository added successfully",
                    List.of(repositoryDTO)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/repository/{id}")
    public ResponseEntity<?> deleteRepository(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = getUserIdFromToken(token);

            repositoryService.deleteRepository(id, userId);

            return ResponseEntity.ok("Repository deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/repository/{id}/dependencies")
    public ResponseEntity<?> getRepositoryDependencies(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = getUserIdFromToken(token);

            List<DependencieObject> dependencies = repositoryService.getRepositoryDependencies(id, userId);

            return ResponseEntity.ok(new ResponseDependecyRequest("Dependencies retrieved", dependencies));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // TODO: Move this method to a utility class
    // Extract user ID from JWT token
    private Long getUserIdFromToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return user.getId();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }
}
