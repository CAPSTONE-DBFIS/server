package capstone.dbfis.chatbot.domain.insight.controller;

import capstone.dbfis.chatbot.domain.insight.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/insight")
public class InsightController {

    @Autowired
    private InsightService insightService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getInsights(@RequestParam("date") String date) {
        return ResponseEntity.ok(insightService.getInsights(date));
    }

    @GetMapping("/related-search")
    public ResponseEntity<String> searchArticles(
            @RequestParam String keyword,
            @RequestParam String relatedKeyword,
            @RequestParam String date,
            @RequestParam(required = false, defaultValue = "0") int page) throws IOException {

        String response = insightService.searchByRelatedKeywords(keyword, relatedKeyword, date, page);
        return ResponseEntity.ok(response);
    }
}