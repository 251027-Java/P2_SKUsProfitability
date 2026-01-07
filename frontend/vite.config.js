import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 3000,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Vite Proxy -> Gateway (AUTH):', req.method, req.url);
          });
        }
      },
      '/product': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Vite Proxy -> Gateway (PRODUCT):', req.method, req.url);
          });
          proxy.on('error', (err, req, res) => {
            console.error('Proxy Error (PRODUCT):', err);
          });
        }
      },
      '/calculator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Vite Proxy -> Gateway (CALCULATOR):', req.method, req.url);
          });
        }
      }
    }
  }
})