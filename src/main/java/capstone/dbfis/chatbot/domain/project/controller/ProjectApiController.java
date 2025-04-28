package capstone.dbfis.chatbot.domain.project.controller;

import capstone.dbfis.chatbot.domain.project.dto.AddProjectRequest;
import capstone.dbfis.chatbot.domain.project.dto.ProjectResponse;
import capstone.dbfis.chatbot.domain.project.dto.UpdateProjectRequest;
import capstone.dbfis.chatbot.domain.project.entity.Project;
import capstone.dbfis.chatbot.domain.project.service.ProjectService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Project API", description = "프로젝트 관리 API")
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {

    private final ProjectService projectService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "프로젝트 생성", description = "특정 팀에 새로운 프로젝트를 생성합니다. 요청자는 해당 팀의 리더여야 합니다.")
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestHeader("Authorization") String token,
                                                         @RequestBody AddProjectRequest request) {
        String creatorId = tokenProvider.getMemberId(token);

        Project project = projectService.createProject(
                request.getTeamId(),
                creatorId,
                request.getName(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate()
        );

        ProjectResponse response = new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getTeam().getId(),
                project.getStartDate(),
                project.getEndDate()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로젝트 수정", description = "프로젝트 이름과 설명을 수정합니다. 요청자는 해당 팀의 리더여야 합니다.")
    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@RequestHeader("Authorization") String token,
                                                @PathVariable Long projectId, @RequestBody UpdateProjectRequest request) {
        String requesterId = tokenProvider.getMemberId(token);

        projectService.updateProject(projectId, requesterId, request);
        return ResponseEntity.ok("프로젝트 정보가 수정되었습니다.");
    }

    @Operation(summary = "프로젝트 삭제", description = "지정한 프로젝트를 삭제합니다. 요청자는 해당 팀의 리더여야 합니다.")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@RequestHeader("Authorization") String token,
                                                @PathVariable Long projectId) {
        String requesterId = tokenProvider.getMemberId(token);

        projectService.deleteProject(projectId, requesterId);
        return ResponseEntity.ok("프로젝트가 삭제되었습니다.");
    }

    @Operation(summary = "사용자가 속한 모든 프로젝트 조회", description = "사용자가 속한 모든 프로젝트를 반환합니다.")
    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        List<ProjectResponse> projects = projectService.getProjectsByMember(memberId);
        return ResponseEntity.ok(projects);
    }
}