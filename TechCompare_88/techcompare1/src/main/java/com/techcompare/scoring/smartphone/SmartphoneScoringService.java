package com.techcompare.scoring.smartphone;

import com.techcompare.scoring.common.MetricNormalizer;
import com.techcompare.scoring.common.WeightedScore;
import com.techcompare.scoring.common.WeightedScoreCalculator;
import com.techcompare.smartphone.Smartphone;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.springframework.stereotype.Service;
/**
 * Calculates smartphone-only metric and category scores.
 *
 * This service does not access the database or calculate laptop scores.
 * Each metric first normalizes its underlying specifications, then combines
 * those specification scores. Category scoring combines the resulting metric
 * scores. Missing values are skipped at each level.
 *
 * The ranges and weights below are provisional v1 values and can be adjusted
 * without changing the calculation utilities.
 */
@Service
public class SmartphoneScoringService {

    private final MetricNormalizer normalizer = new MetricNormalizer();
    private final WeightedScoreCalculator calculator = new WeightedScoreCalculator();

    /**
     * Returns every configured category and its individual metrics for a phone.
     */
    public SmartphoneScoreResult score(Smartphone phone) {
        Map<SmartphoneCategory, SmartphoneCategoryResult> categories = new LinkedHashMap<>();

        categories.put(SmartphoneCategory.GAMING, scoreGaming(phone));
        categories.put(SmartphoneCategory.CREATIVE_WORK, scoreCreativeWork(phone));
        categories.put(SmartphoneCategory.DAILY_USE, scoreDailyUse(phone));

        return new SmartphoneScoreResult(
                phone.getId(),
                phone.getName(),
                phone.getBrand(),
                Map.copyOf(categories)
        );
    }

    private SmartphoneCategoryResult scoreGaming(Smartphone phone) {
        List<MetricScoreResult> metrics = List.of(
                metric("GPU_PERFORMANCE", "GPU Performance",
                        scoreHigher(phone.getAntutuGpuScore(), 100_000, 1_500_000), 35, 100),
                metric("CPU_PERFORMANCE", "CPU Performance",
                        scoreHigher(phone.getGeekbenchMulti(), 1_000, 8_000), 25, 100),
                metric("MEMORY", "Memory",
                        scoreHigher(phone.getRamGb(), 2, 16), 15, 100),
                metric("STORAGE", "Storage",
                        storageScore(phone.getStorageGb()), 10, 100),
                metric("DISPLAY_SMOOTHNESS", "Display Smoothness",
                        scoreHigher(phone.getScreenRefreshHz(), 60, 240), 15, 100)
        );

        return category(SmartphoneCategory.GAMING, metrics);
    }

    private SmartphoneCategoryResult scoreCreativeWork(Smartphone phone) {
        List<MetricScoreResult> metrics = List.of(
                metric("PROCESSING", "Processing",
                        composite(
                                part(scoreHigher(phone.getGeekbenchMulti(), 1_000, 8_000), 60),
                                part(scoreHigher(phone.getGeekbenchSingle(), 500, 2_500), 40)
                        ), 25, 100),
                metric("MEMORY", "Memory",
                        scoreHigher(phone.getRamGb(), 2, 16), 15, 100),
                metric("STORAGE", "Storage",
                        storageScore(phone.getStorageGb()), 10, 100),
                metric("PHOTOGRAPHY", "Photography",
                        composite(
                                part(scoreHigher(phone.getMainCameraMp(), 8, 200), 30),
                                part(telephotoScore(phone.getHasTelephotoLens()), 20),
                                part(scoreHigher(phone.getVideoMaxFps(), 30, 240), 30),
                                part(scoreHigher(phone.getFrontCameraMp(), 5, 60), 20)
                        ), 35, 100),
                metric("DISPLAY_QUALITY", "Display Quality",
                        composite(
                                part(scoreHigher(phone.getScreenBrightnessNits(), 400, 2_500), 70),
                                part(scoreHigher(phone.getScreenRefreshHz(), 60, 240), 30)
                        ), 15, 100)
        );

        return category(SmartphoneCategory.CREATIVE_WORK, metrics);
    }

