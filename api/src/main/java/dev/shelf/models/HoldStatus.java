package dev.shelf.models;

public enum HoldStatus {
  QUEUED,
  READY,
  FULFILLED,
  EXPIRED,
  CANCELLED;

  /** A hold that still claims a place in line or a copy waiting at the desk. */
  public boolean isActive() {
    return this == QUEUED || this == READY;
  }
}
