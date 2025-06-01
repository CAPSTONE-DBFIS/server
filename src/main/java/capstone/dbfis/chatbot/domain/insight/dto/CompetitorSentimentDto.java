package capstone.dbfis.chatbot.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CompetitorSentimentDto {
    private String name;           // 기업명
    private int positiveCount;     // 긍정 기사 수
    private int negativeCount;     // 부정 기사 수 
    private int neutralCount;      // 중립 기사 수
    private double positiveRate;   // 긍정 비율 (%)
    private double negativeRate;   // 부정 비율 (%)
    private double neutralRate;    // 중립 비율 (%)
} 