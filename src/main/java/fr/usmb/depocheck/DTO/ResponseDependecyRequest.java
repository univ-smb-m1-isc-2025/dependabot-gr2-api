package fr.usmb.depocheck.DTO;

import fr.usmb.depocheck.Objects.DependencieObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseDependecyRequest {
    private String response;
    private List<DependencieObject> dependencies;
    public ResponseDependecyRequest(String response, List<DependencieObject> dependencies) {
        this.response = response;
        this.dependencies = dependencies;
    }
}
