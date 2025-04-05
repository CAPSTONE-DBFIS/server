package capstone.dbfis.chatbot.domain.trackingKeyword.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tracking_keywords")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(name = "requester_id", nullable = false)
    private String requesterId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @OneToMany(mappedBy = "trackingKeyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackingResult> trackingResults;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }
}