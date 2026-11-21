package com.techcompare.comparison.common;

import java.util.List;
import java.util.Map;

/** Results for a comparison containing any supported number of devices. */
public record MultiDeviceComparisonResult(
        Map<String, Integer> categoryWeights,
        List<ComparedDeviceResult> results
) {
}
