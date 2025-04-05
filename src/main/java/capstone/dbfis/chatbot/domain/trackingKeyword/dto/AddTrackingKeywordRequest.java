package capstone.dbfis.chatbot.domain.trackingKeyword.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddTrackingKeywordRequest {
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
}