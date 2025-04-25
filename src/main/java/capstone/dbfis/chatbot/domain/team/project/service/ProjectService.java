package capstone.dbfis.chatbot.domain.team.project.service;

import capstone.dbfis.chatbot.domain.team.project.dto.ProjectResponse;
import capstone.dbfis.chatbot.domain.team.project.dto.UpdateProjectRequest;
import capstone.dbfis.chatbot.domain.team.project.entity.Project;
import capstone.dbfis.chatbot.domain.team.project.repository.ProjectRepository;
import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.team.entity.TeamMember;
import capstone.dbfis.chatbot.domain.team.repository.TeamMemberRepository;
import capstone.dbfis.chatbot.domain.team.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    // 프로젝트 생성 메소드
    @Transactional
    public Project createProject(Long teamId, String creatorId, String projectName, String description) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // 요청자가 팀 멤버인지 확인
        boolean isTeamMember = teamMemberRepository.existsByTeam_IdAndMember_Id(teamId, creatorId);
        if (!isTeamMember) {
            throw new IllegalArgumentException("요청자는 해당 팀의 멤버가 아닙니다.");
        }

        // 팀 리더만 프로젝트 생성 가능
        boolean isLeader = teamMemberRepository
                .findByTeam_IdAndMember_Id(teamId, creatorId)
                .map(tm -> "리더".equals(tm.getTeamRole()))
                .orElse(false);

        if (!isLeader) {
            throw new IllegalArgumentException("프로젝트는 팀 리더만 생성할 수 있습니다.");
        }

        Project project = Project.builder()
                .team(team)
                .name(projectName)
                .description(description)
                .build();

        return projectRepository.save(project);
    }

    @Transactional
    public void updateProject(Long projectId, String requesterId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        Long teamId = project.getTeam().getId();

        // 리더 권한 확인
        TeamMember requester = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, requesterId)
                .orElseThrow(() -> new IllegalArgumentException("팀에 속하지 않은 사용자입니다."));
        if (!"리더".equals(requester.getTeamRole())) {
            throw new IllegalArgumentException("프로젝트 수정은 팀 리더만 가능합니다.");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId, String requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        Long teamId = project.getTeam().getId();

        // 리더 권한 확인
        TeamMember requester = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, requesterId)
                .orElseThrow(() -> new IllegalArgumentException("팀에 속하지 않은 사용자입니다."));
        if (!"리더".equals(requester.getTeamRole())) {
            throw new IllegalArgumentException("프로젝트 삭제는 팀 리더만 가능합니다.");
        }

        projectRepository.delete(project);
    }

    @Transactional
    public List<ProjectResponse> getProjectsByMember(String memberId) {
        List<Team> teams = teamMemberRepository.findByMember_Id(memberId)
                .stream()
                .map(TeamMember::getTeam)
                .toList();

        return teams.stream()
                .flatMap(team -> projectRepository.findByTeam_Id(team.getId()).stream())
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getTeam().getId()
                ))
                .toList();
    }
}