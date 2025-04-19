package capstone.dbfis.chatbot.domain.filesharing.service;

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
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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

    // 팀 접근 권한 체크 메서드
    private void checkTeamAccess(Long teamId, String memberId) {
        if (!teamService.isUserInTeam(teamId, memberId)) // 팀에 속한 멤버인지 확인
            throw new IllegalArgumentException("팀 접근 권한이 없습니다.");
    }

    // 가상 폴더 생성
    @Transactional
    public FolderDto createFolder(Long teamId, Long parentId, String name, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 부모 폴더 조회
        Folder parent = (parentId != null)
                ? folderRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("부모 폴더가 없습니다."))
                : null;

        Folder folder = Folder.builder()
                .teamId(teamId)
                .parent(parent) // 부모 폴더 설정 (nullable)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();

        // DB 저장
        folder = folderRepository.save(folder);

        return new FolderDto(folder.getId(), folder.getName(),
                parent != null ? parent.getId() : null,
                folder.getCreatedAt());
    }

    // 하위 폴더 목록 조회 메서드
    @Transactional(readOnly = true)
    public List<FolderDto> listFolders(Long teamId, Long parentId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        List<Folder> folders = (parentId == null)
                ? folderRepository.findByTeamIdAndParentIsNull(teamId) // 루트 폴더 조회
                : folderRepository.findByTeamIdAndParentId(teamId, parentId); // 하위 폴더 조회

        return folders.stream()
                .map(f -> new FolderDto(
                        f.getId(),
                        f.getName(),
                        f.getParent() != null ? f.getParent().getId() : null,
                        f.getCreatedAt()))
                .collect(Collectors.toList()); // FolderDto 리스트 반환
    }

    // 팀 파일 업로드 (S3 + RAG + DB 저장)
    @Transactional
    public FileDto uploadTeamFile(MultipartFile file, Long teamId, Long folderId, String memberId) throws IOException {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // 원본 파일명
        String original = file.getOriginalFilename();

        // 중복 파일명 방지 처리
        String key = teamId + "/" + folderId + "/" + resolveDuplicate(Objects.requireNonNull(original), teamId, folderId);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(file.getContentType());

        // S3 업로드
        amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), meta));

        // FastApi로 RAG 동기 요청
        ragService.sendToRagSync("team", key, file.getBytes(), memberId, teamId);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("업로드 대상 폴더가 없습니다."));

        File entity = File.builder()
                .teamId(teamId)
                .folder(folder)
                .s3Key(key)
                .originalName(original)
                .size(file.getSize())
                .uploaderId(memberId)
                .uploadedAt(LocalDateTime.now())
                .build();

        // DB에 파일 메타데이터 저장
        entity = fileRepository.save(entity);

        return new FileDto(entity.getId(), entity.getOriginalName(), entity.getSize(),
                entity.getUploadedAt(), entity.getUploaderId(), 0); // 응답 DTO
    }

    // 특정 폴더 내 파일 목록 조회
    @Transactional(readOnly = true)
    public List<FileDto> listFiles(Long teamId, Long folderId, String memberId, String sortDirection) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // Spring Data JPA 의 Sort 객체 생성
        Sort sort = Sort.by("uploadedAt");
        sort = "asc".equalsIgnoreCase(sortDirection) ? sort.ascending() : sort.descending();

        // repository 에 Sort 파라미터 넘기기
        List<File> entities =
                fileRepository.findByTeamIdAndFolderId(teamId, folderId, sort);

        // DTO 변환
        return entities.stream()
                .map(e -> new FileDto(
                        e.getId(),
                        e.getOriginalName(),
                        e.getSize(),
                        e.getUploadedAt(),
                        e.getUploaderId(),
                        e.getDownloadCount()))
                .collect(Collectors.toList());
    }

    // 파일 다운로드
    @Transactional
    public S3DownloadResult downloadFile(Long teamId, Long fileId, String memberId) {
        // 팀 접근 권한 체크
        checkTeamAccess(teamId, memberId);

        // DB에서 메타 조회
        File entity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 없습니다."));

        // S3에서 객체 가져오기
        S3Object obj = amazonS3.getObject(bucketName, entity.getS3Key());
        String contentType = obj.getObjectMetadata().getContentType();
        InputStreamResource resource = new InputStreamResource(obj.getObjectContent());

        // 다운로드 카운트 증가 및 저장
        entity.setDownloadCount(entity.getDownloadCount() + 1);
        fileRepository.save(entity);

        // Resource, Content-Type, 그리고 원본 이름을 함께 반환
        return new S3DownloadResult(resource, contentType, entity.getOriginalName());
    }

    // 원본 파일명 반환 메서드 (다운로드 시 사용)
    public String getOriginalName(Long fileId) {
        return fileRepository.findById(fileId)
                .map(File::getOriginalName)
                .orElse(""); // 존재하지 않으면 빈 문자열
    }

    // 파일 삭제 (S3 + FastApi Milvus 파이프라인 + DB)
    @Transactional
    public void deleteFile(Long teamId, Long fileId, String memberId) {
        checkTeamAccess(teamId, memberId); // 권한 체크

        File e = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일이 없습니다."));

        amazonS3.deleteObject(bucketName, e.getS3Key()); // S3 삭제
        ragService.deleteTeamFileEmbeddingSync(e.getOriginalName(), teamId); // RAG에서 삭제
        fileRepository.deleteById(fileId); // DB에서 삭제
    }

    // 개인 트렌드 보고서 업로드
    public String uploadPublicReport(MultipartFile file) {
        String key = "reports/" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)); // 업로드
            return key; // 저장된 키 반환
        } catch (IOException e) {
            throw new RuntimeException("공개 리포트 업로드 실패", e);
        }
    }

    // 챗봇 tool - 트렌드 보고서용 presigned URL 생성
    public String generatePublicReportPresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7); // 7일 유효

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(request).toString();
    }

    // 중복된 파일명 방지 로직
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

    // 전체 경로에서 가장 많이 다운된 파일 4개를 반환하는 메서드
    @Transactional(readOnly = true)
    public List<FileDto> recommendFiles(Long teamId, String memberId) {
        checkTeamAccess(teamId, memberId);
        List<File> top = fileRepository
                .findTop4ByTeamIdOrderByDownloadCountDesc(teamId);
        return top.stream()
                .map(e -> new FileDto(
                        e.getId(),
                        e.getOriginalName(),
                        e.getSize(),
                        e.getUploadedAt(),
                        e.getUploaderId(),
                        e.getDownloadCount()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFolder(Long teamId, Long folderId, String memberId) {
        // 접근 권한 검사
        checkTeamAccess(teamId, memberId);

        // 폴더 존재 확인
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 폴더가 없습니다."));

        // 이 폴더에 속한 파일 먼저 삭제 (S3, RAG, DB)
        List<File> files = fileRepository.findByTeamIdAndFolderId(teamId, folderId, null);
        for (File f : files) {
            // S3 삭제
            amazonS3.deleteObject(bucketName, f.getS3Key());
            // Milvus(RAG) 삭제
            ragService.deleteTeamFileEmbeddingSync(f.getOriginalName(), teamId);
        }
        fileRepository.deleteAll(files);

        // 자식 폴더들을 재귀적으로 삭제
        List<Folder> children = folderRepository.findByParentId(folderId);
        for (Folder child : children) {
            deleteFolder(teamId, child.getId(), memberId);
        }

        // 해당 폴더를 DB에서 삭제
        folderRepository.delete(folder);
    }
}