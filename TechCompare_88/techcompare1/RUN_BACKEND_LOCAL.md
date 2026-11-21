# Run TechCompare backend locally

The project contains fallback Supabase database settings in `src/main/resources/application.yml`, so `bootRun` can start without manually entering DB_URL/DB_USERNAME/DB_PASSWORD. Environment variables still override those defaults.

Run from the project root:

```powershell
.\gradlew.bat bootRun
```

The API should start on http://localhost:8080.

`GEMINI_API_KEY` is intentionally not included because the uploaded project did not contain a Gemini API key. The database-backed catalogue and comparison endpoints can run without it; AI overview requires setting `GEMINI_API_KEY` in the run environment.

The API endpoints are excluded from CSRF protection so frontend POST requests such as `/api/v1/comparisons/smartphones` and `/api/v1/comparisons/laptops` are not rejected with 403.
