package capstone.dbfis.chatbot.domain.chatbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ChatbotViewController {
    @GetMapping("/chat/{chatroomId}")
    public String chatRoomPage(@PathVariable Long chatroomId, Model model) {
        model.addAttribute("chatroomId", chatroomId);
        return "chat";  // chat.html 템플릿 반환 (/resource/templates)
    }
}
