package com.techcompare.scoring.laptop;

/**
 * One metric shown by the frontend.
 * A null score means the metric could not be calculated from available specs.
 * availableWeight is the part of the metric's internal spec weighting actually used.
 */
public record MetricScoreResult(
        String id,
        String name,
        Integer score,
        int configuredWeight,
        int availableWeight
) {
}
