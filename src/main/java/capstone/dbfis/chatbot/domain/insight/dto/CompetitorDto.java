package capstone.dbfis.chatbot.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CompetitorDto {
    private String name;        // 기업명
    private int articleCount;   // 기사 수
    private double percentage;  // 비율 (%)
} 