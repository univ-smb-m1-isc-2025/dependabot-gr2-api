package fr.usmb.depocheck.Entities;

import fr.usmb.depocheck.RepoType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "last_verification_date")
    private LocalDateTime lastVerificationDate;

    @Column(name = "pending_updates_count")
    private Integer pendingUpdatesCount;

    @Column(name = "number_of_dependencies")
    private Integer numberOfDependencies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}