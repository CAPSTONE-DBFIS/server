package capstone.dbfis.chatbot.domain.member.dto;

import lombok.Data;

@Data
public class EmailVerificationResponse {
    private boolean success;           // 성공 여부
    private String message;            // 상태 메시지
    private String verificationCode;   // 인증 코드
}