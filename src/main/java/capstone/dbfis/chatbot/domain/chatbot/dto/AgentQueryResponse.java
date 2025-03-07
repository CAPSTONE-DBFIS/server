package capstone.dbfis.chatbot.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentQueryResponse {
    @JsonProperty("gpt_response")
    private String gptResponse;

    @JsonProperty("query")
    private String query;
}