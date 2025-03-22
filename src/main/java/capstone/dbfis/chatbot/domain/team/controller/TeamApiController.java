package capstone.dbfis.chatbot.domain.team.controller;

import capstone.dbfis.chatbot.domain.member.dto.MyPageResponse;
import capstone.dbfis.chatbot.domain.team.dto.*;
import capstone.dbfis.chatbot.domain.team.entity.Team;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamApiController {

    private final TeamService teamService;
    private final TokenProvider tokenProvider;

    // 팀 생성
    @PostMapping
    @Operation(summary = "팀 생성",
            description = "팀 이름과 설명을 받아 새로운 팀을 생성하고, 팀의 첫 리더로 사용자를 지정합니다. 요청자 ID와 역할을 기반으로 팀이 생성됩니다.")
    public ResponseEntity<Team> createTeam(@RequestHeader("Authorization") String token,
                                           @RequestBody CreateTeamRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        Team createdTeam = teamService.createTeam(request.getName(), request.getDescription(), memberId, request.getRole());
        return ResponseEntity.ok(createdTeam);
    }

    // 팀 삭제
    @DeleteMapping("/{teamId}")
    @Operation(summary = "팀 삭제",
            description = "팀 리더만 해당 팀을 삭제할 수 있습니다. 팀 삭제 시, 팀에 속한 모든 멤버도 삭제됩니다.")
    public ResponseEntity<String> deleteTeam(@RequestHeader("Authorization") String token,
                                             @PathVariable Long teamId) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.deleteTeam(teamId, requesterId);
        return ResponseEntity.ok("팀이 삭제되었습니다.");
    }

    // 팀 멤버 추가
    @PostMapping("/{teamId}/members")
    @Operation(summary = "팀 멤버 추가",
            description = "팀 리더가 팀에 새로운 멤버를 추가할 수 있습니다. 요청자는 멤버의 ID와 역할을 함께 요청 본문에 포함시켜야 합니다.")
    public ResponseEntity<TeamMemberResponse> addMemberToTeam(@RequestHeader("Authorization") String token,
                                                              @PathVariable Long teamId,
                                                              @RequestBody AddTeamMemberRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        TeamMemberResponse addedMember = teamService.addMemberToTeam(teamId, requesterId, request);
        return ResponseEntity.ok(addedMember);
    }

    // 팀 멤버 역할 수정
    @PutMapping("/{teamId}/members/{memberId}")
    @Operation(summary = "팀 멤버 역할 수정",
            description = "팀 리더가 팀 멤버의 직무 및 팀 내 역할을 수정할 수 있습니다. 요청자는 수정할 팀 멤버 ID와 새 역할을 함께 요청 본문에 포함시켜야 합니다.")
    public ResponseEntity<String> updateTeamMemberRole(@RequestHeader("Authorization") String token,
                                                       @PathVariable Long teamId,
                                                       @PathVariable String memberId,
                                                       @RequestBody UpdateTeamMemberRoleRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.updateTeamMemberRole(teamId, requesterId, memberId, request.getNewRole(), request.getNewTeamRole());
        return ResponseEntity.ok("팀원의 역할이 성공적으로 변경되었습니다.");
    }

    // 팀 멤버 삭제
    @DeleteMapping("/{teamId}/members/{memberId}")
    @Operation(summary = "팀 멤버 삭제",
            description = "팀 리더가 팀에서 멤버를 삭제할 수 있습니다. 요청자는 삭제하려는 팀 멤버의 ID를 경로 파라미터로 보내야 합니다.")
    public ResponseEntity<String> removeMemberFromTeam(@RequestHeader("Authorization") String token,
                                                       @PathVariable Long teamId,
                                                       @PathVariable String memberId) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.removeMemberFromTeam(teamId, requesterId, memberId);
        return ResponseEntity.ok("멤버가 팀에서 삭제되었습니다.");
    }

    // 사용자가 속한 팀 조회
    @GetMapping("/my-teams")
    @Operation(summary = "사용자가 속한 팀 조회",
            description = "JWT 토큰을 통해 사용자가 속한 팀 목록을 조회합니다. 각 팀의 이름, 설명, 팀원 목록이 포함됩니다.")
    public ResponseEntity<List<MyPageResponse.TeamResponse>> getUserTeams(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        List<MyPageResponse.TeamResponse> userTeams = teamService.getUserTeams(memberId);
        return ResponseEntity.ok(userTeams);
    }

    // 팀 설명 수정
    @PutMapping("/{teamId}")
    @Operation(summary = "팀 설명 수정",
            description = "팀 리더가 팀의 이름과 설명을 수정할 수 있습니다. 요청자는 팀의 ID와 수정할 정보를 포함해야 합니다.")
    public ResponseEntity<String> updateTeamDescription(@RequestHeader("Authorization") String token,
                                                        @PathVariable Long teamId,
                                                        @RequestBody UpdateTeamRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        teamService.updateTeamDescription(teamId, requesterId, request);
        return ResponseEntity.ok("팀 정보가 수정되었습니다.");
    }
}