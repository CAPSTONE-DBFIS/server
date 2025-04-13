package capstone.dbfis.chatbot.domain.trackingKeyword.repository;

import capstone.dbfis.chatbot.domain.trackingKeyword.entity.TrackingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingKeywordRepository extends JpaRepository<TrackingKeyword, Long> {
    List<TrackingKeyword> findByRequesterId(String requesterId);
    Optional<TrackingKeyword> findByRequesterIdAndKeyword(String requesterId, String keyword);
}