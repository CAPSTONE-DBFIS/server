package capstone.dbfis.chatbot.domain.trackingkeyword.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tracking_keywords")
@Getter
@Setter
public class TrackingKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "requester_id")
    private String requesterId; // JWT 토큰으로 부터 추출된 요청자 ID

    @Column(name = "tracking_interval")
    private int trackingInterval; // 단위: 일

    @Column(name = "last_tracked_at")
    private LocalDate lastTrackedAt;
}