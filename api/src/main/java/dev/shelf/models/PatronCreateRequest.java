package dev.shelf.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PatronCreateRequest(@NotBlank String name, @NotBlank @Email String email) {}
