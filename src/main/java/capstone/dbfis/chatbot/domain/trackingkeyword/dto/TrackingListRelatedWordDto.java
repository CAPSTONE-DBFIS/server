package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class TrackingListRelatedWordDto {
    private Long id;
    private String keyword;
    private String relatedWord;
}
