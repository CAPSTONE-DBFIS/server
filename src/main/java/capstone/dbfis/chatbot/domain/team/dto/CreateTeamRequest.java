package capstone.dbfis.chatbot.domain.team.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeamRequest {
    private String name;
    private String description;
    private String role;
}
