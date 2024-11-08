package capstone.dbfis.chatbot.domain.comment;

import capstone.dbfis.chatbot.domain.post.Post;
import capstone.dbfis.chatbot.domain.schedule.ShceduleId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(CommentId.class)

public class Comment {
    @Id
    @Column(name = "id", updatable = false)
    private String id;

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
    @Column(name = "postid")
    private String postid;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "memberid"),
            @JoinColumn(name = "role", referencedColumnName = "role"),
            @JoinColumn(name = "department", referencedColumnName = "department"),
            @JoinColumn(name = "postid", referencedColumnName = "id")
    })
    private Post post;

    @Column(name = "content")
    private String content;

    @Column(name = "creatAt")
    private LocalDateTime creatAt;

    @Column(name = "modifiedAt")
    private LocalDateTime modifiedAt;

}
