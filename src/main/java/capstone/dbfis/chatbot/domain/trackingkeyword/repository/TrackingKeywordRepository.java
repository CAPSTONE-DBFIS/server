package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TrackingKeywordRepository extends JpaRepository<TrackingKeyword, Long> {
    List<TrackingKeyword> findByRequesterId(String requesterId);
}