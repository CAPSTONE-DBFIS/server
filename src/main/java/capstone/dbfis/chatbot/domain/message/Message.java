package capstone.dbfis.chatbot.domain.message;

import capstone.dbfis.chatbot.domain.chatparticipant.ChatParticipant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class Message {
    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "chat_id", nullable = false)
    private String chat_id;

    @Column(name = "user_id", nullable = false)
    private String user_id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "sendAt", nullable = false)
    private LocalDateTime sendAt;

    @Column(name = "isRead", nullable = false)
    private boolean isRead;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "memberid"),
            @JoinColumn(name = "role", referencedColumnName = "role"),
            @JoinColumn(name = "department", referencedColumnName = "department"),
            @JoinColumn(name = "chatroomid", referencedColumnName = "chatroomid")
    })
    private ChatParticipant chatParticipant;
}
