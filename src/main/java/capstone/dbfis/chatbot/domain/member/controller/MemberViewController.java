package capstone.dbfis.chatbot.domain.member.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class MemberViewController {
    @GetMapping("/login")
    public String login() {
        return "login";  // 로그인 페이지로 이동
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";  // 회원가입 페이지로 이동
    }
}