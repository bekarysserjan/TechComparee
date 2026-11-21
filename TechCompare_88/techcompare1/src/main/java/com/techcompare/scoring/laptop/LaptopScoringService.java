package com.techcompare.scoring.laptop;

import com.techcompare.laptop.Laptop;
import com.techcompare.scoring.common.MetricNormalizer;
import com.techcompare.scoring.common.WeightedScore;
import com.techcompare.scoring.common.WeightedScoreCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Calculates laptop-only scores. This service does not read the database;
 * the controller supplies a Laptop entity loaded by the repository.
 *
 * V1 scoring ranges and weights are provisional and intentionally kept here
 * so they can be calibrated without changing the shared scoring arithmetic.
 * Missing specifications are skipped and remaining weights are re-normalized.
 */
@Service
public class LaptopScoringService {

    private final MetricNormalizer normalizer = new MetricNormalizer();
    private final WeightedScoreCalculator calculator = new WeightedScoreCalculator();

    /** Scores all three categories for one laptop. */
    public LaptopScoreResult score(Laptop laptop) {
        Map<LaptopCategory, LaptopCategoryResult> categories = new LinkedHashMap<>();
        categories.put(LaptopCategory.GAMING, scoreGaming(laptop));
        categories.put(LaptopCategory.CREATIVE_WORK, scoreCreativeWork(laptop));
        categories.put(LaptopCategory.DAILY_USE, scoreDailyUse(laptop));

        return new LaptopScoreResult(
                laptop.getId(), laptop.getName(), laptop.getBrand(),
                Collections.unmodifiableMap(categories)
        );
    }

    private LaptopCategoryResult scoreGaming(Laptop laptop) {
        List<MetricScoreResult> metrics = List.of(
                metric("GPU_PERFORMANCE", "GPU Performance",
                        single(higher(laptop.getGpuBenchmarkScore(), 10_000, 250_000)), 35),
                metric("CPU_PERFORMANCE", "CPU Performance",
                        composite(
                                part(higher(laptop.getGeekbenchMulti(), 1_000, 20_000), 70),
                                part(higher(laptop.getGeekbenchSingle(), 500, 3_500), 30)
                        ), 25),
                metric("MEMORY", "Memory",
                        single(higher(laptop.getRamGb(), 4, 64)), 15),
                metric("STORAGE", "Storage",
                        single(storageScore(laptop.getStorageGb())), 10),
                metric("DISPLAY_DETAIL", "Display Detail",
                        single(higher(laptop.getScreenResolutionPixels(), 1_000_000, 8_300_000)), 15)
        );
        return category(LaptopCategory.GAMING, metrics);
    }

    private LaptopCategoryResult scoreCreativeWork(Laptop laptop) {
        List<MetricScoreResult> metrics = List.of(
                metric("CPU_PROCESSING", "CPU Processing",
                        composite(
                                part(higher(laptop.getGeekbenchMulti(), 1_000, 20_000), 65),
                                part(higher(laptop.getGeekbenchSingle(), 500, 3_500), 35)
                        ), 25),
                metric("GPU_PERFORMANCE", "GPU Performance",
                        single(higher(laptop.getGpuBenchmarkScore(), 10_000, 250_000)), 25),
                metric("MEMORY", "Memory",
                        single(higher(laptop.getRamGb(), 4, 64)), 20),
                metric("STORAGE", "Storage",
                        single(storageScore(laptop.getStorageGb())), 10),
                metric("DISPLAY_QUALITY", "Display Quality",
                        composite(
                                part(higher(laptop.getScreenResolutionPixels(), 1_000_000, 8_300_000), 40),
                                part(screenSizeScore(laptop.getScreenSizeInch()), 20),
                                part(panelTypeScore(laptop.getPanelType()), 40)
                        ), 20)
        );
        return category(LaptopCategory.CREATIVE_WORK, metrics);
    }

    private LaptopCategoryResult scoreDailyUse(Laptop laptop) {
        List<MetricScoreResult> metrics = List.of(
                metric("CPU_RESPONSIVENESS", "CPU Responsiveness",
                        single(higher(laptop.getGeekbenchSingle(), 500, 3_500)), 20),
                metric("BATTERY_LIFE", "Battery Life",
                        single(higher(laptop.getBatteryLifeHours(), 2, 20)), 25),
                metric("PORTABILITY", "Portability",
                        single(lower(weightInGrams(laptop.getWeightKg()), 800, 4_000)), 20),
                metric("MEMORY", "Memory",
                        single(higher(laptop.getRamGb(), 4, 64)), 10),
                metric("STORAGE", "Storage",
                        single(storageScore(laptop.getStorageGb())), 10),
                metric("DISPLAY_AND_FEATURES", "Display and Features",
                        composite(
                                part(screenSizeScore(laptop.getScreenSizeInch()), 70),
                                part(touchscreenScore(laptop.getHasTouchscreen()), 30)
                        ), 15)
        );
        return category(LaptopCategory.DAILY_USE, metrics);
    }

