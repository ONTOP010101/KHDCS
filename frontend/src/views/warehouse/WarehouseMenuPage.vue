<template>
  <div class="warehouse-page wh-menu-page">
    <div class="wh-page-header">
      <h1 class="wh-page-title">选择<span class="wh-highlight">功能</span>模块</h1>
      <p class="wh-page-subtitle">请选择需要使用的功能进行操作</p>
    </div>
    <div class="wh-menu-grid">
      <div class="wh-menu-card" @click="goToCodePage">
        <div class="wh-menu-icon">🔢</div>
        <div class="wh-menu-card-title">择样管理</div>
        <div class="wh-menu-card-desc">通过代号查询报价信息并确认加入</div>
      </div>
      <div class="wh-menu-card" @click="alert('库存功能开发中...')">
        <div class="wh-menu-icon">📦</div>
        <div class="wh-menu-card-title">库存管理</div>
        <div class="wh-menu-card-desc">实时查看和管理仓库库存状态</div>
      </div>
    </div>
    <button class="wh-back-btn" @click="logout">退出登录</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goToCodePage() {
  router.push('/warehouse/code')
}

function logout() {
  router.push('/warehouse/login')
}
</script>
