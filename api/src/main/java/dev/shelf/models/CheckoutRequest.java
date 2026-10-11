package dev.shelf.models;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(@NotNull Long patronId, @NotNull Long bookId) {}
