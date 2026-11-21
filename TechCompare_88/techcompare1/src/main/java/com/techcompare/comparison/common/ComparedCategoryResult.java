package com.techcompare.comparison.common;

import java.util.List;

/** Full score breakdown for one category in a device comparison. */
public record ComparedCategoryResult(
        Integer score,
        int configuredWeight,
        int availableMetricWeight,
        List<ComparedMetricResult> metrics
) { }
