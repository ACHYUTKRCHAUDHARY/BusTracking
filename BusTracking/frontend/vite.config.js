import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // sockjs-client assumes a Node-style `global` (used for its WebSocket
  // feature-detection); Vite's browser build doesn't provide one.
  define: {
    global: 'globalThis',
  },
})
