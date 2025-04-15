package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingResultRepository extends JpaRepository<TrackingResult, Long> {
    List<TrackingResult> findByTrackingKeywordIdOrderByAnalysisDateDesc(Long keywordId);
    void deleteByTrackingKeywordId(Long trackingKeywordId);
}
