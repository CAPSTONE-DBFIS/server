package capstone.dbfis.chatbot.domain.trackingkeyword.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingMediaCompanyDto {
    private String date;
    private String companyName;
    private int frequency;
    private int createdOrder;
}
