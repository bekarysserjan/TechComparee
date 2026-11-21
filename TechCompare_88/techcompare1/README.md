# TechCompare backend — milestone 1

A readable Spring Boot backend starter that reads laptop and smartphone records from the existing Supabase PostgreSQL database.

## Requirements

- JDK 21
- Gradle (or use the Gradle integration in your IDE)
- Supabase project with the `laptops` and `smartphones` tables

## Configure the database

1. Copy `.env.example` as a reference; do not put real credentials into Git.
2. In your IDE run configuration, set these environment variables using the PostgreSQL connection information from Supabase:
   - `DB_URL`: JDBC URL, for example `jdbc:postgresql://<host>:5432/postgres?sslmode=require`
   - `DB_USERNAME`: database username from Supabase
   - `DB_PASSWORD`: database password
3. If the direct connection is unavailable on your network, use the connection string and pooler settings shown by Supabase for your project.

## Run

Run `TechCompareApplication` from your IDE, or run `./gradlew bootRun` if a Gradle wrapper has been added. The app uses `ddl-auto: validate`, so it will not create or alter your tables.

## Endpoints

- `GET http://localhost:8080/api/v1/laptops`
- `GET http://localhost:8080/api/v1/laptops/{id}`
- `GET http://localhost:8080/api/v1/smartphones`
- `GET http://localhost:8080/api/v1/smartphones/{id}`

## Current scope

This milestone only verifies database connectivity and device retrieval. Scoring, comparison templates, custom weights, response DTOs, and Gemini integration will be added in later milestones.

## Important note about NULL values

The scoring engine will skip null metric values. The current SQL schema declares most specification columns `NOT NULL`, so those columns must be made nullable in Supabase if you want to represent unknown specifications as SQL `NULL`. Review defaults such as `has_5g DEFAULT TRUE` and `has_telephoto_lens DEFAULT 0` too: defaults should not imply a specification was verified when it was not.
