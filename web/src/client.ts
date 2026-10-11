import { createClient } from "../sdk/generated";

export type ShelfClient = ReturnType<typeof createClient>;

export const client: ShelfClient = createClient("/api");
