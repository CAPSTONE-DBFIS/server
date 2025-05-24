package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.CreatePersonaRequest;
import capstone.dbfis.chatbot.domain.chatbot.dto.PersonaDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.UpdatePersonaRequest;
import capstone.dbfis.chatbot.domain.chatbot.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "페르소나 조회" , description = "기본 시스템 프리셋 페르소나와 사용자가 등록한 페르소나를 불러옵니다.")
    @GetMapping
    public ResponseEntity<List<PersonaDto>> list(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(personaService.listAllForUser(auth));
    }

    @Operation(summary = "페르소나 등록", description = "사용자의 페르소나 이름, 페르소나 프롬프트(성격, 말투 묘사)를 등록합니다.")
    @PostMapping
    public ResponseEntity<PersonaDto> create(@RequestHeader("Authorization") String auth,
                                             @Valid @RequestBody CreatePersonaRequest req) {
        PersonaDto dto = personaService.createPersona(auth, req);
        return ResponseEntity.status(201).body(dto);
    }

    @Operation(summary = "페르소나 수정", description = "사용자의 페르소나 이름, 페르소나 프롬프트를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@RequestHeader("Authorization") String auth,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdatePersonaRequest req) {
        personaService.updatePersona(auth, id, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "페르소나 삭제", description = "사용자의 페르소나를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String auth,
                                       @PathVariable Long id) {
        personaService.deletePersona(auth, id);
        return ResponseEntity.noContent().build();
    }
}