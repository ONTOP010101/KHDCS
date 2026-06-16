<template>
  <div class="admin-page">
    <div class="admin-stats">
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon blue"><UsersRound :size="22" /></div>
        <div class="admin-stat-info">
          <span>用户总数</span>
          <strong>{{ totalRecords }}</strong>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon green"><CheckCircle :size="22" /></div>
        <div class="admin-stat-info">
          <span>正常用户</span>
          <strong>{{ enabledCount }}</strong>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon orange"><PauseCircle :size="22" /></div>
        <div class="admin-stat-info">
          <span>禁用用户</span>
          <strong>{{ disabledCount }}</strong>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon purple"><Wifi :size="22" /></div>
        <div class="admin-stat-info">
          <span>在线用户</span>
          <strong>-</strong>
        </div>
      </div>
    </div>

    <div class="admin-card admin-toolbar">
      <div class="admin-search">
        <Search :size="16" />
        <input v-model="searchKeyword" placeholder="姓名/账号/手机号" @keyup.enter="handleQuery" />
      </div>
      <select v-model="filterDept" class="admin-select">
        <option value="">全部部门</option>
        <option v-for="d in deptOptions" :key="d" :value="d">{{ d }}</option>
      </select>
      <select v-model="filterStatus" class="admin-select">
        <option value="">全部状态</option>
        <option value="enabled">正常</option>
        <option value="disabled">禁用</option>
      </select>
      <button class="admin-btn admin-btn-primary" @click="handleQuery">
        <Search :size="16" /> 查询
      </button>
      <span></span>
      <button class="admin-btn admin-btn-danger" :disabled="selectedIds.length === 0" @click="onBatchDelete">
        <Trash2 :size="16" /> 批量删除
      </button>
      <button class="admin-btn admin-btn-primary" @click="openAddModal">
        <UserPlus :size="16" /> 添加用户
      </button>
    </div>

    <div class="admin-card admin-table-card">
      <div class="admin-table-head">
        <div class="admin-table-title">
          <strong>用户列表</strong>
          <span>共 {{ totalRecords }} 条</span>
        </div>
      </div>
      <div class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th><input type="checkbox" class="admin-check" :checked="isAllSelected" @change="toggleSelectAll" /></th>
              <th>用户信息</th>
              <th>登录账号</th>
              <th>部门</th>
              <th>角色</th>
              <th>手机号</th>
              <th>状态</th>
              <th>最近登录</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td><input type="checkbox" class="admin-check" :checked="selectedIds.includes(user.id)" @change="toggleSelect(user.id)" /></td>
              <td>
                <div class="admin-user-main">
                  <div class="admin-avatar">{{ (user.realName || '?').charAt(0) }}</div>
                  <div class="admin-main-text">
                    <strong>{{ user.realName }}</strong>
                  </div>
                </div>
              </td>
              <td>{{ user.username }}</td>
              <td>{{ user.department }}</td>
              <td><span class="admin-badge blue">{{ user.role || '未分配' }}</span></td>
              <td>{{ user.phone }}</td>
              <td>
                <span class="admin-badge" :class="user.status === 1 ? 'green' : 'gray'">{{ user.status === 1 ? '正常' : '禁用' }}</span>
              </td>
              <td>{{ formatTime(user.lastLoginTime) }}</td>
              <td>
                <div class="admin-row-actions">
                  <button class="admin-row-btn" @click="openEditModal(user)"><Pencil :size="14" /> 编辑</button>
                  <button class="admin-row-btn" @click="openRoleModal(user)"><ShieldCheck :size="14" /> 角色</button>
                  <button class="admin-row-btn warning" @click="resetPassword(user)"><KeyRound :size="14" /> 重置</button>
                  <button class="admin-row-btn warning" @click="toggleStatus(user)">
                    <component :is="user.status === 1 ? PauseCircle : PlayCircle" :size="14" />
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button class="admin-row-btn danger" @click="deleteUser(user)"><Trash2 :size="14" /> 删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="users.length === 0 && !tableLoading">
              <td colspan="9">
                <div class="admin-empty">暂无数据</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="admin-modal-mask" :class="{ show: showFormModal }" @click.self="showFormModal = false">
      <div class="admin-modal">
        <div class="admin-modal-head">
          <strong>{{ formMode === 'add' ? '添加用户' : '编辑用户' }}</strong>
          <button class="admin-btn admin-btn-ghost" @click="showFormModal = false"><X :size="16" /></button>
        </div>
        <div class="admin-modal-body">
          <div class="admin-form-grid">
            <div class="admin-form-item">
              <label>用户姓名</label>
              <input v-model="formData.realName" class="admin-input" placeholder="用户姓名" />
            </div>
            <div class="admin-form-item">
              <label>登录账号</label>
              <input v-model="formData.username" class="admin-input" placeholder="登录账号" :disabled="formMode === 'edit'" />
            </div>
            <div class="admin-form-item" v-if="formMode === 'add'">
              <label>初始密码</label>
              <input v-model="formData.password" type="password" class="admin-input" placeholder="初始密码" />
            </div>
            <div class="admin-form-item">
              <label>手机号</label>
              <input v-model="formData.phone" class="admin-input" placeholder="手机号" />
            </div>
            <div class="admin-form-item">
              <label>邮箱</label>
              <input v-model="formData.email" class="admin-input" placeholder="邮箱" />
            </div>
            <div class="admin-form-item">
              <label>部门</label>
              <select v-model="formData.department" class="admin-select">
                <option v-for="d in deptOptions" :key="d" :value="d">{{ d }}</option>
              </select>
            </div>
            <div class="admin-form-item">
              <label>角色</label>
              <select v-model="formData.roleId" class="admin-select">
                <option :value="null">未分配</option>
                <option v-for="r in roleOptions" :key="r.id" :value="r.id">{{ r.name }}</option>
              </select>
            </div>
            <div class="admin-form-item">
              <label>状态</label>
              <select v-model="formData.status" class="admin-select">
                <option :value="1">正常</option>
                <option :value="0">禁用</option>
              </select>
            </div>
          </div>
        </div>
        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" @click="showFormModal = false"><X :size="16" /> 取消</button>
          <button class="admin-btn admin-btn-primary" @click="saveUser" :disabled="formLoading"><Save :size="16" /> 保存</button>
        </div>
      </div>
    </div>

    <div class="admin-modal-mask" :class="{ show: showRoleModal }" @click.self="showRoleModal = false">
      <div class="admin-modal">
        <div class="admin-modal-head">
          <strong>分配角色</strong>
          <button class="admin-btn admin-btn-ghost" @click="showRoleModal = false"><X :size="16" /></button>
        </div>
        <div class="admin-modal-body">
          <div class="admin-form-item">
            <label>角色</label>
            <select v-model="roleFormData.roleId" class="admin-select">
              <option :value="null">未分配</option>
              <option v-for="r in roleOptions" :key="r.id" :value="r.id">{{ r.name }}</option>
            </select>
          </div>
        </div>
        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" @click="showRoleModal = false"><X :size="16" /> 取消</button>
          <button class="admin-btn admin-btn-primary" @click="saveRole"><Check :size="16" /> 保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import '@/styles/users.css'
