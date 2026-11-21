package com.techcompare.ai;

import java.util.Map;

/** A read-only view of one device's already calculated comparison result. */
public record AiDeviceResult(
        Integer deviceId,
        String deviceName,
        String brand,
        Integer overallScore,
        Integer availableCategoryWeight,
        Integer rank,
        Map<String, Integer> categoryScores
) {
}
