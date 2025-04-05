package capstone.dbfis.chatbot.domain.trackingKeyword.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tracking_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_keyword_id")
    private Long trackingKeywordId;

    @Column(name = "collected_date")
    private LocalDate collectedDate;

    @Column(name = "article_count")
    private Integer articleCount;

    @Column(name = "article_title_1")
    private String articleTitle1;

    @Column(name = "article_link_1", length = 500)
    private String articleLink1;

    @Column(name = "comment_count_1")
    private Integer commentCount1;

    @Column(name = "positive_count_1")
    private Integer positiveCount1;

    @Column(name = "negative_count_1")
    private Integer negativeCount1;

    @Column(name = "neutral_count_1")
    private Integer neutralCount1;

    @Column(name = "article_title_2")
    private String articleTitle2;

    @Column(name = "article_link_2", length = 500)
    private String articleLink2;

    @Column(name = "comment_count_2")
    private Integer commentCount2;

    @Column(name = "positive_count_2")
    private Integer positiveCount2;

    @Column(name = "negative_count_2")
    private Integer negativeCount2;

    @Column(name = "neutral_count_2")
    private Integer neutralCount2;

    @Column(name = "article_title_3")
    private String articleTitle3;

    @Column(name = "article_link_3", length = 500)
    private String articleLink3;

    @Column(name = "comment_count_3")
    private Integer commentCount3;

    @Column(name = "positive_count_3")
    private Integer positiveCount3;

    @Column(name = "negative_count_3")
    private Integer negativeCount3;

    @Column(name = "neutral_count_3")
    private Integer neutralCount3;

    @Column(name = "overall_description", columnDefinition = "TEXT")
    private String overallDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_keyword_id", insertable = false, updatable = false)
    private TrackingKeyword trackingKeyword;
}