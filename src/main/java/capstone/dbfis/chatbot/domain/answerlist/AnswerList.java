package capstone.dbfis.chatbot.domain.answerlist;

import capstone.dbfis.chatbot.domain.member.Member;
import capstone.dbfis.chatbot.domain.questionlist.QuestionList;
import capstone.dbfis.chatbot.domain.questionlist.QuestionListId;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(AnswerListId.class)

public class AnswerList {

    @Id
    @Column(name = "id")
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
    @Column(name = "answerid")  // 추가 기본 키 컬럼
    private String answerid;

    @OneToOne
    @MapsId  // 복합 키 상속
    @JoinColumns({
            @JoinColumn(name = "id", referencedColumnName = "id"),
            @JoinColumn(name = "memberid", referencedColumnName = "memberid"),
            @JoinColumn(name = "role", referencedColumnName = "role"),
            @JoinColumn(name = "department", referencedColumnName = "department")
    })
    private QuestionList questionList;


    @Column(name = "answer_content", nullable = false)
    private String answer_content;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

}
