package fr.usmb.depocheck.Objects;

import fr.usmb.depocheck.RepoType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepoObject {
    private String name;
    private String branch;
    private String username;
    private String token;
    private RepoType type;
}
