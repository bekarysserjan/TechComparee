import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Прокси отправляет запросы API на Spring Boot и избавляет от проблем CORS.
export default defineConfig({
  plugins: [react()],
  server: { proxy: { "/api": "http://localhost:8080" } }
});