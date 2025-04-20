package capstone.dbfis.chatbot.domain.filesharing.dto;

import org.springframework.core.io.Resource;

public record S3DownloadResult(
        Resource resource,
        String contentType,
        String originalFilename
) {}