import {
  UsersRound, CheckCircle, PauseCircle, Wifi, Search, Trash2, UserPlus,
  RefreshCw, Pencil, ShieldCheck, KeyRound, PlayCircle, X, Check, Save
} from 'lucide-vue-next'
import { api } from '@/api'
import { appAlert, appConfirm, appPrompt, appToast } from '@/utils/dialog'

// ===== 数据状态 =====
const users = ref([])
const totalRecords = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const tableLoading = ref(false)

const searchKeyword = ref('')
const filterDept = ref('')
const filterStatus = ref('')
const selectedIds = ref([])

// ===== 选项数据 =====
const deptOptions = ['设计部', '摄影部', '运营部', '数据部', '技术部']
const roleOptions = ref([]) // 从后端加载

// ===== 弹窗状态 =====
const showFormModal = ref(false)
const formMode = ref('add')
const formLoading = ref(false)
const formData = reactive({
  id: null,
  realName: '',
  username: '',
  password: '',
  phone: '',
  email: '',
  department: '设计部',
  roleId: null,
  status: 1
})

const showRoleModal = ref(false)
const roleFormData = reactive({ id: null, roleId: null })

// ===== 计算属性 =====
const enabledCount = computed(() => users.value.filter(u => u.status === 1).length)
const disabledCount = computed(() => users.value.filter(u => u.status === 0).length)

const isAllSelected = computed(() => {
  return users.value.length > 0 && users.value.every(u => selectedIds.value.includes(u.id))
})

// ===== 工具方法 =====
function formatTime(val) {
  if (!val) return '-'
  if (typeof val === 'string') return val.replace('T', ' ').slice(0, 16)
  return val
}

// ===== 数据加载 =====
const loadUsers = async () => {
  tableLoading.value = true
  try {
    const params = new URLSearchParams({
      current: currentPage.value,
      size: pageSize.value
    })
    if (searchKeyword.value) params.set('keyword', searchKeyword.value)
    if (filterDept.value) params.set('department', filterDept.value)
    if (filterStatus.value) params.set('status', filterStatus.value)
    const res = await api(`/users?${params.toString()}`)
    const data = res.data || res
    users.value = data.records || []
    totalRecords.value = data.total || 0
  } catch (e) {
    console.error('加载用户列表失败:', e)
    users.value = []
  } finally {
    tableLoading.value = false
  }
}

