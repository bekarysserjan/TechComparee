package com.techcompare.comparison.common;

import java.util.List;
import java.util.Map;

/** Request for comparing two or more devices of the same domain. */
public record MultiDeviceComparisonRequest(
        List<Integer> deviceIds,
        Map<String, Integer> categoryWeights
) {
}
