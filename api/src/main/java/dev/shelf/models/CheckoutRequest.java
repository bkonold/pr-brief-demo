package dev.shelf.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
    @NotNull Long patronId,
    @NotNull Long bookId,
    @NotNull @Min(1) @Max(28) Integer dueDays) {}
