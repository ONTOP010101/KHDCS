const { app, BrowserWindow, dialog } = require('electron')
const { spawn } = require('child_process')
const path = require('path')
const http = require('http')
const fs = require('fs')
const os = require('os')

let mainWindow = null
let javaProcess = null
let debugWindow = null

const SERVER_PORT = 8080

function getServerUrl() {
  const configPath = path.join(path.dirname(process.execPath), 'server.conf')
  if (fs.existsSync(configPath)) {
    try {
      const content = fs.readFileSync(configPath, 'utf-8').trim()
      const match = content.match(/^SERVER_URL\s*=\s*(.+)$/m)
      if (match && match[1].trim()) {
        return match[1].trim()
      }
    } catch (e) {}
  }
  return `http://localhost:${SERVER_PORT}`
}

const SERVER_URL = getServerUrl()

const LOG_DIR = path.join(os.homedir(), '.photo-management', 'logs')
const LOG_FILE = path.join(LOG_DIR, 'backend.log')

function ensureLogDir() {
  if (!fs.existsSync(LOG_DIR)) {
    fs.mkdirSync(LOG_DIR, { recursive: true })
  }
}

function getJavaPath() {
  const jreDir = path.join(process.resourcesPath, 'jre', 'bin')
  const javaExe = process.platform === 'win32' ? 'java.exe' : 'java'
  const bundledJava = path.join(jreDir, javaExe)
  if (fs.existsSync(bundledJava)) {
    return bundledJava
  }
  return 'java'
}

function getJarPath() {
  const devPath = path.join(__dirname, '..', 'backend', 'target', 'photo-management-1.0.0.jar')
  if (fs.existsSync(devPath)) {
    return devPath
  }
  return path.join(process.resourcesPath, 'backend', 'app.jar')
}

function getConfigDir() {
  const devConfig = path.join(__dirname, '..', '..', 'config')
  if (fs.existsSync(devConfig)) {
    return devConfig
  }
  const exeDir = path.dirname(process.execPath)
  const distribConfig = path.join(exeDir, 'config')
  if (fs.existsSync(distribConfig)) {
    return distribConfig
  }
  return null
}

function showDebugWindow() {
  debugWindow = new BrowserWindow({
    width: 800,
    height: 600,
    title: '启动日志 - 拍照管理系统',
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    }
  })
  debugWindow.setMenuBarVisibility(false)

  const html = `<!DOCTYPE html>
<html><head><meta charset="utf-8"><style>
body { margin:0; padding:16px; background:#0d1117; color:#c9d1d9; font-family:Consolas,monospace; font-size:13px; line-height:1.6; }
.green { color:#3fb950; }
.yellow { color:#d29922; }
.red { color:#f85149; }
.blue { color:#58a6ff; }
</style></head><body>
<h3 class="blue">拍照管理系统 - 启动日志</h3>
<div id="log"></div>
<script>
const { ipcRenderer } = require('electron')
ipcRenderer.on('log', (event, text, color) => {
  const el = document.createElement('div')
  el.className = color || ''
  el.textContent = text
  document.getElementById('log').appendChild(el)
})
</script></body></html>`
  debugWindow.loadURL('data:text/html;charset=utf-8,' + encodeURIComponent(html))
}

function sendLog(text, color) {
  console.log(text)
  if (debugWindow && !debugWindow.isDestroyed()) {
    debugWindow.webContents.send('log', text, color || '')
  }
}

function waitForServer(retries = 180, interval = 1000) {
  return new Promise((resolve, reject) => {
    let attempts = 0
    let lastStatus = ''
    const check = () => {
      attempts++
      const req = http.request(SERVER_URL + '/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      }, (res) => {
        let body = ''
        res.on('data', (chunk) => { body += chunk })
        res.on('end', () => {
          const status = `HTTP ${res.statusCode}`
          if (status !== lastStatus) {
            sendLog(`[${attempts}] /auth/login → ${status}`, 'green')
            lastStatus = status
          }
          try {
            const json = JSON.parse(body)
            if (json && json.code !== undefined) {
              sendLog(`[${attempts}] 后端数据库已就绪 (code:${json.code})`, 'green')
              resolve()
              return
            }
          } catch (e) {}
          if (attempts % 10 === 0) {
            sendLog(`[${attempts}/${retries}] 等待数据库连接池就绪...`, 'yellow')
          }
          if (attempts < retries) {
            setTimeout(check, interval)
          } else {
            reject(new Error('Database not ready'))
          }
        })
      })
      req.on('error', () => {
        if (attempts % 10 === 0) {
          sendLog(`[${attempts}/${retries}] 等待后端启动...`, 'yellow')
        }
        if (attempts < retries) {
          setTimeout(check, interval)
        } else {
          reject(new Error('Server did not become ready'))
        }
      })
      req.write('{"username":"x","password":"x"}')
      req.end()
    }
    check()
  })
}

