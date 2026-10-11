import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import type { Book } from "../sdk/generated";
import type { ShelfClient } from "./client";
import { BookListPage } from "./BookListPage";

const dune: Book = { id: 1, title: "Dune", author: "Frank Herbert", isbn: "9780441172719", available: true };
const emma: Book = { id: 2, title: "Emma", author: "Jane Austen", isbn: "9780141439587", available: false };

function fakeClient(books: Book[]): ShelfClient {
  return { listBooks: async () => books } as unknown as ShelfClient;
}

test("lists each book with its availability", async () => {
  render(<BookListPage client={fakeClient([dune, emma])} />);

  expect(await screen.findByText("Dune")).toBeTruthy();
  expect(screen.getByText("Available")).toBeTruthy();
  expect(screen.getByText("Checked out")).toBeTruthy();
});
