package capstone.dbfis.chatbot.domain.passwordreset;

import capstone.dbfis.chatbot.domain.member.Member;
import capstone.dbfis.chatbot.domain.searchhistory.SearchHistoryId;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(PassowrdResetId.class)

public class PasswordReset {

    @Id
    @Column(name = "id")
    private String id;

    @Id
    @Column(name = "memberid")
    private String memberid;

    @Id
    @Column(name = "role")
    private String role;

    @Id
    @Column(name = "department")
    private String department;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "id"),
            @JoinColumn(name = "role", referencedColumnName = "role_id"),
            @JoinColumn(name = "department", referencedColumnName = "department_id")
    })
    private Member member;

    @Column(name = "request_time", nullable = false)
    private String request_time;

    @Column(name = "request_token", nullable = false)
    private LocalDateTime request_token;

    @Column(name = "is_active", nullable = false)
    private LocalDateTime is_active;
}

