package com.techcompare.comparison.smartphone;

import com.techcompare.scoring.smartphone.SmartphoneCategory;
import java.util.Map;
import java.util.List;

/** Request body for comparing multiple smartphones with custom category weights. */
public record SmartphoneComparisonRequest(List<Integer> deviceIds, Map<SmartphoneCategory, Integer> categoryWeights) { }
