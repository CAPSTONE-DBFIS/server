package capstone.dbfis.chatbot.domain.insight.repository;

import capstone.dbfis.chatbot.domain.insight.dto.KeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.dto.RelatedKeywordDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;

@Repository
public class InsightRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 상위 10개 키워드와 각각의 연관 키워드 10개를 가져오는 메소드
    public List<KeywordInsightDto> getTopKeywordsByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        Date sqlDate = Date.valueOf(localDate);

        String query = """
        SELECT kf.id, 
               kf.keyword, 
               kf.frequency, 
               kf.rank AS current_rank,
               yf.rank AS previous_rank
        FROM keyword_frequencies kf
        LEFT JOIN keyword_frequencies yf
            ON kf.keyword = yf.keyword 
            AND yf.date = CAST(? AS DATE) - INTERVAL '1 day'
        WHERE kf.date = CAST(? AS DATE)
        ORDER BY kf.rank ASC
        LIMIT 10;
    """;

        List<KeywordInsightDto> topKeywords = jdbcTemplate.query(query, new Object[]{sqlDate, sqlDate}, (rs, rowNum) -> {
            Long keyword_id = rs.getLong("id");
            String keyword = rs.getString("keyword");
            int frequency = rs.getInt("frequency");
            int currentRank = rs.getInt("current_rank");

            // previousRank가 null일 수 있으므로 Integer로 받아오기
            Integer previousRank = rs.getObject("previous_rank", Integer.class);

            // previousRank가 null이면 rankChange도 null로 설정
            Integer rankChange = (previousRank != null) ? previousRank - currentRank : null;

            // 연관 키워드 가져오기
            List<RelatedKeywordDto> relatedKeywords = getRelatedKeywords(keyword_id, sqlDate);

            return new KeywordInsightDto(keyword_id, keyword, frequency, currentRank, rankChange, relatedKeywords);
        });

        return topKeywords;
    }

    // 연관 키워드를 가져오는 메소드
    public List<RelatedKeywordDto> getRelatedKeywords(Long keywordId, Date date) {
        String query = """
    SELECT ka.related_keyword,
           ka.frequency,
           ka.rank
    FROM keyword_analysis ka
    WHERE ka.keyword_id = ?
      AND ka.date = ?  -- 현재 날짜 데이터
    ORDER BY ka.rank ASC;
    """;

        List<RelatedKeywordDto> relatedKeywords = jdbcTemplate.query(query, new Object[]{keywordId, date}, (rs, rowNum) -> {
            String relatedKeyword = rs.getString("related_keyword");
            int frequency = rs.getInt("frequency");
            int currentRank = rs.getInt("rank");

            return new RelatedKeywordDto(relatedKeyword, frequency, currentRank);
        });

        return relatedKeywords;
    }
}