import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 3000,
    proxy: {
      // Proxy for Authentication
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Vite Proxy -> Gateway (AUTH):', req.method, req.url);
          });
        }
      },
      
      // Proxy for Product/SKU Service
      '/product': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // // This removes '/product' from the path before it hits the backend
        // rewrite: (path) => path.replace(/^\/product/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Vite Proxy -> Gateway (PRODUCT):', req.method, req.url);
          });
          // Added error logging to see if the proxy itself is failing
          proxy.on('error', (err, req, res) => {
            console.error('Proxy Error (PRODUCT):', err);
          });
        }
      },

      // Proxy for Calculator Service
      '/calculator': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // This removes '/calculator' from the path before it hits the backend
        //rewrite: (path) => path.replace(/^\/calculator/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Vite Proxy -> Gateway (CALCULATOR):', req.method, req.url);
          });
        }
      }
    }
  }
})