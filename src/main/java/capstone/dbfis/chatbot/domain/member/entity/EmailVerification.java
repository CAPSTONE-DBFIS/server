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

    @ManyToOne(cascade = CascadeType.REMOVE) // 회원 삭제시 인증 데이터도 함께 삭제
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원과의 관계 설정

    @Column(name = "verification_code", nullable = false)
    private String verificationCode; // 인증 코드

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false; // 인증 여부
}
