package dev.shelf.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "loans")
public class LoanEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id")
  private BookEntity book;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "patron_id")
  private PatronEntity patron;

  @Column(name = "checked_out_at", nullable = false)
  private Instant checkedOutAt;

  @Column(name = "due_at", nullable = false)
  private Instant dueAt;

  @Column(name = "returned_at")
  private Instant returnedAt;

  protected LoanEntity() {}

  public LoanEntity(BookEntity book, PatronEntity patron, Instant checkedOutAt, Instant dueAt) {
    this.book = book;
    this.patron = patron;
    this.checkedOutAt = checkedOutAt;
    this.dueAt = dueAt;
  }

  public Long getId() {
    return id;
  }

  public BookEntity getBook() {
    return book;
  }

  public PatronEntity getPatron() {
    return patron;
  }

  public Instant getCheckedOutAt() {
    return checkedOutAt;
  }

  public Instant getDueAt() {
    return dueAt;
  }

  public Instant getReturnedAt() {
    return returnedAt;
  }

  public boolean isOpen() {
    return returnedAt == null;
  }

  public void markReturned(Instant when) {
    this.returnedAt = when;
  }
}
