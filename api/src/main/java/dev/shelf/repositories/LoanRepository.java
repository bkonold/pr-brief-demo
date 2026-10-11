package dev.shelf.repositories;

import dev.shelf.entities.LoanEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

  List<LoanEntity> findByPatronIdOrderByCheckedOutAtDesc(Long patronId);
}
