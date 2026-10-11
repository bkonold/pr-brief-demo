package dev.shelf.models;

import jakarta.validation.constraints.NotBlank;

public record BookCreateRequest(
    @NotBlank String title, @NotBlank String author, @NotBlank String isbn) {}
