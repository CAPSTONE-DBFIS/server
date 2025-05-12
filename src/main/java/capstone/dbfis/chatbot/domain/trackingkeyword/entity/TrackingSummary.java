package capstone.dbfis.chatbot.domain.trackingkeyword.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tracking_Summary")
@Getter
@Setter

public class TrackingSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_keyword_id", nullable = true)
    private TrackingKeyword trackingKeyword;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "created_order")
    private int createdOrder;

    @Column(name = "sentiment_report", columnDefinition = "JSON")
    private String sentimentReport;

    @Column(name = "article_count_report", columnDefinition = "JSON")
    private String articleCountReport;

    @Column(name = "media_companies_report", columnDefinition = "JSON")
    private String mediaCompaniesReport;

    @Column(name = "related_word_report", columnDefinition = "JSON")
    private String relatedWordReport;

    @Column(name = "record_date", columnDefinition = "JSON")
    private String recordDate;

    @Column(name = "article_cnt_change", columnDefinition = "JSON")
    private String articleCntChange;

    @Column(name = "llm_description", columnDefinition = "JSON")
    private String llmDescription;
}
