package fr.usmb.depocheck.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRepoDependencies {
    private String dependencie_name;
    private String old_version;
    private String new_version;

}
