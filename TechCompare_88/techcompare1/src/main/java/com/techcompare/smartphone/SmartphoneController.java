package com.techcompare.smartphone;

import com.techcompare.exception.DeviceNotFoundException;
import com.techcompare.scoring.smartphone.SmartphoneScoreResult;
import com.techcompare.scoring.smartphone.SmartphoneScoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP endpoints for smartphone data and smartphone scores.
 * Laptop endpoints and scoring live in their own packages.
 */
@RestController
@RequestMapping("/api/v1/smartphones")
public class SmartphoneController {

    private final SmartphoneRepository smartphoneRepository;
    private final SmartphoneScoringService smartphoneScoringService;

    public SmartphoneController(
            SmartphoneRepository smartphoneRepository,
            SmartphoneScoringService smartphoneScoringService
    ) {
        this.smartphoneRepository = smartphoneRepository;
        this.smartphoneScoringService = smartphoneScoringService;
    }

    @GetMapping
    public List<Smartphone> getAllSmartphones() {
        return smartphoneRepository.findAll();
    }

    @GetMapping("/{id}")
    public Smartphone getSmartphoneById(@PathVariable Integer id) {
        return findSmartphoneOrThrow(id);
    }

    /**
     * Returns category scores and each individual metric score for the frontend.
     * This endpoint does not compare devices yet; it scores one phone.
     */
    @GetMapping("/{id}/scores")
    public SmartphoneScoreResult getSmartphoneScores(@PathVariable Integer id) {
        Smartphone phone = findSmartphoneOrThrow(id);
        return smartphoneScoringService.score(phone);
    }

    private Smartphone findSmartphoneOrThrow(Integer id) {
        return smartphoneRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Smartphone", id));
    }
}
