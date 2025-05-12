package capstone.dbfis.chatbot.domain.trackingkeyword.service;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.TrackingResultResponseDto;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingSummary;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingKeywordRepository;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingResultService {

    private final TrackingKeywordRepository trackingKeywordRepository;
    private final TrackingSummaryRepository trackingSummaryRepository;

    /**
     * 특정 키워드에 대한 추적 결과 리스트를 조회합니다.
     */

    public List<TrackingResultResponseDto> getResultsByKeywordId(String memberId, Long keywordId) {

        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "추적 키워드를 찾을 수 없습니다."
                ));

        if (!keyword.getRequesterId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "조회 권한이 없습니다."
            );
        }

        List<TrackingSummary> trackingResults = trackingSummaryRepository.findByTrackingKeywordId(keywordId);

        return trackingResults.stream()
                .map(result -> new TrackingResultResponseDto(
                        result.getId(),
                        result.getKeyword(),
                        result.getCreatedAt(),
                        result.getCreatedOrder(),
                        result.getSentimentReport(),
                        result.getArticleCountReport(),
                        result.getMediaCompaniesReport(),
                        result.getRelatedWordReport(),
                        result.getRecordDate(),
                        result.getArticleCntChange(),
                        result.getLlmDescription()

                ))
                .collect(Collectors.toList());
    }
}
