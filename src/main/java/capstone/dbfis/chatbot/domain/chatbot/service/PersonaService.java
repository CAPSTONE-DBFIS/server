package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.dto.CreatePersonaRequest;
import capstone.dbfis.chatbot.domain.chatbot.dto.PersonaDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.UpdatePersonaRequest;
import capstone.dbfis.chatbot.domain.chatbot.entity.Persona;
import capstone.dbfis.chatbot.domain.chatbot.repository.PersonaRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final TokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public List<PersonaDto> listAllForUser(String auth) {
        String memberId = tokenProvider.getMemberId(auth);
        return personaRepository.findByOwnerIdOrPresetTrue(memberId).stream()
                .map(p -> PersonaDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .prompt(p.getPrompt())
                        .preset(p.isPreset())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonaDto createPersona(String auth, CreatePersonaRequest req) {
        String memberId = tokenProvider.getMemberId(auth);
        Persona p = Persona.builder()
                .name(req.getName())
                .prompt(req.getPrompt())
                .preset(req.isPreset())
                .ownerId(req.isPreset() ? "SYSTEM" : memberId)
                .createdAt(LocalDateTime.now())
                .build();
        personaRepository.save(p);
        return PersonaDto.builder()
                .id(p.getId())
                .name(p.getName())
                .prompt(p.getPrompt())
                .preset(p.isPreset())
                .build();
    }

    @Transactional
    public void updatePersona(String auth, Long id, UpdatePersonaRequest req) {
        String memberId = tokenProvider.getMemberId(auth);
        Persona p = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona not found"));
        if (!p.isPreset() && !p.getOwnerId().equals(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        p.setName(req.getName());
        p.setPrompt(req.getPrompt());
        p.setActive(req.isActive());
        p.setUpdatedAt(LocalDateTime.now());
        personaRepository.save(p);
    }

    @Transactional
    public void deletePersona(String auth, Long id) {
        String memberId = tokenProvider.getMemberId(auth);
        Persona p = personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona not found"));
        if (p.isPreset() || !p.getOwnerId().equals(memberId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        personaRepository.delete(p);
    }
}