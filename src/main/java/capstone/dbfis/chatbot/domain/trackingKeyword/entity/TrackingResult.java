package capstone.dbfis.chatbot.domain.trackingKeyword.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tracking_results")
@Getter
@Setter
public class TrackingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_keyword_id")
    private Long trackingKeywordId;

    @Column(name = "article_count")
    private int articleCount;

    @Column(name = "analysis_date")
    private LocalDate analysisDate;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "summary_report", columnDefinition = "TEXT")
    private String summaryReport;

    @Column(name = "media_companies", columnDefinition = "TEXT")
    private String mediaCompanies;

    @Column(name = "keyword")
    private String keyword;          // 메인 키워드

    @Column(name = "related_keyword", columnDefinition = "JSON")
    private String relatedKeyword;   // 연관 키워드 (JSON 문자열 등)
}