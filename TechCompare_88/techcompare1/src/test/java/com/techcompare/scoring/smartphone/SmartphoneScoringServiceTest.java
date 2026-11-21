package com.techcompare.scoring.smartphone;

import com.techcompare.smartphone.Smartphone;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests for the smartphone scoring profile and its missing-data behavior.
 */
class SmartphoneScoringServiceTest {

    @Test
    void returnsAllThreeCategoriesAndTheirMetrics() throws Exception {
        Smartphone phone = smartphoneWithDefaults();
        SmartphoneScoreResult result = new SmartphoneScoringService().score(phone);

        assertEquals(3, result.categories().size());
        assertEquals(5, result.categories().get(SmartphoneCategory.GAMING).metrics().size());
        assertEquals(5, result.categories().get(SmartphoneCategory.CREATIVE_WORK).metrics().size());
        assertEquals(6, result.categories().get(SmartphoneCategory.DAILY_USE).metrics().size());
    }

    @Test
    void skipsNullMetricInsteadOfScoringItAsZero() throws Exception {
        Smartphone phone = smartphoneWithDefaults();
        SmartphoneScoreResult result = new SmartphoneScoringService().score(phone);

        SmartphoneCategoryResult gaming = result.categories().get(SmartphoneCategory.GAMING);
        assertNotNull(gaming.score());
        assertEquals(90, gaming.availableMetricWeight()); // GPU, CPU, RAM, refresh; storage is null.
        MetricScoreResult storage = gaming.metrics().stream()
                .filter(metric -> metric.id().equals("STORAGE"))
                .findFirst()
                .orElseThrow();
        assertNull(storage.score());
    }

    @Test
    void categoryIsUnavailableWhenAllItsMetricsAreMissing() throws Exception {
        Smartphone phone = smartphoneWithAllScoringFieldsNull();
        SmartphoneScoreResult result = new SmartphoneScoringService().score(phone);

        assertNull(result.categories().get(SmartphoneCategory.GAMING).score());
        assertEquals(0, result.categories().get(SmartphoneCategory.GAMING).availableMetricWeight());
    }

    private Smartphone smartphoneWithDefaults() throws Exception {
        Smartphone phone = newEntity();
        set(phone, "geekbenchMulti", 4_000);
        set(phone, "geekbenchSingle", 1_500);
        set(phone, "antutuGpuScore", 800_000);
        set(phone, "ramGb", 8);
        set(phone, "storageGb", null);
        set(phone, "batteryMah", 4_500);
        set(phone, "mainCameraMp", 50);
        set(phone, "hasTelephotoLens", 1);
        set(phone, "videoMaxFps", 120);
        set(phone, "frontCameraMp", 12);
        set(phone, "screenRefreshHz", 120);
        set(phone, "screenBrightnessNits", 1_200);
        set(phone, "weightG", 221);
        set(phone, "has5g", true);
        set(phone, "id", 201);
        set(phone, "name", "Test Phone");
        set(phone, "brand", "Test Brand");
        return phone;
    }

    private Smartphone smartphoneWithAllScoringFieldsNull() throws Exception {
        Smartphone phone = newEntity();
        set(phone, "name", "Unknown Phone");
        set(phone, "brand", "Unknown");
        return phone;
    }

    private Smartphone newEntity() throws Exception {
        Constructor<Smartphone> constructor = Smartphone.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void set(Smartphone phone, String fieldName, Object value) throws Exception {
        var field = Smartphone.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(phone, value);
    }
}
