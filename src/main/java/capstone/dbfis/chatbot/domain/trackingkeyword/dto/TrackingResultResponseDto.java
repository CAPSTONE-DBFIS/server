package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TrackingResultResponseDto {
    private Long id;
    private String keyword;
    private LocalDate createdAt;
    private int createdOrder;
    private String sentimentReport;
    private String articleCountReport;
    private String mediaCompaniesReport;
    private String relatedWordReport;
    private String recordDate;
    private String articleCntChange;
    private String llmDescription;;   // 연관 키워드 (JSON 문자열 형식으로 저장)
}
