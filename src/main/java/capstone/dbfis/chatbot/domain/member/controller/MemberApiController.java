package capstone.dbfis.chatbot.domain.member.controller;

import capstone.dbfis.chatbot.domain.member.dto.AddMemberRequest;
import capstone.dbfis.chatbot.domain.member.dto.LoginRequest;
import capstone.dbfis.chatbot.domain.member.dto.LoginResponse;
import capstone.dbfis.chatbot.domain.member.dto.UpdateMemberRequest;
import capstone.dbfis.chatbot.domain.member.service.MemberService;
import capstone.dbfis.chatbot.domain.member.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberApiController {

    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    // 로그인 (JWT 발급)
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인을 진행합니다. 로그인 성공시 AccessToken과 RefreshToken을 발급합니다.")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    // 회원 가입 (인증 불필요)
    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "회원가입을 진행합니다. 인증 번호를 가입시 등록한 메일로 전송합니다.")
    public ResponseEntity<String> registerMember(@RequestBody AddMemberRequest req) {
        memberService.registerMember(req);
        return ResponseEntity.ok("회원가입이 완료되었습니다. 인증 이메일을 확인하세요.");
    }

    // 이메일 인증 번호 확인
    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증 번호 확인", description = "사용자가 입력한 인증 번호의 유효성을 확인합니다.")
    public ResponseEntity<String> verifyEmail(@RequestParam String verificationCode) {
        boolean isVerified = emailVerificationService.verifyCode(verificationCode);
        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 유효하지 않습니다.");
        }
    }
}