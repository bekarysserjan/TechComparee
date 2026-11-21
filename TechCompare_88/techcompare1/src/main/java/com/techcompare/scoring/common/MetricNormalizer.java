package com.techcompare.scoring.common;

/**
 * Converts numeric specifications into integer scores from 0 to 100.
 *
 * Reference ranges are fixed in a scoring profile, so adding another device to
 * the database does not change the score of an existing device.
 */
public class MetricNormalizer {

    /**
     * Normalizes a value where larger values are better.
     * Values at or below the minimum score 0; values at or above the maximum score 100.
     * A null input stays unavailable instead of becoming zero.
     */
    public Integer higherIsBetter(Integer value, int minimum, int maximum) {
        validateRange(minimum, maximum);

        if (value == null) {
            return null;
        }
        if (value <= minimum) {
            return 0;
        }
        if (value >= maximum) {
            return 100;
        }

        long numerator = (long) (value - minimum) * 100;
        long denominator = maximum - minimum;
        return (int) ((numerator + denominator / 2) / denominator);
    }

    /**
     * Normalizes a value where smaller values are better, such as device weight.
     * Values at or below the minimum score 100; values at or above the maximum score 0.
     * A null input stays unavailable instead of becoming zero.
     */
    public Integer lowerIsBetter(Integer value, int minimum, int maximum) {
        validateRange(minimum, maximum);

        if (value == null) {
            return null;
        }
        if (value <= minimum) {
            return 100;
        }
        if (value >= maximum) {
            return 0;
        }

        long numerator = (long) (maximum - value) * 100;
        long denominator = maximum - minimum;
        return (int) ((numerator + denominator / 2) / denominator);
    }

    private void validateRange(int minimum, int maximum) {
        if (minimum >= maximum) {
            throw new IllegalArgumentException("Minimum must be smaller than maximum.");
        }
    }
}
