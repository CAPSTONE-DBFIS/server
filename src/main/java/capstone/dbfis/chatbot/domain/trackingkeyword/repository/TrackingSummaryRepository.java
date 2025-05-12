package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingSummaryRepository extends JpaRepository<TrackingSummary, Long> {
    List<TrackingSummary> findByTrackingKeywordId(Long keywordId);
}