package capstone.dbfis.chatbot.domain.Member;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
    MemberService memberService;
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
