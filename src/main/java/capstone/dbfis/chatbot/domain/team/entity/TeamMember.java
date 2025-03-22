package capstone.dbfis.chatbot.domain.team.entity;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 중간 테이블 PK

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;  // 팀 정보

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 회원 정보
    private String role; // 직무
    private String teamRole;  // 팀 내 역할 (리더, 팀원)
}

