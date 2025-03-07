package capstone.dbfis.chatbot.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String id;
    private String password;
}
