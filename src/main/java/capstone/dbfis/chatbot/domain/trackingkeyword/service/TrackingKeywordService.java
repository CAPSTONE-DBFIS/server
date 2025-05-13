package capstone.dbfis.chatbot.domain.trackingkeyword.service;

import capstone.dbfis.chatbot.domain.project.repository.TrackingProjectRepository;
import capstone.dbfis.chatbot.domain.trackingkeyword.dto.AddTrackingKeywordRequest;
import capstone.dbfis.chatbot.domain.trackingkeyword.dto.TrackingKeywordResponseDto;
import capstone.dbfis.chatbot.domain.trackingkeyword.dto.UpdateTrackingKeywordRequest;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 추적 키워드 관리 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
public class TrackingKeywordService {

    private final TrackingKeywordRepository trackingKeywordRepository;
    private final TrackingProjectRepository trackingProjectRepository;

    /**
     * 새로운 추적 키워드를 생성합니다.
     */
    public TrackingKeywordResponseDto createKeyword(String requesterId, AddTrackingKeywordRequest request) {
        TrackingKeyword keyword = new TrackingKeyword();
        keyword.setRequesterId(requesterId);
        keyword.setKeyword(request.getKeyword());
        keyword.setStartDate(request.getStartDate());
        keyword.setEndDate(request.getEndDate());
        keyword.setTrackingInterval(request.getTrackingInterval());
        keyword.setProjectId(
                trackingProjectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found"))
        );

        TrackingKeyword saved = trackingKeywordRepository.save(keyword);

        return new TrackingKeywordResponseDto(
                saved.getId(),
                saved.getKeyword(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getTrackingInterval(),
                saved.getProjectId() != null ? saved.getProjectId().getId() : null
        );
    }

    /**
     * 기존 추적 키워드를 수정합니다.
     */
    public TrackingKeyword updateKeyword(String requesterId, Long id, UpdateTrackingKeywordRequest request) {
        // 멤버의 추적 키워드 조회
        TrackingKeyword keyword = trackingKeywordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "키워드를 찾을 수 없습니다."
                ));

        // 멤버의 ID와 해당 키워드의 요청자 ID가 동일한지 검증
        if (!keyword.getRequesterId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        keyword.setStartDate(request.getStartDate());
        keyword.setEndDate(request.getEndDate());
        keyword.setTrackingInterval(request.getTrackingInterval());
        return trackingKeywordRepository.save(keyword);
    }

    /**
     * 추적 키워드를 삭제합니다.
     */
    @Transactional
    public void deleteKeyword(String requesterId, Long id) {
        // 멤버의 추적 키워드 조회
        TrackingKeyword keyword = trackingKeywordRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "키워드를 찾을 수 없습니다."
                ));

        // 추적 키워드의 요청자 ID와 멤버 ID가 일치하는지 검증
        if (!keyword.getRequesterId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        trackingKeywordRepository.delete(keyword);

    }

    /**
     * 사용자가 생성한 모든 추적 키워드를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TrackingKeywordResponseDto> getAllKeywords(String requesterId) {

        List<TrackingKeyword> keywords = trackingKeywordRepository.findWithProjectAndTeamByRequesterId(requesterId);
        return keywords.stream()
                .map(k -> new TrackingKeywordResponseDto(
                        k.getId(),
                        k.getKeyword(),
                        k.getStartDate(),
                        k.getEndDate(),
                        k.getTrackingInterval(),
                        k.getProjectId() != null ? k.getProjectId().getId() : null
                ))
                .collect(Collectors.toList());
    }
}

