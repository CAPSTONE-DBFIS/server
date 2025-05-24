package capstone.dbfis.chatbot.domain.filesharing.service;

import capstone.dbfis.chatbot.domain.filesharing.dto.*;
import capstone.dbfis.chatbot.domain.filesharing.entity.File;
import capstone.dbfis.chatbot.domain.filesharing.entity.Folder;
import capstone.dbfis.chatbot.domain.filesharing.repository.FileRepository;
import capstone.dbfis.chatbot.domain.filesharing.repository.FolderRepository;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FileSharingService {

    private static final Logger logger = LoggerFactory.getLogger(FileSharingService.class);

    private final AmazonS3 amazonS3;
    private final TeamService teamService;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final WebClient webClient;
    private final ApplicationContext context;

    private FileSharingService self() {
        return context.getBean(FileSharingService.class); // 프록시 객체 반환
    }

    @Value("${fastapi.url}")
    private String fastapiUrl;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final long TEAM_STORAGE_LIMIT_MB = 1024; // 1GB in MB
    private static final long BYTES_PER_MB = 1024 * 1024; // 1MB in bytes
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "hwp", "txt", "png", "jpg", "jpeg");
    private static final String S3_PATH_FORMAT = "%d/%d/%s";
    private static final String FILENAME_DUPLICATE_FORMAT = "%s(%d)%s";

    /**
     * 팀 접근 권한 체크 메서드
     */
    private void checkTeamAccess(Long teamId, String memberId) {
        // 팀에 속한 멤버인지 확인
        if (!teamService.isUserInTeam(teamId, memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다.");
        }
    }

    /**
     * 가상 폴더 생성
     */
    @Transactional
    public FolderDto createFolder(Long teamId, Long parentId, String name, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 부모 폴더 조회
        Folder parent = findParentFolder(parentId);

        // 폴더 엔티티 생성
        Folder folder = buildFolder(teamId, parent, name);

        // DB 저장
        folder = folderRepository.save(folder);

        // DTO로 변환 후 반환
        return toFolderDto(folder);
    }

    /**
     * 부모 폴더 조회
     */
    private Folder findParentFolder(Long parentId) {
        // 부모 폴더 ID가 없으면 null 반환
        if (parentId == null) {
            return null;
        }
        // 부모 폴더 조회, 없으면 예외
        return folderRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "부모 폴더가 없습니다."));
    }

    /**
     * 폴더 객체 생성
     */
    private Folder buildFolder(Long teamId, Folder parent, String name) {
        // 새로운 폴더 엔티티 빌드
        return Folder.builder()
                .teamId(teamId)
                .parent(parent) // 부모 폴더 설정 (nullable)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * FolderDto로 변환
     */
    private FolderDto toFolderDto(Folder folder) {
        // 폴더 엔티티를 DTO로 변환
        return new FolderDto(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getCreatedAt()
        );
    }

    /**
     * 폴더 내 폴더와 파일들을 반환
     */
    public List<ContentDto> listContents(Long teamId, Long parentId, String memberId, String sortDirection) {
        // 팀 권한 검증
        checkTeamAccess(teamId, memberId);

        // 폴더 목록 조회
        List<ContentDto> folders = getFolderContents(teamId, parentId);

        // 파일 목록 조회
        List<ContentDto> files = getFileContents(teamId, parentId, sortDirection);

        // 폴더와 파일 목록 합쳐서 반환
        return Stream.concat(folders.stream(), files.stream())
                .collect(Collectors.toList());
    }

    /**
     * 폴더 컨텐츠 조회
     */
    private List<ContentDto> getFolderContents(Long teamId, Long parentId) {
        // 팀 ID와 부모 폴더 ID로 폴더 목록 조회
        return folderRepository.findByTeamIdAndParentId(teamId, parentId)
                .stream()
                .map(f -> new ContentDto(f.getId(), f.getName(), "FOLDER", null, null))
                .collect(Collectors.toList());
    }

    /**
     * 파일 컨텐츠 조회
     */
    private List<ContentDto> getFileContents(Long teamId, Long parentId, String sortDirection) {
        // 업로드 시간 기준으로 정렬 설정
        Sort sort = Sort.by("uploadedAt");
        sort = "asc".equalsIgnoreCase(sortDirection) ? sort.ascending() : sort.descending();

        // 팀 ID와 폴더 ID로 파일 목록 조회
        return fileRepository.findByTeamIdAndFolderId(teamId, parentId, sort)
                .stream()
                .map(f -> new ContentDto(f.getId(), f.getOriginalName(), "FILE", f.getSize(), f.getUploaderId()))
                .toList();
    }

    /**
     * 팀 파일 업로드 (S3 + DB + Milvus 저장)
     */
    @Transactional
    public FileDto uploadTeamFile(MultipartFile file, Long teamId, Long folderId, String memberId) throws IOException {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 파일 유효성 검사
        validateFile(file);

        // 원본 파일명 정규화
        String originalName = normalizeKorean(file.getOriginalFilename());
        validateFileName(originalName);
        validateFileExtension(originalName);

        // 업로드 대상 폴더 조회
        Folder folder = findFolder(folderId);

        // 중복 파일명 방지 처리 후 S3 키 생성
        String s3Key = generateUniqueS3Key(teamId, folderId, originalName);

        // S3에 파일 업로드
        uploadToS3(file, s3Key);

        // DB에 파일 메타데이터 저장
        File entity = saveFileMetadata(teamId, folder, s3Key, originalName, file.getSize(), memberId);

        // Milvus에 파일 데이터 전송
        sendToMilvusAsync(teamId, entity.getId(), file);

        // DTO로 변환 후 반환
        return toFileDto(entity);
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일을 선택하세요.");
        }
    }

    /**
     * 파일명 유효성 검사
     */
    private void validateFileName(String originalName) {
        // 파일명이 null이거나 빈 문자열인지 확인
        if (originalName == null || originalName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일명이 유효하지 않습니다.");
        }
    }

    /**
     * 파일 확장자 유효성 검사
     */
    private void validateFileExtension(String filename) {
        // 파일 확장자 확인
        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "지원하지 않는 파일 형식입니다. 허용된 형식: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    /**
     * 업로드 폴더 조회
     */
    private Folder findFolder(Long folderId) {
        // 폴더 ID로 폴더 조회, 없으면 예외
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드 대상 폴더가 없습니다."));
    }

    /**
     * 고유 S3 키 생성
     */
    private String generateUniqueS3Key(Long teamId, Long folderId, String originalName) {
        // 파일명과 확장자 분리
        String baseName = originalName;
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            baseName = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }

        // S3 경로 생성
        String key = String.format(S3_PATH_FORMAT, teamId, folderId, originalName);
        int count = 1;

        // 중복 파일명 확인 및 처리
        while (amazonS3.doesObjectExist(bucketName, key)) {
            String newName = String.format(FILENAME_DUPLICATE_FORMAT, baseName, count++, extension); // ex. 파일(1).pdf
            key = String.format(S3_PATH_FORMAT, teamId, folderId, newName);
        }

        // 최종 S3 키 반환
        return key;
    }

    /**
     * S3에 파일 업로드
     */
    private void uploadToS3(MultipartFile file, String s3Key) throws IOException {
        // S3 객체 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3에 파일 업로드
        amazonS3.putObject(new PutObjectRequest(bucketName, s3Key, file.getInputStream(), metadata));
    }

    /**
     * 파일 메타데이터 저장
     */
    private File saveFileMetadata(Long teamId, Folder folder, String s3Key, String originalName, long size, String memberId) {
        // 파일 엔티티 생성
        File file = File.builder()
                .teamId(teamId)
                .folder(folder)
                .s3Key(s3Key)
                .originalName(originalName)
                .size(size)
                .uploaderId(memberId)
                .uploadedAt(LocalDateTime.now())
                .build();

        // DB에 저장
        return fileRepository.save(file);
    }

    /**
     * Milvus에 비동기 전송
     */
    private void sendToMilvusAsync(Long teamId, Long fileId, MultipartFile file) {
        // Milvus 저장
        try {
            sendFileToFastApiForMilvusAsync(teamId, fileId, file).get();
            logger.info("Milvus에 파일 전송 성공 - teamId: {}, fileId: {}", teamId, fileId);
        } catch (Exception e) {
            logger.error("Milvus 전송 실패 - teamId: {}, fileId: {}", teamId, fileId, e);
            throw new IllegalStateException("Milvus 전송 실패", e);
        }
    }

    /**
     * FileDto로 변환
     */
    private FileDto toFileDto(File entity) {
        // 파일 엔티티를 DTO로 변환
        return new FileDto(
                entity.getId(),
                entity.getOriginalName(),
                entity.getSize(),
                entity.getUploadedAt(),
                entity.getUploaderId(),
                entity.getDownloadCount()
        );
    }

    /**
     * 파일명 정규화 메소드: 한글 파일명 인코딩 문제
     */
    public String normalizeKorean(String input) {
        // 한글 파일명 인코딩 문제 해결을 위해 NFC 정규화
        return input != null ? Normalizer.normalize(input, Normalizer.Form.NFC) : null;
    }

    /**
     * 파일 확장자 가져오는 메소드
     */
    private String getFileExtension(String filename) {
        // 파일명에서 확장자 추출
        return (filename == null || !filename.contains(".")) ? "" : filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 파일 다운로드
     */
    @Transactional
    public S3DownloadResult downloadFile(Long teamId, Long fileId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // DB에서 메타 조회
        File entity = findFile(fileId);

        // S3에서 객체 가져오기
        S3Object s3Object = amazonS3.getObject(bucketName, entity.getS3Key());

        // 다운로드 카운트 증가 및 저장
        incrementDownloadCount(entity);

        // 다운로드 결과 반환
        return new S3DownloadResult(
                new InputStreamResource(s3Object.getObjectContent()),
                s3Object.getObjectMetadata().getContentType(),
                entity.getOriginalName()
        );
    }

    /**
     * 파일 엔티티 조회
     */
    private File findFile(Long fileId) {
        // 파일 ID로 파일 조회, 없으면 예외
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."));
    }

    /**
     * 다운로드 카운트 증가
     */
    private void incrementDownloadCount(File entity) {
        // 다운로드 횟수 증가
        entity.setDownloadCount(entity.getDownloadCount() + 1);
        fileRepository.save(entity);
    }

    /**
     * 파일 삭제 (S3 + DB + Milvus)
     */
    @Transactional
    public void deleteFile(Long teamId, Long fileId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 파일 엔티티 조회
        File file = findFile(fileId);

        // S3 삭제
        amazonS3.deleteObject(bucketName, file.getS3Key());

        // DB 메타 삭제
        fileRepository.deleteById(fileId);

        // Milvus 임베딩 삭제
        try {
            deleteEmbeddingFromFastApi(teamId.toString(), fileId.toString());
            logger.info("파일 임베딩 삭제 성공 - teamId: {}, fileId: {}", teamId, fileId);
        } catch (Exception e) {
            logger.error("파일 임베딩 삭제 실패 - teamId: {}, fileId: {}", teamId, fileId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "임베딩 삭제 실패");
        }
    }

    /**
     * Milvus 임베딩 삭제
     */
    public void deleteEmbeddingFromFastApi(String teamId, String fileId){
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/team-files/{team_id}/{file_id}")
                            .build(teamId, fileId))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("FastAPI 임베딩 삭제 실패 - teamId: {}, fileId: {}, 상태: {}, 응답: {}",
                    teamId, fileId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI 임베딩 삭제 실패");
        }
    }

    /**
     * 전체 경로에서 가장 많이 다운된 파일 4개를 반환하는 메서드
     */
    @Transactional(readOnly = true)
    public List<FileDto> recommendFiles(Long teamId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 다운로드 수 기준 상위 4개 파일 조회
        return fileRepository.findTop4ByTeamIdOrderByDownloadCountDesc(teamId)
                .stream()
                .map(this::toFileDto)
                .collect(Collectors.toList());
    }

    /**
     * 폴더 삭제 (하위 폴더, 파일 포함)
     */
    @Transactional
    public void deleteFolder(Long teamId, Long folderId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 폴더 조회
        Folder folder = findFolder(folderId);

        try {
            // 하위 컨텐츠 삭제
            deleteFolderRecursive(teamId, folderId, memberId);

            // 폴더 자체 삭제
            folderRepository.delete(folder);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "폴더 삭제 실패");
        }
    }

    private void deleteFolderRecursive(Long teamId, Long folderId, String memberId) {
        for (File file : fileRepository.findByTeamIdAndFolderId(teamId, folderId, Sort.unsorted())) {
            // @Transactional이 적용되도록 반드시 self()를 통해 프록시 객체로 호출해야 함
            self().deleteFile(teamId, file.getId(), memberId);
        }

        for (Folder child : folderRepository.findByParentId(folderId)) {
            deleteFolderRecursive(teamId, child.getId(), memberId);
        }
    }


    /**
     * 잔여 용량 계산 (MB 단위)
     */
    public RemainingStorageResponse getRemainingStorage(Long teamId) {
        // DB에서 해당 팀의 모든 파일 크기 합계 (바이트 단위)
        Long usedBytes = fileRepository.sumSizeByTeamId(teamId);
        long usedMb = usedBytes != null ? usedBytes / BYTES_PER_MB : 0L; // 바이트를 MB로 변환

        // 잔여 용량 계산 (MB 단위)
        long remainingMb = Math.max(TEAM_STORAGE_LIMIT_MB - usedMb, 0);

        // 결과 반환
        return new RemainingStorageResponse(TEAM_STORAGE_LIMIT_MB, usedMb, remainingMb);
    }

    /**
     * 파일명 검색
     */
    public List<FileDto> searchFilesByName(Long teamId, String keyword) {
        // 파일명으로 파일 검색
        return fileRepository.searchByNameNative(teamId, keyword)
                .stream()
                .map(this::toFileDto)
                .collect(Collectors.toList());
    }

    /**
     * FastAPI로 파일 비동기 전송 (Milvus 저장)
     */
    private CompletableFuture<Void> sendFileToFastApiForMilvusAsync(Long teamId, Long fileId, MultipartFile file) {
        try {
            // 멀티파트 요청 빌드
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(StandardCharsets.UTF_8);
            builder.addTextBody("team_id", teamId.toString(), ContentType.TEXT_PLAIN);
            builder.addTextBody("file_id", fileId.toString(), ContentType.TEXT_PLAIN);

            // 파일명 인코딩 처리
            String encodedFileName = "UTF-8''" + URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            // 파일 데이터 준비
            ByteArrayBody fileBody = new ByteArrayBody(
                    file.getBytes(),
                    ContentType.create(Objects.requireNonNull(file.getContentType()), StandardCharsets.UTF_8),
                    file.getOriginalFilename()
            );

            // 파일 파트 생성
            FormBodyPartBuilder filePart = FormBodyPartBuilder.create()
                    .setName("file")
                    .setBody(fileBody)
                    .addField("Content-Disposition",
                            String.format("form-data; name=\"file\"; filename*=%s", encodedFileName));
            builder.addPart(filePart.build());

            // 요청 바디 생성
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            var entity = builder.build();
            entity.writeTo(bos);
            String boundary = entity.getContentType().getValue().split("boundary=")[1];

            // HTTP 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fastapiUrl + "/team-files"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bos.toByteArray()))
                    .build();

            // 비동기 요청 전송
            return HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> logger.info("FastAPI 응답: {}", response.body()));
        } catch (Exception e) {
            logger.error("FastAPI 전송 중 오류 - teamId: {}, fileId: {}", teamId, fileId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 팀 파일 기반 RAG 채팅
     */
    public Flux<ServerSentEvent<String>> streamTeamFileChat(String teamId, String query) {
        // FastAPI로 RAG 쿼리 전송
        return webClient.post()
                .uri(fastapiUrl + "/team-file/query")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("team_id", teamId).with("query", query))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> ServerSentEvent.builder(data).build());
    }
}