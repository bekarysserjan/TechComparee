# Smartphone score endpoint patch

This is an incremental patch for the existing TechCompare project. It replaces
only two source files:

- `SmartphoneScoringService.java` — registers the service with Spring and reports
  metric coverage accurately for composite metrics.
- `SmartphoneController.java` — adds `GET /api/v1/smartphones/{id}/scores`.

## Apply

Extract this ZIP into your existing project root and allow IntelliJ to overwrite
the two matching files. It does not replace your database configuration, entities,
repositories, or laptop code.

## Test

Run:

```powershell
.\gradlew.bat test
```

Then start the application and call:

```http
GET http://localhost:8080/api/v1/smartphones/201/scores
```

Replace `201` with an ID that exists in your Supabase `smartphones` table.

Expected response fields include:
- `deviceId`, `deviceName`, `brand`
- `categories.GAMING`, `categories.CREATIVE_WORK`, `categories.DAILY_USE`
- each category's integer `score`, `availableMetricWeight`, and `metrics`
- each metric's `id`, `name`, integer `score` or null, `configuredWeight`,
  and `availableWeight`

All metric reference ranges and weights are provisional v1 values.
