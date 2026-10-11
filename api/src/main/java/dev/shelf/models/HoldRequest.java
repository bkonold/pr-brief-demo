package dev.shelf.models;

import jakarta.validation.constraints.NotNull;

public record HoldRequest(@NotNull Long patronId) {}
