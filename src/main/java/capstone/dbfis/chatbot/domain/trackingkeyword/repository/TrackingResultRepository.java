package capstone.dbfis.chatbot.domain.trackingkeyword.repository;

import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingResult;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TrackingResultRepository extends JpaRepository<TrackingResult, Long>{
    @Query("SELECT tr.trackingKeyword.id, SUM(tr.articleCount) FROM TrackingResult tr " +
            "WHERE tr.trackingKeyword.id IN :keywordIds " +
            "GROUP BY tr.trackingKeyword.id")
    List<Object[]> sumArticleCountByKeywordIds(@Param("keywordIds") List<Long> keywordIds);

    @Query("SELECT tr.trackingKeyword.id, tr.relatedKeyword " +
            "FROM TrackingResult tr " +
            "WHERE tr.createdAt IN (" +
            "   SELECT MAX(tr2.createdAt) FROM TrackingResult tr2 " +
            "   WHERE tr2.trackingKeyword.id = tr.trackingKeyword.id" +
            ") AND tr.trackingKeyword.id IN :keywordIds")
    List<Object[]> findLatestRelatedWords(@Param("keywordIds") List<Long> keywordIds);
}
