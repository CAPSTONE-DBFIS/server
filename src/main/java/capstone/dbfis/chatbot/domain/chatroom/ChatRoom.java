package capstone.dbfis.chatbot.domain.chatroom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class ChatRoom {
    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}
