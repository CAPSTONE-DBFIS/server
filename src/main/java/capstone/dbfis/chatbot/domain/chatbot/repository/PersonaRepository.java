package capstone.dbfis.chatbot.domain.chatbot.repository;

import capstone.dbfis.chatbot.domain.chatbot.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    List<Persona> findByOwnerIdOrPresetTrue(String ownerId);
}