package capstone.dbfis.chatbot.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class KeywordInsightDto {
    private Long id;
    private String keyword;
    private int frequency;
    private int rank;
    private Integer rankChange; // int → Integer로 변경 (null 허용)
    private List<RelatedKeywordDto> relatedKeywords;
    private boolean isForeign; // 해외 인사이트인지 여부
    
    // 기존 생성자를 유지하기 위한 추가 생성자
    public KeywordInsightDto(Long id, String keyword, int frequency, int rank, Integer rankChange, List<RelatedKeywordDto> relatedKeywords) {
        this(id, keyword, frequency, rank, rankChange, relatedKeywords, false);
    }
}
