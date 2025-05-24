package capstone.dbfis.chatbot.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateProfileRequest {
    private String name;
    private String nickname;
    private String phone;
    private String profileImage;
    private String department;
    private String role;
}
