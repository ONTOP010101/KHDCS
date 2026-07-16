<template>
  <div class="warehouse-page wh-login-page">
    <div class="wh-logo-container">
      <img class="wh-logo" src="/logo.png" alt="LOGO">
      <div class="wh-logo-subtitle">新悦翔玩具展馆</div>
      <div class="wh-logo-title">ON TOP</div>
    </div>
    <div class="wh-login-form">
      <div class="wh-form-group">
        <label class="wh-form-label">用户名</label>
        <input
          v-model="username"
          class="wh-form-input"
          placeholder="请输入用户名"
          @keydown.enter="login"
        >
      </div>
      <div class="wh-form-group">
        <label class="wh-form-label">密码</label>
        <input
          ref="passwordRef"
          v-model="password"
          type="password"
          class="wh-form-input"
          placeholder="请输入密码"
          @keydown.enter="login"
        >
      </div>
      <button class="wh-btn-primary" @click="login">登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')
const passwordRef = ref(null)

function login() {
  if (username.value && password.value) {
    router.push('/warehouse/menu')
  } else {
    alert('请输入用户名和密码')
  }
}
</script>
