package capstone.dbfis.chatbot.domain.member.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageResponse {
    private String name;
    private String nickname;
    private String phone;
    private String department;
    private String role;
    private String email;
    private String profileImage;

    private NotificationSettings notifications;
    private List<TeamResponse> teams;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationSettings {
        private boolean messageAlert;
        private boolean threadAlert;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamResponse {
        private Long teamId;
        private String teamName;
        private String teamDescription;
        private List<MemberResponse> members;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberResponse {
        private String name;
        private String id;
        private String phone;
        private String email;
        private String role;
        private String team_role;
    }
}
