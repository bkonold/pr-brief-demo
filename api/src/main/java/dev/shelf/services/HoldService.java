package dev.shelf.services;

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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HoldService {

  /** How long a hold may wait in line before it lapses. */
  static final Duration QUEUE_LIFETIME = Duration.ofDays(30);

  /** How long a returned book is kept at the desk for the patron at the front of the line. */
  static final Duration PICKUP_WINDOW = Duration.ofDays(3);

  private final HoldRepository holds;
  private final BookRepository books;
  private final PatronRepository patrons;
  private final Clock clock;

  public HoldService(
      HoldRepository holds, BookRepository books, PatronRepository patrons, Clock clock) {
    this.holds = holds;
    this.books = books;
    this.patrons = patrons;
    this.clock = clock;
  }

  @Transactional
  public Hold place(long bookId, HoldRequest request) {
    BookEntity book = books.findById(bookId).orElseThrow(() -> notFound("book"));
    PatronEntity patron = patrons.findById(request.patronId()).orElseThrow(() -> notFound("patron"));
    if (book.isAvailable()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "book is on the shelf; check it out instead");
    }
    List<HoldStatus> active = Arrays.stream(HoldStatus.values()).filter(HoldStatus::isActive).toList();
    if (holds.existsByBookIdAndPatronIdAndStatusIn(bookId, request.patronId(), active)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "patron already holds this book");
    }
    Instant now = clock.instant();
    HoldEntity hold = holds.save(new HoldEntity(book, patron, now, now.plus(QUEUE_LIFETIME)));
    return Hold.from(hold);
  }

  @Transactional(readOnly = true)
  public List<Hold> listForPatron(long patronId) {
    return holds.findByPatronIdOrderByPlacedAtAsc(patronId).stream().map(Hold::from).toList();
  }

  @Transactional
  public void cancel(long holdId) {
    HoldEntity hold = holds.findById(holdId).orElseThrow(() -> notFound("hold"));
    if (!hold.getStatus().isActive()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "hold is already " + hold.getStatus());
    }
    boolean wasReady = hold.getStatus() == HoldStatus.READY;
    hold.setStatus(HoldStatus.CANCELLED);
    if (wasReady) {
      releaseBook(hold.getBook(), clock.instant());
    }
  }

  /**
   * Hands a book that just came back, or whose pickup window lapsed, to the first patron in line
   * who is still waiting. Holds that outlived their queue lifetime are expired on the way past, so
   * the next live one is promoted. With nobody left in line the book goes back on the shelf.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void releaseBook(BookEntity book, Instant now) {
    for (HoldEntity hold : holds.findQueue(book.getId(), HoldStatus.QUEUED)) {
      if (hold.getExpiresAt().isBefore(now)) {
        hold.setStatus(HoldStatus.EXPIRED);
        continue;
      }
      hold.markReady(now, now.plus(PICKUP_WINDOW));
      book.setAvailable(false);
      return;
    }
    book.setAvailable(true);
  }

  /** The patron's own ready hold on the book, if its pickup window is still open. */
  @Transactional(propagation = Propagation.MANDATORY)
  public Optional<HoldEntity> readyHoldFor(BookEntity book, PatronEntity patron, Instant now) {
    return holds.findQueue(book.getId(), HoldStatus.READY).stream()
        .filter(hold -> hold.getPatron().getId().equals(patron.getId()))
        .filter(hold -> hold.getExpiresAt().isAfter(now))
        .findFirst();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void fulfill(HoldEntity hold) {
    hold.setStatus(HoldStatus.FULFILLED);
  }

  /** Releases ready holds nobody picked up, so the next patron in line is not stuck behind them. */
  @Scheduled(fixedRate = 3_600_000)
  @Transactional
  public void expireUnclaimedHolds() {
    Instant now = clock.instant();
    for (HoldEntity hold : holds.findByStatusAndExpiresAtBefore(HoldStatus.READY, now)) {
      hold.setStatus(HoldStatus.EXPIRED);
      releaseBook(hold.getBook(), now);
    }
  }

  private static ResponseStatusException notFound(String what) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "no such " + what);
  }
}