    private LaptopCategoryResult category(LaptopCategory category, List<MetricScoreResult> metrics) {
        List<WeightedScore> entries = new ArrayList<>();
        for (MetricScoreResult metric : metrics) {
            entries.add(new WeightedScore(metric.score(), metric.configuredWeight()));
        }

        OptionalInt calculated = calculator.calculate(entries);
        int availableWeight = metrics.stream()
                .filter(metric -> metric.score() != null)
                .mapToInt(MetricScoreResult::configuredWeight)
                .sum();

        return new LaptopCategoryResult(
                category,
                calculated.isPresent() ? calculated.getAsInt() : null,
                availableWeight,
                metrics
        );
    }

    private MetricScoreResult metric(String id, String name, MetricCalculation calculation, int configuredWeight) {
        return new MetricScoreResult(id, name, calculation.score(), configuredWeight,
                calculation.availableWeight());
    }

    private MetricCalculation single(Integer score) {
        return new MetricCalculation(score, score == null ? 0 : 100);
    }

    /** Combines parts of a metric, ignoring parts whose raw specification is missing. */
    private MetricCalculation composite(WeightedScore... parts) {
        List<WeightedScore> entries = new ArrayList<>(List.of(parts));
        OptionalInt result = calculator.calculate(entries);
        int availableWeight = entries.stream()
                .filter(entry -> entry.score() != null)
                .mapToInt(WeightedScore::weight)
                .sum();
        return new MetricCalculation(result.isPresent() ? result.getAsInt() : null, availableWeight);
    }

    private record MetricCalculation(Integer score, int availableWeight) { }


    private WeightedScore part(Integer score, int weight) {
        return new WeightedScore(score, weight);
    }

    private Integer higher(Integer value, int minimum, int maximum) {
        return normalizer.higherIsBetter(value, minimum, maximum);
    }

    private Integer lower(Integer value, int minimum, int maximum) {
        return normalizer.lowerIsBetter(value, minimum, maximum);
    }

    /**
     * Storage is scored by capacity tiers rather than linearly, because moving
     * from 128 GB to 256 GB is a practical step, not simply twice the usefulness.
     */
    private Integer storageScore(Integer storageGb) {
        if (storageGb == null) return null;
        if (storageGb < 64) return 0;
        if (storageGb < 128) return 20;
        if (storageGb < 256) return 40;
        if (storageGb < 512) return 60;
        if (storageGb < 1024) return 80;
        return 100;
    }

    /** Converts the database's kg value to integer grams for laptop-only scoring. */
    private Integer weightInGrams(BigDecimal weightKg) {
        if (weightKg == null) return null;
        return weightKg.multiply(BigDecimal.valueOf(1_000))
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /** Converts a screen size to tenths of an inch for integer normalization. */
    private Integer screenSizeInTenths(BigDecimal screenSizeInch) {
        if (screenSizeInch == null) return null;
        return screenSizeInch.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private Integer screenSizeScore(BigDecimal screenSizeInch) {
        return higher(screenSizeInTenths(screenSizeInch), 110, 180);
    }

    /** Manual v1 ranking. Unknown labels are skipped instead of assigned a bad score. */
    private Integer panelTypeScore(String panelType) {
        if (panelType == null || panelType.isBlank()) return null;
        String panel = panelType.trim().toLowerCase()
                .replace("-", "").replace("_", "").replace(" ", "");

        return switch (panel) {
            case "oled", "amoled" -> 100;
            case "miniled", "mini-led" -> 95;
            case "ips" -> 75;
            case "va" -> 65;
            case "tn" -> 40;
            default -> null;
        };
    }

    /** Touchscreen is a bonus feature, not a requirement: false receives 50, null is skipped. */
    private Integer touchscreenScore(Boolean hasTouchscreen) {
        if (hasTouchscreen == null) return null;
        return hasTouchscreen ? 100 : 50;
    }
}
