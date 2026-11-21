package com.techcompare.comparison.laptop;

import com.techcompare.laptop.Laptop;
import com.techcompare.laptop.LaptopRepository;
import com.techcompare.scoring.common.WeightedScoreCalculator;
import com.techcompare.scoring.laptop.LaptopCategory;
import com.techcompare.scoring.laptop.LaptopCategoryResult;
import com.techcompare.scoring.laptop.LaptopScoreResult;
import com.techcompare.scoring.laptop.LaptopScoringService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LaptopComparisonServiceTest {
    @Test void comparesMoreThanTwoLaptopsAndRanksResults() {
        LaptopRepository repository = mock(LaptopRepository.class);
        LaptopScoringService scoring = mock(LaptopScoringService.class);
        Laptop a = laptop(203, "ASUS", "ROG G14");
        Laptop b = laptop(201, "Apple", "MacBook Pro");
        Laptop c = laptop(205, "Lenovo", "Legion");
        when(repository.findAllById(List.of(203, 201, 205))).thenReturn(List.of(a, b, c));
        when(scoring.score(a)).thenReturn(score(57, 61, 61));
        when(scoring.score(b)).thenReturn(score(76, 76, 77));
        when(scoring.score(c)).thenReturn(score(70, 65, 60));
        Map<LaptopCategory, Integer> weights = weights();
        LaptopComparisonResult result = new LaptopComparisonService(repository, scoring, new WeightedScoreCalculator())
                .compare(new LaptopComparisonRequest(List.of(203, 201, 205), weights));
        assertEquals(3, result.results().size());
        assertEquals(201, result.results().get(0).deviceId());
        assertEquals(76, result.results().get(0).overallScore());
        assertEquals(205, result.results().get(1).deviceId());
        assertEquals(203, result.results().get(2).deviceId());
        assertEquals(1, result.results().get(0).rank());
    }

    @Test void rejectsDuplicateIds() {
        LaptopRepository repository = mock(LaptopRepository.class);
        LaptopScoringService scoring = mock(LaptopScoringService.class);
        assertThrows(ResponseStatusException.class, () -> new LaptopComparisonService(repository, scoring, new WeightedScoreCalculator())
                .compare(new LaptopComparisonRequest(List.of(203, 203), weights())));
        verifyNoInteractions(repository, scoring);
    }

    private Map<LaptopCategory, Integer> weights() {
        Map<LaptopCategory, Integer> map = new EnumMap<>(LaptopCategory.class);
        map.put(LaptopCategory.GAMING, 50); map.put(LaptopCategory.CREATIVE_WORK, 25); map.put(LaptopCategory.DAILY_USE, 25); return map;
    }
    private Laptop laptop(int id, String brand, String name) {
        Laptop item = mock(Laptop.class); when(item.getId()).thenReturn(id); when(item.getBrand()).thenReturn(brand); when(item.getName()).thenReturn(name); return item;
    }
    private LaptopScoreResult score(int gaming, int creative, int daily) {
        Map<LaptopCategory, LaptopCategoryResult> map = new EnumMap<>(LaptopCategory.class);
        map.put(LaptopCategory.GAMING, new LaptopCategoryResult(LaptopCategory.GAMING, gaming, 100, List.of()));
        map.put(LaptopCategory.CREATIVE_WORK, new LaptopCategoryResult(LaptopCategory.CREATIVE_WORK, creative, 100, List.of()));
        map.put(LaptopCategory.DAILY_USE, new LaptopCategoryResult(LaptopCategory.DAILY_USE, daily, 100, List.of()));
        return new LaptopScoreResult(1, "Test laptop", "Test", map);
    }
}
