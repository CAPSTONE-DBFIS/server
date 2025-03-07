package capstone.dbfis.chatbot.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequest {
    private String id;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String nickname;
    private String department;
    private String role;
    private String interests;
    private String persona_preset;
}
