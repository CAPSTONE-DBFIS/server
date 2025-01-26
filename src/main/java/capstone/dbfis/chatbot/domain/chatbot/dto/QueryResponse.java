package capstone.dbfis.chatbot.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryResponse {
    @JsonProperty("results")  // Flask의 'results' 필드를 매핑
    private Results results;

    @Getter
    @Setter
    public static class Results {
        @JsonProperty("gpt_response")
        private String gptResponse;

        @JsonProperty("query")
        private String query;

        @JsonProperty("search_results")
        private List<SearchResult> searchResults;
    }

    @Getter
    @Setter
    public static class SearchResult {
        private String title;
        private String date;
        private String category;
        private String url;
        private double score;
    }
}