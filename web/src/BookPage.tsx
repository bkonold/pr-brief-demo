import { useEffect, useState } from "react";
import type { Book, Hold } from "../sdk/generated";
import type { ShelfClient } from "./client";

interface Props {
  client: ShelfClient;
  bookId: number;
  patronId: number;
}

export function BookPage({ client, bookId, patronId }: Props) {
  const [book, setBook] = useState<Book | null>(null);
  const [hold, setHold] = useState<Hold | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    client.getBook(bookId).then(setBook);
  }, [client, bookId]);

  function placeHold() {
    setError(null);
    client.placeHold(bookId, { patronId }).then(setHold, (e: Error) => setError(e.message));
  }

  if (book === null) {
    return <p>Loading book...</p>;
  }

  return (
    <article>
      <h1>{book.title}</h1>
      <p>{book.author}</p>
      <p>{book.available ? "Available" : "Checked out"}</p>
      {!book.available && hold === null && <button onClick={placeHold}>Place hold</button>}
      {hold !== null && <p>You are in line for this book. Your hold lapses on {hold.expiresAt.slice(0, 10)}.</p>}
      {error !== null && <p role="alert">Could not place the hold: {error}</p>}
    </article>
  );
}
