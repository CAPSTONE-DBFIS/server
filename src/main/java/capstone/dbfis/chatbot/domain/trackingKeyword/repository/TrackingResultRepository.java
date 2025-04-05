package capstone.dbfis.chatbot.domain.trackingKeyword.repository;

import capstone.dbfis.chatbot.domain.trackingKeyword.entity.TrackingResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingResultRepository extends JpaRepository<TrackingResult, Long> {
}