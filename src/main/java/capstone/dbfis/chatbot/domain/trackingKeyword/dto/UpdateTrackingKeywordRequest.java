package capstone.dbfis.chatbot.domain.trackingKeyword.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTrackingKeywordRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private int trackingInterval;
}