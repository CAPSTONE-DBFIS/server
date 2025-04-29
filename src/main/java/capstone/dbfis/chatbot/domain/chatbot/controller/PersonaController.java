package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.CreatePersonaRequest;
import capstone.dbfis.chatbot.domain.chatbot.dto.PersonaDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.UpdatePersonaRequest;
import capstone.dbfis.chatbot.domain.chatbot.service.PersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/persona")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @GetMapping
    public ResponseEntity<List<PersonaDto>> list(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(personaService.listAllForUser(auth));
    }

    @PostMapping
    public ResponseEntity<PersonaDto> create(@RequestHeader("Authorization") String auth,
                                             @Valid @RequestBody CreatePersonaRequest req) {
        PersonaDto dto = personaService.createPersona(auth, req);
        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@RequestHeader("Authorization") String auth,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdatePersonaRequest req) {
        personaService.updatePersona(auth, id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String auth,
                                       @PathVariable Long id) {
        personaService.deletePersona(auth, id);
        return ResponseEntity.noContent().build();
    }
}