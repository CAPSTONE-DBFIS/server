package capstone.dbfis.chatbot.domain.post;

import capstone.dbfis.chatbot.domain.department.Department;
import capstone.dbfis.chatbot.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(PostId.class)
public class Post {

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


    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "id"),
            @JoinColumn(name = "role", referencedColumnName = "role_id"),
            @JoinColumn(name = "department", referencedColumnName = "department_id")
    })
    private Member member;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department departmentid;


    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "creatAt", nullable = false)
    private LocalDateTime creatAt;

    @Column(name = "modifiedAt", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(name = "view_cnt")
    private String view_cnt;

    @Column(name = "like_cnt")
    private String like_cnt;

    @Column(name = "hashtag")
    private String hashtag;

}
