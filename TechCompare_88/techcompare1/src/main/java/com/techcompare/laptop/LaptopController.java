package com.techcompare.laptop;

import com.techcompare.exception.DeviceNotFoundException;
import com.techcompare.scoring.laptop.LaptopScoreResult;
import com.techcompare.scoring.laptop.LaptopScoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** HTTP endpoints for laptop data and laptop-specific scoring. */
@RestController
@RequestMapping("/api/v1/laptops")
public class LaptopController {

    private final LaptopRepository laptopRepository;
    private final LaptopScoringService laptopScoringService;

    public LaptopController(LaptopRepository laptopRepository, LaptopScoringService laptopScoringService) {
        this.laptopRepository = laptopRepository;
        this.laptopScoringService = laptopScoringService;
    }

    @GetMapping
    public List<Laptop> getAllLaptops() {
        return laptopRepository.findAll();
    }

    @GetMapping("/{id}")
    public Laptop getLaptopById(@PathVariable Integer id) {
        return laptopRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Laptop", id));
    }

    /** Returns Gaming, Creative Work, and Daily Use scores with their component metrics. */
    @GetMapping("/{id}/scores")
    public LaptopScoreResult getLaptopScores(@PathVariable Integer id) {
        Laptop laptop = laptopRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Laptop", id));
        return laptopScoringService.score(laptop);
    }
}
