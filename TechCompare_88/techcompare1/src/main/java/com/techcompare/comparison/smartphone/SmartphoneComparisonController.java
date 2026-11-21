package com.techcompare.comparison.smartphone;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoint for comparing multiple smartphones. */
@RestController
@RequestMapping("/api/v1/comparisons/smartphones")
public class SmartphoneComparisonController {
    private final SmartphoneComparisonService comparisonService;
    public SmartphoneComparisonController(SmartphoneComparisonService comparisonService) { this.comparisonService = comparisonService; }
    @PostMapping
    public SmartphoneComparisonResult compareSmartphones(@RequestBody SmartphoneComparisonRequest request) { return comparisonService.compare(request); }
}
