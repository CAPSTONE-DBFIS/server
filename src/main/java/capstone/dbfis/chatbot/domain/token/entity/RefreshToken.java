package capstone.dbfis.chatbot.domain.token.entity;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    // Member 엔티티와의 관계 설정
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "member_id", nullable = false, referencedColumnName = "id", unique = true)
    private Member member;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    public RefreshToken(Member member, String refreshToken) {
        this.member = member;
        this.refreshToken = refreshToken;
    }

    public RefreshToken update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        return this;
    }
}
