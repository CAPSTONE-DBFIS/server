package capstone.dbfis.chatbot.domain.filesharing.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Data
@Getter
@AllArgsConstructor
public class FileDto {
    private Long id;
    private String originalName;
    private Long size;
    private LocalDateTime uploadedAt;
    private String uploaderId;
    private int downloadCount;
}