package fr.usmb.depocheck.DTO;

import fr.usmb.depocheck.RepoType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRepositoryRequest {
    private String name;
    private String url;
    private String branch;
    private String username;
    private RepoType type;
    private String token;
}