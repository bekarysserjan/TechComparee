package com.techcompare.scoring.smartphone;

import java.util.List;

/**
 * A smartphone category score plus the individual metrics that produced it.
 *
 * score is null when none of the category's metrics could be calculated.
 * availableMetricWeight indicates how much configured category weight was used.
 */
public record SmartphoneCategoryResult(
        SmartphoneCategory category,
        Integer score,
        int availableMetricWeight,
        List<MetricScoreResult> metrics
) {
}
