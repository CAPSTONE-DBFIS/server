package capstone.dbfis.chatbot.domain.filesharing.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data @AllArgsConstructor
public class FolderDto {
    private Long id;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
}