package capstone.dbfis.chatbot.domain.communityTrend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    private String keyword;     // 검색 키워드
    private int maxResults;     // 최대 검색 결과 수
    private int days;           // 검색 기간(일)
}

