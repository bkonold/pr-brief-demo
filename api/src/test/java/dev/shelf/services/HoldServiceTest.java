package dev.shelf.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.shelf.entities.BookEntity;
import dev.shelf.entities.HoldEntity;
import dev.shelf.entities.PatronEntity;
import dev.shelf.models.Hold;
import dev.shelf.models.HoldRequest;
import dev.shelf.models.HoldStatus;
import dev.shelf.repositories.BookRepository;
import dev.shelf.repositories.HoldRepository;
import dev.shelf.repositories.PatronRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class HoldServiceTest {

  private static final Instant NOW = Instant.parse("2026-03-10T09:00:00Z");

  private final HoldRepository holds = mock(HoldRepository.class);
  private final BookRepository books = mock(BookRepository.class);
  private final PatronRepository patrons = mock(PatronRepository.class);
  private final HoldService service =
      new HoldService(holds, books, patrons, Clock.fixed(NOW, ZoneOffset.UTC));

  private BookEntity book;

  @BeforeEach
  void setUp() {
    book = new BookEntity("Dune", "Frank Herbert", "9780441172719");
    ReflectionTestUtils.setField(book, "id", 1L);
    book.setAvailable(false);
    when(books.findById(1L)).thenReturn(Optional.of(book));
    when(holds.save(any(HoldEntity.class))).thenAnswer(call -> call.getArgument(0));
  }

  private PatronEntity patron(long id) {
    PatronEntity patron = new PatronEntity("Patron " + id, "p" + id + "@example.com");
    ReflectionTestUtils.setField(patron, "id", id);
    when(patrons.findById(id)).thenReturn(Optional.of(patron));
    return patron;
  }

  private HoldEntity queued(long patronId, Instant placedAt, Instant expiresAt) {
    return new HoldEntity(book, patron(patronId), placedAt, expiresAt);
  }

  @Test
  void aHoldOnABookOnTheShelfIsAConflict() {
    patron(7L);
    book.setAvailable(true);

    assertThatThrownBy(() -> service.place(1L, new HoldRequest(7L)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("check it out instead");
  }

  @Test
  void aNewHoldWaitsInLineForTheQueueLifetime() {
    patron(7L);

    Hold hold = service.place(1L, new HoldRequest(7L));

    assertThat(hold.status()).isEqualTo(HoldStatus.QUEUED);
    assertThat(hold.expiresAt()).isEqualTo(NOW.plus(HoldService.QUEUE_LIFETIME));
  }

  @Test
  void aPatronCannotHoldTheSameBookTwice() {
    patron(7L);
    when(holds.existsByBookIdAndPatronIdAndStatusIn(1L, 7L, List.of(HoldStatus.QUEUED, HoldStatus.READY)))
        .thenReturn(true);

    assertThatThrownBy(() -> service.place(1L, new HoldRequest(7L)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("already holds");
  }

  @Test
  void aReturnedBookGoesToTheFirstPatronInLine() {
    HoldEntity first = queued(7L, NOW.minusSeconds(7200), NOW.plus(HoldService.QUEUE_LIFETIME));
    HoldEntity second = queued(8L, NOW.minusSeconds(3600), NOW.plus(HoldService.QUEUE_LIFETIME));
    when(holds.findQueue(1L, HoldStatus.QUEUED)).thenReturn(List.of(first, second));

    service.releaseBook(book, NOW);

    assertThat(first.getStatus()).isEqualTo(HoldStatus.READY);
    assertThat(first.getExpiresAt()).isEqualTo(NOW.plus(HoldService.PICKUP_WINDOW));
    assertThat(second.getStatus()).isEqualTo(HoldStatus.QUEUED);
    assertThat(book.isAvailable()).isFalse();
  }

  @Test
  void aLapsedHoldIsSkippedAndTheNextOneIsPromoted() {
    HoldEntity lapsed = queued(7L, NOW.minusSeconds(86_400 * 40L), NOW.minusSeconds(86_400 * 10L));
    HoldEntity live = queued(8L, NOW.minusSeconds(3600), NOW.plus(HoldService.QUEUE_LIFETIME));
    when(holds.findQueue(1L, HoldStatus.QUEUED)).thenReturn(List.of(lapsed, live));

    service.releaseBook(book, NOW);

    assertThat(lapsed.getStatus()).isEqualTo(HoldStatus.EXPIRED);
    assertThat(live.getStatus()).isEqualTo(HoldStatus.READY);
    assertThat(book.isAvailable()).isFalse();
  }

  @Test
  void aReturnedBookWithNoLiveHoldsGoesBackOnTheShelf() {
    HoldEntity lapsed = queued(7L, NOW.minusSeconds(86_400 * 40L), NOW.minusSeconds(86_400 * 10L));
    when(holds.findQueue(1L, HoldStatus.QUEUED)).thenReturn(List.of(lapsed));

    service.releaseBook(book, NOW);

    assertThat(lapsed.getStatus()).isEqualTo(HoldStatus.EXPIRED);
    assertThat(book.isAvailable()).isTrue();
  }

  @Test
  void anUnclaimedReadyHoldExpiresAndPassesTheBookOn() {
    HoldEntity unclaimed = queued(7L, NOW.minusSeconds(86_400 * 5L), NOW.minusSeconds(60));
    unclaimed.markReady(NOW.minusSeconds(86_400 * 3L - 60), NOW.minusSeconds(60));
    HoldEntity next = queued(8L, NOW.minusSeconds(3600), NOW.plus(HoldService.QUEUE_LIFETIME));
    when(holds.findByStatusAndExpiresAtBefore(HoldStatus.READY, NOW)).thenReturn(List.of(unclaimed));
    when(holds.findQueue(1L, HoldStatus.QUEUED)).thenReturn(List.of(next));

    service.expireUnclaimedHolds();

    assertThat(unclaimed.getStatus()).isEqualTo(HoldStatus.EXPIRED);
    assertThat(next.getStatus()).isEqualTo(HoldStatus.READY);
  }

  @Test
  void cancellingAReadyHoldPassesTheBookOn() {
    HoldEntity ready = queued(7L, NOW.minusSeconds(7200), NOW.plus(HoldService.PICKUP_WINDOW));
    ready.markReady(NOW.minusSeconds(60), NOW.plus(HoldService.PICKUP_WINDOW));
    when(holds.findById(3L)).thenReturn(Optional.of(ready));
    when(holds.findQueue(1L, HoldStatus.QUEUED)).thenReturn(List.of());

    service.cancel(3L);

    assertThat(ready.getStatus()).isEqualTo(HoldStatus.CANCELLED);
    assertThat(book.isAvailable()).isTrue();
  }
}
