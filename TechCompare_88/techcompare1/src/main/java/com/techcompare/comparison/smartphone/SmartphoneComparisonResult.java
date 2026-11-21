package com.techcompare.comparison.smartphone;

import com.techcompare.comparison.common.ComparedDeviceResult;
import com.techcompare.scoring.smartphone.SmartphoneCategory;
import java.util.List;
import java.util.Map;

/** Response for a multi-smartphone comparison. */
public record SmartphoneComparisonResult(Map<SmartphoneCategory, Integer> categoryWeights,
                                         List<ComparedDeviceResult> results) { }
