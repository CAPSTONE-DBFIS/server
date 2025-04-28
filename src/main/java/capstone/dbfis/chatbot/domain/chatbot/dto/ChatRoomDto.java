package capstone.dbfis.chatbot.domain.chatbot.dto;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String name;
    private ChatRoomType type;
    private Long projectId; // null 이면 개인 채팅방
}
