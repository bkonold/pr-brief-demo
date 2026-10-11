package dev.shelf.services;

import dev.shelf.entities.BookEntity;
import dev.shelf.models.Book;
import dev.shelf.models.BookCreateRequest;
import dev.shelf.repositories.BookRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {

  private final BookRepository books;

  public BookService(BookRepository books) {
    this.books = books;
  }

  @Transactional(readOnly = true)
  public List<Book> list() {
    return books.findAll().stream().map(Book::from).toList();
  }

  @Transactional(readOnly = true)
  public Book get(long bookId) {
    return books
        .findById(bookId)
        .map(Book::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no such book"));
  }

  @Transactional
  public Book create(BookCreateRequest request) {
    BookEntity book = new BookEntity(request.title(), request.author(), request.isbn());
    return Book.from(books.save(book));
  }
}
