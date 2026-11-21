package com.techcompare.comparison.laptop;

import com.techcompare.scoring.laptop.LaptopCategory;

/** Side-by-side scores for one category. Difference is A minus B. */
public record LaptopCategoryComparison(
        LaptopCategory category,
        int configuredWeight,
        Integer deviceAScore,
        Integer deviceBScore,
        Integer scoreDifference
) {
}
