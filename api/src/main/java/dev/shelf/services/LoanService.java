package dev.shelf.services;

import dev.shelf.entities.BookEntity;
import dev.shelf.entities.LoanEntity;
import dev.shelf.entities.PatronEntity;
import dev.shelf.models.CheckoutRequest;
import dev.shelf.models.Loan;
import dev.shelf.repositories.BookRepository;
import dev.shelf.repositories.LoanRepository;
import dev.shelf.repositories.PatronRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoanService {

  static final Duration LOAN_PERIOD = Duration.ofDays(14);

  private final LoanRepository loans;
  private final BookRepository books;
  private final PatronRepository patrons;
  private final Clock clock;

  public LoanService(
      LoanRepository loans, BookRepository books, PatronRepository patrons, Clock clock) {
    this.loans = loans;
    this.books = books;
    this.patrons = patrons;
    this.clock = clock;
  }

  @Transactional
  public Loan checkout(CheckoutRequest request) {
    BookEntity book = books.findById(request.bookId()).orElseThrow(() -> notFound("book"));
    PatronEntity patron = patrons.findById(request.patronId()).orElseThrow(() -> notFound("patron"));
    if (!book.isAvailable()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "book is already checked out");
    }
    book.setAvailable(false);
    Instant now = clock.instant();
    LoanEntity loan = loans.save(new LoanEntity(book, patron, now, now.plus(LOAN_PERIOD)));
    return Loan.from(loan);
  }

  @Transactional
  public Loan returnBook(long loanId) {
    LoanEntity loan = loans.findById(loanId).orElseThrow(() -> notFound("loan"));
    if (!loan.isOpen()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "loan is already returned");
    }
    loan.markReturned(clock.instant());
    loan.getBook().setAvailable(true);
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
