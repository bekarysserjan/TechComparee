package com.techcompare.scoring.laptop;

import java.util.List;

/** Category score plus the metrics that contributed to it. */
public record LaptopCategoryResult(
        LaptopCategory category,
        Integer score,
        int availableMetricWeight,
        List<MetricScoreResult> metrics
) {
}
