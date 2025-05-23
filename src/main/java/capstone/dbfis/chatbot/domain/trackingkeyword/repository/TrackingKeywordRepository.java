package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TrackingKeywordRepository extends JpaRepository<TrackingKeyword, Long> {
    @Query("SELECT k FROM TrackingKeyword k " +
            "LEFT JOIN FETCH k.projectId p " +
            "LEFT JOIN FETCH p.team " +
            "WHERE k.requesterId = :requesterId")
    List<TrackingKeyword> findWithProjectAndTeamByRequesterId(@Param("requesterId") String requesterId);

    @Query("SELECT k FROM TrackingKeyword k " +
            "JOIN FETCH k.projectId p " +
            "JOIN FETCH p.team " +
            "WHERE k.requesterId = :requesterId AND k.id = :id")
    Optional<TrackingKeyword> findWithProjectAndTeamByRequesterIdAndId(@Param("requesterId") String requesterId,
                                                                       @Param("id") Long id);
    @Query("SELECT k FROM TrackingKeyword k " +
            "JOIN FETCH k.projectId p " +
            "JOIN FETCH p.team t " +
            "WHERE k.requesterId = :requesterId AND p.id = :projectId")
    List<TrackingKeyword> findByRequesterIdAndProjectId(@Param("requesterId") String requesterId, @Param("projectId") Long projectId);
}