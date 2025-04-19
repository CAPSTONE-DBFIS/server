package capstone.dbfis.chatbot.domain.filesharing.controller;

import capstone.dbfis.chatbot.domain.filesharing.dto.CreateFolderRequest;
import capstone.dbfis.chatbot.domain.filesharing.dto.FileDto;
import capstone.dbfis.chatbot.domain.filesharing.dto.FolderDto;
import capstone.dbfis.chatbot.domain.filesharing.service.FileSharingService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileSharingApiController {

    private final FileSharingService fileSharingService;
    private final TokenProvider tokenProvider;

    @PostMapping("/folders")
    @Operation(summary = "새 폴더 생성",
            description = "지정된 팀(teamId) 내에서 parentId 하위에 새로운 가상 폴더를 생성합니다.\n" +
                        "1) Authorization 헤더의 토큰으로 사용자 권한을 확인합니다.\n" +
                        "2) 팀에 속한 멤버만 폴더를 생성할 수 있습니다.\n" +
                        "3) 생성된 폴더의 ID, 이름, 부모 ID, 생성 시간이 반환됩니다.")
    public ResponseEntity<FolderDto> createFolder(@RequestHeader("Authorization") String auth,
                                                  @RequestBody CreateFolderRequest req) {
        String userId = tokenProvider.getMemberId(auth);
        FolderDto folder = fileSharingService.createFolder(req.getTeamId(), req.getParentId(), req.getName(), userId);
        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/folders")
    @Operation(
            summary = "폴더 및 하위 항목 삭제",
            description = "지정된 팀(teamId)과 폴더(folderId)에 대해 해당 폴더와 그 하위 모든 파일·폴더를 재귀적으로 삭제합니다.\n" +
                    "1) S3 스토리지, Milvus 벡터 스토어, DB 메타 데이터를 모두 제거합니다.\n" +
                    "2) 권한이 없는 사용자는 접근할 수 없습니다."
    )
    public ResponseEntity<Void> deleteFolder(@RequestHeader("Authorization") String auth,
                                             @RequestParam Long teamId,
                                             @RequestParam Long folderId) {
        String userId = tokenProvider.getMemberId(auth);
        fileSharingService.deleteFolder(teamId, folderId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/folders")
    @Operation(
            summary = "폴더 목록 조회",
            description = "주어진 팀(teamId)의 특정 parentId 하위에 있는 모든 가상 폴더 목록을 반환합니다.\n" +
                    "parentId가 null이면 루트 폴더만, 인증된 사용자만 접근 가능합니다."
    )
    public ResponseEntity<List<FolderDto>> listFolders(@RequestHeader("Authorization") String auth,
                                                       @RequestParam Long teamId,
                                                       @RequestParam(required = false) Long parentId) {
        String userId = tokenProvider.getMemberId(auth);
        List<FolderDto> list = fileSharingService.listFolders(teamId, parentId, userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    @Operation(
            summary = "파일 목록 조회",
            description = "팀(teamId)과 폴더(folderId)에 속한 모든 파일의 메타 정보를 조회합니다.\n" +
                    "sort 파라미터(asc/desc)로 업로드 날짜 기준 정렬 가능(기본: desc).\n" +
                    "ID, 원본 이름, 크기, 업로드 시간, 업로더 ID, 다운로드 횟수를 반환합니다."
    )
    public ResponseEntity<List<FileDto>> listFiles(@RequestHeader("Authorization") String auth,
                                                   @RequestParam Long teamId,
                                                   @RequestParam(required = false) Long folderId,
                                                   @RequestParam(defaultValue = "desc") String sort) {
        String userId = tokenProvider.getMemberId(auth);
        List<FileDto> files = fileSharingService.listFiles(teamId, folderId, userId, sort);
        return ResponseEntity.ok(files);
    }

    @PostMapping("/upload")
    @Operation(
            summary = "파일 업로드",
            description = "사용자가 선택한 문서 파일을 지정된 팀(teamId)과 폴더(folderId)에 업로드합니다.\n" +
                    "1) S3에 파일 저장\n" +
                    "2) FastAPI 서버에 임베딩 변환 요청\n" +
                    "3) Milvus 벡터 스토어에 임베딩 저장\n" +
                    "4) DB에 메타 정보 저장\n" +
                    "성공 시 FileDto 반환."
    )

    public ResponseEntity<FileDto> uploadFile(@RequestHeader("Authorization") String auth,
                                              @RequestParam Long teamId,
                                              @RequestParam Long folderId,
                                              @RequestParam("file") MultipartFile file) throws Exception {
        String userId = tokenProvider.getMemberId(auth);
        FileDto dto = fileSharingService.uploadTeamFile(file, teamId, folderId, userId);
        return ResponseEntity.ok(dto);
    }



    @GetMapping("/download")
    @Operation(
            summary = "파일 다운로드",
            description = "1) S3에서 fileId에 해당하는 파일을 읽어옵니다.\n" +
                    "2) DB의 다운로드 카운트를 1 증가시킵니다.\n" +
                    "3) Content-Disposition 헤더에 원본 파일명을 설정하여 다운로드를 제공합니다."
    )
    public ResponseEntity<Resource> downloadFile(@RequestHeader("Authorization") String auth,
                                                 @RequestParam Long teamId,
                                                 @RequestParam Long fileId) throws Exception {
        String userId = tokenProvider.getMemberId(auth);
        var result = fileSharingService.downloadFile(teamId, fileId, userId);
        // 파일명 URL 인코딩
        String encodedName = URLEncoder
                .encode(result.originalFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                // Content-Type 헤더 설정
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
                .body(result.resource());
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "파일 삭제",
            description = "지정된 파일(fileId)을 S3, Milvus, RDB에서 모두 삭제합니다.\n" +
                    "1) S3 객체 삭제\n" +
                    "2) Milvus 임베딩 삭제\n" +
                    "3) DB 메타 삭제"
    )
    public ResponseEntity<Void> deleteFile(@RequestHeader("Authorization") String auth,
                                           @RequestParam Long teamId,
                                           @RequestParam Long fileId) {
        String userId = tokenProvider.getMemberId(auth);
        fileSharingService.deleteFile(teamId, fileId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recommend")
    @Operation(
            summary = "추천 파일 조회",
            description = "팀(teamId) 저장소에서 다운로드 수 상위 4개의 파일을 조회하여 반환합니다.\n" +
                    "사용자가 가장 자주 다운로드한 주요 파일을 빠르게 확인할 수 있습니다."
    )
    public ResponseEntity<List<FileDto>> recommendFiles(@RequestHeader("Authorization") String auth,
                                                        @RequestParam Long teamId) {
        String memberId = tokenProvider.getMemberId(auth);
        List<FileDto> recs = fileSharingService.recommendFiles(teamId, memberId);
        return ResponseEntity.ok(recs);
    }
}