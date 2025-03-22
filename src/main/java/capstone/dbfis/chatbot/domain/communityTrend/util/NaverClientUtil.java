package capstone.dbfis.chatbot.domain.communityTrend.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NaverClientUtil {

    private final RestTemplate restTemplate;

    // RestTemplate 생성자 주입
    public NaverClientUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String get(String url, String clientId, String clientSecret) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", clientId);  // client-id 설정
        headers.add("X-Naver-Client-Secret", clientSecret);  // client-secret 설정

        // 요청 엔티티 생성
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // 응답 반환
        return response.getBody();
    }
}