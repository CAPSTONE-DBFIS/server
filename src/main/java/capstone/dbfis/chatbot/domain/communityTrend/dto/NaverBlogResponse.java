package capstone.dbfis.chatbot.domain.communityTrend.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NaverBlogResponse {
    private String title;
    private String link;
    private String description;
    private String bloggerName;
    private String postDate;

}
