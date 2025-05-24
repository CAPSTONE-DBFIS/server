package capstone.dbfis.chatbot.domain.trackingkeyword.entity;

import capstone.dbfis.chatbot.domain.project.entity.TrackingProject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tracking_keywords")
@Getter
@Setter
public class TrackingKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = true)
    private TrackingProject projectId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "requester_id")
    private String requesterId; // JWT 토큰으로 부터 추출된 요청자 ID

    @Column(name = "tracking_interval")
    private int trackingInterval; // 단위: 일

    @OneToMany(mappedBy = "trackingKeyword", cascade = CascadeType.REMOVE)
    private List<TrackingResult> trackingResults;

    @OneToMany(mappedBy = "trackingKeyword", cascade = CascadeType.REMOVE)
    private List<TrackingSummary> trackingSummaries;

}