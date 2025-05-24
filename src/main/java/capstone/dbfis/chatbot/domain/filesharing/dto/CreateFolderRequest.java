package capstone.dbfis.chatbot.domain.filesharing.dto;
import lombok.Data;

@Data
public class CreateFolderRequest {
    private Long teamId;
    private Long parentId;  // 최상위이면 null
    private String name;
}