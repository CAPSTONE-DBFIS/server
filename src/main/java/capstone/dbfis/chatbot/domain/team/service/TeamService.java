package capstone.dbfis.chatbot.domain.team.service;

import capstone.dbfis.chatbot.domain.member.dto.MyPageResponse;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.project.dto.TrProjectResponse;
import capstone.dbfis.chatbot.domain.project.repository.TrackingProjectRepository;
import capstone.dbfis.chatbot.domain.team.dto.TeamMemberResponse;
import capstone.dbfis.chatbot.domain.team.dto.AddTeamMemberRequest;
import capstone.dbfis.chatbot.domain.team.dto.UpdateTeamRequest;
import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.team.entity.TeamMember;
import capstone.dbfis.chatbot.domain.team.repository.TeamMemberRepository;
import capstone.dbfis.chatbot.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final TrackingProjectRepository trprojectRepository;


    /**
     * 새로운 팀을 생성하고 요청 사용자를 리더로 등록합니다.
     */
    @Transactional
    public Team createTeam(String name, String description, String creatorId, String creatorRole) {
        // 사용자 존재 여부 확인
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."));

        // 팀 저장
        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        Team savedTeam = teamRepository.save(team);

        // 리더 멤버 등록
        TeamMember leader = new TeamMember();
        leader.setTeam(savedTeam);
        leader.setMember(creator);
        leader.setRole(creatorRole);
        leader.setTeamRole("리더");
        teamMemberRepository.save(leader);

        return savedTeam;
    }

    /**
     * 지정된 팀을 삭제합니다. 요청자는 반드시 리더여야 합니다.
     */
    @Transactional
    public void deleteTeam(Long teamId, String requesterId) {
        // 리더 권한 확인
        checkIfLeader(teamId, requesterId);

        // 팀 존재 여부 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 팀이 존재하지 않습니다."));

        // 팀 멤버 전부 삭제
        List<TeamMember> members = teamMemberRepository.findByTeam_Id(teamId);
        teamMemberRepository.deleteAll(members);

        // 팀 삭제
        teamRepository.delete(team);
    }

    /**
     * 팀 이름과 설명을 수정합니다. 요청자는 리더여야 합니다.
     */
    @Transactional
    public void updateTeamDescription(Long teamId, String requesterId, UpdateTeamRequest request) {
        // 팀 존재 여부 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀이 존재하지 않습니다."));

        // 리더 권한 확인
        checkIfLeader(teamId, requesterId);

        // 정보 업데이트
        team.setName(request.getTeamName());
        team.setDescription(request.getDescription());
        teamRepository.save(team);
    }

    /**
     * 팀 멤버를 추가합니다. 요청자는 리더여야 하며 중복 등록을 방지합니다.
     */
    @Transactional
    public TeamMemberResponse addMemberToTeam(Long teamId, String requesterId, AddTeamMemberRequest request) {
        // 리더 권한 확인
        checkIfLeader(teamId, requesterId);

        // 팀 존재 여부 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 팀입니다."));

        // 멤버 존재 여부 확인
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 멤버입니다."));

        // 중복 등록 방지
        if (teamMemberRepository.existsByTeam_IdAndMember_Id(teamId, request.getMemberId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 멤버는 이미 팀에 추가되어 있습니다.");
        }

        // 멤버 등록
        TeamMember tm = new TeamMember();
        tm.setTeam(team);
        tm.setMember(member);
        tm.setRole(request.getRole());
        tm.setTeamRole(request.getTeamRole());
        TeamMember saved = teamMemberRepository.save(tm);

        return new TeamMemberResponse(saved);
    }

    /**
     * 팀 멤버의 직무와 팀 내 역할을 수정합니다. 요청자는 리더여야 합니다.
     */
    @Transactional
    public void updateTeamMemberRole(Long teamId, String requesterId, String memberId, String newRole, String newTeamRole) {
        // 리더 권한 확인
        checkIfLeader(teamId, requesterId);

        // 대상 멤버 조회
        TeamMember tm = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 멤버는 팀에 존재하지 않습니다."));

        // 유일한 리더 유지 조건 검사
        if ("리더".equals(tm.getTeamRole()) && !"리더".equals(newTeamRole)) {
            long leaderCount = teamMemberRepository.countByTeam_IdAndTeamRole(teamId, "리더");
            if (leaderCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀에는 최소 한 명 이상의 리더가 필요합니다.");
            }
        }

        // 역할 업데이트
        tm.setRole(newRole);
        tm.setTeamRole(newTeamRole);
        teamMemberRepository.save(tm);
    }

    /**
     * 팀 멤버를 제거합니다. 요청자는 리더여야 하며 최소 한 명의 리더를 유지합니다.
     */
    @Transactional
    public void removeMemberFromTeam(Long teamId, String requesterId, String memberId) {
        // 리더 권한 확인
        checkIfLeader(teamId, requesterId);

        // 대상 멤버 조회
        TeamMember tm = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 팀에 존재하지 않는 멤버입니다."));

        // 유일한 리더 방지
        if ("리더".equals(tm.getTeamRole())) {
            long leaderCount = teamMemberRepository.countByTeam_IdAndTeamRole(teamId, "리더");
            if (leaderCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀에는 최소 한 명의 리더가 필요합니다.");
            }
        }

        // 멤버 삭제
        teamMemberRepository.delete(tm);

        // 팀이 비었으면 삭제
        if (teamMemberRepository.countByTeam_Id(teamId) == 0) {
            teamRepository.deleteById(teamId);
        }
    }

    /**
     * 지정된 사용자가 해당 팀의 리더인지 확인하고, 아니면 예외를 던집니다.
     */
    private void checkIfLeader(Long teamId, String memberId) {
        TeamMember tm = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "팀 리더 권한이 필요합니다."));
        if (!"리더".equals(tm.getTeamRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업은 팀 리더만 수행할 수 있습니다.");
        }
    }

    /**
     * 사용자가 해당 팀의 리더인지 반환합니다.
     */
    public boolean isUserTeamLeader(Long teamId, String memberId) {
        return teamMemberRepository.findByTeam_IdAndMember_Id(teamId, memberId)
                .map(tm -> "리더".equals(tm.getTeamRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀원 정보가 없습니다."));
    }

    /**
     * 사용자가 해당 팀에 속해있는지 확인합니다.
     */
    public boolean isUserInTeam(Long teamId, String memberId) {
        return teamMemberRepository.existsByTeam_IdAndMember_Id(teamId, memberId);
    }

    /**
     * 사용자가 속한 모든 팀의 정보(팀, 멤버)를 조회합니다.
     */
    public List<MyPageResponse.TeamResponse> getUserTeams(String memberId) {
        List<Team> teams = teamRepository.findAllByTeamMembers_Member_Id(memberId);
        return teams.stream()
                .map(team -> MyPageResponse.TeamResponse.builder()
                        .teamId(team.getId())
                        .teamName(team.getName())
                        .teamDescription(team.getDescription())
                        .members(getTeamMembers(team.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 팀의 모든 멤버를 조회하고, 리더를 먼저 정렬하여 반환합니다.
     */
    private List<MyPageResponse.MemberResponse> getTeamMembers(Long teamId) {
        return teamMemberRepository.findByTeam_Id(teamId).stream()
                .sorted(Comparator.comparing(tm -> !"리더".equals(tm.getTeamRole())))
                .map(tm -> new MyPageResponse.MemberResponse(
                        tm.getMember().getName(),
                        tm.getMember().getId(),
                        tm.getMember().getPhone(),
                        tm.getMember().getEmail(),
                        tm.getRole(),
                        tm.getTeamRole()))
                .collect(Collectors.toList());
    }

    /**
     * 팀에 속한 모든 프로젝트를 조회합니다.
     */
    private List<TrProjectResponse> getTeamProjects(Long teamId) {
        return trprojectRepository.findByTeam_Id(teamId).stream()
                .map(p -> new TrProjectResponse(
                        p.getId(), p.getName(),
                        p.getDescription(),
                        p.getTeam().getId(),
                        p.getTeam().getName(),
                        p.getStartDate(), p.getEndDate()))
                .collect(Collectors.toList());
    }
}