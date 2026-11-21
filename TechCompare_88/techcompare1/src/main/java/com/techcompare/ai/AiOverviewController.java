package com.techcompare.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Produces a plain-language explanation of an already calculated comparison.
 * The AI endpoint does not calculate scores or choose the winner.
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiOverviewController {

    private final AiOverviewService aiOverviewService;

    public AiOverviewController(AiOverviewService aiOverviewService) {
        this.aiOverviewService = aiOverviewService;
    }

    @PostMapping("/overview")
    public AiOverviewResponse createOverview(@RequestBody AiOverviewRequest request) {
        return new AiOverviewResponse(aiOverviewService.createOverview(request));
    }
}
