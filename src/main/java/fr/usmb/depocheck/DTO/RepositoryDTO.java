package fr.usmb.depocheck.DTO;

import fr.usmb.depocheck.Entities.Repository;
import fr.usmb.depocheck.RepoType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO {
    private Long id;
    private String name;
    private String url;
    private String branch;
    private String username;
    private RepoType type;

    // Convert Entity to DTO
    public static RepositoryDTO fromEntity(Repository repository) {
        RepositoryDTO dto = new RepositoryDTO();
        dto.setId(repository.getId());
        dto.setName(repository.getName());
        dto.setUrl(repository.getUrl());
        dto.setBranch(repository.getBranch());
        dto.setUsername(repository.getUsername());
        dto.setType(repository.getType());
        return dto;
    }

    // Convert list of entities to list of DTOs
    public static List<RepositoryDTO> fromEntityList(List<Repository> repositories) {
        return repositories.stream()
                .map(RepositoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
}