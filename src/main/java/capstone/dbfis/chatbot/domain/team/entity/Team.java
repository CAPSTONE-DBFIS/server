package capstone.dbfis.chatbot.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 팀 ID (자동 증가)

    @Column(nullable = false)
    private String name;  // 팀 이름

    private String description;  // 팀 설명

    // 다대다 관계를 매핑하기 위한 중간 테이블 (TeamMember)
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers;
}
