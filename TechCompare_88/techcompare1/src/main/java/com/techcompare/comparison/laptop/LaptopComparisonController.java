package com.techcompare.comparison.laptop;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoint for comparing multiple laptops. */
@RestController
@RequestMapping("/api/v1/comparisons/laptops")
public class LaptopComparisonController {
    private final LaptopComparisonService comparisonService;
    public LaptopComparisonController(LaptopComparisonService comparisonService) { this.comparisonService = comparisonService; }
    @PostMapping
    public LaptopComparisonResult compareLaptops(@RequestBody LaptopComparisonRequest request) { return comparisonService.compare(request); }
}
