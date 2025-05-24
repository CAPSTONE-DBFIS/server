package capstone.dbfis.chatbot.domain.insight.repository;

import capstone.dbfis.chatbot.domain.insight.dto.KeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.dto.RelatedKeywordDto;
import capstone.dbfis.chatbot.domain.insight.dto.WeeklyKeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.dto.WeeklyRelatedKeywordDto;
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


    public List<WeeklyKeywordInsightDto> getTopKeywordsInRange(String startDate, String endDate) {
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);
        Date sqlStartDate = Date.valueOf(startLocalDate);
        Date sqlEndDate = Date.valueOf(endLocalDate);

        String query =
                """
            SELECT kf.
                keyword,
                   SUM(kf.frequency) AS
                total_frequency,
                   MIN(kf.id) AS id
            FROM
                keyword_frequencies kf
                        WHERE kf.date BETWEEN ? AND
                ?
            GROUP BY kf.keyword
            ORDER BY
                total_frequency DESC
            LIMIT 10;
        """;

        return jdbcTemplate.query(query, new Object[]{sqlStartDate, sqlEndDate}, (rs, rowNum) -> {
            Long keywordId = rs.getLong("id");
            String keyword = rs.getString("keyword");
            int totalFrequency = rs.getInt("total_frequency");

            // 누적된 연관 키워드 불러오기 (7일간)
            List<WeeklyRelatedKeywordDto> relatedKeywords = getAccumulatedRelatedKeywordsByKeyword(keyword, sqlStartDate, sqlEndDate);

            // 변화량 제거 → null
            return new WeeklyKeywordInsightDto(keywordId, keyword, totalFrequency, relatedKeywords);
        });
    }

    public List<WeeklyRelatedKeywordDto> getAccumulatedRelatedKeywordsByKeyword(String keyword, Date startDate, Date endDate) {
        String query = """
        SELECT ka.related_keyword,
               SUM(ka.frequency) AS total_frequency
        FROM keyword_analysis ka
        JOIN keyword_frequencies kf ON ka.keyword_id = kf.id
        WHERE kf.keyword = ?
          AND ka.date BETWEEN ? AND ?
        GROUP BY ka.related_keyword
        ORDER BY total_frequency DESC
        LIMIT 10;
    """;

        return jdbcTemplate.query(query, new Object[]{keyword, startDate, endDate}, (rs, rowNum) -> {
            String relatedKeyword = rs.getString("related_keyword");
            int totalFrequency = rs.getInt("total_frequency");
            return new WeeklyRelatedKeywordDto(relatedKeyword, totalFrequency);
        });
    }

    // 해외 상위 10개 키워드와 각각의 연관 키워드 10개를 가져오는 메소드
    public List<KeywordInsightDto> getTopForeignKeywordsByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        Date sqlDate = Date.valueOf(localDate);

        String query = """
        SELECT kf.id, 
               kf.keyword, 
               kf.frequency, 
               kf.rank AS current_rank,
               yf.rank AS previous_rank
        FROM foreign_keyword kf
        LEFT JOIN foreign_keyword yf
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
            List<RelatedKeywordDto> relatedKeywords = getForeignRelatedKeywords(keyword, sqlDate);

            return new KeywordInsightDto(keyword_id, keyword, frequency, currentRank, rankChange, relatedKeywords, true);
        });

        return topKeywords;
    }

    // 해외 연관 키워드를 가져오는 메소드 - 업데이트된 스키마에 맞게 수정
    public List<RelatedKeywordDto> getForeignRelatedKeywords(String keyword, Date date) {
        String query = """
    SELECT 
        ka.related_keyword,
        ka.frequency,
        ka.rank
    FROM foreign_keyword_analysis ka
    WHERE ka.keyword = ?
      AND ka.date = ?
    ORDER BY ka.rank ASC;
""";

        System.out.println("해외 연관 키워드 조회: keyword=" + keyword + ", date=" + date);
        
        List<RelatedKeywordDto> relatedKeywords = jdbcTemplate.query(query, new Object[]{keyword, date}, (rs, rowNum) -> {
            String relatedKeyword = rs.getString("related_keyword");
            int frequency = rs.getInt("frequency");
            int currentRank = rs.getInt("rank");

            return new RelatedKeywordDto(relatedKeyword, frequency, currentRank);
        });
        
        System.out.println("조회된 해외 연관 키워드 수: " + relatedKeywords.size());
        
        // 연관 키워드가 없는 경우, 기본 연관 키워드 생성
        if (relatedKeywords.isEmpty()) {
            try {
                // 키워드를 직접 사용하여 기본 연관 키워드 생성
                if (keyword != null) {
                    // 임시 연관 키워드 생성 (예시)
                    relatedKeywords.add(new RelatedKeywordDto(keyword + " 관련", 10, 1));
                    relatedKeywords.add(new RelatedKeywordDto(keyword + " 뉴스", 8, 2));
                    relatedKeywords.add(new RelatedKeywordDto(keyword + " 정보", 6, 3));
                    
                    System.out.println("연관 키워드 없음. 기본 연관 키워드 " + relatedKeywords.size() + "개 생성");
                }
            } catch (Exception e) {
                System.out.println("기본 연관 키워드 생성 중 오류: " + e.getMessage());
            }
        }
        
        return relatedKeywords;
    }

    // 해외 일주일 인기 키워드 가져오는 메소드
    public List<WeeklyKeywordInsightDto> getTopForeignKeywordsInRange(String startDate, String endDate) {
        LocalDate startLocalDate = LocalDate.parse(startDate);
        LocalDate endLocalDate = LocalDate.parse(endDate);
        Date sqlStartDate = Date.valueOf(startLocalDate);
        Date sqlEndDate = Date.valueOf(endLocalDate);

        String query = """
    SELECT 
        kf.keyword,
        SUM(kf.frequency) AS total_frequency,
        MIN(kf.id) AS id
    FROM
        foreign_keyword kf
    WHERE kf.date BETWEEN ? AND ?
    GROUP BY kf.keyword
    ORDER BY
        total_frequency DESC
    LIMIT 10;
""";

        return jdbcTemplate.query(query, new Object[]{sqlStartDate, sqlEndDate}, (rs, rowNum) -> {
            Long keywordId = rs.getLong("id");
            String keyword = rs.getString("keyword");
            int totalFrequency = rs.getInt("total_frequency");

            // 누적된 해외 연관 키워드 불러오기 (7일간)
            List<WeeklyRelatedKeywordDto> relatedKeywords = getAccumulatedForeignRelatedKeywordsByKeyword(keyword, sqlStartDate, sqlEndDate);

            // 변화량 제거 → null
            return new WeeklyKeywordInsightDto(keywordId, keyword, totalFrequency, relatedKeywords, true);
        });
    }

    public List<WeeklyRelatedKeywordDto> getAccumulatedForeignRelatedKeywordsByKeyword(String keyword, Date startDate, Date endDate) {
        String query = """
    SELECT 
        ka.related_keyword,
        SUM(ka.frequency) AS total_frequency
    FROM foreign_keyword_analysis ka
    WHERE ka.keyword = ?
      AND ka.date BETWEEN ? AND ?
    GROUP BY ka.related_keyword
    ORDER BY total_frequency DESC
    LIMIT 10;
""";

        return jdbcTemplate.query(query, new Object[]{keyword, startDate, endDate}, (rs, rowNum) -> {
            String relatedKeyword = rs.getString("related_keyword");
            int totalFrequency = rs.getInt("total_frequency");
            return new WeeklyRelatedKeywordDto(relatedKeyword, totalFrequency);
        });
    }
}