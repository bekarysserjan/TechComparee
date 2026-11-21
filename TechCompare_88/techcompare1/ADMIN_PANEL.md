# TechCompare Admin Panel

The admin UI is part of the existing Spring Boot application and uses the existing Supabase PostgreSQL connection. It does not create or alter tables (`spring.jpa.hibernate.ddl-auto` remains `validate`).

## Start

1. Configure the existing database variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
2. Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` in your IDE Run Configuration / environment. Change the example password to a strong, unique password.
3. Run the Spring Boot application.
4. Open `http://localhost:8080/admin`. Spring Security will redirect unauthenticated users to the login page.
5. Sign in with the admin environment variables.

## Features

- Dashboard with device counts.
- List/search existing smartphones and laptops.
- Create, edit, and delete device records.
- CSRF protection and an authenticated `ROLE_ADMIN` requirement for `/admin/**`.
- The public `/api/v1/smartphones` and `/api/v1/laptops` endpoints remain accessible as before.

## Important notes

- This panel uses the columns already mapped by the JPA entities, including `image_url`. Confirm that the Supabase tables have these columns before starting; Hibernate schema validation is intentionally enabled.
- `has_telephoto_lens` is represented by an integer in the current entity (use 0/1), while `has_5g` and `has_touchscreen` are Boolean fields.
- Use a strong `ADMIN_PASSWORD` and do not commit real database/admin credentials.
- I could not run the Gradle test suite in this environment because the Gradle wrapper distribution could not be downloaded (network unavailable). Run `gradlew test` locally to verify against your configured environment.
