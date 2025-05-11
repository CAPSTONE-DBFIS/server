package capstone.dbfis.chatbot.domain.chatbot.dto;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String name;
    private ChatRoomType type;
    private Long teamId;
    private boolean favorite; // 즐겨찾기 여부
    private LocalDateTime favoriteAddedat; // 즐겨찾기 등록 시간
}
