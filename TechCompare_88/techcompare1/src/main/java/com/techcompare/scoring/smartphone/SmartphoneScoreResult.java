package com.techcompare.scoring.smartphone;

import java.util.Map;

/**
 * All category scores for one smartphone.
 *
 * This result deliberately contains smartphone categories only. Overall
 * comparison scoring will be added separately when the comparison API exists.
 */
public record SmartphoneScoreResult(
        Integer deviceId,
        String deviceName,
        String brand,
        Map<SmartphoneCategory, SmartphoneCategoryResult> categories
) {
}
