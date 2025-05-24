package capstone.dbfis.chatbot.domain.team.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTeamMemberRequest {
    private String memberId;
    private String role;
    private String teamRole;
}
