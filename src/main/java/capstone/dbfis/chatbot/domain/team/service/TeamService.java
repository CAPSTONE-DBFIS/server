package capstone.dbfis.chatbot.domain.team.service;

import capstone.dbfis.chatbot.domain.member.dto.MyPageResponse;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.team.dto.TeamMemberResponse;
import capstone.dbfis.chatbot.domain.team.dto.AddTeamMemberRequest;
import capstone.dbfis.chatbot.domain.team.dto.UpdateTeamRequest;
import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.team.entity.TeamMember;
import capstone.dbfis.chatbot.domain.team.project.dto.ProjectResponse;
import capstone.dbfis.chatbot.domain.team.project.entity.Project;
import capstone.dbfis.chatbot.domain.team.project.repository.ProjectRepository;
import capstone.dbfis.chatbot.domain.team.repository.TeamMemberRepository;
import capstone.dbfis.chatbot.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    // 팀 추가 메서드
    @Transactional
    public Team createTeam(String name, String description, String creatorId, String creatorRole) {
        Optional<Member> creatorOpt = memberRepository.findById(creatorId);
        if (creatorOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Member creator = creatorOpt.get();

        // 팀 생성
        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        Team savedTeam = teamRepository.save(team);

        // 팀을 생성한 멤버를 팀의 리더로 추가
        TeamMember leader = new TeamMember();
        leader.setTeam(savedTeam);
        leader.setMember(creator);
        leader.setRole(creatorRole);
        leader.setTeamRole("리더");
        teamMemberRepository.save(leader);

        return savedTeam;
    }

    // 팀 삭제 메서드 (role이 리더인 경우에만 가능)
    @Transactional
    public void deleteTeam(Long teamId, String requesterId) {
        // 권한 확인 (리더)
        checkIfLeader(teamId, requesterId);

        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 팀이 존재하지 않습니다.");
        }

        Team team = teamOpt.get();

        // 팀에 속한 모든 멤버 삭제
        List<TeamMember> teamMembers = teamMemberRepository.findByTeam_Id(teamId);
        teamMemberRepository.deleteAll(teamMembers);

        // 팀 삭제
        teamRepository.delete(team);
    }

    // 팀 설명 수정 메서드
    public void updateTeamDescription(Long teamId, String requesterId, UpdateTeamRequest request) {
        // 팀이 존재하는지 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀이 존재하지 않습니다."));

        // 권한 확인 (팀 리더만 설명 수정 가능)
        checkIfLeader(teamId, requesterId);

        // 팀 이름 수정
        team.setName(request.getTeamName());

        // 팀 설명 수정
        team.setDescription(request.getDescription());
        teamRepository.save(team);
    }

    // 팀 멤버 추가 메서드 (role이 리더인 경우에만 가능)
    @Transactional
    public TeamMemberResponse addMemberToTeam(Long teamId, String requesterId, AddTeamMemberRequest request) {
        checkIfLeader(teamId, requesterId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));

        boolean alreadyInTeam = teamMemberRepository.existsByTeam_IdAndMember_Id(teamId, request.getMemberId());
        if (alreadyInTeam) {
            throw new IllegalArgumentException("해당 멤버는 이미 팀에 추가되어 있습니다.");
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setMember(member);
        teamMember.setRole(request.getRole()); // 직무
        teamMember.setTeamRole(request.getTeamRole()); // 팀내 역할

        return new TeamMemberResponse(teamMemberRepository.save(teamMember));
    }

    // 팀 멤버 역할 수정
    @Transactional
    public void updateTeamMemberRole(Long teamId, String requesterId, String memberId, String newRole, String newTeamRole) {
        // 리더 권한 확인 (요청자가 리더인지 체크)
        checkIfLeader(teamId, requesterId);

        TeamMember teamMember = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버는 팀에 존재하지 않습니다."));

        // 기존 역할이 "리더"이고, 변경하고자 하는 역할이 "리더"가 아니면서 한 명 이상의 리더가 존재하지 않는다면 예외 처리
        if ("리더".equals(teamMember.getTeamRole()) && !"리더".equals(newTeamRole)) {
            long leaderCount = teamMemberRepository.countByTeam_IdAndTeamRole(teamId, "리더");

            if (leaderCount <= 1) {
                throw new IllegalArgumentException("팀에는 최소 한 명 이상의 리더가 필요합니다.");
            }
        }

        // 팀원의 역할 변경
        teamMember.setRole(newRole);  // 직무 역할 변경
        teamMember.setTeamRole(newTeamRole);  // 팀 내 역할 변경

        teamMemberRepository.save(teamMember);  // 변경 사항 저장
    }

    // 팀 멤버 삭제 메서드 (role이 리더인 경우에만 가능)
    @Transactional
    public void removeMemberFromTeam(Long teamId, String requesterId, String memberId) {
        // 리더 권한 확인
        checkIfLeader(teamId, requesterId);

        Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId);

        if (teamMemberOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 팀에 존재하지 않는 멤버입니다.");
        }

        TeamMember teamMember = teamMemberOpt.get();

        // 삭제 대상이 리더인지 확인
        boolean isTargetLeader = "리더".equals(teamMember.getTeamRole());

        if (isTargetLeader) {
            // 현재 팀에 남아 있는 리더 수 확인
            long leaderCount = teamMemberRepository.countByTeam_IdAndTeamRole(teamId, "리더");

            // 유일한 리더라면 삭제 불가
            if (leaderCount <= 1) {
                throw new IllegalArgumentException("팀에는 최소 한 명의 리더가 필요합니다. 다른 리더를 지정한 후 삭제하세요.");
            }
        }

        // 멤버 삭제
        teamMemberRepository.delete(teamMember);

        // 팀에 남은 멤버가 있는지 확인
        boolean isTeamEmpty = teamMemberRepository.countByTeam_Id(teamId) == 0;

        // 남은 멤버가 없다면 팀 삭제
        if (isTeamEmpty) {
            teamRepository.deleteById(teamId);
        }
    }


    // 팀 리더 권한 예외 처리 메서드
    private void checkIfLeader(Long teamId, String memberId) {
        Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId);
        if (teamMemberOpt.isEmpty() || !"리더".equals(teamMemberOpt.get().getTeamRole())) {
            throw new IllegalArgumentException("해당 작업은 팀 리더만 수행할 수 있습니다.");
        }
    }

    // 팀 리더 확인 메소드
    public boolean isUserTeamLeader(Long teamId, String memberId) {
        TeamMember teamMember = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("팀원 정보가 없습니다."));
        return "리더".equals(teamMember.getTeamRole());
    }

    // 사용자가 팀에 속해있는지 체크하는 메서드
    public boolean isUserInTeam(Long teamId, String memberId) {
        return teamMemberRepository.existsByTeam_IdAndMember_Id(teamId, memberId);
    }

    // 사용자가 속한 팀 정보 조회 메서드
    public List<MyPageResponse.TeamResponse> getUserTeams(String memberId) {
        List<Team> teams = teamRepository.findAllByTeamMembers_Member_Id(memberId);
        return teams.stream().map(team -> MyPageResponse.TeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .teamDescription(team.getDescription())
                .members(getTeamMembers(team.getId()))
                .projects(getTeamProjects(team.getId()))
                .build()).collect(Collectors.toList());
    }

    // 팀원 목록 조회 메서드
    private List<MyPageResponse.MemberResponse> getTeamMembers(Long teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findByTeam_Id(teamId);
        return teamMembers.stream()
                .map(teamMember -> new MyPageResponse.MemberResponse(
                        teamMember.getMember().getName(),
                        teamMember.getMember().getId(),
                        teamMember.getMember().getPhone(),
                        teamMember.getMember().getEmail(),
                        teamMember.getRole(),
                        teamMember.getTeamRole()
                ))
                .collect(Collectors.toList());
    }

    // 팀 프로젝트 조회 목록
    private List<ProjectResponse> getTeamProjects(Long teamId) {
        List<Project> projects = projectRepository.findByTeam_Id(teamId);
        return projects.stream().map(p -> new ProjectResponse(
                p.getId(), p.getName(), p.getDescription(), p.getTeam().getId()
        )).collect(Collectors.toList());
    }
}