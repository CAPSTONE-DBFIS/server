package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingSummaryRepository extends JpaRepository<TrackingSummary, Long> {
    List<TrackingSummary> findByTrackingKeywordId(Long keywordId);

    @Query("SELECT ts.trackingKeyword.id, MAX(ts.createdOrder) FROM TrackingSummary ts " +
            "WHERE ts.trackingKeyword.id IN :keywordIds GROUP BY ts.trackingKeyword.id")
    List<Object[]> findLatestCreatedOrderByKeywordIds(@Param("keywordIds") List<Long> keywordIds);
}