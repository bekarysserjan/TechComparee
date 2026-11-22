FROM node:22-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ .
RUN npm run build


FROM gradle:8.14-jdk21 AS build

WORKDIR /app

COPY . .

COPY --from=frontend-build /frontend/dist ./src/main/resources/static

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
