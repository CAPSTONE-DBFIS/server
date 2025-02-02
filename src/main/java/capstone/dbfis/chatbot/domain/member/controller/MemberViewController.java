package capstone.dbfis.chatbot.domain.member.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class MemberViewController {
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
}