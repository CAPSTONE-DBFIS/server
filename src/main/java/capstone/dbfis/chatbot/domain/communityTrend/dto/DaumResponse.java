package capstone.dbfis.chatbot.domain.communityTrend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DaumResponse {
//    title	String	블로그 글 제목
//    contents	String	블로그 글 요약
//    url	String	블로그 글 URL
//    blogname	String	블로그의 이름
//    thumbnail	String	검색 시스템에서 추출한 대표 미리보기 이미지 URL, 미리보기 크기 및 화질은 변경될 수 있음
//    datetime	Datetime	블로그 글 작성시간, ISO 8601
//            [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].000+[tz]
    private String title;           // 제목
    private String contents;        // 본문 내용
    private String url;            // 문서 URL
    private String datetime;       // 작성시간
}
