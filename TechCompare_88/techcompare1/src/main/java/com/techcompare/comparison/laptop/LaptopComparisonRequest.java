package com.techcompare.comparison.laptop;

import com.techcompare.comparison.common.MultiDeviceComparisonRequest;
import com.techcompare.scoring.laptop.LaptopCategory;

import java.util.EnumMap;
import java.util.Map;

/** Request body for comparing multiple laptops with custom category weights. */
public record LaptopComparisonRequest(java.util.List<Integer> deviceIds,
                                     Map<LaptopCategory, Integer> categoryWeights) {
    public MultiDeviceComparisonRequest toCommonRequest() {
        Map<String, Integer> weights = new java.util.LinkedHashMap<>();
        if (categoryWeights != null) categoryWeights.forEach((key, value) -> weights.put(key == null ? null : key.name(), value));
        return new MultiDeviceComparisonRequest(deviceIds, weights);
    }
}
