<template>
  <div class="login-page">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>
    <div class="background-grid"></div>
    <div class="noise"></div>

    <main class="login-shell">
      <section class="hero-panel">
        <div class="hero-top">
          <div class="brand-row">
            <div class="login-logo-box">
              <img src="/logo.png" alt="LOGO">
            </div>
            <div class="login-brand-text">
              <strong>拍照管理系统</strong>
              <span>ON TOP</span>
            </div>
          </div>

          <h1 class="hero-title">
            Capture.
            <span>Manage.</span>
            Deliver.
          </h1>

          <p class="hero-desc">
            以更高级、更智能、更便捷的方式管理拍摄任务
          </p>
        </div>

        <div class="visual-stage">
          <div class="photo-chip one"><div class="chip-inner"></div></div>
          <div class="photo-chip two"><div class="chip-inner"></div></div>
          <div class="photo-chip three"><div class="chip-inner"></div></div>
          <div class="phone-card">
            <div class="phone-screen">
              <div class="camera-circle"></div>
            </div>
          </div>
        </div>

        <div class="hero-bottom">
          <span class="login-status-dot"></span>
          <span>System Online · Secure Capture</span>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-card">
          <div class="mobile-logo">
            <img src="/logo.png" alt="LOGO">
          </div>

          <div class="form-eyebrow">
            <i data-lucide="sparkles"></i>
            <span>Glowly</span>
          </div>

          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">登录后继续管理拍摄任务、照片数据和业务流程。</p>

          <div class="login-error" :class="{ show: errorMsg }">
            <i data-lucide="alert-circle"></i>
            <span>{{ errorMsg }}</span>
          </div>

          <div class="field-group">
            <label class="field-label" for="login-username">用户名</label>
            <div class="input-wrap">
              <input
                type="text"
                id="login-username"
                class="login-input"
                placeholder="请输入用户名"
                autocomplete="username"
                v-model="username"
                @keydown.enter="$refs.passwordInput.focus()"
              />
              <i data-lucide="user" class="login-input-icon"></i>
            </div>
          </div>

          <div class="field-group">
            <label class="field-label" for="login-password">密码</label>
            <div class="input-wrap">
              <input
                ref="passwordInput"
                :type="showPassword ? 'text' : 'password'"
                id="login-password"
                class="login-input"
                placeholder="请输入密码"
                autocomplete="current-password"
                v-model="password"
                @keydown.enter="handleLogin"
              />
              <i data-lucide="lock" class="login-input-icon"></i>
              <button type="button" class="password-toggle" @click="showPassword = !showPassword" aria-label="显示或隐藏密码">
                <EyeOff v-if="showPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="login-remember">
              <input type="checkbox" v-model="remember" />
              <span>记住我</span>
            </label>
            <a href="javascript:void(0)" class="forgot-link" @click="forgotPassword">忘记密码？</a>
          </div>

          <button class="login-btn" :class="{ loading: isLoading }" :disabled="isLoading" @click="handleLogin">
            <span class="btn-spinner"></span>
            <span class="btn-text">{{ isLoading ? '登录中...' : '登录系统' }}</span>
            <ArrowRight class="w-4 h-4 btn-arrow" />
          </button>

          <div class="secure-row">
            <ShieldCheck class="w-3.5 h-3.5" style="color: #34c759" />
            <span>加密连接 · 安全登录</span>
          </div>

          <div class="form-footer">
            <p>请联系管理员获取登录账号</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Eye, EyeOff, ArrowRight, ShieldCheck } from 'lucide-vue-next'
import { login } from '@/api'
import { useAuth } from '@/stores/auth'

const router = useRouter()
const { setAuth, isLoggedIn } = useAuth()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const remember = ref(false)
const isLoading = ref(false)
const errorMsg = ref('')
const passwordInput = ref(null)

onMounted(() => {
  if (isLoggedIn()) {
    router.replace('/')
  }
})

function forgotPassword() {
  alert('请联系管理员重置密码')
}

async function handleLogin() {
  if (!username.value.trim() || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }

  errorMsg.value = ''
  isLoading.value = true

  try {
    const json = await login(username.value.trim(), password.value)
    if (json.code === 200) {
      setAuth(json.data, remember.value)
      router.replace('/')
    } else {
      errorMsg.value = json.message || '登录失败'
    }
  } catch {
    errorMsg.value = '网络错误，请检查后端服务是否启动'
  } finally {
    isLoading.value = false
  }
}
</script>
