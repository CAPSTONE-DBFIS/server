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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.url}")
    private String fastapiBaseUrl;

    private static final String TEAM_FILE_ENDPOINT = "/rag/team";
    private static final String PERSONAL_FILE_ENDPOINT = "/rag/personal";

    /**
     * 개인이나 팀 파일의 텍스트를 추출하여 임베딩 변환 후 Milvus 벡터 스토어에 저장하는 메서드
     */
    public void sendToRagSync(
            String mode,        // "team" or "personal"
            String originalFilename,
            byte[] fileBytes,
            String uploaderId,
            Long teamId         // 팀일 경우만 필요
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
        try {
            restTemplate.postForEntity(targetUrl, request, String.class);
        } catch (Exception ex) {
            log.error("[RAG 파일 업로드 실패] filename={}, mode={}, uploaderId={}, teamId={}", originalFilename, mode, uploaderId, teamId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "RAG 파일 업로드 중 오류가 발생했습니다.",
                    ex
            );
        }
    }

    /**
     * 팀 파일 임베딩 삭제
     */
    public void deleteTeamFileEmbeddingSync(String filename, Long teamId) {
        String url = fastapiBaseUrl
                + TEAM_FILE_ENDPOINT
                + "?filename=" + filename
                + "&team_id=" + teamId;
        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            log.error("[Milvus 벡터 삭제 실패] filename={}, team_id={}", filename, teamId, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Milvus 벡터 삭제 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}
