package capstone.dbfis.chatbot.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Member {
    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "interests")
    private String interests;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "persona_preset")
    private int personaPreset;
}