const loadRoles = async () => {
  if (roleOptions.value.length > 0) return // 缓存
  try {
    const res = await api('/roles?current=1&size=100')
    const data = res.data || res
    roleOptions.value = data.records || []
  } catch (e) {
    console.error('加载角色列表失败:', e)
  }
}

// ===== 操作方法 =====
function handleQuery() {
  currentPage.value = 1
  loadUsers()
}

function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = users.value.map(u => u.id)
  }
}

function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

function openAddModal() {
  formMode.value = 'add'
  formData.id = null
  formData.realName = ''
  formData.username = ''
  formData.password = ''
  formData.phone = ''
  formData.email = ''
  formData.department = '设计部'
  formData.roleId = null
  formData.status = 1
  showFormModal.value = true
}

function openEditModal(user) {
  formMode.value = 'edit'
  formData.id = user.id
  formData.realName = user.realName || ''
  formData.username = user.username || ''
  formData.password = ''
  formData.phone = user.phone || ''
  formData.email = user.email || ''
  formData.department = user.department || ''
  formData.roleId = user.roleId || null
  formData.status = user.status
  showFormModal.value = true
}

async function saveUser() {
  const realName = formData.realName.trim()
  const username = formData.username.trim()
  if (!realName) { appAlert('请输入用户姓名', '表单验证', 'warning'); return }
  if (!username) { appAlert('请输入登录账号', '表单验证', 'warning'); return }

  formLoading.value = true
  try {
    if (formMode.value === 'add') {
      if (!formData.password) { appAlert('请输入初始密码', '表单验证', 'warning'); formLoading.value = false; return }
      await api('/users', {
        method: 'POST',
        body: JSON.stringify({
          username,
          password: formData.password,
          realName,
          phone: formData.phone.trim(),
          email: formData.email.trim(),
          department: formData.department,
          status: formData.status,
          roleId: formData.roleId
        })
      })
      appToast('用户已添加')
    } else {
      await api(`/users/${formData.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          realName,
          phone: formData.phone.trim(),
          email: formData.email.trim(),
          department: formData.department,
          status: formData.status
        })
      })
      appToast('用户已更新')
    }
    showFormModal.value = false
    loadUsers()
  } catch (e) {
    appAlert(e.message || '操作失败', '操作失败', 'danger')
  } finally {
    formLoading.value = false
  }
}

async function toggleStatus(user) {
  const nextStatus = user.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'
  const ok = await appConfirm(`确认${actionText}用户「${user.realName}」？`, `${actionText}用户`, 'warning')
  if (!ok) return
  try {
    await api(`/users/${user.id}/status?status=${nextStatus}`, { method: 'PUT' })
    user.status = nextStatus
  } catch (e) {
    appAlert(e.message || '操作失败', '操作失败', 'danger')
  }
}

async function deleteUser(user) {
  const ok = await appConfirm(`确认删除用户「${user.realName}」？<br/><small style="color:rgba(29,29,31,0.45)">删除后不可恢复</small>`, '删除用户', 'danger')
  if (!ok) return
  try {
    await api(`/users/${user.id}`, { method: 'DELETE' })
    loadUsers()
  } catch (e) {
    appAlert(e.message || '删除失败', '删除失败', 'danger')
  }
}

async function onBatchDelete() {
  if (selectedIds.value.length === 0) return
  const ok = await appConfirm(`确认删除选中的 ${selectedIds.value.length} 个用户？<br/><small style="color:rgba(29,29,31,0.45)">删除后不可恢复</small>`, '批量删除', 'danger')
  if (!ok) return
  try {
    await api('/users/batch', {
      method: 'DELETE',
      body: JSON.stringify(selectedIds.value)
    })
    selectedIds.value = []
    loadUsers()
  } catch (e) {
    appAlert(e.message || '批量删除失败', '批量删除失败', 'danger')
  }
}

async function resetPassword(user) {
  const newPassword = await appPrompt(`请输入用户「${user.realName}」的新密码：`, '', '重置密码', 'warning')
  if (!newPassword) return
  try {
    await api(`/users/${user.id}/password`, {
      method: 'PUT',
      body: JSON.stringify({ password: newPassword })
    })
    appToast('密码已重置')
  } catch (e) {
    appAlert(e.message || '重置失败', '重置失败', 'danger')
  }
}

function openRoleModal(user) {
  roleFormData.id = user.id
  roleFormData.roleId = user.roleId || null
  showRoleModal.value = true
}

async function saveRole() {
  try {
    await api(`/users/${roleFormData.id}/roles`, {
      method: 'PUT',
      body: JSON.stringify({ roleId: roleFormData.roleId })
    })
    showRoleModal.value = false
    loadUsers()
    appToast('角色已分配')
  } catch (e) {
    appAlert(e.message || '分配角色失败', '分配角色失败', 'danger')
  }
}

// ===== 生命周期 =====
onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>
