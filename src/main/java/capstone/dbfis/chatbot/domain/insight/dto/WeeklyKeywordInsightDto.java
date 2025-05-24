package capstone.dbfis.chatbot.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class WeeklyKeywordInsightDto {
    private Long keywordId;
    private String keyword;
    private int totalFrequency;
    private List<WeeklyRelatedKeywordDto> relatedKeywords;
    private boolean isForeign;
    
    // 기존 생성자를 유지하기 위한 추가 생성자
    public WeeklyKeywordInsightDto(Long keywordId, String keyword, int totalFrequency, List<WeeklyRelatedKeywordDto> relatedKeywords) {
        this(keywordId, keyword, totalFrequency, relatedKeywords, false);
    }
}