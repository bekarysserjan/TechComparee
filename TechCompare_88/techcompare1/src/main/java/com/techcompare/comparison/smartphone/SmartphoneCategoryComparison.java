package com.techcompare.comparison.smartphone;

import com.techcompare.scoring.smartphone.SmartphoneCategory;

/** Side-by-side scores for one category. Difference is A minus B. */
public record SmartphoneCategoryComparison(
        SmartphoneCategory category,
        int configuredWeight,
        Integer deviceAScore,
        Integer deviceBScore,
        Integer scoreDifference
) {
}
