import { useEffect, useState } from "react";
import type { Book } from "../sdk/generated";
import type { ShelfClient } from "./client";

interface Props {
  client: ShelfClient;
  bookId: number;
}

export function BookPage({ client, bookId }: Props) {
  const [book, setBook] = useState<Book | null>(null);

  useEffect(() => {
    client.getBook(bookId).then(setBook);
  }, [client, bookId]);

  if (book === null) {
    return <p>Loading book...</p>;
  }

  return (
    <article>
      <h1>{book.title}</h1>
      <p>{book.author}</p>
      <p>{book.available ? "Available" : "Checked out"}</p>
      {book.notes && <p>Librarian's note: {book.notes}</p>}
    </article>
  );
}
