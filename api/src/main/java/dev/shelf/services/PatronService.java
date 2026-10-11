package dev.shelf.services;

import dev.shelf.entities.PatronEntity;
import dev.shelf.models.Patron;
import dev.shelf.models.PatronCreateRequest;
import dev.shelf.repositories.PatronRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatronService {

  private final PatronRepository patrons;

  public PatronService(PatronRepository patrons) {
    this.patrons = patrons;
  }

  @Transactional
  public Patron register(PatronCreateRequest request) {
    PatronEntity patron = new PatronEntity(request.name(), request.email());
    return Patron.from(patrons.save(patron));
  }

  @Transactional(readOnly = true)
  public Patron get(long patronId) {
    return patrons
        .findById(patronId)
        .map(Patron::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no such patron"));
  }
}
