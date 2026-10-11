package dev.shelf.repositories;

import dev.shelf.entities.HoldEntity;
import dev.shelf.models.HoldStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoldRepository extends JpaRepository<HoldEntity, Long> {

  /** The holds on a book with this status, first come first served. */
  @Query(
      "select h from HoldEntity h where h.book.id = :bookId and h.status = :status"
          + " order by h.placedAt asc, h.id asc")
  List<HoldEntity> findQueue(@Param("bookId") Long bookId, @Param("status") HoldStatus status);

  List<HoldEntity> findByPatronIdOrderByPlacedAtAsc(Long patronId);

  List<HoldEntity> findByStatusAndExpiresAtBefore(HoldStatus status, Instant cutoff);

  boolean existsByBookIdAndPatronIdAndStatusIn(
      Long bookId, Long patronId, Collection<HoldStatus> statuses);
}
