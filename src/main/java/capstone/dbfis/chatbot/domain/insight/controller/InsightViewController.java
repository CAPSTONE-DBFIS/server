package capstone.dbfis.chatbot.domain.insight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 인사이트 웹 페이지를 제공하는 컨트롤러 클래스
 */
@Controller
public class InsightViewController {

    /**
     * 국내 인사이트 페이지
     */
    @GetMapping("/insight-view")
    public String insight() {
        return "insight";
    }

    /**
     * 해외 인사이트 페이지
     */
    @GetMapping("/foreign-insight-view")
    public String foreignInsight() {
        return "foreign-insight";
    }
} 