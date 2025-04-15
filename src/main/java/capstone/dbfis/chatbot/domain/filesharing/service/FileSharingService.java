package capstone.dbfis.chatbot.domain.filesharing.service;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@RequiredArgsConstructor
@Service
public class FileSharingService {
    private final AmazonS3 amazonS3;
    @Value("${aws.s3.bucket}")
    private String bucketName;
    private final TeamService teamService;
    private final MemberRepository memberRepository;


    // 파일 업로드 메서드
    public String uploadFile(MultipartFile file, Long teamId, String uploaderId) {
        // 팀에 속해 있는지 검증
        if (!teamService.isUserInTeam(teamId, uploaderId)) {
            throw new IllegalArgumentException("사용자가 해당 팀에 속해있지 않습니다.");
        }

        String fileName = teamId + "/" + file.getOriginalFilename();  // 팀별 폴더에 저장

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize()); // 파일 크기
            metadata.setContentType(file.getContentType()); // 파일 타입
            metadata.addUserMetadata("uploader", URLEncoder.encode(uploaderId, "UTF-8")); // 업로더 ID 추가: UTF-8로 인코딩하지 않을시 오류 발생

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 실패", e);
        }

        return amazonS3.getUrl(bucketName, fileName).toString();  // 파일 URL 반환
    }

    // 파일 다운 메서드
    public Resource downloadFile(Long teamId, String fileName, String downloaderId) {
        // 팀에 속해 있는지 검증
        if (!teamService.isUserInTeam(teamId, downloaderId)) {
            throw new IllegalArgumentException("사용자가 해당 팀에 속해있지 않습니다.");
        }

        try {
            // 파일명 URL 디코딩
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");

            // S3에서 파일 가져오기
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, decodedFileName));

            // S3 객체에서 InputStream을 가져오기
            InputStream inputStream = s3Object.getObjectContent();

            return new InputStreamResource(inputStream);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("파일명 URL 디코딩 실패", e);
        }
    }

    // 파일의 Content-Type을 가져오는 메서드
    public String getFileContentType(String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, decodedFileName));
            return s3Object.getObjectMetadata().getContentType();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("파일명 URL 디코딩 실패", e);
        }
    }

    // 버킷 내 파일 목록 가져오는 메서드
    public List<Map<String, Object>> listFiles(Long teamId, String viewerId) {
        // 팀에 속해 있는지 검증
        if (!teamService.isUserInTeam(teamId, viewerId)) {
            throw new IllegalArgumentException("사용자가 해당 팀에 속해있지 않습니다.");
        }

        String prefix = teamId + "/";  // 팀별 폴더 경로 지정
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix);

        ListObjectsV2Result result = amazonS3.listObjectsV2(request);
        List<S3ObjectSummary> summaries = result.getObjectSummaries();

        List<Map<String, Object>> fileList = new ArrayList<>();
        for (S3ObjectSummary summary : summaries) {
            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("fileName", summary.getKey());  // 파일 이름
            fileMetadata.put("size", summary.getSize());  // 파일 크기
            fileMetadata.put("lastModified", summary.getLastModified());  // 마지막 수정 날짜

            // 게시자 정보를 메타데이터에서 가져오기
            ObjectMetadata metadata = amazonS3.getObjectMetadata(bucketName, summary.getKey());
            String uploaderId = metadata.getUserMetadata().get("uploader");

            // 게시자 이름 가져오기
            String uploaderName = memberRepository.findById(uploaderId)
                    .map(Member::getName)
                    .orElse("undefined");  // 존재하지 않을 경우 undefined

            fileMetadata.put("uploaderId", uploaderId);  // 게시자 ID
            fileMetadata.put("uploaderName", uploaderName);  // 게시자 이름

            fileList.add(fileMetadata);
        }

        return fileList;
    }

    // 파일 삭제 메서드
    public void deleteFile(Long teamId, String fileName, String requesterId) {
        // 팀에 속해 있는지 확인
        if (!teamService.isUserInTeam(teamId, requesterId)) {
            throw new IllegalArgumentException("사용자가 해당 팀에 속해있지 않습니다.");
        }

        // 메타데이터 가져오기
        GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(bucketName, fileName);
        ObjectMetadata metadata;
        try {
            metadata = amazonS3.getObjectMetadata(metadataRequest);
        } catch (AmazonS3Exception e) {
            throw new IllegalStateException("파일 메타데이터를 가져오는 중 오류 발생", e);
        }

        // 파일 메타데이터에서 업로더 ID 확인
        String uploaderId = metadata.getUserMetadata().get("uploader");

        // 팀 리더이거나 게시자 본인만 삭제 가능
        if (uploaderId.equals(requesterId) || teamService.isUserTeamLeader(teamId, requesterId)) {
            // S3에서 파일 삭제
            try {
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            } catch (AmazonS3Exception e) {
                throw new IllegalStateException("파일 삭제 실패: S3에서 오류 발생", e);
            }
        } else {
            throw new IllegalStateException("게시자 또는 팀 리더만 파일을 삭제할 수 있습니다.");
        }
    }


    // 개인 트렌드 보고서 업로드 메서드
    public String uploadPublicReport(MultipartFile file) {
        String key = "reports/" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata));
            return key;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
    // 트렌드 보고서 presigned url 생성 메서드
    public String generatePresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7);  // 링크 7일 유효
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(request).toString();
    }
}


