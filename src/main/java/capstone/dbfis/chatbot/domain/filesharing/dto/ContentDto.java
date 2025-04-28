package capstone.dbfis.chatbot.domain.filesharing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ContentDto {
    private Long id;
    private String name;
    private String type;       // "FOLDER" 또는 "FILE"
    private Long size;         // 파일인 경우만
    private String uploaderId; // 파일인 경우만
}