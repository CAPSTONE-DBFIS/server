package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TrackingKeywordRepository extends JpaRepository<TrackingKeyword, Long> {
    List<TrackingKeyword> findByRequesterId(String requesterId);
    @Query("SELECT k FROM TrackingKeyword k " +
            "LEFT JOIN FETCH k.projectId p " +
            "LEFT JOIN FETCH p.team " +
            "WHERE k.requesterId = :requesterId")
    List<TrackingKeyword> findWithProjectAndTeamByRequesterId(@Param("requesterId") String requesterId);
}