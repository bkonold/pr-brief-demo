package dev.shelf.models;

import dev.shelf.entities.BookEntity;
import jakarta.validation.constraints.NotNull;

public record Book(
    @NotNull Long id,
    @NotNull String title,
    @NotNull String author,
    @NotNull String isbn,
    boolean available) {

  public static Book from(BookEntity book) {
    return new Book(
        book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.isAvailable());
  }
}
