package capstone.dbfis.chatbot.domain.team.project.controller;

import capstone.dbfis.chatbot.domain.team.project.dto.AddProjectRequest;
import capstone.dbfis.chatbot.domain.team.project.dto.ProjectResponse;
import capstone.dbfis.chatbot.domain.team.project.dto.UpdateProjectRequest;
import capstone.dbfis.chatbot.domain.team.project.entity.Project;
import capstone.dbfis.chatbot.domain.team.project.service.ProjectService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "프로젝트 API", description = "프로젝트 생성, 수정, 삭제 API")
public class ProjectApiController {

    private final ProjectService projectService;
    private final TokenProvider tokenProvider;

    @Operation(
            summary = "프로젝트 생성",
            description = "특정 팀에 새로운 프로젝트를 생성합니다. 요청자는 해당 팀의 리더여야 합니다."
    )
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String token,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프로젝트 생성 요청 객체", required = true
            )
            @RequestBody AddProjectRequest request) {

        String creatorId = tokenProvider.getMemberId(token);

        Project project = projectService.createProject(
                request.getTeamId(),
                creatorId,
                request.getName(),
                request.getDescription()
        );

        ProjectResponse response = new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getTeam().getId()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "프로젝트 수정",
            description = "프로젝트 이름과 설명을 수정합니다. 요청자는 해당 팀의 리더여야 합니다."
    )
    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "수정할 프로젝트 ID", required = true)
            @PathVariable Long projectId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 프로젝트 정보", required = true
            )
            @RequestBody UpdateProjectRequest request) {

        String requesterId = tokenProvider.getMemberId(token);
        projectService.updateProject(projectId, requesterId, request);
        return ResponseEntity.ok("프로젝트 정보가 수정되었습니다.");
    }

    @Operation(
            summary = "프로젝트 삭제",
            description = "지정한 프로젝트를 삭제합니다. 요청자는 해당 팀의 리더여야 합니다."
    )
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String token,
            @Parameter(description = "삭제할 프로젝트 ID", required = true)
            @PathVariable Long projectId) {

        String requesterId = tokenProvider.getMemberId(token);
        projectService.deleteProject(projectId, requesterId);
        return ResponseEntity.ok("프로젝트가 삭제되었습니다.");
    }
}