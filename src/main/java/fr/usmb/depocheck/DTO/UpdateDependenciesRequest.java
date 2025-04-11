package fr.usmb.depocheck.DTO;

import fr.usmb.depocheck.Objects.DependencieObject;
import fr.usmb.depocheck.RepoType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateDependenciesRequest {
    private List<DependencieObject> dependencies;
    private RepoType repoType;
}