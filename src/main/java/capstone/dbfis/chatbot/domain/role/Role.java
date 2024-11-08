package capstone.dbfis.chatbot.domain.role;

import capstone.dbfis.chatbot.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class Role {
    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "role_name", nullable = false)
    private String role_name;

    @Column(name = "delete_auth", nullable = false)
    private boolean delete_auth;

    @Column(name = "edit_auth", nullable = false)
    private boolean edit_auth;

    @Column(name = "create_auth", nullable = false)
    private boolean create_auth;

    @Column(name = "refresh_token")
    private String refreshToken;
}
