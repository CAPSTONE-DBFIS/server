package capstone.dbfis.chatbot.domain.filesharing.service;

import capstone.dbfis.chatbot.domain.filesharing.dto.ContentDto;
import capstone.dbfis.chatbot.domain.filesharing.dto.FileDto;
import capstone.dbfis.chatbot.domain.filesharing.dto.FolderDto;
import capstone.dbfis.chatbot.domain.filesharing.dto.S3DownloadResult;
import capstone.dbfis.chatbot.domain.filesharing.entity.File;
import capstone.dbfis.chatbot.domain.filesharing.entity.Folder;
import capstone.dbfis.chatbot.domain.filesharing.repository.FileRepository;
import capstone.dbfis.chatbot.domain.filesharing.repository.FolderRepository;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileSharingService {

    private final AmazonS3 amazonS3;
    private final TeamService teamService;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final RagService ragService;

    @Value("${aws.s3.bucket}")
    private String bucketName; // S3 버킷 이름

    /**
     * 팀 접근 권한 체크 메서드
     */
    private void checkTeamAccess(Long teamId, String memberId) {
        if (!teamService.isUserInTeam(teamId, memberId)) // 팀에 속한 멤버인지 확인
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다.");
    }

    /**
     * 가상 폴더 생성
     */
    @Transactional
    public FolderDto createFolder(Long teamId, Long parentId, String name, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 부모 폴더 조회
        Folder parent = (parentId != null)
                ? folderRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "부모 폴더가 없습니다."))
                : null;

        Folder folder = Folder.builder()
                .teamId(teamId)
                .parent(parent) // 부모 폴더 설정 (nullable)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();

        // DB 저장
        folder = folderRepository.save(folder);

        return new FolderDto(
                folder.getId(),
                folder.getName(),
                parent != null ? parent.getId() : null,
                folder.getCreatedAt()
        );
    }

    /**
     * 폴더 내 폴더와 파일들을 리턴
     */
    public List<ContentDto> listContents(Long teamId, Long parentId, String memberId, String sortDirection) {
        // 팀 권한 검증
        checkTeamAccess(teamId, memberId);

        // 폴더
        List<ContentDto> folders = folderRepository
                .findByTeamIdAndParentId(teamId, parentId)
                .stream()
                .map(f -> new ContentDto(
                        f.getId(), f.getName(), "FOLDER", null, null
                ))
                .collect(Collectors.toList());

        // 파일
        Sort sort = Sort.by("uploadedAt");
        sort = "asc".equalsIgnoreCase(sortDirection) ? sort.ascending() : sort.descending();
        List<ContentDto> files = fileRepository
                .findByTeamIdAndFolderId(teamId, parentId, sort)
                .stream()
                .map(f -> new ContentDto(
                        f.getId(), f.getOriginalName(), "FILE", f.getSize(), f.getUploaderId()
                ))
                .toList();

        // 합쳐서 반환
        folders.addAll(files);
        return folders;
    }

    /**
     * 하위 폴더 목록 조회 메서드
     */
    @Transactional(readOnly = true)
    public List<FolderDto> listFolders(Long teamId, Long parentId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        List<Folder> folders = (parentId == null)
                ? folderRepository.findByTeamIdAndParentIsNull(teamId) // 루트 폴더 조회
                : folderRepository.findByTeamIdAndParentId(teamId, parentId); // 하위 폴더 조회

        // FolderDto 리스트 반환
        return folders.stream()
                .map(f -> new FolderDto(
                        f.getId(),
                        f.getName(),
                        f.getParent() != null ? f.getParent().getId() : null,
                        f.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 팀 파일 업로드 (S3 + RAG + DB 저장)
     */
    @Transactional
    public FileDto uploadTeamFile(MultipartFile file, Long teamId, Long folderId, String memberId) throws IOException {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일을 선택하세요.");
        }

        // 원본 파일명
        String original = file.getOriginalFilename();

        // 중복 파일명 방지 처리
        String key = teamId + "/" + folderId + "/"
                + resolveDuplicate(Objects.requireNonNull(original), teamId, folderId);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(file.getContentType());

        // S3 업로드
        amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), meta));

        // FastApi로 RAG 동기 요청
        ragService.sendToRagSync("team", key, file.getBytes(), memberId, teamId);

        // 업로드 대상 폴더 조회
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "업로드 대상 폴더가 없습니다."));

        // DB에 파일 메타데이터 저장
        File entity = File.builder()
                .teamId(teamId)
                .folder(folder)
                .s3Key(key)
                .originalName(original)
                .size(file.getSize())
                .uploaderId(memberId)
                .uploadedAt(LocalDateTime.now())
                .build();
        entity = fileRepository.save(entity);

        return new FileDto(
                entity.getId(),
                entity.getOriginalName(),
                entity.getSize(),
                entity.getUploadedAt(),
                entity.getUploaderId(),
                0
        );
    }

    /**
     * 특정 폴더 내 파일 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FileDto> listFiles(Long teamId, Long folderId, String memberId, String sortDirection) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // Sort 객체 생성
        Sort sort = Sort.by("uploadedAt");
        sort = "asc".equalsIgnoreCase(sortDirection)
                ? sort.ascending()
                : sort.descending();

        // repository에 Sort 파라미터 넘기기
        List<File> entities = fileRepository.findByTeamIdAndFolderId(teamId, folderId, sort);

        // DTO 변환
        return entities.stream()
                .map(e -> new FileDto(
                        e.getId(),
                        e.getOriginalName(),
                        e.getSize(),
                        e.getUploadedAt(),
                        e.getUploaderId(),
                        e.getDownloadCount()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 파일 다운로드
     */
    @Transactional
    public S3DownloadResult downloadFile(Long teamId, Long fileId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // DB에서 메타 조회
        File entity = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."));

        // S3에서 객체 가져오기
        S3Object obj = amazonS3.getObject(bucketName, entity.getS3Key());
        InputStreamResource resource = new InputStreamResource(obj.getObjectContent());

        // 다운로드 카운트 증가 및 저장
        entity.setDownloadCount(entity.getDownloadCount() + 1);
        fileRepository.save(entity);

        return new S3DownloadResult(
                resource,
                obj.getObjectMetadata().getContentType(),
                entity.getOriginalName()
        );
    }

    /**
     * 파일 삭제 (S3 + FastApi Milvus 파이프라인 + DB)
     */
    @Transactional
    public void deleteFile(Long teamId, Long fileId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        File e = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."));

        // S3 삭제
        amazonS3.deleteObject(bucketName, e.getS3Key());

        // RAG 임베딩 삭제
        ragService.deleteTeamFileEmbeddingSync(e.getOriginalName(), teamId);

        // DB 메타 삭제
        fileRepository.deleteById(fileId);
    }

    /**
     * 개인 트렌드 보고서 업로드
     */
    public String uploadPublicReport(MultipartFile file) {
        String key = "reports/" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)); // 업로드
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공개 리포트 업로드 실패");
        }
        return key;
    }

    /**
     * 챗봇 tool - 트렌드 보고서용 presigned URL 생성
     */
    public String generatePublicReportPresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7); // 7일 유효
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(request).toString();
    }

    /**
     * 중복된 파일명 방지 로직
     */
    private String resolveDuplicate(String original, Long teamId, Long folderId) {
        String base = original, ext = "";

        int dot = original.lastIndexOf('.');

        if (dot >= 0) {
            base = original.substring(0, dot);
            ext = original.substring(dot);
        }

        String key = teamId + "/" + folderId + "/" + original;

        int cnt = 1;

        while (amazonS3.doesObjectExist(bucketName, key)) {
            String name = String.format("%s(%d)%s", base, cnt++, ext);  // ex. 파일(1).pdf
            key = teamId + "/" + folderId + "/" + name;
        }

        // 중복 방지 처리된 파일명 반환
        return key.substring((teamId + "/" + folderId + "/").length());
    }

    /**
     * 전체 경로에서 가장 많이 다운된 파일 4개를 반환하는 메서드
     */
    @Transactional(readOnly = true)
    public List<FileDto> recommendFiles(Long teamId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        List<File> top = fileRepository.findTop4ByTeamIdOrderByDownloadCountDesc(teamId);
        return top.stream()
                .map(e -> new FileDto(
                        e.getId(),
                        e.getOriginalName(),
                        e.getSize(),
                        e.getUploadedAt(),
                        e.getUploaderId(),
                        e.getDownloadCount()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 폴더 삭제 (재귀)
     */
    @Transactional
    public void deleteFolder(Long teamId, Long folderId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "삭제할 폴더가 없습니다."));

        // 폴더 내 파일 삭제
        fileRepository.findByTeamIdAndFolderId(teamId, folderId, Sort.unsorted())
                .forEach(f -> deleteFile(teamId, f.getId(), memberId));

        // 자식 폴더 재귀 삭제
        folderRepository.findByParentId(folderId)
                .forEach(child -> deleteFolder(teamId, child.getId(), memberId));

        // 폴더 자체 삭제
        folderRepository.delete(folder);
    }
}