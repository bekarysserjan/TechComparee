package com.techcompare.comparison.laptop;

import com.techcompare.comparison.common.ComparedDeviceResult;
import com.techcompare.scoring.laptop.LaptopCategory;

import java.util.List;
import java.util.Map;

/** Response for a multi-laptop comparison. */
public record LaptopComparisonResult(
        Map<LaptopCategory, Integer> categoryWeights,
        List<ComparedDeviceResult> results
) {
}