    private SmartphoneCategoryResult scoreDailyUse(Smartphone phone) {
        List<MetricScoreResult> metrics = List.of(
                metric("CPU_RESPONSIVENESS", "CPU Responsiveness",
                        scoreHigher(phone.getGeekbenchSingle(), 500, 2_500), 20, 100),
                metric("BATTERY_CAPACITY", "Battery Capacity",
                        scoreHigher(phone.getBatteryMah(), 2_000, 6_000), 25, 100),
                metric("STORAGE", "Storage",
                        storageScore(phone.getStorageGb()), 10, 100),
                metric("DISPLAY_VISIBILITY", "Display Visibility",
                        scoreHigher(phone.getScreenBrightnessNits(), 400, 2_500), 15, 100),
                metric("PORTABILITY", "Portability",
                        scoreLower(phone.getWeightG(), 140, 300), 20, 100),
                metric("CONNECTIVITY", "Connectivity",
                        fiveGScore(phone.getHas5g()), 10, 100)
        );

        return category(SmartphoneCategory.DAILY_USE, metrics);
    }

    /**
     * Combines the available metric scores using the configured category weights.
     * Unavailable metrics are retained in the response but excluded from the sum.
     */
    private SmartphoneCategoryResult category(
            SmartphoneCategory category,
            List<MetricScoreResult> metrics
    ) {
        List<WeightedScore> weightedScores = new ArrayList<>();
        for (MetricScoreResult metric : metrics) {
            weightedScores.add(new WeightedScore(metric.score(), metric.configuredWeight()));
        }

        OptionalInt score = calculator.calculate(weightedScores);
        int availableWeight = metrics.stream()
                .filter(metric -> metric.score() != null)
                .mapToInt(MetricScoreResult::configuredWeight)
                .sum();

        return new SmartphoneCategoryResult(
                category,
                score.isPresent() ? score.getAsInt() : null,
                availableWeight,
                List.copyOf(metrics)
        );
    }

    /**
     * Normalizes one raw specification and wraps it as a one-specification metric.
     */
    private Integer scoreHigher(Integer value, int minimum, int maximum) {
        return normalizer.higherIsBetter(value, minimum, maximum);
    }

    private Integer scoreLower(Integer value, int minimum, int maximum) {
        return normalizer.lowerIsBetter(value, minimum, maximum);
    }

    /**
     * Storage uses fixed capacity tiers rather than a linear range.
     * This gives ordinary capacities useful scores while preserving diminishing returns.
     * Values below 64 GB score 0; null remains unavailable.
     */
    private Integer storageScore(Integer storageGb) {
        if (storageGb == null) {
            return null;
        }
        if (storageGb < 64) {
            return 0;
        }
        if (storageGb < 128) {
            return 20;
        }
        if (storageGb < 256) {
            return 40;
        }
        if (storageGb < 512) {
            return 60;
        }
        if (storageGb < 1_024) {
            return 80;
        }
        return 100;
    }

    /**
     * Telephoto support is stored as 0, 1, or 2. We assign a manual ordinal score.
     * Unknown values remain unavailable instead of being treated as no telephoto.
     */
    private Integer telephotoScore(Integer lensCount) {
        if (lensCount == null) {
            return null;
        }

        return switch (lensCount) {
            case 0 -> 0;
            case 1 -> 50;
            case 2 -> 100;
            default -> throw new IllegalArgumentException(
                    "has_telephoto_lens must be 0, 1, 2, or null."
            );
        };
    }

    /**
     * 5G support is a categorical capability: supported = 100, unsupported = 0.
     * A null database value remains unavailable.
     */
    private Integer fiveGScore(Boolean has5g) {
        if (has5g == null) {
            return null;
        }
        return has5g ? 100 : 0;
    }

    /**
     * Combines normalized specifications into one metric.
     * The internal weights need not total 100 because missing values are skipped.
     */
    private Integer composite(WeightedScore... parts) {
        List<WeightedScore> entries = List.of(parts);
        OptionalInt score = calculator.calculate(entries);
        return score.isPresent() ? score.getAsInt() : null;
    }

    private WeightedScore part(Integer score, int weight) {
        return new WeightedScore(score, weight);
    }

    private MetricScoreResult metric(
            String id,
            String name,
            Integer score,
            int configuredWeight,
            int availableWeight
    ) {
        // For a metric made from several specifications, availableWeight is
        // reserved for reporting specification coverage in a later refinement.
        return new MetricScoreResult(
                id,
                name,
                score,
                configuredWeight,
                score == null ? 0 : availableWeight
        );
    }
}
