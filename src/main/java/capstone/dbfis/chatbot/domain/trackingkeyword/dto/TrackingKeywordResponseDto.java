package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TrackingKeywordResponseDto {
    private Long id;
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private int trackingInterval;
    private Long projectId;
}

