package capstone.dbfis.chatbot.domain.filesharing.controller;

import capstone.dbfis.chatbot.domain.filesharing.dto.*;
import capstone.dbfis.chatbot.domain.filesharing.service.FileSharingService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Team FileSharing API", description = "팀 내 파일 공유 API")
@RequestMapping("/api/teams/{teamId}")
public class FileSharingApiController {

    private final FileSharingService fileSharingService;
    private final TokenProvider tokenProvider;

    /**
     * 새 팀 폴더 생성
     */
    @Operation(summary = "새 팀 폴더 생성", description = "팀 내에 새 가상 폴더를 생성합니다.")
    @PostMapping("/folders")
    public ResponseEntity<FolderDto> createFolder(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable Long teamId,
            @RequestBody CreateFolderRequest req
    ) {
        String memberId = tokenProvider.getMemberId(auth);
        FolderDto folder = fileSharingService.createFolder(teamId, req.getParentId(), req.getName(), memberId);
        return ResponseEntity.status(201).body(folder);
    }

    /**
     * 팀 폴더 삭제 (하위 폴더·파일 전체 재귀 삭제)
     */
    @Operation(summary = "폴더 삭제", description = "지정 폴더와 그 하위 파일·폴더를 모두 삭제합니다. (S3, DB, Milvus 삭제 포함)")
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable Long teamId,
            @PathVariable Long folderId
    ) {
        String memberId = tokenProvider.getMemberId(auth);
        fileSharingService.deleteFolder(teamId, folderId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 폴더·파일 목록 조회
     */
    @Operation(
            summary = "폴더·파일 목록 조회",
            description = "특정 폴더(또는 루트)의 하위 폴더와 파일을 반환합니다. 루트의 경우: /folders/contents 엔드포인트, 폴더의 경우: /folders/{folderId}/contents 엔드포인트 사용"
    )
    @GetMapping({"/folders/{folderId}/contents", "/folders/contents"})
    public ResponseEntity<List<ContentDto>> listContents(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable Long teamId,
            @PathVariable(required = false) Long folderId,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        String memberId = tokenProvider.getMemberId(auth);
        List<ContentDto> contents = fileSharingService
                .listContents(teamId, folderId, memberId, sort);
        return ResponseEntity.ok(contents);
    }

    /**
     * 파일 업로드
     */
    @Operation(summary = "파일 업로드", description = "팀 폴더에 문서 파일을 업로드하고 S3, DB에 파일, 메타데이터를 저장하고, Milvus에 임베딩을 저장합니다.")
    @PostMapping("/folders/{folderId}/files")
    public ResponseEntity<FileDto> uploadFile(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable Long teamId,
            @PathVariable Long folderId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String memberId = tokenProvider.getMemberId(auth);
        FileDto dto = fileSharingService.uploadTeamFile(file, teamId, folderId, memberId);
        return ResponseEntity.status(201).body(dto);
    }

    /**
     * 파일 다운로드
     */
    @Operation(summary = "파일 다운로드", description = "S3에서 파일을 읽어 다운로드합니다.")
    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable Long teamId,
            @PathVariable Long fileId
    ) {
        String memberId = tokenProvider.getMemberId(auth);
        var result = fileSharingService.downloadFile(teamId, fileId, memberId);
        String encodedName = URLEncoder.encode(result.originalFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
                .body(result.resource());
    }

    /**
     * 파일 삭제
     */
    @Operation(summary = "파일 삭제", description = "파일을 S3, DB, Milvus에서 모두 제거합니다.")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable Long teamId,
            @PathVariable Long fileId
    ) {
        String memberId = tokenProvider.getMemberId(auth);
        fileSharingService.deleteFile(teamId, fileId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 추천 파일 조회
     */
    @Operation(summary = "추천 파일 조회", description = "다운로드 상위 4개 파일을 반환합니다.")
    @GetMapping("/files/recommend")
    public ResponseEntity<List<FileDto>> recommendFiles(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable @Min(1) Long teamId
    ) {
        String memberId = tokenProvider.getMemberId(auth);
        List<FileDto> recs = fileSharingService.recommendFiles(teamId, memberId);
        return ResponseEntity.ok(recs);
    }

    /**
     * 파일명 검색
     * */
    @Operation(summary = "팀 내 파일명 검색", description = "파일명에 포함된 키워드로 팀 내 파일을 조회합니다.")
    @GetMapping("/files/search")
    public ResponseEntity<List<FileDto>> searchFilesByName(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable @Min(1) Long teamId,
            @RequestParam("name") @NotBlank String nameKeyword
    ) {
        tokenProvider.getMemberId(auth);
        List<FileDto> results = fileSharingService.searchFilesByName(teamId, nameKeyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 스토리지 잔여 용량 조회
     * */
    @Operation(summary = "팀 스토리지 잔여 용량 조회", description = "팀 당 스토리지 한도를 1GB로 설정하고, 현재 사용량(MB), 잔여 용량(MB)을 반환합니다.")
    @GetMapping("/storage/remaining")
    public ResponseEntity<RemainingStorageResponse> getRemainingStorage(
            @RequestHeader("Authorization") @NotBlank String auth,
            @PathVariable @Min(1) Long teamId
    ) {
        tokenProvider.getMemberId(auth);
        RemainingStorageResponse resp = fileSharingService.getRemainingStorage(teamId);
        return ResponseEntity.ok(resp);
    }

    /**
     * 팀 공유 파일 기반 RAG 응답 챗봇
     * */
    @Operation(summary = "팀 공유 파일 기반 챗봇", description = "팀 공유 파일 기반 RAG 응답 챗봇")
    @PostMapping("/files/query")
    public Flux<ServerSentEvent<String>> proxyTeamFilesQuery(
            @PathVariable("teamId") String teamId,
            @RequestParam("query") String query
    ) {
        return fileSharingService.streamTeamFileChat(teamId, query);
    }
}