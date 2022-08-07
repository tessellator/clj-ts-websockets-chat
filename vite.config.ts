import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: { outDir: "resources/public" },
  server: {
    proxy: {
      "^/api/.*": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
