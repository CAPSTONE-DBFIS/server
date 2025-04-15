package capstone.dbfis.chatbot.domain.trackingkeyword.service;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.*;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingKeywordService {

    private final TrackingKeywordRepository trackingKeywordRepository;
    private final TrackingResultRepository trackingResultRepository;

    public TrackingKeyword createKeyword(String requesterId, AddTrackingKeywordRequest request) {
        TrackingKeyword keyword = new TrackingKeyword();
        keyword.setRequesterId(requesterId);
        keyword.setKeyword(request.getKeyword());
        keyword.setStartDate(request.getStartDate());
        keyword.setEndDate(request.getEndDate());
        keyword.setTrackingInterval(request.getTrackingInterval());
        return trackingKeywordRepository.save(keyword);
    }

    public TrackingKeyword updateKeyword(String requesterId, Long id, UpdateTrackingKeywordRequest request) {
        TrackingKeyword keyword = trackingKeywordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("키워드를 찾을 수 없습니다."));

        // 요청자 ID 검증
        if (!keyword.getRequesterId().equals(requesterId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        keyword.setStartDate(request.getStartDate());
        keyword.setEndDate(request.getEndDate());
        keyword.setTrackingInterval(request.getTrackingInterval());
        return trackingKeywordRepository.save(keyword);
    }

    @Transactional
    public void deleteKeyword(String requesterId, Long id) {
        TrackingKeyword keyword = trackingKeywordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("키워드를 찾을 수 없습니다."));
        // 요청자 ID 검증
        if (!keyword.getRequesterId().equals(requesterId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        // 키워드에 연관된 result들도 함께 삭제합니다.
        trackingResultRepository.deleteByTrackingKeywordId(keyword.getId());

        trackingKeywordRepository.delete(keyword);
    }

    public List<TrackingKeyword> getAllKeywords(String requesterId) {
        // TrackingKeywordRepository에 반드시 findByRequesterId(String requesterId) 메서드를 구현해야 합니다.
        return trackingKeywordRepository.findByRequesterId(requesterId);
    }
}