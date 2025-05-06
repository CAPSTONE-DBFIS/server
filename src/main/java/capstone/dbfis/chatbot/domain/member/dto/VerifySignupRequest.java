package capstone.dbfis.chatbot.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifySignupRequest {
    @NotBlank
    private String memberId;

    @NotBlank
    private String verificationCode;
}