package capstone.dbfis.chatbot.domain.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String name; // 채팅방 이름 추가

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type; // 채팅방 타입(PERSONAL/PROJECT)

    // 프로젝트 소속 채팅방이면 projectId, 아니면 null
    @Column(name = "project_id")
    private Long projectId;

    public void updateName(String name) {
        this.name = name;
    }
}
