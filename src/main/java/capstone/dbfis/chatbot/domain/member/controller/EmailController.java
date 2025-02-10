package capstone.dbfis.chatbot.domain.member.controller;

import capstone.dbfis.chatbot.domain.member.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class EmailController {
    private final EmailVerificationService emailVerificationService;

    // 인증 이메일 전송
    @PostMapping("/mailSend")
    @Operation(summary = "인증 메일 전송", description = "회원가입 인증 이메일을 전송합니다.")
    public ResponseEntity<HashMap<String, Object>> mailSend(@RequestParam String memberId) {
        HashMap<String, Object> map = new HashMap<>();

        try {
            emailVerificationService.sendVerificationEmail(memberId); // 이메일 인증 메일 발송
            map.put("success", Boolean.TRUE);
            map.put("message", "인증 이메일이 전송되었습니다.");
        } catch (Exception e) {
            map.put("success", Boolean.FALSE);
            map.put("error", e.getMessage());
        }

        return ResponseEntity.ok(map);
    }

    // 인증번호 확인
    @PostMapping("/mailCheck")
    @Operation(summary = "인증 번호 확인", description = "회원가입 인증 번호를 확인합니다.")
    public ResponseEntity<String> mailCheck(@RequestParam String verificationCode) {
        boolean isVerified = emailVerificationService.verifyCode(verificationCode);

        if (isVerified) {
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 유효하지 않거나 이미 인증된 상태입니다.");
        }
    }
}