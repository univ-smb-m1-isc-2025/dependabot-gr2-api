package fr.usmb.depocheck.Repository;

import fr.usmb.depocheck.Entities.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    List<Repository> findByUserId(Long userId);
    Optional<Repository> findByIdAndUserId(Long id, Long userId);
}