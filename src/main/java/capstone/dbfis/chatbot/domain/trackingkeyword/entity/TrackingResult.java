package capstone.dbfis.chatbot.domain.trackingkeyword.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_keyword_id", nullable = true)
    private TrackingKeyword trackingKeyword;

    @Column(name = "keyword")
    private String keyword;          // 메인 키워드

    @Column(name = "article_count")
    private int articleCount;        //전체기사수

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "media_companies", columnDefinition = "TEXT") //json 문자열
    private String mediaCompanies;

    @Column(name = "sentiment_count", columnDefinition = "TEXT")
    private String sentimentCount;

    @Column(name = "related_keyword", columnDefinition = "JSON")
    private String relatedKeyword;   // 연관 키워드 (JSON 문자열 등)
}
