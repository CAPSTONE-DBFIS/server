package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddTrackingKeywordRequest {
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private int trackingInterval;
}
