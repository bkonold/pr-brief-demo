CREATE TABLE books (
    id        BIGSERIAL PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    isbn      VARCHAR(20)  NOT NULL UNIQUE,
    available BOOLEAN      NOT NULL DEFAULT TRUE,
    notes     TEXT
);

CREATE TABLE patrons (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    member_since DATE         NOT NULL DEFAULT CURRENT_DATE
);
