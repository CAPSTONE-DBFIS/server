package capstone.dbfis.chatbot.domain.project.entity;

import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
    private String name;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.REMOVE)
    private List<TrackingKeyword> trackingKeywords;
}
