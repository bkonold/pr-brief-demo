import { useEffect, useState } from "react";
import type { Book } from "../sdk/generated";
import type { ShelfClient } from "./client";

interface Props {
  client: ShelfClient;
}

export function BookListPage({ client }: Props) {
  const [books, setBooks] = useState<Book[] | null>(null);

  useEffect(() => {
    client.listBooks().then(setBooks);
  }, [client]);

  if (books === null) {
    return <p>Loading books...</p>;
  }

  return (
    <main>
      <h1>Books</h1>
      <ul>
        {books.map((book) => (
          <li key={book.id}>
            <a href={`/books/${book.id}`}>{book.title}</a> by {book.author}{" "}
            <span>{book.available ? "Available" : "Checked out"}</span>
          </li>
        ))}
      </ul>
    </main>
  );
}
