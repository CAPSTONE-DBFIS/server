package capstone.dbfis.chatbot.domain.communityTrend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class RedditPost {
    private String title;
    private String selftext;
    private String url;
    private String author;
    private int score;
    private String subreddit;

    // UNIX 타임스탬프 (내부적으로만 사용)
    private long created_utc;

    // UTC 시간
    private String created_utc_formatted;

    // 한국 시간(KST)
    private String created_kst;

    // created_utc 설정 시 자동으로 시간 변환
    public void setCreated_utc(long created_utc) {
        this.created_utc = created_utc;
        this.created_utc_formatted = convertUtcToUtcString(created_utc);
        this.created_kst = convertUtcToKst(created_utc);
    }

    // UTC UNIX 타임스탬프 → 사람이 읽을 수 있는 UTC 시간 변환
    private String convertUtcToUtcString(long utc) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(utc), ZoneId.of("UTC"));
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC";
    }

    // UTC → 한국 시간(KST) 변환
    private String convertUtcToKst(long utc) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(utc), ZoneId.of("Asia/Seoul"));
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " KST";
    }
}

