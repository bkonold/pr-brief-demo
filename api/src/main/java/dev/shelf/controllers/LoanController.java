package dev.shelf.controllers;

import dev.shelf.models.CheckoutRequest;
import dev.shelf.models.Loan;
import dev.shelf.services.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoanController {

  private final LoanService loans;

  public LoanController(LoanService loans) {
    this.loans = loans;
  }

  @PostMapping("/loans")
  @ResponseStatus(HttpStatus.CREATED)
  public Loan checkoutBook(@Valid @RequestBody CheckoutRequest request) {
    return loans.checkout(request);
  }

  @PostMapping("/loans/{loanId}/return")
  public Loan returnBook(@PathVariable long loanId) {
    return loans.returnBook(loanId);
  }
}
