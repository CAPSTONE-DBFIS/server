package capstone.dbfis.chatbot.domain.trackingKeyword.service;

import capstone.dbfis.chatbot.domain.trackingKeyword.dto.TrackingResultResponse;
import capstone.dbfis.chatbot.domain.trackingKeyword.entity.TrackingKeyword;
import capstone.dbfis.chatbot.domain.trackingKeyword.entity.TrackingResult;
import capstone.dbfis.chatbot.domain.trackingKeyword.repository.TrackingKeywordRepository;
import capstone.dbfis.chatbot.domain.trackingKeyword.repository.TrackingResultRepository;
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
                .orElseThrow(() -> new RuntimeException("Tracking keyword not found."));
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
