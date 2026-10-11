package dev.shelf.repositories;

import dev.shelf.entities.PatronEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatronRepository extends JpaRepository<PatronEntity, Long> {}
