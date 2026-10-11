package dev.shelf.models;

import dev.shelf.entities.LoanEntity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record Loan(
    @NotNull Long id,
    @NotNull Long bookId,
    @NotNull Long patronId,
    @NotNull Instant checkedOutAt,
    @NotNull Instant dueAt,
    Instant returnedAt) {

  public static Loan from(LoanEntity loan) {
    return new Loan(
        loan.getId(),
        loan.getBook().getId(),
        loan.getPatron().getId(),
        loan.getCheckedOutAt(),
        loan.getDueAt(),
        loan.getReturnedAt());
  }
}
