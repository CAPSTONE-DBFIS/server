package capstone.dbfis.chatbot.domain.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TeamDto {
    private Long teamId;
    private String teamName;
}