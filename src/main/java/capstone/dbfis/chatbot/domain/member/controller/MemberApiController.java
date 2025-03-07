package capstone.dbfis.chatbot.domain.member.controller;

import capstone.dbfis.chatbot.domain.member.dto.*;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.member.service.MemberService;
import capstone.dbfis.chatbot.domain.member.service.EmailVerificationService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberApiController {

    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;
    private final TokenProvider tokenProvider;

    // 로그인 (JWT 발급)
    @PostMapping("/login")
    @Operation(summary = "로그인",
            description = "로그인을 진행합니다. 로그인 성공시 AccessToken과 RefreshToken을 발급합니다.")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(response);
    }

    // 회원 가입 (인증 불필요)
    @PostMapping("/signup")
    @Operation(summary = "회원 가입",
            description = "회원가입을 진행합니다. 인증 번호를 가입시 등록한 메일로 전송합니다.")
    public ResponseEntity<String> registerMember(@RequestBody AddMemberRequest request) {
        memberService.registerMember(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다. 인증 이메일을 확인하세요.");
    }

    // 이메일 인증 번호 확인
    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증 번호 확인",
            description = "사용자가 입력한 인증 번호의 유효성을 확인합니다.")
    public ResponseEntity<String> verifyEmail(@RequestParam String verificationCode) {
        boolean isVerified = emailVerificationService.verifySignUpCode(verificationCode);
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 유효하지 않습니다.");
        }
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기",
            description = "사용자의 이메일을 통해 사용자의 아이디를 찾습니다. 사용자 Id의 앞 3글자만 보여주고 나머지는 마스킹 처리한 결과를 반환합니다..")
    public ResponseEntity<String> forgotId(@RequestBody EmailRequest request) {
        String maskedId = memberService.findId(request.getEmail());
        return ResponseEntity.ok(maskedId);
    }

    // 비밀번호 찾기 이메일 발송
    @PostMapping("/find-password")
    @Operation(summary = "비밀번호 찾기",
            description = "사용자의 이메일로 비밀번호 재설정 링크를 보냅니다. 비밀번호 재설정 토큰은 30분 동안 유효합니다.")
    public ResponseEntity<String> forgotPassword(@RequestBody EmailRequest request) {
        memberService.findPassword(request.getEmail());
        return ResponseEntity.ok("비밀번호 재설정 링크를 이메일로 전송했습니다.");
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정",
            description = "비밀번호 재설정 토큰을 검증하고 새 비밀번호로 변경합니다.")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            memberService.resetPassword(request);
            return ResponseEntity.ok().body("비밀번호가 성공적으로 변경되었습니다.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
        }
    }

    // 마이페이지 회원 정보 조회
    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 회원 정보 조회", description = "사용자의 마이페이지 정보를 조회합니다.")
    public ResponseEntity<MyPageResponse> getUserProfile(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(memberService.getMyPageData(memberId));
    }

    @PatchMapping("/update-profile")
    @Operation(summary = "회원 정보 수정", description = "사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<String> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        memberService.updateProfile(memberId, request);
        return ResponseEntity.ok("프로필 정보이 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/update-password")
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    public ResponseEntity<String> updatePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdatePasswordRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        memberService.updatePassword(memberId, request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

}