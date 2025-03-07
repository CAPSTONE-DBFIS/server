package capstone.dbfis.chatbot.domain.team.dto;

import capstone.dbfis.chatbot.domain.team.entity.TeamMember;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeamMemberResponse {
    private Long id;
    private String teamRole;  // 팀 내 역할 (리더, 팀원)
    private String role;  // 직무 (프론트/백엔드 개발자, 데이터 분석가 등)
    private String memberId;
    private String memberName;
    private String memberEmail;

    public TeamMemberResponse(TeamMember teamMember) {
        this.id = teamMember.getId();
        this.teamRole = teamMember.getTeam_role();
        this.role = teamMember.getMember().getRole();
        if (teamMember.getMember() != null) {
            this.memberId = teamMember.getMember().getId();
            this.memberName = teamMember.getMember().getName();
            this.memberEmail = teamMember.getMember().getEmail();
        }
    }
}
