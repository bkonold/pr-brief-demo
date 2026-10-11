package dev.shelf.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.shelf.entities.BookEntity;
import dev.shelf.entities.HoldEntity;
import dev.shelf.entities.LoanEntity;
import dev.shelf.entities.PatronEntity;
import dev.shelf.models.CheckoutRequest;
import dev.shelf.models.Loan;
import dev.shelf.repositories.BookRepository;
import dev.shelf.repositories.LoanRepository;
import dev.shelf.repositories.PatronRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class LoanServiceTest {

  private static final Instant NOW = Instant.parse("2026-03-02T10:00:00Z");

  private final LoanRepository loans = mock(LoanRepository.class);
  private final BookRepository books = mock(BookRepository.class);
  private final PatronRepository patrons = mock(PatronRepository.class);
  private final HoldService holds = mock(HoldService.class);
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private final LoanService service = new LoanService(loans, books, patrons, holds, clock);

  private BookEntity book;
  private PatronEntity patron;

  @BeforeEach
  void setUp() {
    book = new BookEntity("Dune", "Frank Herbert", "9780441172719");
    ReflectionTestUtils.setField(book, "id", 1L);
    patron = new PatronEntity("Ada", "ada@example.com");
    ReflectionTestUtils.setField(patron, "id", 7L);
    when(books.findById(1L)).thenReturn(Optional.of(book));
    when(patrons.findById(7L)).thenReturn(Optional.of(patron));
    when(loans.save(any(LoanEntity.class))).thenAnswer(call -> call.getArgument(0));
  }

  @Test
  void checkoutMarksTheBookUnavailableAndDueAfterTheRequestedDays() {
    Loan loan = service.checkout(new CheckoutRequest(7L, 1L, 21));

    assertThat(book.isAvailable()).isFalse();
    assertThat(loan.dueAt()).isEqualTo(NOW.plus(21, ChronoUnit.DAYS));
  }

  @Test
  void checkoutOfACheckedOutBookIsAConflict() {
    book.setAvailable(false);
    when(holds.readyHoldFor(book, patron, NOW)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.checkout(new CheckoutRequest(7L, 1L, 14)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("already checked out");
  }

  @Test
  void aPatronWithAReadyHoldMayCheckOutAnUnavailableBook() {
    HoldEntity ready = new HoldEntity(book, patron, NOW.minusSeconds(7200), NOW.plusSeconds(3600));
    ReflectionTestUtils.setField(ready, "id", 3L);
    book.setAvailable(false);
    when(holds.readyHoldFor(book, patron, NOW)).thenReturn(Optional.of(ready));

    service.checkout(new CheckoutRequest(7L, 1L, 14));

    verify(holds).fulfill(ready);
  }

  @Test
  void returningALoanReleasesTheBookToTheHoldQueue() {
    LoanEntity loan = new LoanEntity(book, patron, NOW.minusSeconds(3600), NOW.plusSeconds(3600));
    ReflectionTestUtils.setField(loan, "id", 5L);
    book.setAvailable(false);
    when(loans.findById(5L)).thenReturn(Optional.of(loan));

    Loan returned = service.returnBook(5L);

    verify(holds).releaseBook(book, NOW);
    assertThat(returned.returnedAt()).isEqualTo(NOW);
  }
}
