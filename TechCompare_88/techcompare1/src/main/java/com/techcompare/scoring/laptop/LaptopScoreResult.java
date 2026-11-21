package com.techcompare.scoring.laptop;

import java.util.Map;

/** Scores for one laptop. Overall comparison scoring is handled separately. */
public record LaptopScoreResult(
        Integer deviceId,
        String deviceName,
        String brand,
        Map<LaptopCategory, LaptopCategoryResult> categories
) {
}
