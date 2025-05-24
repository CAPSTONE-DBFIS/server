package capstone.dbfis.chatbot.domain.team.controller;

import capstone.dbfis.chatbot.domain.member.dto.MyPageResponse;
import capstone.dbfis.chatbot.domain.team.dto.CreateTeamRequest;
import capstone.dbfis.chatbot.domain.team.dto.AddTeamMemberRequest;
import capstone.dbfis.chatbot.domain.team.dto.TeamMemberResponse;
import capstone.dbfis.chatbot.domain.team.dto.UpdateTeamMemberRoleRequest;
import capstone.dbfis.chatbot.domain.team.dto.UpdateTeamRequest;
import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
@Tag(name = "Team API", description = "팀 관리 API")
public class TeamApiController {

    private final TeamService teamService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "팀 생성", description = "새 팀을 생성하고 요청자를 리더로 지정합니다.")
    @PostMapping
    public ResponseEntity<Team> createTeam(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid CreateTeamRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        Team createdTeam = teamService.createTeam(
                request.getName(), request.getDescription(), memberId, request.getRole());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTeam);
    }

    @Operation(summary = "팀 삭제", description = "팀 리더만 팀을 삭제할 수 있습니다.")
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable @Min(1) Long teamId) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.deleteTeam(teamId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 멤버 추가", description = "팀 리더가 팀 멤버를 추가할 수 있습니다.")
    @PostMapping("/{teamId}/members")
    public ResponseEntity<TeamMemberResponse> addMemberToTeam(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable @Min(1) Long teamId,
            @RequestBody @Valid AddTeamMemberRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        TeamMemberResponse addedMember =
                teamService.addMemberToTeam(teamId, requesterId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addedMember);
    }

    @Operation(summary = "팀 멤버 역할 수정", description = "팀 멤버의 역할을 수정합니다.")
    @PutMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> updateTeamMemberRole(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable @Min(1) Long teamId,
            @PathVariable @NotBlank String memberId,
            @RequestBody @Valid UpdateTeamMemberRoleRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.updateTeamMemberRole(
                teamId, requesterId, memberId,
                request.getNewRole(), request.getNewTeamRole());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 멤버 삭제", description = "팀 멤버를 제거합니다.")
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> removeMemberFromTeam(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable @Min(1) Long teamId,
            @PathVariable @NotBlank String memberId) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.removeMemberFromTeam(teamId, requesterId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자가 속한 팀 조회", description = "사용자가 속한 팀 목록을 반환합니다.")
    @GetMapping("/my-teams")
    public ResponseEntity<List<MyPageResponse.TeamResponse>> getUserTeams(
            @RequestHeader("Authorization") @NotBlank String token) {
        String memberId = tokenProvider.getMemberId(token);
        List<MyPageResponse.TeamResponse> userTeams = teamService.getUserTeams(memberId);
        return ResponseEntity.ok(userTeams);
    }

    @Operation(summary = "팀 설명 수정", description = "팀의 이름과 설명을 수정합니다.")
    @PutMapping("/{teamId}")
    public ResponseEntity<Void> updateTeamDescription(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable @Min(1) Long teamId,
            @RequestBody @Valid UpdateTeamRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.updateTeamDescription(teamId, requesterId, request);
        return ResponseEntity.noContent().build();
    }
}
