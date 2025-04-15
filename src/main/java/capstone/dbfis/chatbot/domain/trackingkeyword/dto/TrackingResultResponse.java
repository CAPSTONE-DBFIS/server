package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TrackingResultResponse {
    private Long id;
    private LocalDate analysisDate;
    private int articleCount;
    private String summaryReport;
    private String mediaCompanies;
    private String keyword;          // 메인 키워드
    private String relatedKeyword;   // 연관 키워드 (JSON 문자열 형식으로 저장)
}
