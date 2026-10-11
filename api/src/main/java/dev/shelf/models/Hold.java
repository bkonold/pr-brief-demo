package dev.shelf.models;

import dev.shelf.entities.HoldEntity;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record Hold(
    @NotNull Long id,
    @NotNull Long bookId,
    @NotNull Long patronId,
    @NotNull HoldStatus status,
    @NotNull Instant placedAt,
    @NotNull Instant expiresAt) {

  public static Hold from(HoldEntity hold) {
    return new Hold(
        hold.getId(),
        hold.getBook().getId(),
        hold.getPatron().getId(),
        hold.getStatus(),
        hold.getPlacedAt(),
        hold.getExpiresAt());
  }
}
