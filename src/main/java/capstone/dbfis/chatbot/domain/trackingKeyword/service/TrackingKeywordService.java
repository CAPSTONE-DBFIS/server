package capstone.dbfis.chatbot.domain.trackingKeyword.service;

import capstone.dbfis.chatbot.domain.trackingKeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.entity.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        keywords.forEach(keyword -> keyword.getTrackingResults().sort(Comparator.comparing(TrackingResult::getCollectedDate)));

        return keywords.stream()
                .map(TrackingKeywordResponse::new)
                .collect(Collectors.toList());
    }

    // 추적 키워드를 추가하는 메서드
    public TrackingKeywordResponse addTrackingKeyword(String requesterId, AddTrackingKeywordRequest request) {
        LocalDate today = LocalDate.now();

        // 시작 날짜가 오늘 이전이면 예외 처리
        if (request.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("시작 날짜는 오늘 이후로 설정해야 합니다.");
        }

        Optional<TrackingKeyword> existingKeyword = trackingKeywordRepository.findByRequesterIdAndKeyword(requesterId, request.getKeyword());

        // 중복 키워드 예외 처리
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

    // 추적 키워드를 삭제하는 메서드
    public void deleteTrackingKeyword(String requesterId, Long keywordId) {
        // keyword ID 확인
        TrackingKeyword trackingKeyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 키워드입니다."));

        // requesterId 확인
        if (!trackingKeyword.getRequesterId().equals(requesterId)) {
            throw new IllegalArgumentException("해당 키워드를 삭제할 권한이 없습니다.");
        }

        trackingKeywordRepository.delete(trackingKeyword);
    }

    // 추적 키워드의 일자 변경 메서드 (StartDate, EndDate 변경)
    public TrackingKeywordResponse updateTrackingKeywordDate(String requesterId, Long keywordId, UpdateTrackingKeywordRequest request) {
        // keyword ID 확인
        TrackingKeyword trackingKeyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 키워드입니다."));

        // requesterId 확인
        if (!trackingKeyword.getRequesterId().equals(requesterId)) {
            throw new IllegalArgumentException("해당 키워드를 변경할 권한이 없습니다.");
        }

        LocalDate today = LocalDate.now();

        // 시작 날짜 검증
        if (request.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("시작 날짜는 오늘 이후로 설정해야 합니다.");
        }

        trackingKeyword.setStartDate(request.getStartDate());
        trackingKeyword.setEndDate(request.getEndDate());

        trackingKeywordRepository.save(trackingKeyword);

        return new TrackingKeywordResponse(trackingKeyword);
    }
}