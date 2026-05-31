# 拍照管理系统 — 服务器部署流程

## 一、服务器环境要求

| 组件 | 版本要求 | 用途 |
|------|---------|------|
| 操作系统 | Windows Server 2016+ / Windows 10+ | — |
| JDK | 17 或以上 | 运行 Spring Boot |
| MySQL | 8.0 | 数据库 |
| Redis | 5.0+ | 缓存 |

---

## 二、部署前准备

### 2.1 准备以下文件，拷贝到服务器

```
D:\photo-management\
├── backend.jar              ← 在开发机上 mvn package 后得到
├── sql\
│   ├── init.sql             ← 建库建表
│   └── index-optimization.sql  ← 索引优化
├── web\                     ← 前端静态文件（从 project/ 目录拷贝）
│   ├── index.html
│   └── assets\
│       ├── css\
│       ├── js\
│       └── images\
└── start.bat                ← 启动脚本
```

### 2.2 在开发机上打包

```powershell
cd d:\全新页面\backend
& "$env:USERPROFILE\apache-maven-3.9.6\bin\mvn.cmd" clean package -DskipTests
```

产物路径：`d:\全新页面\backend\target\photo-management-1.0.0.jar`

### 2.3 在开发机上准备前端文件

```powershell
# 复制前端到 D:\photo-management\web\
Copy-Item "d:\全新页面\project\*" "D:\photo-management\web\" -Recurse
```

---

## 三、服务器上操作步骤

### 第一步：安装 JDK 17+

下载：https://adoptium.net/download/
选 **JDK 17 LTS**，Windows x64，安装后验证：

```powershell
java -version
# 应显示 17.x.x
```

### 第二步：安装 MySQL 8.0

下载：https://dev.mysql.com/downloads/installer/

安装时记住 root 密码，建议与配置一致设为 `123456`。

安装后连接验证：

```powershell
mysql -u root -p
```

### 第三步：初始化数据库

```powershell
# 创建数据库和所有表
mysql -u root -p < D:\photo-management\sql\init.sql

# 添加性能索引
mysql -u root -p < D:\photo-management\sql\index-optimization.sql
```

### 第四步：安装 Redis

下载 Redis-x64-5.0.14.1.zip：
https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip

解压到 `C:\Redis`，注册为 Windows 服务（管理员 PowerShell）：

```powershell
C:\Redis\redis-server.exe --service-install --service-name Redis6379 --port 6379
net start Redis6379
```

验证：

```powershell
C:\Redis\redis-cli.exe ping
# 返回 PONG
```

### 第五步：创建图片存储目录

```powershell
mkdir D:\photo-management\images
mkdir D:\photo-management\thumbnails
mkdir D:\photo-management\uploads
```

### 第六步：配置并启动后端

创建启动脚本 `D:\photo-management\start.bat`：

```batch
@echo off
title Photo Management System
cd /d D:\photo-management
java -jar backend.jar
pause
```

如果 MySQL 密码不是 `123456`，用以下方式启动（覆盖密码）：

```batch
java -jar backend.jar --spring.datasource.password=你的密码
```

### 第七步：配置 Nginx（推荐，生产环境）

下载 Nginx：https://nginx.org/en/download.html （Windows 版本）

配置 `nginx.conf`：

```nginx
worker_processes 1;
events { worker_connections 1024; }

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile      on;
    keepalive_timeout 65;
    gzip on;
    gzip_types text/css application/javascript image/svg+xml;
    gzip_min_length 256;

    upstream backend {
        server 127.0.0.1:8080;
    }

    server {
        listen 80;
        server_name _;

        # 前端静态文件，30 天浏览器缓存
        location /assets/ {
            root D:/photo-management/web;
            expires 30d;
            add_header Cache-Control "public, immutable";
        }

        location /index.html {
            root D:/photo-management/web;
            expires -1;
        }

        location / {
            root D:/photo-management/web;
            try_files $uri $uri/ /index.html;
        }

        # API 代理到后端
        location /auth/ {
            proxy_pass http://backend/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
        location /users/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /roles/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /samples/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /gallery/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /images/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /friends/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /chat/ { proxy_pass http://backend/; proxy_set_header Host $host; }
        location /logs/ { proxy_pass http://backend/; proxy_set_header Host $host; }
    }
}
```

启动 Nginx：

```powershell
nginx.exe -c D:\photo-management\nginx.conf
```

### 第八步：配置开机自启动

创建计划任务（管理员 PowerShell）：

```powershell
# MySQL 和 Redis 安装时通常已注册为服务，会自启

# 后端自启动（创建计划任务）
$action = New-ScheduledTaskAction -Execute "java" -Argument "-jar D:\photo-management\backend.jar" -WorkingDirectory "D:\photo-management"
$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
Register-ScheduledTask -TaskName "PhotoManagementBackend" -Action $action -Trigger $trigger -Principal $principal -Description "拍照管理系统后端"

# Nginx 自启动（按需）
$nginxAction = New-ScheduledTaskAction -Execute "D:\nginx\nginx.exe" -WorkingDirectory "D:\nginx"
$nginxTrigger = New-ScheduledTaskTrigger -AtStartup
Register-ScheduledTask -TaskName "NginxPhoto" -Action $nginxAction -Trigger $nginxTrigger -Principal $principal
```

---

## 四、访问与验证

### 局域网内访问

```powershell
# 查看服务器 IP
ipconfig
```

其他电脑浏览器访问：`http://服务器IP:8080`

（如果配了 Nginx，直接 `http://服务器IP` 即可，默认 80 端口）

### 默认登录账号

```
用户名: admin
密码:   admin123
```

### 验证清单

| 检查项 | 操作 |
|--------|------|
| 登录 | admin / admin123 |
| 用户管理 | 能看到用户列表，新增用户 |
| 角色管理 | 能看到角色和权限 |
| Redis 缓存 | `C:\Redis\redis-cli.exe keys "*"` 有缓存数据 |
| 图片上传 | 上传图片后 D:\photo-management\images\ 下有文件 |

---

## 五、防火墙配置

如果需要局域网内其他电脑访问：

```powershell
# 打开防火墙端口
netsh advfirewall firewall add rule name="Photo 8080" dir=in action=allow protocol=TCP localport=8080

# 如果配了 Nginx 80 端口
netsh advfirewall firewall add rule name="Photo 80" dir=in action=allow protocol=TCP localport=80
```

---

## 六、日常运维

```powershell
# 检查 MySQL 状态
net start | findstr MySQL

# 检查 Redis 状态
C:\Redis\redis-cli.exe ping

# 重启后端
taskkill /f /im java.exe
D:\photo-management\start.bat

# 查看 Java 进程
tasklist | findstr java
```
