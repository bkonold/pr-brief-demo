package dev.shelf.entities;

import dev.shelf.models.HoldStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "holds")
public class HoldEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id")
  private BookEntity book;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "patron_id")
  private PatronEntity patron;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private HoldStatus status = HoldStatus.QUEUED;

  @Column(name = "placed_at", nullable = false)
  private Instant placedAt;

  @Column(name = "ready_at")
  private Instant readyAt;

  /** While queued, when the hold lapses; once ready, the last moment the patron can pick it up. */
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  protected HoldEntity() {}

  public HoldEntity(BookEntity book, PatronEntity patron, Instant placedAt, Instant expiresAt) {
    this.book = book;
    this.patron = patron;
    this.placedAt = placedAt;
    this.expiresAt = expiresAt;
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

  public HoldStatus getStatus() {
    return status;
  }

  public void setStatus(HoldStatus status) {
    this.status = status;
  }

  public Instant getPlacedAt() {
    return placedAt;
  }

  public Instant getReadyAt() {
    return readyAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void markReady(Instant now, Instant pickupDeadline) {
    this.status = HoldStatus.READY;
    this.readyAt = now;
    this.expiresAt = pickupDeadline;
  }
}
