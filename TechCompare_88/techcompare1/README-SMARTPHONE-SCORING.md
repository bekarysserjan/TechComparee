# Smartphone scoring increment

This ZIP contains **additive files only** for the existing TechCompare project.
Extract it into the project root. It does not replace your existing entities,
controllers, Supabase configuration, or laptop code.

## Added files

- `SmartphoneScoringService`: smartphone-only scoring profiles.
- `SmartphoneCategory`: supported smartphone categories.
- `MetricScoreResult`: one metric score for the frontend diagram.
- `SmartphoneCategoryResult`: category score, metric results, and coverage.
- `SmartphoneScoreResult`: all three categories for one smartphone.
- `SmartphoneScoringServiceTest`: basic behavior and missing-value tests.

## Provisional v1 profiles

### Gaming (weights total 100)
- GPU Performance: 35
- CPU Performance: 25
- Memory: 15
- Storage: 10
- Display Smoothness: 15

### Creative Work (weights total 100)
- Processing: 25 (Geekbench multi 60%, single 40%)
- Memory: 15
- Storage: 10
- Photography: 35 (main camera 30%, telephoto support 20%, video FPS 30%, front camera 20%)
- Display Quality: 15 (brightness 70%, refresh rate 30%)

### Daily Use (weights total 100)
- CPU Responsiveness: 20
- Battery Capacity: 25
- Storage: 10
- Display Visibility: 15
- Portability: 20
- Connectivity: 10

All numeric reference ranges are provisional and intentionally simple. Adjust them in
`SmartphoneScoringService` after testing sample records. Manual rules:
- telephoto lens count: 0 -> 0, 1 -> 50, 2 -> 100
- 5G: true -> 100, false -> 0
- null -> unavailable

## Important

This increment adds the scoring service but does not expose a scoring endpoint yet.
First extract the files, run `.\gradlew.bat test`, and resolve any errors. After the
tests pass, the next step is to add a dedicated smartphone score endpoint that loads
a phone from `SmartphoneRepository` and returns `SmartphoneScoreResult`.
