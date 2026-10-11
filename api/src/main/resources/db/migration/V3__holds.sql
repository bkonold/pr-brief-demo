-- Patrons queue up for a book that is checked out; the oldest live hold gets it when it comes back.
CREATE TABLE holds (
    id         BIGSERIAL PRIMARY KEY,
    book_id    BIGINT      NOT NULL REFERENCES books (id),
    patron_id  BIGINT      NOT NULL REFERENCES patrons (id),
    status     VARCHAR(16) NOT NULL,
    placed_at  TIMESTAMPTZ NOT NULL,
    ready_at   TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_holds_queue ON holds (book_id, status, placed_at);
CREATE INDEX idx_holds_patron ON holds (patron_id);

-- Set when a loan is the pickup of a ready hold.
ALTER TABLE loans ADD COLUMN fulfilled_hold_id BIGINT;

-- Librarian notes moved to the staff wiki; nothing reads them any more.
ALTER TABLE books DROP COLUMN notes;
