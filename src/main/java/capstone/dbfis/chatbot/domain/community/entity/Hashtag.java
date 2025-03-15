package capstone.dbfis.chatbot.domain.community.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "hashtag")
    private Set<Post> posts = new HashSet<>(); // 여러 게시글이 이 해시태그를 참조합니다.

    public Hashtag(String name) {
        this.name = name;
    }
}
