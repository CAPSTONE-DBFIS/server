package capstone.dbfis.chatbot.domain.filesharing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.url}")
    private String fastapiBaseUrl;

    private static final String TEAM_FILE_ENDPOINT = "/rag/team";
    private static final String PERSONAL_FILE_ENDPOINT = "/rag/personal";

    // 개인이나 팀 파일의 텍스트를 추출하여 임베딩 변환 후 Milvus 벡터 스토어에 저장하는 메서드
    public void sendToRagSync(
            String mode, // "team" or "personal"
            String originalFilename,
            byte[] fileBytes,
            String uploaderId,
            Long teamId                // 팀일 경우만 필요
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        });
        body.add("uploader_id", uploaderId);

        if ("team".equalsIgnoreCase(mode)) {
            body.add("team_id", teamId);
        }

        String endpoint = "team".equalsIgnoreCase(mode) ? TEAM_FILE_ENDPOINT : PERSONAL_FILE_ENDPOINT;
        String targetUrl = fastapiBaseUrl + endpoint;

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(targetUrl, request, String.class);
    }

    public void deleteTeamFileEmbeddingSync(String filename, Long teamId) {
        try {
            String url = fastapiBaseUrl
                + TEAM_FILE_ENDPOINT
                + "?filename=" + filename
                + "&team_id=" + teamId;
            restTemplate.delete(url);
        } catch (Exception e) {
            log.error("[Milvus 벡터 삭제 실패] filename={}, team_id={}", filename, teamId, e);
        }
    }
}
