package fr.usmb.depocheck.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRepoDependenciesList {
    private List<UpdateRepoDependencies> dependencies;
    private String repo;
}
