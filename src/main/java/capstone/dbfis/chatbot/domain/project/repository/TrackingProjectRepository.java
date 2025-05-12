package capstone.dbfis.chatbot.domain.project.repository;

import capstone.dbfis.chatbot.domain.project.entity.TrackingProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingProjectRepository extends JpaRepository<TrackingProject, Long> {
    List<TrackingProject> findByTeam_Id(Long teamId);
    Optional<TrackingProject> findByIdAndTeam_IdIn(Long id, List<Long> teamIds);
}
