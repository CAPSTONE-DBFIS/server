package capstone.dbfis.chatbot.domain.questionlist;

import java.util.Objects;

public class QuestionListId {
    private String id;
    private String memberid;
    private String role;
    private String department;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionListId questionListId)) return false;
        return Objects.equals(id, questionListId.id) &&
                Objects.equals(memberid, questionListId.memberid) &&
                Objects.equals(role, questionListId.role) &&
                Objects.equals(department, questionListId.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberid, role, department);

    }
}
