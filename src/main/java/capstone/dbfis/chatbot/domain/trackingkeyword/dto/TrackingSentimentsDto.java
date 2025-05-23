package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class TrackingSentimentsDto {
    private String date;
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
    private int createOrder;
}
