package capstone.dbfis.chatbot.domain.trackingKeyword.dto;

import capstone.dbfis.chatbot.domain.trackingKeyword.entity.TrackingResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingResultResponse {
    private Long id;
    private LocalDate collectedDate;
    private Integer articleCount;

    private String articleTitle1;
    private String articleLink1;
    private Integer commentCount1;
    private Integer positiveCount1;
    private Integer negativeCount1;
    private Integer neutralCount1;

    private String articleTitle2;
    private String articleLink2;
    private Integer commentCount2;
    private Integer positiveCount2;
    private Integer negativeCount2;
    private Integer neutralCount2;

    private String articleTitle3;
    private String articleLink3;
    private Integer commentCount3;
    private Integer positiveCount3;
    private Integer negativeCount3;
    private Integer neutralCount3;

    private String overallDescription;

    public TrackingResultResponse(TrackingResult r) {
        this.id = r.getId();
        this.collectedDate = r.getCollectedDate();
        this.articleCount = r.getArticleCount();
        this.articleTitle1 = r.getArticleTitle1();
        this.articleLink1 = r.getArticleLink1();
        this.commentCount1 = r.getCommentCount1();
        this.positiveCount1 = r.getPositiveCount1();
        this.negativeCount1 = r.getNegativeCount1();
        this.neutralCount1 = r.getNeutralCount1();

        this.articleTitle2 = r.getArticleTitle2();
        this.articleLink2 = r.getArticleLink2();
        this.commentCount2 = r.getCommentCount2();
        this.positiveCount2 = r.getPositiveCount2();
        this.negativeCount2 = r.getNegativeCount2();
        this.neutralCount2 = r.getNeutralCount2();

        this.articleTitle3 = r.getArticleTitle3();
        this.articleLink3 = r.getArticleLink3();
        this.commentCount3 = r.getCommentCount3();
        this.positiveCount3 = r.getPositiveCount3();
        this.negativeCount3 = r.getNegativeCount3();
        this.neutralCount3 = r.getNeutralCount3();

        this.overallDescription = r.getOverallDescription();
    }
}