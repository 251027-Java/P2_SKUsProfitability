import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
  tailwindcss()
],
  server: {
    port: 3000,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        ws: true,
        timeout: 10000,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('Proxy error (this is normal if backend just started):', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Proxying to auth-service:', req.method, req.url);
          });
        },
        bypass: function(req, res, proxyOptions) {
          // For GET requests to /api/auth/login or /api/auth/register, serve index.html
          // This allows React Router to handle the route
          if (req.method === 'GET' && (req.url === '/api/auth/login' || req.url === '/api/auth/register')) {
            return '/index.html';
          }
          // For all other requests, proxy to auth-service
          return null;
        }
      },
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        ws: true,
        timeout: 10000,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('Proxy error (this is normal if backend just started):', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Proxying to main backend:', req.method, req.url);
          });
        }
      }
    }
  }
})
