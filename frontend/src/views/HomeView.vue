<template>
  <div class="dashboard">
    <div class="welcome-card">
      <div class="welcome-title">
        <h3>欢迎回来，{{ displayName }}</h3>
        <p>
          这里是资料管理系统的工作台。你可以快速查看样品资料、图库资产、厂商信息以及近期业务流程。
        </p>
      </div>

      <button class="welcome-action" @click="$router.push('/sample')">
        管理样品
      </button>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <Database class="w-[19px] h-[19px]" />
        </div>
        <div class="stat-value">128</div>
        <div class="stat-label">样品资料</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <ImageIcon class="w-[19px] h-[19px]" />
        </div>
        <div class="stat-value">3.6k</div>
        <div class="stat-label">图库照片</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <Store class="w-[19px] h-[19px]" />
        </div>
        <div class="stat-value">56</div>
        <div class="stat-label">合作厂商</div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <Activity class="w-[19px] h-[19px]" />
        </div>
        <div class="stat-value">98%</div>
        <div class="stat-label">系统状态</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Database, Image as ImageIcon, Store, Activity } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'

const { state } = useAuth()
const displayName = computed(() => state.userInfo?.realName || state.userInfo?.username || 'Admin')
</script>
