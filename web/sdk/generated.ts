// Generated from api/openapi.json. Do not edit by hand.

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  available: boolean;
}

export interface BookCreateRequest {
  title: string;
  author: string;
  isbn: string;
}

export interface CheckoutRequest {
  patronId: number;
  bookId: number;
  dueDays: number;
}

export interface Hold {
  id: number;
  bookId: number;
  patronId: number;
  status: HoldStatus;
  placedAt: string;
  expiresAt: string;
}

export interface HoldRequest {
  patronId: number;
}

export const HoldStatus = {
  QUEUED: "QUEUED",
  READY: "READY",
  FULFILLED: "FULFILLED",
  EXPIRED: "EXPIRED",
  CANCELLED: "CANCELLED",
} as const;
export type HoldStatus = (typeof HoldStatus)[keyof typeof HoldStatus];

export interface Loan {
  id: number;
  bookId: number;
  patronId: number;
  checkedOutAt: string;
  dueAt: string;
  returnedAt?: string;
}

export interface Patron {
  id: number;
  name: string;
  email: string;
  memberSince: string;
}

export interface PatronCreateRequest {
  name: string;
  email: string;
}

export function createClient(baseUrl: string) {
  async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const response = await fetch(`${baseUrl}${path}`, {
      method,
      headers: body === undefined ? undefined : { "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    if (!response.ok) {
      throw new Error(`${method} ${path} failed with ${response.status}`);
    }
    return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
  }

  return {
    listBooks: (): Promise<Book[]> => request("GET", "/books"),
    createBook: (body: BookCreateRequest): Promise<Book> => request("POST", "/books", body),
    getBook: (bookId: number): Promise<Book> => request("GET", `/books/${bookId}`),
    checkoutBook: (body: CheckoutRequest): Promise<Loan> => request("POST", "/loans", body),
    returnBook: (loanId: number): Promise<Loan> => request("POST", `/loans/${loanId}/return`),
    placeHold: (bookId: number, body: HoldRequest): Promise<Hold> => request("POST", `/books/${bookId}/holds`, body),
    cancelHold: (holdId: number): Promise<void> => request("DELETE", `/holds/${holdId}`),
    registerPatron: (body: PatronCreateRequest): Promise<Patron> => request("POST", "/patrons", body),
    getPatron: (patronId: number): Promise<Patron> => request("GET", `/patrons/${patronId}`),
    listPatronHolds: (patronId: number): Promise<Hold[]> => request("GET", `/patrons/${patronId}/holds`),
    listPatronLoans: (patronId: number): Promise<Loan[]> => request("GET", `/patrons/${patronId}/loans`),
  };
}
