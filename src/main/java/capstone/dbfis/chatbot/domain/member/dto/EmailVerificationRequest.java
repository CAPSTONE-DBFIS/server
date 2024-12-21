package capstone.dbfis.chatbot.domain.member.dto;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String memberId;  // 사용자 ID
    private String email;     // 인증 메일을 보낼 주소
}