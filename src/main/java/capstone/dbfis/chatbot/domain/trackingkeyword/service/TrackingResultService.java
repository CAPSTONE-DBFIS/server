package capstone.dbfis.chatbot.domain.trackingkeyword.service;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingKeyword;
import capstone.dbfis.chatbot.domain.trackingkeyword.entity.TrackingSummary;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingKeywordRepository;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingResultRepository;
import capstone.dbfis.chatbot.domain.trackingkeyword.repository.TrackingSummaryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingResultService {

    private final TrackingKeywordRepository trackingKeywordRepository;
    private final TrackingSummaryRepository trackingSummaryRepository;
    private final TrackingResultRepository trackingResultRepository;

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

        if (trackingResults.isEmpty()) {
            // 기본값 DTO 생성
            TrackingResultResponseDto defaultDto = new TrackingResultResponseDto(
                    0L,
                    keyword.getKeyword(),
                    null,
                    0,
                    null,
                    null
            );
            return List.of(defaultDto);
        }

        return trackingResults.stream()
                .map(result -> new TrackingResultResponseDto(
                        result.getId(),
                        result.getKeyword(),
                        result.getCreatedAt(),
                        result.getCreatedOrder(),
                        result.getArticleCntChange(),
                        result.getLlmDescription()

                ))
                .collect(Collectors.toList());
    }

    public List<TrackingListResponseDto> getListByKeywordId(String memberId, Long projectId) {

        List<TrackingKeyword> keywords = trackingKeywordRepository.findByRequesterIdAndProjectId(memberId, projectId);
        List<Long> keywordIds = keywords.stream().map(TrackingKeyword::getId).collect(Collectors.toList());

        // 1. 기사수 전체 합 (TrackingResult 기준)
        List<Object[]> articleCountResults = trackingResultRepository.sumArticleCountByKeywordIds(keywordIds);
        Map<Long, Long> articleCountMap = articleCountResults.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],   // 키워드 ID
                        row -> (Long) row[1]    // 기사 갯수 합계
                ));

        List<Object[]> latestCreatedOrders = trackingSummaryRepository.findLatestCreatedOrderByKeywordIds(keywordIds);
        Map<Long, Integer> createdOrderMap = latestCreatedOrders.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Integer) row[1]
                ));
        List<TrackingListResponseDto> responseList = new ArrayList<>();

        for (TrackingKeyword keyword : keywords) {
            Long id = keyword.getId();

            LocalDate nextCollectionDate = calculateNextCollectionDate(
                    keyword.getStartDate(),
                    keyword.getEndDate(),
                    keyword.getTrackingInterval()
            );

            TrackingListResponseDto dto = new TrackingListResponseDto();
            dto.setId(id);
            dto.setKeyword(keyword.getKeyword());
            dto.setCreatedAt(nextCollectionDate);
            dto.setCreatedOrder(createdOrderMap.getOrDefault(id, 0));
            dto.setArticleCountReport(articleCountMap.getOrDefault(id, 0L)+"");

            responseList.add(dto);
        }
        return responseList;
    }

    public List<TrackingListRelatedWordDto> getListRelatedWord(String memberId, Long projectId) {

        List<TrackingKeyword> keywords = trackingKeywordRepository.findByRequesterIdAndProjectId(memberId, projectId);
        List<Long> keywordIds = keywords.stream().map(TrackingKeyword::getId).collect(Collectors.toList());

        List<Object[]> latestRelatedWords = trackingResultRepository.findLatestRelatedWords(keywordIds);
        Map<Long, String> relatedWordMap = latestRelatedWords.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));

        ObjectMapper objectMapper = new ObjectMapper();
        List<TrackingListRelatedWordDto> responseList = new ArrayList<>();

        for (TrackingKeyword keyword : keywords) {
            Long id = keyword.getId();
            String keywordStr = keyword.getKeyword();
            String rawRelatedWord = relatedWordMap.getOrDefault(id, "[]");

            try {
                List<List<Object>> wordPairs = objectMapper.readValue(rawRelatedWord, new TypeReference<List<List<Object>>>() {});
                if (wordPairs.isEmpty()) {
                    // 연관어가 아예 없을 경우 하나의 null DTO 추가
                    TrackingListRelatedWordDto dto = new TrackingListRelatedWordDto();
                    dto.setId(id);
                    dto.setKeyword(keywordStr);
                    dto.setRelatedWord(null);
                    responseList.add(dto);
                } else {
                    for (List<Object> pair : wordPairs) {
                        TrackingListRelatedWordDto dto = new TrackingListRelatedWordDto();
                        dto.setId(id);
                        dto.setKeyword(keywordStr);
                        dto.setRelatedWord((String) pair.get(0));
                        responseList.add(dto);
                    }
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 null로 하나 추가
                TrackingListRelatedWordDto dto = new TrackingListRelatedWordDto();
                dto.setId(id);
                dto.setKeyword(keywordStr);
                dto.setRelatedWord(null);
                responseList.add(dto);
            }
        }

        return responseList;
    }


    public static LocalDate calculateNextCollectionDate(LocalDate startDate, LocalDate endDate, int intervalDays) {
        LocalDate today = LocalDate.now();

        if (intervalDays <= 0) return null;

        long daysPassed = ChronoUnit.DAYS.between(startDate, today);
        long intervalsPassed = Math.max(0, daysPassed / intervalDays);

        LocalDate nextCollectionDate = startDate.plusDays((intervalsPassed + 1) * intervalDays);

        // 종료일을 넘으면 null
        return nextCollectionDate.isAfter(endDate) ? null : nextCollectionDate;
    }

    public List<TrackingArticleCountsDto>parseArticleCounts(String memberId, Long keywordId) {

        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "추적 키워드를 찾을 수 없습니다."
                ));

        if (!keyword.getRequesterId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "조회 권한이 없습니다."
            );
        }
        List<TrackingSummary> trackingSummaries = trackingSummaryRepository.findByTrackingKeywordId(keywordId);
        if (trackingSummaries.isEmpty()) {
            TrackingArticleCountsDto defaultDto = new TrackingArticleCountsDto(
                    null,
                    0,
                    0
            );
            return List.of(defaultDto);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        List<TrackingArticleCountsDto> responseList = new ArrayList<>();

        for (TrackingSummary summary : trackingSummaries) {
            int createdOrder = summary.getCreatedOrder();
            List<String> dates;
            List<Integer> articleCounts;

            try {
                dates = objectMapper.readValue(summary.getRecordDate(), new TypeReference<List<String>>() {});
                articleCounts = objectMapper.readValue(summary.getArticleCountReport(), new TypeReference<List<Integer>>() {});
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 오류", e);
            }

            if (dates.size() != articleCounts.size()) {
                throw new IllegalArgumentException("날짜 수와 기사 수가 일치하지 않습니다.");
            }

            for (int i = 0; i < dates.size(); i++) {
                responseList.add(new TrackingArticleCountsDto(dates.get(i), articleCounts.get(i),createdOrder));
            }
        }

        return responseList;
    }

    public List<TrackingSentimentsDto>parseSentimentsCounts(String memberId, Long keywordId) {

        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "추적 키워드를 찾을 수 없습니다."
                ));

        if (!keyword.getRequesterId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "조회 권한이 없습니다."
            );
        }
        List<TrackingSummary> trackingSummaries = trackingSummaryRepository.findByTrackingKeywordId(keywordId);
        if (trackingSummaries.isEmpty()) {
            TrackingSentimentsDto defaultDto = new TrackingSentimentsDto(
                    null,
                    0,
                    0,
                    0,
                    0
            );
            return List.of(defaultDto);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        List<TrackingSentimentsDto> responseList = new ArrayList<>();

        for (TrackingSummary summary : trackingSummaries) {
            int createdOrder = summary.getCreatedOrder();
            List<String> dates;
            List<String> sentimentsJson;

            try {
                dates = objectMapper.readValue(summary.getRecordDate(), new TypeReference<List<String>>() {});
                sentimentsJson = objectMapper.readValue(summary.getSentimentReport(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 오류", e);
            }

            if (dates.size() != sentimentsJson.size()) {
                throw new IllegalArgumentException("날짜 수와 감정 묶음 수가 일치하지 않습니다.");
            }

            for (int i = 0; i < dates.size(); i++) {
                try {
                    Map<String, Integer> sentimentMap = objectMapper.readValue(sentimentsJson.get(i), new TypeReference<Map<String, Integer>>() {
                    });
                    int positive = sentimentMap.getOrDefault("positive", 0);
                    int negative = sentimentMap.getOrDefault("negative", 0);
                    int neutral = sentimentMap.getOrDefault("neutral", 0);

                    responseList.add(new TrackingSentimentsDto(dates.get(i), positive, negative, neutral, createdOrder));
                } catch (JsonProcessingException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "감성 데이터 JSON 파싱 오류", e);
                }
            }
        }

        return responseList;
    }


    public List<TrackingRelatedWordsDto>parseRelatedWordCounts(String memberId, Long keywordId) {

        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "추적 키워드를 찾을 수 없습니다."
                ));

        if (!keyword.getRequesterId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "조회 권한이 없습니다."
            );
        }
        List<TrackingSummary> trackingSummaries = trackingSummaryRepository.findByTrackingKeywordId(keywordId);
        if (trackingSummaries.isEmpty()) {
            TrackingRelatedWordsDto defaultDto = new TrackingRelatedWordsDto(
                    null,    // date
                    null,    // word
                    0,       // frequency
                    0        // createdOrder
            );
            return List.of(defaultDto);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        List<TrackingRelatedWordsDto> responseList = new ArrayList<>();

        for (TrackingSummary summary : trackingSummaries) {
            int createdOrder = summary.getCreatedOrder();
            List<String> dates;
            List<List<List<Object>>> relatedWordsList;

            try {
                dates = objectMapper.readValue(summary.getRecordDate(), new TypeReference<List<String>>() {});
                relatedWordsList = objectMapper.readValue(summary.getRelatedWordReport(), new TypeReference<List<List<List<Object>>>>() {});
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 오류", e);
            }

            if (dates.size() != relatedWordsList.size()) {
                throw new IllegalArgumentException("날짜 수와 연관어 데이터 수가 일치하지 않습니다.");
            }

            for (int i = 0; i < dates.size(); i++) {
                String date = dates.get(i);
                List<List<Object>> wordsForDate = relatedWordsList.get(i);
                for (List<Object> wordEntry : wordsForDate) {
                    String word = (String) wordEntry.get(0);
                    int frequency = (int) wordEntry.get(1);

                    responseList.add(new TrackingRelatedWordsDto(date, word, frequency, createdOrder));
                }
            }
        }

        responseList.sort(Comparator.comparing(TrackingRelatedWordsDto::getDate));
        return responseList;
    }

    public List<TrackingMediaCompanyDto>parseMediaCounts(String memberId, Long keywordId) {

        TrackingKeyword keyword = trackingKeywordRepository.findById(keywordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "추적 키워드를 찾을 수 없습니다."
                ));

        if (!keyword.getRequesterId().equals(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "조회 권한이 없습니다."
            );
        }
        List<TrackingSummary> trackingSummaries = trackingSummaryRepository.findByTrackingKeywordId(keywordId);
        if (trackingSummaries.isEmpty()) {
            TrackingMediaCompanyDto defaultDto = new TrackingMediaCompanyDto(
                    null,    // date
                    null,    // companyName
                    0,       // frequency
                    0        // createdOrder
            );
            return List.of(defaultDto);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        List<TrackingMediaCompanyDto> responseList = new ArrayList<>();

        for (TrackingSummary summary : trackingSummaries) {
            int createdOrder = summary.getCreatedOrder();
            List<String> dates;
            List<String> mediaCompanyJsonList;

            try {
                dates = objectMapper.readValue(summary.getRecordDate(), new TypeReference<List<String>>() {});
                mediaCompanyJsonList = objectMapper.readValue(summary.getMediaCompaniesReport(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 오류", e);
            }

            if (dates.size() != mediaCompanyJsonList.size()) {
                throw new IllegalArgumentException("날짜 수와 언론사 데이터 수가 일치하지 않습니다.");
            }

            for (int i = 0; i < dates.size(); i++) {
                String date = dates.get(i);
                try {
                    Map<String, Integer> companyMap = objectMapper.readValue(mediaCompanyJsonList.get(i), new TypeReference<Map<String, Integer>>() {});
                    for (Map.Entry<String, Integer> entry : companyMap.entrySet()) {
                        String companyName = entry.getKey();
                        int frequency = entry.getValue();
                        responseList.add(new TrackingMediaCompanyDto(date, companyName, frequency, createdOrder));
                    }
                } catch (JsonProcessingException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "언론사 JSON 파싱 오류", e);
                }
            }
        }

        responseList.sort(Comparator.comparing(TrackingMediaCompanyDto::getDate));
        return responseList;
    }
}
