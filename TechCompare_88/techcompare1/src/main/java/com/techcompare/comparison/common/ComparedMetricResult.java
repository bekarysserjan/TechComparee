package com.techcompare.comparison.common;

/** One metric's score and configured/available weight in a comparison result. */
public record ComparedMetricResult(
        String id,
        String name,
        Integer score,
        int configuredWeight,
        int availableWeight
) { }
