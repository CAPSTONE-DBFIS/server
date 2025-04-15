package capstone.dbfis.chatbot.domain.trackingkeyword.service;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.TrackingResultResponse;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingKeywordRepository;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingResultService {

    private final TrackingKeywordRepository trackingKeywordRepository;
    private final TrackingResultRepository trackingResultRepository;

    public List<TrackingResultResponse> getResultsByKeywordId(String memberId, Long keywordId) {
        // 요청한 키워드가 요청자에게 속하는지 검증
        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new RuntimeException("추적 키워드를 찾을 수 없습니다."));
        if (!keyword.getRequesterId().equals(memberId)) {
            throw new RuntimeException("조회 권한이 없습니다.");
        }

        return trackingResultRepository.findByTrackingKeywordIdOrderByAnalysisDateDesc(keywordId)
                .stream()
                .map(r -> new TrackingResultResponse(
                        r.getId(),
                        r.getAnalysisDate(),
                        r.getArticleCount(),
                        r.getSummaryReport(),
                        r.getMediaCompanies(),
                        r.getKeyword(),
                        r.getRelatedKeyword()))
                .collect(Collectors.toList());
    }
}
