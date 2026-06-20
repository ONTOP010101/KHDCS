import { app, BrowserWindow, shell } from 'electron'
import { spawn } from 'child_process'
import pathModule from 'path'
import http from 'http'
import fs from 'fs'
import { fileURLToPath } from 'url'

const { join } = pathModule
const __filename = fileURLToPath(import.meta.url)
const __dirname = pathModule.dirname(__filename)

// 防止管道断开的 EPIPE 错误导致崩溃
process.stdout.on('error', () => {})
process.stderr.on('error', () => {})

let mainWindow = null
let backendProcess = null

function getJavaPath() {
  const jrePath = join(process.resourcesPath, 'jre', 'bin', 'java.exe')
  if (fs.existsSync(jrePath)) return jrePath
  return 'java'
}

function getJarPath() {
  const jarPath = join(process.resourcesPath, 'backend', 'app.jar')
  if (fs.existsSync(jarPath)) return jarPath
  return null
}

function startBackend() {
  const jarPath = getJarPath()
  if (!jarPath) {
    console.log('[Backend] JAR not found, assuming external backend')
    return Promise.resolve()
  }

  const javaPath = getJavaPath()
  console.log(`[Backend] Starting: ${javaPath} -jar ${jarPath}`)

  backendProcess = spawn(javaPath, ['-jar', jarPath, '--server.port=8080'], {
    cwd: join(process.resourcesPath, 'backend'),
    stdio: 'pipe',
    windowsHide: true
  })

  backendProcess.stdout.on('data', (data) => {
    try { console.log(`[Backend] ${data.toString().trim()}`) } catch {}
  })

  backendProcess.stderr.on('data', (data) => {
    try { console.log(`[Backend] ${data.toString().trim()}`) } catch {}
  })

  backendProcess.on('error', (err) => {
    try { console.error('[Backend] Failed to start:', err.message) } catch {}
  })

  // 等待后端启动就绪
  return waitForBackend()
}

function waitForBackend(retries = 60) {
  return new Promise((resolve, reject) => {
    function check() {
      http.get('http://localhost:8080/', (res) => {
        console.log('[Backend] Ready')
        resolve()
      }).on('error', () => {
        retries--
        if (retries <= 0) {
          console.error('[Backend] Timeout waiting for backend')
          resolve()
        } else {
          setTimeout(check, 500)
        }
      })
    }
    setTimeout(check, 1000)
  })
}

function stopBackend() {
  if (backendProcess) {
    console.log('[Backend] Stopping...')
    backendProcess.kill()
    backendProcess = null
  }
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1440,
    height: 900,
    minWidth: 1024,
    minHeight: 680,
    show: false,
    frame: true,
    autoHideMenuBar: true,
    icon: join(__dirname, '../public/logo.png'),
    webPreferences: {
      preload: join(__dirname, 'preload.js'),
      sandbox: false,
      contextIsolation: true,
      nodeIntegration: false
    }
  })

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  if (process.env.VITE_DEV_SERVER_URL) {
    mainWindow.loadURL(process.env.VITE_DEV_SERVER_URL)
  } else {
    mainWindow.loadFile(join(__dirname, '../dist/index.html'))
  }
}

app.whenReady().then(async () => {
  // 只有生产模式才自动启动后端
  if (!process.env.VITE_DEV_SERVER_URL) {
    await startBackend()
  }

  createWindow()

  // 注入脚本：拦截 img.src 设置，将相对路径重定向到 localhost:8080
  mainWindow.webContents.on('did-finish-load', () => {
    mainWindow.webContents.executeJavaScript(`
      (function() {
        var d = Object.getOwnPropertyDescriptor(HTMLImageElement.prototype, 'src')
        Object.defineProperty(HTMLImageElement.prototype, 'src', {
          get: d.get,
          set: function(v) {
            if (typeof v === 'string' && (v.indexOf('/thumbnails/') === 0 || v.indexOf('/images/') === 0)) {
              v = 'http://localhost:8080' + v
            }
            d.set.call(this, v)
          },
          configurable: true
        })
      })()
    `)
  })

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  stopBackend()
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('before-quit', () => {
  stopBackend()
})
