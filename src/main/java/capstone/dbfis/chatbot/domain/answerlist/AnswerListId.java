package capstone.dbfis.chatbot.domain.answerlist;

import java.io.Serializable;
import java.util.Objects;

public class AnswerListId implements Serializable {
    private String id;
    private String memberid;
    private String role;
    private String department;
    private String answerid;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnswerListId answerListId)) return false;
        return Objects.equals(id, answerListId.id) &&
                Objects.equals(memberid, answerListId.memberid) &&
                Objects.equals(role, answerListId.role) &&
                Objects.equals(department, answerListId.department)&&
                Objects.equals(answerid, answerListId.answerid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberid, role, department, answerid);

    }
}
