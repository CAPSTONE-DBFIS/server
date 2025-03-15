package capstone.dbfis.chatbot.domain.community.entity;


import capstone.dbfis.chatbot.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor

public class PostEmotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String emotion; // "like", "angry", "sad", "happy"

    public PostEmotion() {}

    public PostEmotion(Post post, Member member, String emotion) {
        this.post = post;
        this.member = member;
        this.emotion = emotion;
    }

}
