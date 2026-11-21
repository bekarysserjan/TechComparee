package com.techcompare.comparison.common;

import java.util.Map;

/** Scores, category/metric details, and ranking for one device. */
public record ComparedDeviceResult(
        Integer deviceId,
        String deviceName,
        String brand,
        Integer overallScore,
        Integer availableCategoryWeight,
        Integer rank,
        Map<String, Integer> categoryScores,
        Map<String, ComparedCategoryResult> categories
) {
    /** Backward-compatible constructor for callers that only have category totals. */
    public ComparedDeviceResult(Integer deviceId, String deviceName, String brand,
                                Integer overallScore, Integer availableCategoryWeight,
                                Integer rank, Map<String, Integer> categoryScores) {
        this(deviceId, deviceName, brand, overallScore, availableCategoryWeight,
                rank, categoryScores, Map.of());
    }
}