function startBackend() {
  const jarPath = getJarPath()
  const javaPath = getJavaPath()

  sendLog(`JRE 路径: ${javaPath}`, 'blue')
  sendLog(`JAR 路径: ${jarPath}`, 'blue')

  if (!fs.existsSync(jarPath)) {
    const msg = `找不到后端文件:\n${jarPath}`
    sendLog(msg, 'red')
    dialog.showErrorBox('启动失败', msg)
    app.quit()
    return
  }

  if (!fs.existsSync(javaPath)) {
    const msg = `找不到 Java 运行环境:\n${javaPath}\n请在下载时包含完整的程序包。`
    sendLog(msg, 'red')
    dialog.showErrorBox('启动失败', msg)
    app.quit()
    return
  }

  sendLog('正在启动后端服务...', 'blue')

  const configDir = getConfigDir()
  const javaArgs = [
    '-Djava.net.preferIPv4Stack=true',
    '-Dfile.encoding=UTF-8',
    '-Dsun.jnu.encoding=UTF-8',
    '-jar',
    jarPath,
    '--server.port=' + SERVER_PORT,
    '--spring.web.resources.static-locations=classpath:/static/'
  ]
  if (configDir) {
    sendLog(`外部配置目录: ${configDir}`, 'blue')
    javaArgs.push('--spring.config.additional-location=file:' + configDir + '/')
  }

  javaProcess = spawn(javaPath, javaArgs, {
    stdio: ['ignore', 'pipe', 'pipe'],
    windowsHide: true,
    env: { ...process.env }
  })

  javaProcess.stdout.on('data', (data) => {
    const text = data.toString().trim()
    if (text) sendLog(text, 'green')
  })

  javaProcess.stderr.on('data', (data) => {
    const text = data.toString().trim()
    if (text) sendLog(text, 'yellow')
  })

  javaProcess.on('error', (err) => {
    const msg = '无法启动后端服务:\n' + err.message
    sendLog(msg, 'red')
    dialog.showErrorBox('启动失败', msg)
    app.quit()
  })

  javaProcess.on('exit', (code) => {
    sendLog(`后端进程退出，退出码: ${code}`, code === 0 ? 'green' : 'red')
    javaProcess = null
  })
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1440,
    height: 900,
    minWidth: 1024,
    minHeight: 680,
    title: '拍照管理系统',
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    },
    show: false,
    backgroundColor: '#f5f9ff'
  })

  mainWindow.setMenuBarVisibility(false)
  mainWindow.loadURL(SERVER_URL)

  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
    sendLog('窗口已打开', 'green')
    if (debugWindow && !debugWindow.isDestroyed()) {
      debugWindow.close()
      debugWindow = null
    }
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

app.whenReady().then(async () => {
  ensureLogDir()
  showDebugWindow()

  const isLocalServer = SERVER_URL.includes('localhost') || SERVER_URL.includes('127.0.0.1')

  if (isLocalServer) {
    startBackend()
    try {
      await waitForServer(180, 1000)
    } catch (e) {
      sendLog('后端启动超时！请检查 MySQL 是否在运行。', 'red')
      sendLog('需要: MySQL 在 localhost:3306，数据库 photo_management', 'yellow')
      dialog.showErrorBox('启动超时', '后端启动超时（2分钟）。\n\n请检查:\n1. MySQL 是否已启动\n2. 数据库 photo_management 是否存在\n3. 端口 8080 是否被占用')
      if (javaProcess) {
        javaProcess.kill()
      }
      return
    }
  } else {
    sendLog('远程模式: ' + SERVER_URL, 'blue')
    sendLog('不会启动本地后端，直接连接远程服务器', 'yellow')
    try {
      await waitForServer(30, 1000)
    } catch (e) {
      sendLog('无法连接远程服务器: ' + SERVER_URL, 'red')
      dialog.showErrorBox('连接失败', '无法连接远程服务器:\n' + SERVER_URL + '\n\n请检查:\n1. 服务器是否已启动\n2. IP地址是否正确\n3. 防火墙是否放行8080端口')
      return
    }
  }

  createWindow()
})

app.on('window-all-closed', () => {
  app.quit()
})

app.on('before-quit', () => {
  if (javaProcess) {
    javaProcess.kill()
    javaProcess = null
  }
})

app.on('activate', () => {
  if (mainWindow === null) {
    createWindow()
  }
})
