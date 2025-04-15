package capstone.dbfis.chatbot.domain.team.project.repository;
import capstone.dbfis.chatbot.domain.team.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeam_Id(Long teamId);
}