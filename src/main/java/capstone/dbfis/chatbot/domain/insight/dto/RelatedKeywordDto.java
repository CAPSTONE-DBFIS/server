package capstone.dbfis.chatbot.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class RelatedKeywordDto {
    private String relatedKeyword;
    private int frequency;
    private Integer rank;
}