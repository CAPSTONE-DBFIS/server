package capstone.dbfis.chatbot.domain.project.repository;
import capstone.dbfis.chatbot.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeam_Id(Long teamId);
}