package dev.shelf.models;

import dev.shelf.entities.PatronEntity;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record Patron(
    @NotNull Long id, @NotNull String name, @NotNull String email, @NotNull LocalDate memberSince) {

  public static Patron from(PatronEntity patron) {
    return new Patron(
        patron.getId(), patron.getName(), patron.getEmail(), patron.getMemberSince());
  }
}
