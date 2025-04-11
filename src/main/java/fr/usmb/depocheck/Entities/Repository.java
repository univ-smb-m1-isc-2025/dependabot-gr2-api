package fr.usmb.depocheck.Entities;

import fr.usmb.depocheck.RepoType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;
    private String branch;
    private String username;

    @Enumerated(EnumType.STRING)
    private RepoType type;

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}