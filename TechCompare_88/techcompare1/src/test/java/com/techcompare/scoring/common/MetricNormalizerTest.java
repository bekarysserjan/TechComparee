package com.techcompare.scoring.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricNormalizerTest {

    private final MetricNormalizer normalizer = new MetricNormalizer();

    @Test
    void normalizesHigherValuesAsBetter() {
        assertEquals(0, normalizer.higherIsBetter(2000, 2000, 10000));
        assertEquals(25, normalizer.higherIsBetter(4000, 2000, 10000));
        assertEquals(100, normalizer.higherIsBetter(12000, 2000, 10000));
    }

    @Test
    void normalizesLowerValuesAsBetter() {
        assertEquals(100, normalizer.lowerIsBetter(140, 140, 300));
        assertEquals(50, normalizer.lowerIsBetter(220, 140, 300));
        assertEquals(0, normalizer.lowerIsBetter(350, 140, 300));
    }

    @Test
    void keepsNullValuesUnavailable() {
        assertNull(normalizer.higherIsBetter(null, 0, 100));
        assertNull(normalizer.lowerIsBetter(null, 0, 100));
    }

    @Test
    void rejectsInvalidReferenceRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> normalizer.higherIsBetter(10, 100, 100));
    }
}
