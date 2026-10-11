CREATE TABLE loans (
    id             BIGSERIAL PRIMARY KEY,
    book_id        BIGINT      NOT NULL REFERENCES books (id),
    patron_id      BIGINT      NOT NULL REFERENCES patrons (id),
    checked_out_at TIMESTAMPTZ NOT NULL,
    due_at         TIMESTAMPTZ NOT NULL,
    returned_at    TIMESTAMPTZ
);

CREATE INDEX idx_loans_patron ON loans (patron_id);
CREATE INDEX idx_loans_book ON loans (book_id);
