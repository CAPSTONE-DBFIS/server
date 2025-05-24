package capstone.dbfis.chatbot.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String token; // 비밀번호 재설정 토큰
    private String newPassword; // 새 비밀번호
}
