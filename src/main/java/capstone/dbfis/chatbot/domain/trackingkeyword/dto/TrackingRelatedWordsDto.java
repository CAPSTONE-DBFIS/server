package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingRelatedWordsDto {
    private String date;
    private String word;
    private int frequency;
    private int createdOrder;
}
