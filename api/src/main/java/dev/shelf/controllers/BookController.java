package dev.shelf.controllers;

import dev.shelf.models.Book;
import dev.shelf.models.BookCreateRequest;
import dev.shelf.services.BookService;
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
public class BookController {

  private final BookService books;

  public BookController(BookService books) {
    this.books = books;
  }

  @GetMapping("/books")
  public List<Book> listBooks() {
    return books.list();
  }

  @GetMapping("/books/{bookId}")
  public Book getBook(@PathVariable long bookId) {
    return books.get(bookId);
  }

  @PostMapping("/books")
  @ResponseStatus(HttpStatus.CREATED)
  public Book createBook(@Valid @RequestBody BookCreateRequest request) {
    return books.create(request);
  }
}
