package capstone.dbfis.chatbot.domain.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }

    @GetMapping("/upload")
    public String uploadPage() { return "upload"; }

    @GetMapping("/file-list")
    public String fileListPage() { return "file-list"; }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/agent_chat")
    public String agentPage() {
        return "agent_chat";
    }

    @GetMapping("/graph_chat")
    public String graphPage() {
        return "graph_chat";
    }

    @GetMapping("/tracking")
    public String trackingPage() { return "tracking"; }
}
