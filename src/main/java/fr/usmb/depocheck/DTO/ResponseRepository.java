package fr.usmb.depocheck.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ResponseRepository {
    private String response;
    private List<RepositoryDTO> repositories;

    public ResponseRepository(String response, List<RepositoryDTO> repositories) {
        this.response = response;
        this.repositories = repositories;
    }
}