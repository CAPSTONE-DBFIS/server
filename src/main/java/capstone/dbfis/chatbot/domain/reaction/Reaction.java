package capstone.dbfis.chatbot.domain.reaction;

import capstone.dbfis.chatbot.domain.comment.Comment;
import capstone.dbfis.chatbot.domain.post.Post;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

@IdClass(ReactionId.class)
public class Reaction {

    @Id
    @Column(name = "commentid", unique = true)
    private String commentid;

    @Id
    @Column(name = "postid")
    private String postid;


    @Id
    @Column(name = "memberid")
    private String memberid;

    @ManyToOne
    @JoinColumn(name = "commentid", referencedColumnName = "id")
    private Comment comment;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "postid", referencedColumnName = "id"),
            @JoinColumn(name = "memberid", referencedColumnName = "memberid")
    })
    private Post post;

    @Column(name = "reaction_type", nullable = false)
    private String reactionType;
}