package com.techcompare.comparison.smartphone;

import com.techcompare.scoring.common.WeightedScoreCalculator;
import com.techcompare.scoring.smartphone.SmartphoneCategory;
import com.techcompare.scoring.smartphone.SmartphoneCategoryResult;
import com.techcompare.scoring.smartphone.SmartphoneScoreResult;
import com.techcompare.scoring.smartphone.SmartphoneScoringService;
import com.techcompare.smartphone.Smartphone;
import com.techcompare.smartphone.SmartphoneRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmartphoneComparisonServiceTest {
    @Test void comparesMoreThanTwoSmartphonesAndRanksResults() {
        SmartphoneRepository repository = mock(SmartphoneRepository.class);
        SmartphoneScoringService scoring = mock(SmartphoneScoringService.class);
        Smartphone a = phone(101, "Apple", "iPhone 15 Pro Max");
        Smartphone b = phone(103, "Google", "Pixel 8 Pro");
        Smartphone c = phone(105, "Samsung", "Galaxy S");
        when(repository.findAllById(List.of(101, 103, 105))).thenReturn(List.of(a, b, c));
        when(scoring.score(a)).thenReturn(score(52, 62, 72));
        when(scoring.score(b)).thenReturn(score(39, 48, 71));
        when(scoring.score(c)).thenReturn(score(65, 67, 75));
        SmartphoneComparisonResult result = new SmartphoneComparisonService(repository, scoring, new WeightedScoreCalculator())
                .compare(new SmartphoneComparisonRequest(List.of(101, 103, 105), weights()));
        assertEquals(3, result.results().size());
        assertEquals(105, result.results().get(0).deviceId());
        assertEquals(101, result.results().get(1).deviceId());
        assertEquals(103, result.results().get(2).deviceId());
    }

    @Test void rejectsWeightsThatDoNotSumToOneHundred() {
        SmartphoneRepository repository = mock(SmartphoneRepository.class);
        SmartphoneScoringService scoring = mock(SmartphoneScoringService.class);
        Map<SmartphoneCategory, Integer> weights = new EnumMap<>(SmartphoneCategory.class);
        weights.put(SmartphoneCategory.GAMING, 50); weights.put(SmartphoneCategory.CREATIVE_WORK, 25); weights.put(SmartphoneCategory.DAILY_USE, 20);
        assertThrows(ResponseStatusException.class, () -> new SmartphoneComparisonService(repository, scoring, new WeightedScoreCalculator())
                .compare(new SmartphoneComparisonRequest(List.of(101, 103), weights)));
        verifyNoInteractions(repository, scoring);
    }

    private Map<SmartphoneCategory, Integer> weights() {
        Map<SmartphoneCategory, Integer> map = new EnumMap<>(SmartphoneCategory.class);
        map.put(SmartphoneCategory.GAMING, 50); map.put(SmartphoneCategory.CREATIVE_WORK, 25); map.put(SmartphoneCategory.DAILY_USE, 25); return map;
    }
    private Smartphone phone(int id, String brand, String name) {
        Smartphone item = mock(Smartphone.class); when(item.getId()).thenReturn(id); when(item.getBrand()).thenReturn(brand); when(item.getName()).thenReturn(name); return item;
    }
    private SmartphoneScoreResult score(int gaming, int creative, int daily) {
        Map<SmartphoneCategory, SmartphoneCategoryResult> map = new EnumMap<>(SmartphoneCategory.class);
        map.put(SmartphoneCategory.GAMING, new SmartphoneCategoryResult(SmartphoneCategory.GAMING, gaming, 100, List.of()));
        map.put(SmartphoneCategory.CREATIVE_WORK, new SmartphoneCategoryResult(SmartphoneCategory.CREATIVE_WORK, creative, 100, List.of()));
        map.put(SmartphoneCategory.DAILY_USE, new SmartphoneCategoryResult(SmartphoneCategory.DAILY_USE, daily, 100, List.of()));
        return new SmartphoneScoreResult(1, "Test phone", "Test", map);
    }
}
