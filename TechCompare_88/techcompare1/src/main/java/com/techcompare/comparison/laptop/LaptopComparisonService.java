package com.techcompare.comparison.laptop;

import com.techcompare.comparison.common.ComparedDeviceResult;
import com.techcompare.comparison.common.ComparedCategoryResult;
import com.techcompare.comparison.common.ComparedMetricResult;
import com.techcompare.exception.DeviceNotFoundException;
import com.techcompare.laptop.Laptop;
import com.techcompare.laptop.LaptopRepository;
import com.techcompare.scoring.common.WeightedScore;
import com.techcompare.scoring.common.WeightedScoreCalculator;
import com.techcompare.scoring.laptop.LaptopCategory;
import com.techcompare.scoring.laptop.LaptopCategoryResult;
import com.techcompare.scoring.laptop.LaptopScoreResult;
import com.techcompare.scoring.laptop.LaptopScoringService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/** Scores and ranks multiple laptops using the user's category weights. */
@Service
public class LaptopComparisonService {
    private static final int MIN_DEVICES = 2;
    private static final int MAX_DEVICES = 10;

    private final LaptopRepository laptopRepository;
    private final LaptopScoringService scoringService;
    private final WeightedScoreCalculator weightedScoreCalculator;

    public LaptopComparisonService(LaptopRepository laptopRepository, LaptopScoringService scoringService,
                                   WeightedScoreCalculator weightedScoreCalculator) {
        this.laptopRepository = laptopRepository;
        this.scoringService = scoringService;
        this.weightedScoreCalculator = weightedScoreCalculator;
    }

    public LaptopComparisonResult compare(LaptopComparisonRequest request) {
        validateRequest(request);
        List<Laptop> found = laptopRepository.findAllById(request.deviceIds());
        Map<Integer, Laptop> byId = new HashMap<>();
        for (Laptop laptop : found) byId.put(laptop.getId(), laptop);
        for (Integer id : request.deviceIds()) {
            if (!byId.containsKey(id)) throw new DeviceNotFoundException("Laptop", id);
        }

        Map<LaptopCategory, Integer> orderedWeights = orderedWeights(request.categoryWeights());
        List<DeviceWithScore> scored = new ArrayList<>();
        for (Integer id : request.deviceIds()) {
            Laptop laptop = byId.get(id);
            LaptopScoreResult scores = scoringService.score(laptop);
            OptionalInt calculatedOverall =
                    calculateOverallScore(scores, orderedWeights);

            Integer overall = calculatedOverall.isPresent()
                    ? calculatedOverall.getAsInt()
                    : null;            int availableWeight = calculateAvailableWeight(scores, orderedWeights);
            Map<String, Integer> categoryScores = new LinkedHashMap<>();
            Map<String, ComparedCategoryResult> categoryDetails = new LinkedHashMap<>();
            for (LaptopCategory category : LaptopCategory.values()) {
                LaptopCategoryResult categoryResult = scores.categories().get(category);
                categoryScores.put(category.name(), categoryScore(scores, category));
                categoryDetails.put(category.name(), toComparedCategory(categoryResult,
                        orderedWeights.getOrDefault(category, 0)));
            }
            scored.add(new DeviceWithScore(new ComparedDeviceResult(laptop.getId(), laptop.getName(), laptop.getBrand(),
                    overall, availableWeight, null, categoryScores, categoryDetails), id));
        }

        // Sort highest available score first. Missing scores appear last; stable order resolves equal scores.
        scored.sort(Comparator
                .comparing((DeviceWithScore item) -> item.result().overallScore() == null)
                .thenComparing(item -> item.result().overallScore(), Comparator.nullsLast(Comparator.reverseOrder())));

        List<ComparedDeviceResult> ranked = new ArrayList<>();
        Integer previousScore = null;
        Integer previousRank = null;
        for (int i = 0; i < scored.size(); i++) {
            ComparedDeviceResult item = scored.get(i).result();
            Integer score = item.overallScore();
            Integer rank = null;
            if (score != null) {
                rank = score.equals(previousScore) ? previousRank : i + 1;
                previousScore = score;
                previousRank = rank;
            }
            ranked.add(new ComparedDeviceResult(item.deviceId(), item.deviceName(), item.brand(), score,
                    item.availableCategoryWeight(), rank, item.categoryScores(), item.categories()));
        }
        return new LaptopComparisonResult(orderedWeights, ranked);
    }


    private ComparedCategoryResult toComparedCategory(LaptopCategoryResult result, int configuredCategoryWeight) {
        if (result == null) {
            return new ComparedCategoryResult(null, configuredCategoryWeight, 0, List.of());
        }
        List<ComparedMetricResult> metrics = result.metrics().stream()
                .map(metric -> new ComparedMetricResult(metric.id(), metric.name(), metric.score(),
                        metric.configuredWeight(), metric.availableWeight()))
                .toList();
        return new ComparedCategoryResult(result.score(), configuredCategoryWeight,
                result.availableMetricWeight(), metrics);
    }

    private OptionalInt calculateOverallScore(LaptopScoreResult scores, Map<LaptopCategory, Integer> weights) {
        List<WeightedScore> entries = new ArrayList<>();
        for (LaptopCategory category : LaptopCategory.values()) {
            entries.add(new WeightedScore(categoryScore(scores, category), weights.getOrDefault(category, 0)));
        }
        return weightedScoreCalculator.calculate(entries);
    }

    private int calculateAvailableWeight(LaptopScoreResult scores, Map<LaptopCategory, Integer> weights) {
        int available = 0;
        for (LaptopCategory category : LaptopCategory.values()) {
            if (weights.getOrDefault(category, 0) > 0 && categoryScore(scores, category) != null) available += weights.get(category);
        }
        return available;
    }

    private Integer categoryScore(LaptopScoreResult scores, LaptopCategory category) {
        LaptopCategoryResult result = scores.categories().get(category);
        return result == null ? null : result.score();
    }

    private Map<LaptopCategory, Integer> orderedWeights(Map<LaptopCategory, Integer> weights) {
        Map<LaptopCategory, Integer> ordered = new LinkedHashMap<>();
        for (LaptopCategory category : LaptopCategory.values()) ordered.put(category, weights.getOrDefault(category, 0));
        return ordered;
    }

    private void validateRequest(LaptopComparisonRequest request) {
        if (request == null) throw badRequest("Request body is required.");
        if (request.deviceIds() == null || request.deviceIds().size() < MIN_DEVICES || request.deviceIds().size() > MAX_DEVICES)
            throw badRequest("deviceIds must contain between 2 and 10 laptop IDs.");
        if (request.deviceIds().stream().anyMatch(Objects::isNull)) throw badRequest("deviceIds cannot contain null values.");
        if (new HashSet<>(request.deviceIds()).size() != request.deviceIds().size()) throw badRequest("deviceIds must be unique.");
        if (request.categoryWeights() == null || request.categoryWeights().isEmpty()) throw badRequest("categoryWeights must contain integer weights.");
        int total = 0;
        for (Map.Entry<LaptopCategory, Integer> entry : request.categoryWeights().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) throw badRequest("Category names and weights cannot be null.");
            if (entry.getValue() < 0 || entry.getValue() > 100) throw badRequest("Each category weight must be between 0 and 100.");
            total += entry.getValue();
        }
        if (total != 100) throw badRequest("Category weights must add up to exactly 100.");
    }

    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private record DeviceWithScore(ComparedDeviceResult result, Integer originalId) { }
}
