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
}