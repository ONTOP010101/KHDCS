<template>
  <div class="admin-page">
    <div class="admin-stats">
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon" style="background:rgba(59,130,246,0.1);color:#3b82f6">
          <CalendarDays :size="24" />
        </div>
        <div class="admin-stat-info">
          <span class="stat-value">{{ logs.length }}</span>
          <span class="stat-label">今日日志</span>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon" style="background:rgba(34,197,94,0.1);color:#22c55e">
          <LogIn :size="24" />
        </div>
        <div class="admin-stat-info">
          <span class="stat-value">{{ logs.filter(l => l.type === '登录日志').length }}</span>
          <span class="stat-label">登录日志</span>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon" style="background:rgba(168,85,247,0.1);color:#a855f7">
          <MousePointerClick :size="24" />
        </div>
        <div class="admin-stat-info">
          <span class="stat-value">{{ logs.filter(l => l.type === '操作日志').length }}</span>
          <span class="stat-label">操作日志</span>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon" style="background:rgba(239,68,68,0.1);color:#ef4444">
          <Bug :size="24" />
        </div>
        <div class="admin-stat-info">
          <span class="stat-value">{{ logs.filter(l => l.type === '异常日志').length }}</span>
          <span class="stat-label">异常日志</span>
        </div>
      </div>
    </div>

    <div class="admin-card admin-toolbar">
      <input
        v-model="searchText"
        class="admin-search"
        placeholder="搜索操作人/模块/IP"
        @keyup.enter="handleQuery"
      />
      <select v-model="typeFilter" class="admin-select">
        <option value="">全部类型</option>
        <option value="登录日志">登录日志</option>
        <option value="操作日志">操作日志</option>
        <option value="异常日志">异常日志</option>
      </select>
      <select v-model="statusFilter" class="admin-select">
        <option value="">全部状态</option>
        <option value="成功">成功</option>
        <option value="失败">失败</option>
      </select>
      <button class="admin-btn admin-btn-primary" @click="handleQuery">
        <Search :size="16" />
        查询
      </button>
      <button class="admin-btn admin-btn-danger" @click="handleClearAll">
        <Trash2 :size="16" />
        清空日志
      </button>
      <button class="admin-btn admin-btn-primary" @click="handleExport">
        <Download :size="16" />
        导出日志
      </button>
      <button class="admin-btn admin-btn-ghost" @click="handleRefresh">
        <RefreshCw :size="16" />
        刷新
      </button>
    </div>

    <div class="admin-card admin-table-card">
      <div class="admin-table-head">
        <span class="admin-table-title">日志列表</span>
      </div>
      <div class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th>操作人</th>
              <th>日志类型</th>
              <th>操作模块</th>
              <th>操作内容</th>
              <th>IP地址</th>
              <th>状态</th>
              <th>操作时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredLogs.length === 0">
              <td colspan="8" class="admin-empty">暂无日志数据</td>
            </tr>
            <tr v-for="log in filteredLogs" :key="log.id">
              <td>{{ log.user }}</td>
              <td>
                <span
                  class="admin-badge"
                  :style="{
                    background: log.type === '登录日志' ? 'rgba(34,197,94,0.1)' : log.type === '操作日志' ? 'rgba(59,130,246,0.1)' : 'rgba(239,68,68,0.1)',
                    color: log.type === '登录日志' ? '#22c55e' : log.type === '操作日志' ? '#3b82f6' : '#ef4444'
                  }"
                >
                  {{ log.type }}
                </span>
              </td>
              <td>{{ log.module }}</td>
              <td>{{ log.content }}</td>
              <td>{{ log.ip }}</td>
              <td>
                <span
                  class="admin-badge"
                  :style="{
                    background: log.status === '成功' ? 'rgba(34,197,94,0.1)' : 'rgba(239,68,68,0.1)',
                    color: log.status === '成功' ? '#22c55e' : '#ef4444'
                  }"
                >
                  {{ log.status }}
                </span>
              </td>
              <td>{{ log.time }}</td>
              <td>
                <div class="admin-row-actions">
                  <button class="admin-row-btn" @click="handleDetail(log)">
                    <Eye :size="14" /> 详情
                  </button>
                  <button class="admin-row-btn" style="color:#ef4444" @click="handleDelete(log)">
                    <Trash2 :size="14" /> 删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showModal" class="admin-modal-mask" @click.self="showModal = false">
      <div class="admin-modal" style="max-width:640px">
        <div class="admin-modal-head">
          <span>日志详情</span>
          <button style="background:none;border:none;cursor:pointer" @click="showModal = false">
            <X :size="20" />
          </button>
        </div>
        <div class="admin-modal-body">
          <div class="admin-detail-list">
            <div class="admin-detail-item">
              <span class="detail-label">操作人</span>
              <span class="detail-value">{{ detailLog?.user }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">日志类型</span>
              <span class="detail-value">{{ detailLog?.type }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">操作模块</span>
              <span class="detail-value">{{ detailLog?.module }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">操作内容</span>
              <span class="detail-value">{{ detailLog?.content }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">IP地址</span>
              <span class="detail-value">{{ detailLog?.ip }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">状态</span>
              <span class="detail-value">{{ detailLog?.status }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">操作时间</span>
              <span class="detail-value">{{ detailLog?.time }}</span>
            </div>
            <div class="admin-detail-item">
              <span class="detail-label">详细信息</span>
              <span class="detail-value">{{ detailLog?.detail }}</span>
            </div>
          </div>
        </div>
        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" @click="showModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { CalendarDays, LogIn, MousePointerClick, Bug, Search, Trash2, Download, RefreshCw, Eye, X } from 'lucide-vue-next'
import '@/styles/users.css'

const initialLogs = [
  {
    id: 1,
    user: '张三',
    type: '登录日志',
    module: '系统登录',
    content: '用户登录系统',
    ip: '192.168.1.100',
    status: '成功',
    time: '2026-05-23 09:15:30',
    detail: '用户通过账号密码方式登录系统，浏览器Chrome 125，操作系统Windows 11'
  },
  {
    id: 2,
    user: '李四',
    type: '操作日志',
    module: '用户管理',
    content: '新增用户"王五"',
    ip: '192.168.1.101',
    status: '成功',
    time: '2026-05-23 10:22:45',
    detail: '管理员新增用户账号，用户名：wangwu，角色：普通用户，所属部门：技术部'
  },
  {
    id: 3,
    user: '王五',
    type: '登录日志',
    module: '系统登录',
    content: '用户登录系统',
    ip: '192.168.1.102',
    status: '失败',
    time: '2026-05-23 11:05:18',
    detail: '登录失败，密码错误，连续失败2次，浏览器Firefox 126'
  },
  {
    id: 4,
    user: '赵六',
    type: '异常日志',
    module: '数据导出',
    content: '导出报表数据异常',
    ip: '192.168.1.103',
    status: '失败',
    time: '2026-05-23 14:30:55',
    detail: '导出报表时发生NullPointerException，数据量超过最大限制10000条，请联系管理员处理'
  },
  {
    id: 5,
    user: '张三',
    type: '操作日志',
    module: '角色管理',
    content: '修改角色"管理员"权限',
    ip: '192.168.1.100',
    status: '成功',
    time: '2026-05-23 16:45:10',
    detail: '修改管理员角色权限，新增：日志查看、数据导出；移除：系统配置修改'
  }
]

const logs = ref([...initialLogs])
const searchText = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const showModal = ref(false)
const detailLog = ref(null)

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const matchSearch = !searchText.value ||
      log.user.includes(searchText.value) ||
      log.module.includes(searchText.value) ||
      log.ip.includes(searchText.value)
    const matchType = !typeFilter.value || log.type === typeFilter.value
    const matchStatus = !statusFilter.value || log.status === statusFilter.value
    return matchSearch && matchType && matchStatus
  })
})

function handleQuery() {}

function handleRefresh() {
  searchText.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
}

function handleDetail(log) {
  detailLog.value = log
  showModal.value = true
}

function handleDelete(log) {
  if (confirm(`确认删除该条日志？操作人：${log.user}，内容：${log.content}`)) {
    logs.value = logs.value.filter(l => l.id !== log.id)
  }
}

function handleClearAll() {
  if (confirm('确认清空所有日志？此操作不可恢复！')) {
    logs.value = []
  }
}

function handleExport() {
  const header = '操作人|日志类型|操作模块|操作内容|IP地址|状态|操作时间'
  const rows = filteredLogs.value.map(l =>
    `${l.user}|${l.type}|${l.module}|${l.content}|${l.ip}|${l.status}|${l.time}`
  )
  const content = [header, ...rows].join('\n')
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `系统日志_${new Date().toISOString().slice(0, 10)}.txt`
  a.click()
  URL.revokeObjectURL(url)
}
</script>
