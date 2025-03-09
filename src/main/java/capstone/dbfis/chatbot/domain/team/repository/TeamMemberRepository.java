package capstone.dbfis.chatbot.domain.team.repository;

import capstone.dbfis.chatbot.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeam_Id(Long teamId); // 특정 팀에 속한 모든 멤버를 조회
    Optional<TeamMember> findByTeam_IdAndMember_Id(Long teamId, String memberId); // 팀 id와 멤버 id로 특정 팀에 속한 멤버를 조회
    boolean existsByTeam_IdAndMember_Id(Long teamId, String memberId); // 팀 id와 멤버 id로 특정 팀에 멤버가 속해 있는지 확인
    long countByTeam_Id(Long teamId); // 특정 팀에 속한 멤버 수를 반환
    long countByTeam_IdAndTeamRole(Long teamId, String teamRole); // 팀 내 특정 역할을 가진 멤버 수 조회 (리더 수 확인용)
}
