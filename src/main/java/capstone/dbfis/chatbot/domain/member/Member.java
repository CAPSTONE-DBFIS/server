package capstone.dbfis.chatbot.domain.member;

import capstone.dbfis.chatbot.domain.department.Department;
import capstone.dbfis.chatbot.domain.role.Role;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(MemberId.class)
public class Member {

    @Id
    @Column(name= "id")
    private String id;

    @Id
    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Id
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

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

    @Column(name = "refresh_token")
    private String refreshToken;
}
