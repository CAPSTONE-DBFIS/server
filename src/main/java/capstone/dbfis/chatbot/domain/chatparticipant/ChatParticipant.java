package capstone.dbfis.chatbot.domain.chatparticipant;

import capstone.dbfis.chatbot.domain.chatroom.ChatRoom;
import capstone.dbfis.chatbot.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(ChatParticipantId.class)

public class ChatParticipant {

    @Id
    @Column(name = "memberid")
    private String memberid;

    @Id
    @Column(name = "role")
    private String role;

    @Id
    @Column(name = "department")
    private String department;

    @Id
    @Column(name = "chatroomid")
    private String chatroomid;


    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "id"),
            @JoinColumn(name = "role", referencedColumnName = "role_id"),
            @JoinColumn(name = "department", referencedColumnName = "department_id")
    })
    private Member member;

    @ManyToOne
    @JoinColumn(name = "chatroomid", referencedColumnName = "id")
    private ChatRoom chatroom;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
