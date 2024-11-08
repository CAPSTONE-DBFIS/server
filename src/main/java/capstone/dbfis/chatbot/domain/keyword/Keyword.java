package capstone.dbfis.chatbot.domain.keyword;

import capstone.dbfis.chatbot.domain.news.News;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class Keyword {
    @Id
    @Column(name = "keyword", updatable = false)
    private String keyword;

    @Column(name = "cnt", nullable = false)
    private int cnt;

    @ManyToOne
    @JoinColumn(name = "title", nullable = false)
    private News title;

    @Column(name = "sentimental")
    private String sentimental;

    @Column(name = "refresh_token")
    private String refreshToken;

}
