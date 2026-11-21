package com.techcompare.scoring.common;

import java.util.List;
import java.util.OptionalInt;

import org.springframework.stereotype.Component;


/**
 * Shared arithmetic for combining metric scores or category scores.
 *
 * This class does not know anything about laptops or smartphones. D    evice-specific
 * services decide which values and weights should be passed to this calculator.
 */
@Component
public class WeightedScoreCalculator {

    /**
     * Calculates a weighted average, ignoring entries whose score is null.
     *
     * The available weights do not have to add up to 100. The denominator is the
     * sum of weights for entries that actually have a score, so missing values
     * are skipped instead of being treated as zero.
     *
     * Returns an empty result when there are no scored entries with positive weight.
     * Uses integer arithmetic and rounds half up to the nearest whole number.
     */
    public OptionalInt calculate(List<WeightedScore> entries) {
        long weightedTotal = 0;
        long availableWeight = 0;

        for (WeightedScore entry : entries) {
            if (entry.score() == null || entry.weight() == 0) {
                continue;
            }

            weightedTotal += (long) entry.score() * entry.weight();
            availableWeight += entry.weight();
        }

        if (availableWeight == 0) {
            return OptionalInt.empty();
        }

        // Adding half the denominator implements positive integer half-up rounding.
        int roundedScore = (int) ((weightedTotal + availableWeight / 2) / availableWeight);
        return OptionalInt.of(roundedScore);
    }
}
