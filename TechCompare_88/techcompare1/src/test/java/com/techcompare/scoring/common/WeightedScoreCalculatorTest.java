package com.techcompare.scoring.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedScoreCalculatorTest {

    private final WeightedScoreCalculator calculator = new WeightedScoreCalculator();

    @Test
    void calculatesWeightedAverageWhenAllScoresAreAvailable() {
        var result = calculator.calculate(List.of(
                new WeightedScore(90, 40),
                new WeightedScore(80, 30),
                new WeightedScore(70, 30)
        ));

        assertEquals(81, result.orElseThrow());
    }

    @Test
    void skipsMissingScoresAndRenormalizesAvailableWeights() {
        var result = calculator.calculate(java.util.Arrays.asList(
                new WeightedScore(90, 40),
                new WeightedScore(null, 30),
                new WeightedScore(70, 10)
        ));

        // (90 * 40 + 70 * 10) / (40 + 10) = 86
        assertEquals(86, result.orElseThrow());
    }

    @Test
    void returnsEmptyWhenNoUsableScoresExist() {
        var result = calculator.calculate(java.util.Arrays.asList(
                new WeightedScore(null, 50),
                new WeightedScore(null, 50)
        ));

        assertTrue(result.isEmpty());
    }

    @Test
    void roundsHalfUpToAnInteger() {
        var result = calculator.calculate(List.of(
                new WeightedScore(80, 1),
                new WeightedScore(81, 1)
        ));

        assertEquals(81, result.orElseThrow());
    }
}
