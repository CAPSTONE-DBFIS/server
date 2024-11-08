package capstone.dbfis.chatbot.domain.passwordreset;

import capstone.dbfis.chatbot.domain.searchhistory.SearchHistoryId;

import java.io.Serializable;
import java.util.Objects;

public class PassowrdResetId implements Serializable {
    private String id;
    private String memberid;
    private String role;
    private String department;

    public PassowrdResetId() {
    }

    public PassowrdResetId(String id, String memberid, String role, String department) {
        this.id = id;
        this.memberid = memberid;
        this.role = role;
        this.department = department;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassowrdResetId that = (PassowrdResetId) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(memberid, that.memberid) &&
                Objects.equals(role, that.role) &&
                Objects.equals(department, that.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberid, role, department);
    }
}
