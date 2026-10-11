package dev.shelf.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.shelf.models.Hold;
import dev.shelf.models.HoldRequest;
import dev.shelf.models.HoldStatus;
import dev.shelf.services.HoldService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HoldController.class)
class HoldControllerTest {

  private static final Instant PLACED = Instant.parse("2026-03-10T09:00:00Z");

  @Autowired private MockMvc mvc;
  @MockBean private HoldService holds;

  private Hold hold() {
    return new Hold(3L, 1L, 7L, HoldStatus.QUEUED, PLACED, PLACED.plusSeconds(86_400 * 30L));
  }

  @Test
  void placingAHoldReturnsItQueued() throws Exception {
    when(holds.place(1L, new HoldRequest(7L))).thenReturn(hold());

    mvc.perform(
            post("/books/1/holds")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patronId\": 7}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("QUEUED"));
  }

  @Test
  void aHoldWithoutAPatronIsRejected() throws Exception {
    mvc.perform(post("/books/1/holds").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void listsAPatronsHolds() throws Exception {
    when(holds.listForPatron(7L)).thenReturn(List.of(hold()));

    mvc.perform(get("/patrons/7/holds"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].bookId").value(1));
  }

  @Test
  void cancellingAHoldReturnsNoContent() throws Exception {
    mvc.perform(delete("/holds/3")).andExpect(status().isNoContent());

    verify(holds).cancel(3L);
  }
}
