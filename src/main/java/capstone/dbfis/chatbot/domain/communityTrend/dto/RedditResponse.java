package capstone.dbfis.chatbot.domain.communityTrend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RedditResponse {
    private RedditData data;

    @Getter
    @Setter
    public static class RedditData {
        private List<RedditChild> children;
    }

    @Getter
    @Setter
    public static class RedditChild {
        private RedditPost data;
    }
}
