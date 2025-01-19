package capstone.dbfis.chatbot.domain.communityTrend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditTokenResponse {
    private String access_token;
    private String token_type;
    private Integer expires_in;
    private String scope;

    public String getAccessToken() {
        return access_token;
    }
}
