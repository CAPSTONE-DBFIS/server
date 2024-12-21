package capstone.dbfis.chatbot.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequest {
    private String name;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    private String interests;
    private String department;
    private String persona_preset;
}
