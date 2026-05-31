import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const isElectron = process.env.ELECTRON === 'true'

export default defineConfig(async () => {
  const plugins = [vue()]
  if (isElectron) {
    const { default: electron } = await import('vite-plugin-electron')
    const { default: renderer } = await import('vite-plugin-electron-renderer')
    plugins.push(
      electron([
        { entry: 'electron/main.js' },
        { entry: 'electron/preload.js', onstart(args) { args.reload() } }
      ]),
      renderer()
    )
  }
  return {
    plugins,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      host: '0.0.0.0',
      port: 3000,
      proxy: {
        '/auth': { target: 'http://localhost:8080', timeout: 120000 },
        '/samples': { target: 'http://localhost:8080', timeout: 120000 },
        '/images': { target: 'http://localhost:8080', timeout: 120000 },
        '/thumbnails': { target: 'http://localhost:8080', timeout: 120000 },
        '/videos': { target: 'http://localhost:8080', timeout: 120000 },
        '/users': { target: 'http://localhost:8080', timeout: 120000 },
        '/roles': { target: 'http://localhost:8080', timeout: 120000 },
        '/logs': { target: 'http://localhost:8080', timeout: 120000 },
        '/friends': { target: 'http://localhost:8080', timeout: 120000 },
        '/chat': { target: 'http://localhost:8080', timeout: 120000 },
        '/galleries': { target: 'http://localhost:8080', timeout: 120000 },
        '/manufacturers': { target: 'http://localhost:8080', timeout: 120000 },
        '/upload': { target: 'http://localhost:8080', timeout: 120000 }
      }
    }
  }
})
