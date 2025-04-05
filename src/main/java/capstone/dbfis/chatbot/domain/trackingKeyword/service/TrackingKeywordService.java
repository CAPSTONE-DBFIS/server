package capstone.dbfis.chatbot.domain.trackingKeyword.service;

import capstone.dbfis.chatbot.domain.trackingKeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.entity.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackingKeywordService {

    private final TrackingKeywordRepository trackingKeywordRepository;

    // 사용자의 모든 추적 키워드와 추적 결과를 리턴하는 메서드
    public List<TrackingKeywordResponse> getAllKeywordsAndResultsByRequesterId(String requesterId) {
        List<TrackingKeyword> keywords = trackingKeywordRepository.findByRequesterId(requesterId);
        // TrackingKeyword에서 TrackingResult를 collectedDate 기준으로 오름차순 정렬
        keywords.forEach(keyword -> {
            keyword.getTrackingResults().sort(Comparator.comparing(TrackingResult::getCollectedDate)); // collectedDate 기준 오름차순
        });

        return keywords.stream()
                .map(TrackingKeywordResponse::new)
                .collect(Collectors.toList());
    }

    // 추적 키워드를 추가하는 메서드
    public TrackingKeywordResponse addTrackingKeyword(String requesterId, AddTrackingKeywordRequest request) {
        // 중복 키워드 예외 처리
        Optional<TrackingKeyword> existingKeyword = trackingKeywordRepository.findByRequesterIdAndKeyword(requesterId, request.getKeyword());

        if (existingKeyword.isPresent()) {
            throw new IllegalArgumentException("이미 동일한 키워드로 등록된 추적 키워드가 존재합니다.");
        }

        TrackingKeyword trackingKeyword = TrackingKeyword.builder()
                .keyword(request.getKeyword())
                .requesterId(requesterId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        trackingKeywordRepository.save(trackingKeyword);

        return new TrackingKeywordResponse(trackingKeyword);
    }
}