import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test } from "vitest";
import type { Book, Hold, HoldRequest } from "../sdk/generated";
import type { ShelfClient } from "./client";
import { BookPage } from "./BookPage";

const checkedOut: Book = { id: 1, title: "Dune", author: "Frank Herbert", isbn: "9780441172719", available: false };
const onShelf: Book = { ...checkedOut, available: true };

function fakeClient(book: Book, placed: HoldRequest[] = []): ShelfClient {
  return {
    getBook: async () => book,
    placeHold: async (bookId: number, body: HoldRequest): Promise<Hold> => {
      placed.push(body);
      return { id: 3, bookId, patronId: body.patronId, status: "QUEUED", placedAt: "2026-03-10T09:00:00Z", expiresAt: "2026-04-09T09:00:00Z" };
    },
  } as unknown as ShelfClient;
}

test("a checked-out book offers a hold that joins the line", async () => {
  const placed: HoldRequest[] = [];
  render(<BookPage client={fakeClient(checkedOut, placed)} bookId={1} patronId={7} />);

  await userEvent.click(await screen.findByRole("button", { name: "Place hold" }));

  expect(await screen.findByText(/You are in line/)).toBeTruthy();
  expect(placed).toEqual([{ patronId: 7 }]);
});

test("a book on the shelf has no hold button", async () => {
  render(<BookPage client={fakeClient(onShelf)} bookId={1} patronId={7} />);

  await screen.findByText("Available");
  expect(screen.queryByRole("button", { name: "Place hold" })).toBeNull();
});
