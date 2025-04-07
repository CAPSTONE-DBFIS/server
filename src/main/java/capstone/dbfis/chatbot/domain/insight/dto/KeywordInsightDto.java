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
}
