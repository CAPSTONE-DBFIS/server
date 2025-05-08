package capstone.dbfis.chatbot.domain.member.controller;

import capstone.dbfis.chatbot.domain.member.dto.*;
import capstone.dbfis.chatbot.domain.member.service.MemberService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Member API", description = "멤버 관련 로직 API")
@RequestMapping("/api")
public class MemberApiController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자의 아이디와 비밀번호로 로그인 후 AccessToken, RefreshToken, 회원 id, 회원 이름를 발급합니다.")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(memberService.login(request));  // 200 OK
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 후 인증 이메일 발송.")
    public ResponseEntity<String> registerMember(
            @RequestBody @Valid AddMemberRequest request) {
        memberService.registerMember(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)       // 201 Created
                .body("회원가입 완료. 인증 이메일을 확인하세요.");
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "인증 메일 재전송", description = "회원가입 인증 이메일을 다시 보냅니다.")
    public ResponseEntity<Void> resendVerification(
            @RequestBody @Valid ResendVerificationRequest request) {
        memberService.sendNewSignUpEmail(request.getMemberId());
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    @PostMapping("/verify-signup")
    @Operation(summary = "회원가입 이메일 인증번호 검증", description = "사용자가 입력한 인증번호로 회원가입 이메일 인증을 완료합니다.")
    public ResponseEntity<Void> verifySignup(
            @RequestBody @Valid VerifySignupRequest request) {
        memberService.verifySignUpCode(request.getMemberId(), request.getVerificationCode());
        return ResponseEntity.ok().build();        // 200 OK
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기", description = "사용자의 이메일을 통해 사용자의 아이디를 조회합니다.")
    public ResponseEntity<String> findId(
            @RequestBody @Valid EmailRequest request) {
        String foundId = memberService.findId(request.getEmail());
        return ResponseEntity.ok(foundId);         // 200 OK
    }

    @PostMapping("/find-password")
    @Operation(summary = "비밀번호 재설정 링크 발송", description = "사용자의 이메일로 재설정 링크를 전송합니다.")
    public ResponseEntity<Void> findPassword(
            @RequestBody @Valid EmailRequest request) {
        memberService.findPassword(request.getEmail());
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "사용자가 비밀번호 재설정 링크를 클릭했을 경우, 해당 API로 비밀번호를 변경합니다.")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid PasswordResetRequest request) {
        memberService.resetPassword(request);
        return ResponseEntity.ok().build();        // 200 OK
    }

    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 조회", description = "로그인된 사용자의 정보를 반환합니다.")
    public ResponseEntity<MyPageResponse> getMyPage(
            @RequestHeader("Authorization") @NotBlank String token) {
        String memberId = tokenProvider.getMemberId(token);
        MyPageResponse data = memberService.getMyPageData(memberId);
        return ResponseEntity.ok(data);            // 200 OK
    }

    @PatchMapping("/update-profile")
    @Operation(summary = "프로필 수정", description = "사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<Void> updateProfile(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid UpdateProfileRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        memberService.updateProfile(memberId, request);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    @PatchMapping("/update-password")
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    public ResponseEntity<Void> updatePassword(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid UpdatePasswordRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        memberService.updatePassword(memberId, request);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}