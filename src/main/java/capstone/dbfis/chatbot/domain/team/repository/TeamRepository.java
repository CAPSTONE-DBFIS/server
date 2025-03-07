package capstone.dbfis.chatbot.domain.team.repository;


import capstone.dbfis.chatbot.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByTeamMembers_Member_Id(String memberId); // 특정 회원이 속한 모든 팀 조회
}
