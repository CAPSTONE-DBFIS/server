package capstone.dbfis.chatbot.domain.member.controller;


import capstone.dbfis.chatbot.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class MemberViewController {
    private final MemberService memberService;

    @GetMapping("/login")
    @Operation(summary = "로그인 페이지로 이동", description = "로그인 페이지로 이동합니다.")
    public String login() {
        return "login";  // 로그인 페이지로 이동
    }

    @GetMapping("/signup")
    @Operation(summary = "회원가입 페이지로 이동", description = "회원가입 페이지로 이동합니다.")
    public String signup() {
        return "signup";  // 회원가입 페이지로 이동
    }

    @GetMapping("/reset-password")
    @Operation(
            summary = "비밀번호 재설정 페이지 이동",
            description = "사용자가 이메일로 발송된 링크로 비밀번호 재설정을 진행할 수 있도록 해당 페이지를 반환합니다.<br>"
                    + "유효한 토큰이 제공되면 비밀번호 변경 폼을 표시하고, <br>"
                    + "만료(유효기간 30분)되었거나 유효하지 않은 경우 오류 메시지를 전달합니다. <br>"
                    + "비밀번호 확인(새 비밀번호 일치 여부) 검증은 프론트엔드에서 수행합니다."
    )
    public String resetPassword(@RequestParam("token") String token, Model model) {
        // 패스워드 초기화 토큰이 만료되었거나 유효하지 않을때
        if (!memberService.isValidPasswordResetToken(token)) {
            model.addAttribute("errorMessage", "비밀번호 재설정 토큰이 만료되었거나 유효하지 않습니다.");
        }
        // 정상 처리
        else {
            model.addAttribute("token", token); // HTML 템플릿에 토큰 전달
        }
        return "reset-password";  // `reset-password.html` 렌더링
    }
}