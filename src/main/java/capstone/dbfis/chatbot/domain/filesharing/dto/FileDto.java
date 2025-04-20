package capstone.dbfis.chatbot.domain.filesharing.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileDto {
    private Long id;
    private String originalName;
    private Long size;
    private LocalDateTime uploadedAt;
    private String uploaderId;
    private int downloadCount;
}