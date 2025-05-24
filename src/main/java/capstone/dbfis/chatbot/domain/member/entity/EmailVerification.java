package capstone.dbfis.chatbot.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_verification")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유 ID

    // Member 삭제 시 EmailVerification만 제거될 수 있도록 1:1 관계 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "verification_code", nullable = false)
    private String verificationCode; // 인증 코드
}
