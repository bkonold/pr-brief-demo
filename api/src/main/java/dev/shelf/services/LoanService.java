package dev.shelf.services;

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
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoanService {

  private final LoanRepository loans;
  private final BookRepository books;
  private final PatronRepository patrons;
  private final HoldService holds;
  private final Clock clock;

  public LoanService(
      LoanRepository loans,
      BookRepository books,
      PatronRepository patrons,
      HoldService holds,
      Clock clock) {
    this.loans = loans;
    this.books = books;
    this.patrons = patrons;
    this.holds = holds;
    this.clock = clock;
  }

  @Transactional
  public Loan checkout(CheckoutRequest request) {
    BookEntity book = books.findById(request.bookId()).orElseThrow(() -> notFound("book"));
    PatronEntity patron = patrons.findById(request.patronId()).orElseThrow(() -> notFound("patron"));
    Instant now = clock.instant();
    HoldEntity pickup = null;
    if (!book.isAvailable()) {
      pickup =
          holds
              .readyHoldFor(book, patron, now)
              .orElseThrow(
                  () ->
                      new ResponseStatusException(HttpStatus.CONFLICT, "book is already checked out"));
    }
    book.setAvailable(false);
    LoanEntity loan = new LoanEntity(book, patron, now, now.plus(request.dueDays(), ChronoUnit.DAYS));
    if (pickup != null) {
      holds.fulfill(pickup);
      loan.setFulfilledHoldId(pickup.getId());
    }
    return Loan.from(loans.save(loan));
  }

  @Transactional
  public Loan returnBook(long loanId) {
    LoanEntity loan = loans.findById(loanId).orElseThrow(() -> notFound("loan"));
    if (!loan.isOpen()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "loan is already returned");
    }
    Instant now = clock.instant();
    loan.markReturned(now);
    holds.releaseBook(loan.getBook(), now);
    return Loan.from(loan);
  }

  @Transactional(readOnly = true)
  public List<Loan> listForPatron(long patronId) {
    return loans.findByPatronIdOrderByCheckedOutAtDesc(patronId).stream().map(Loan::from).toList();
  }

  private static ResponseStatusException notFound(String what) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "no such " + what);
  }
}
