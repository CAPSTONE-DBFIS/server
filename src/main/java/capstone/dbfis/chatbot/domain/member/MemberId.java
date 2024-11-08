package capstone.dbfis.chatbot.domain.member;


import java.io.Serializable;
import java.util.Objects;

public class MemberId implements Serializable {


    private String id;
    private String role;
    private String department;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberId memberId)) return false;
        return Objects.equals(id, memberId.id) &&
                Objects.equals(role, memberId.role) &&
                Objects.equals(department, memberId.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,role, department);
    }
}

