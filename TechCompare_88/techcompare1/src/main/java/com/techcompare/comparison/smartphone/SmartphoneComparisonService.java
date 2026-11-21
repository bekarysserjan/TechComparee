package com.techcompare.comparison.smartphone;

import com.techcompare.comparison.common.ComparedDeviceResult;
import com.techcompare.comparison.common.ComparedCategoryResult;
import com.techcompare.comparison.common.ComparedMetricResult;
import com.techcompare.exception.DeviceNotFoundException;
import com.techcompare.scoring.common.WeightedScore;
import com.techcompare.scoring.common.WeightedScoreCalculator;
import com.techcompare.scoring.smartphone.SmartphoneCategory;
import com.techcompare.scoring.smartphone.SmartphoneCategoryResult;
import com.techcompare.scoring.smartphone.SmartphoneScoreResult;
import com.techcompare.scoring.smartphone.SmartphoneScoringService;
import com.techcompare.smartphone.Smartphone;
import com.techcompare.smartphone.SmartphoneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Scores and ranks multiple smartphones using the user's category weights. */
@Service
public class SmartphoneComparisonService {
    private static final int MIN_DEVICES = 2;
    private static final int MAX_DEVICES = 10;
    private final SmartphoneRepository smartphoneRepository;
    private final SmartphoneScoringService scoringService;
    private final WeightedScoreCalculator weightedScoreCalculator;

    public SmartphoneComparisonService(SmartphoneRepository smartphoneRepository, SmartphoneScoringService scoringService,
                                       WeightedScoreCalculator weightedScoreCalculator) {
        this.smartphoneRepository = smartphoneRepository;
        this.scoringService = scoringService;
        this.weightedScoreCalculator = weightedScoreCalculator;
    }

    public SmartphoneComparisonResult compare(SmartphoneComparisonRequest request) {
        validateRequest(request);
        List<Smartphone> found = smartphoneRepository.findAllById(request.deviceIds());
        Map<Integer, Smartphone> byId = new HashMap<>();
        for (Smartphone phone : found) byId.put(phone.getId(), phone);
        for (Integer id : request.deviceIds()) if (!byId.containsKey(id)) throw new DeviceNotFoundException("Smartphone", id);

        Map<SmartphoneCategory, Integer> weights = orderedWeights(request.categoryWeights());
        List<ComparedDeviceResult> scored = new ArrayList<>();
        for (Integer id : request.deviceIds()) {
            Smartphone phone = byId.get(id);
            SmartphoneScoreResult scores = scoringService.score(phone);
            OptionalInt calculatedOverall =
                    calculateOverallScore(scores, weights);

            Integer overall = calculatedOverall.isPresent()
                    ? calculatedOverall.getAsInt()
                    : null;            int availableWeight = calculateAvailableWeight(scores, weights);
            Map<String, Integer> categoryScores = new LinkedHashMap<>();
            Map<String, ComparedCategoryResult> categoryDetails = new LinkedHashMap<>();
            for (SmartphoneCategory category : SmartphoneCategory.values()) {
                SmartphoneCategoryResult categoryResult = scores.categories().get(category);
                categoryScores.put(category.name(), categoryScore(scores, category));
                categoryDetails.put(category.name(), toComparedCategory(categoryResult,
                        weights.getOrDefault(category, 0)));
            }
            scored.add(new ComparedDeviceResult(phone.getId(), phone.getName(), phone.getBrand(), overall,
                    availableWeight, null, categoryScores, categoryDetails));
        }
        scored.sort(Comparator.comparing((ComparedDeviceResult item) -> item.overallScore() == null)
                .thenComparing(ComparedDeviceResult::overallScore, Comparator.nullsLast(Comparator.reverseOrder())));
        List<ComparedDeviceResult> ranked = new ArrayList<>();
        Integer previousScore = null, previousRank = null;
        for (int i = 0; i < scored.size(); i++) {
            ComparedDeviceResult item = scored.get(i);
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
        return new SmartphoneComparisonResult(weights, ranked);
    }


    private ComparedCategoryResult toComparedCategory(SmartphoneCategoryResult result, int configuredCategoryWeight) {
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

    private OptionalInt calculateOverallScore(SmartphoneScoreResult scores, Map<SmartphoneCategory, Integer> weights) {
        List<WeightedScore> entries = new ArrayList<>();
        for (SmartphoneCategory category : SmartphoneCategory.values()) entries.add(new WeightedScore(categoryScore(scores, category), weights.getOrDefault(category, 0)));
        return weightedScoreCalculator.calculate(entries);
    }
    private int calculateAvailableWeight(SmartphoneScoreResult scores, Map<SmartphoneCategory, Integer> weights) {
        int available = 0;
        for (SmartphoneCategory category : SmartphoneCategory.values()) if (weights.getOrDefault(category, 0) > 0 && categoryScore(scores, category) != null) available += weights.get(category);
        return available;
    }
    private Integer categoryScore(SmartphoneScoreResult scores, SmartphoneCategory category) {
        SmartphoneCategoryResult result = scores.categories().get(category); return result == null ? null : result.score();
    }
    private Map<SmartphoneCategory, Integer> orderedWeights(Map<SmartphoneCategory, Integer> weights) {
        Map<SmartphoneCategory, Integer> ordered = new LinkedHashMap<>();
        for (SmartphoneCategory category : SmartphoneCategory.values()) ordered.put(category, weights.getOrDefault(category, 0)); return ordered;
    }
    private void validateRequest(SmartphoneComparisonRequest request) {
        if (request == null) throw badRequest("Request body is required.");
        if (request.deviceIds() == null || request.deviceIds().size() < MIN_DEVICES || request.deviceIds().size() > MAX_DEVICES) throw badRequest("deviceIds must contain between 2 and 10 smartphone IDs.");
        if (request.deviceIds().stream().anyMatch(Objects::isNull)) throw badRequest("deviceIds cannot contain null values.");
        if (new HashSet<>(request.deviceIds()).size() != request.deviceIds().size()) throw badRequest("deviceIds must be unique.");
        if (request.categoryWeights() == null || request.categoryWeights().isEmpty()) throw badRequest("categoryWeights must contain integer weights.");
        int total = 0;
        for (Map.Entry<SmartphoneCategory, Integer> entry : request.categoryWeights().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) throw badRequest("Category names and weights cannot be null.");
            if (entry.getValue() < 0 || entry.getValue() > 100) throw badRequest("Each category weight must be between 0 and 100."); total += entry.getValue();
        }
        if (total != 100) throw badRequest("Category weights must add up to exactly 100.");
    }
    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}
