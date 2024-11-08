package capstone.dbfis.chatbot.domain.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class News {
    @Id
    @Column(name = "title", updatable = false)
    private String title;

    @Column(name = "thumbnailurl", nullable = false)
    private String thumbnailurl;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "media", nullable = false)
    private String media;

    @Column(name = "comment_count", nullable = false)
    private String comment_count;
}
