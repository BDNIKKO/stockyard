import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Product images are served by the API at /uploads.
    proxy: { '/uploads': 'http://localhost:8080' },
  },
});
