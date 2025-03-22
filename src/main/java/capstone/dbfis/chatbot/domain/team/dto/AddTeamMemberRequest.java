package capstone.dbfis.chatbot.domain.team.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTeamMemberRequest {  // 이름 변경
    private String memberId;
    private String role;
    private String teamRole;
}
