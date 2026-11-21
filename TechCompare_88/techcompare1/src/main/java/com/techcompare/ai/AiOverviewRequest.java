package com.techcompare.ai;

import java.util.List;
import java.util.Map;

/**
 * Contains the comparison response data that the frontend wants explained.
 * Scores are supplied by TechCompare's scoring/comparison services.
 */
public record AiOverviewRequest(
        String deviceType,
        Map<String, Integer> categoryWeights,
        List<AiDeviceResult> results
) {
}
