package capstone.dbfis.chatbot.domain.communityTrend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YoutubeResponse {
    private String videoId;
    private String title;
    private String description;
    private String channelTitle;
    private String publishedAt;
    private String thumbnailUrl;
    private String videoUrl;
}
