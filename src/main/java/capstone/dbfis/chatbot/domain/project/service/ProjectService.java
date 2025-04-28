package capstone.dbfis.chatbot.domain.project.service;

import capstone.dbfis.chatbot.domain.project.dto.ProjectResponse;
import capstone.dbfis.chatbot.domain.project.dto.UpdateProjectRequest;
import capstone.dbfis.chatbot.domain.project.entity.Project;
import capstone.dbfis.chatbot.domain.project.repository.ProjectRepository;
import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.team.entity.TeamMember;
import capstone.dbfis.chatbot.domain.team.repository.TeamMemberRepository;
import capstone.dbfis.chatbot.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * 팀에 새 프로젝트를 생성합니다.
     */
    @Transactional
    public Project createProject(Long teamId, String creatorId, String projectName, String description, LocalDate startDate, LocalDate endDate) {
        // 팀 검증
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 팀입니다.")
                );

        // 팀 멤버 검증
        boolean isTeamMember = teamMemberRepository.existsByTeam_IdAndMember_Id(teamId, creatorId);
        if (!isTeamMember) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "요청자는 해당 팀의 멤버가 아닙니다."
            );
        }

        // 팀 리더 검증
        boolean isLeader = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, creatorId)
                .map(tm -> "리더".equals(tm.getTeamRole()))
                .orElse(false);
        if (!isLeader) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "프로젝트는 팀 리더만 생성할 수 있습니다."
            );
        }

        Project project = Project.builder()
                .team(team)
                .name(projectName)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return projectRepository.save(project);
    }

    /**
     * 기존 프로젝트를 수정합니다.
     */
    @Transactional
    public void updateProject(Long projectId, String requesterId, UpdateProjectRequest request) {
        // 프로젝트 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 프로젝트입니다.")
                );

        Long teamId = project.getTeam().getId();
        // 팀 멤버 검증
        TeamMember tm = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, requesterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "팀에 속하지 않은 사용자입니다.")
                );

        // 팀 리더 검증
        if (!"리더".equals(tm.getTeamRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "프로젝트 수정은 팀 리더만 가능합니다."
            );
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        projectRepository.save(project);
    }

    /**
     * 프로젝트를 삭제합니다.
     */
    @Transactional
    public void deleteProject(Long projectId, String requesterId) {
        // 프로젝트 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 프로젝트입니다.")
                );

        // 팀 멤버 검증
        Long teamId = project.getTeam().getId();
        TeamMember tm = teamMemberRepository.findByTeam_IdAndMember_Id(teamId, requesterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "팀에 속하지 않은 사용자입니다.")
                );

        // 팀 리더 검증
        if (!"리더".equals(tm.getTeamRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "프로젝트 삭제는 팀 리더만 가능합니다."
            );
        }

        projectRepository.delete(project);
    }

    /**
     * 특정 멤버가 속한 모든 팀의 프로젝트 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByMember(String memberId) {
        return teamMemberRepository.findByMember_Id(memberId).stream()
                .map(TeamMember::getTeam)
                .flatMap(team -> projectRepository.findByTeam_Id(team.getId()).stream())
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getTeam().getId(),
                        project.getStartDate(),
                        project.getEndDate()
                ))
                .collect(Collectors.toList());
    }
}