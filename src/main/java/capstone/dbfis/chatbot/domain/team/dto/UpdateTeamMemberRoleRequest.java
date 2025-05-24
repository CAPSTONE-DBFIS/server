package capstone.dbfis.chatbot.domain.team.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTeamMemberRoleRequest {
    private String newRole;
    private String newTeamRole;
}
