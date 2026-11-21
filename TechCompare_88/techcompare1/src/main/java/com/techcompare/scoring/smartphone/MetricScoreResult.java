package com.techcompare.scoring.smartphone;

/**
 * The score of one named metric shown in the frontend's category diagram.
 *
 * A null score means the metric could not be calculated because all of its
 * required specifications were unavailable. configuredWeight is the metric's
 * configured share of its category; availableWeight is the share actually used.
 */
public record MetricScoreResult(
        String id,
        String name,
        Integer score,
        int configuredWeight,
        int availableWeight
) {
}
