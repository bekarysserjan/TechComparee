package com.techcompare.scoring.common;

/**
 * A score and the weight assigned to it when calculating a weighted average.
 *
 * A null score means that the value is unavailable and must be skipped.
 * Weights are integer percentages or other non-negative integer proportions.
 */
public record WeightedScore(Integer score, int weight) {

    public WeightedScore {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }

        if (score != null && (score < 0 || score > 100)) {
            throw new IllegalArgumentException("Score must be between 0 and 100.");
        }
    }
}
