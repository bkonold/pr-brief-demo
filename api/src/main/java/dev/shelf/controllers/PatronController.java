package dev.shelf.controllers;

import dev.shelf.models.Loan;
import dev.shelf.models.Patron;
import dev.shelf.models.PatronCreateRequest;
import dev.shelf.services.LoanService;
import dev.shelf.services.PatronService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatronController {

  private final PatronService patrons;
  private final LoanService loans;

  public PatronController(PatronService patrons, LoanService loans) {
    this.patrons = patrons;
    this.loans = loans;
  }

  @PostMapping("/patrons")
  @ResponseStatus(HttpStatus.CREATED)
  public Patron registerPatron(@Valid @RequestBody PatronCreateRequest request) {
    return patrons.register(request);
  }

  @GetMapping("/patrons/{patronId}")
  public Patron getPatron(@PathVariable long patronId) {
    return patrons.get(patronId);
  }

  @GetMapping("/patrons/{patronId}/loans")
  public List<Loan> listPatronLoans(@PathVariable long patronId) {
    return loans.listForPatron(patronId);
  }
}
