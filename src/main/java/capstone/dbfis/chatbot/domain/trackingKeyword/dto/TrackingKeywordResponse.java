package capstone.dbfis.chatbot.domain.trackingKeyword.dto;

import capstone.dbfis.chatbot.domain.trackingKeyword.entity.TrackingKeyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingKeywordResponse {
    private Long id;
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TrackingResultResponse> results;

    public TrackingKeywordResponse(TrackingKeyword trackingKeyword) {
        this.id = trackingKeyword.getId();
        this.keyword = trackingKeyword.getKeyword();
        this.startDate = trackingKeyword.getStartDate();
        this.endDate = trackingKeyword.getEndDate();
        this.results = trackingKeyword.getTrackingResults() != null
                ? trackingKeyword.getTrackingResults().stream()
                .map(TrackingResultResponse::new)
                .collect(Collectors.toList())
                : new ArrayList<>();
    }
}