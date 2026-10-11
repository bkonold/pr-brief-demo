package dev.shelf.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.shelf.models.CheckoutRequest;
import dev.shelf.models.Loan;
import dev.shelf.services.LoanService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

  @Autowired private MockMvc mvc;
  @MockBean private LoanService loans;

  @Test
  void checkoutReturnsTheNewLoan() throws Exception {
    Instant out = Instant.parse("2026-03-02T10:00:00Z");
    when(loans.checkout(new CheckoutRequest(7L, 1L)))
        .thenReturn(new Loan(5L, 1L, 7L, out, out.plusSeconds(86_400), null));

    mvc.perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patronId\": 7, \"bookId\": 1}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(5));
  }
}
