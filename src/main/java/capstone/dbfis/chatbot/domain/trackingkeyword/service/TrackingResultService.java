package capstone.dbfis.chatbot.domain.trackingkeyword.service;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.TrackingResultResponse;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingKeywordRepository;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 추적 결과 조회 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
public class TrackingResultService {

    private final TrackingKeywordRepository trackingKeywordRepository;
    private final TrackingResultRepository trackingResultRepository;

    /**
     * 특정 키워드에 대한 추적 결과 리스트를 조회합니다.
     */
    public List<TrackingResultResponse> getResultsByKeywordId(String memberId, Long keywordId) {
        // 해당 멤버의 추적 키워드 조회
        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "추적 키워드를 찾을 수 없습니다."
                ));

        // 추적 키워드의 요청자 ID가 memberId와 일치하는지 검증
        if (!keyword.getRequesterId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "조회 권한이 없습니다."
            );
        }

        // 추적 키워드 결과 리턴
        return trackingResultRepository
                .findByTrackingKeywordIdOrderByAnalysisDateDesc(keywordId)
                .stream()
                .map(r -> new TrackingResultResponse(
                        r.getId(),
                        r.getAnalysisDate(),
                        r.getArticleCount(),
                        r.getSummaryReport(),
                        r.getMediaCompanies(),
                        r.getKeyword(),
                        r.getRelatedKeyword()
                ))
                .collect(Collectors.toList());
    }
}
