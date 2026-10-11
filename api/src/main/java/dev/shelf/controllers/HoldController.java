package dev.shelf.controllers;

import dev.shelf.models.Hold;
import dev.shelf.models.HoldRequest;
import dev.shelf.services.HoldService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HoldController {

  private final HoldService holds;

  public HoldController(HoldService holds) {
    this.holds = holds;
  }

  @PostMapping("/books/{bookId}/holds")
  @ResponseStatus(HttpStatus.CREATED)
  public Hold placeHold(@PathVariable long bookId, @Valid @RequestBody HoldRequest request) {
    return holds.place(bookId, request);
  }

  @GetMapping("/patrons/{patronId}/holds")
  public List<Hold> listPatronHolds(@PathVariable long patronId) {
    return holds.listForPatron(patronId);
  }

  @DeleteMapping("/holds/{holdId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelHold(@PathVariable long holdId) {
    holds.cancel(holdId);
  }
}
