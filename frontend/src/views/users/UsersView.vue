<template>
  <div class="admin-page">
    <div class="admin-stats">
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon blue"><UsersRound :size="22" /></div>
        <div class="admin-stat-info">
          <span>用户总数</span>
          <strong>{{ users.length }}</strong>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon green"><CheckCircle :size="22" /></div>
        <div class="admin-stat-info">
          <span>正常用户</span>
          <strong>{{ users.filter(u => u.status === '正常').length }}</strong>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon orange"><PauseCircle :size="22" /></div>
        <div class="admin-stat-info">
          <span>禁用用户</span>
          <strong>{{ users.filter(u => u.status === '禁用').length }}</strong>
        </div>
      </div>
      <div class="admin-card admin-stat-card">
        <div class="admin-stat-icon purple"><Wifi :size="22" /></div>
        <div class="admin-stat-info">
          <span>在线用户</span>
          <strong>{{ users.filter(u => u.online).length }}</strong>
        </div>
      </div>
    </div>

    <div class="admin-card admin-toolbar">
      <div class="admin-search">
        <Search :size="16" />
        <input v-model="searchKeyword" placeholder="姓名/账号/手机号" @keyup.enter="onSearch" />
      </div>
      <select v-model="filterDept" class="admin-select">
        <option value="">全部部门</option>
        <option v-for="d in deptOptions" :key="d" :value="d">{{ d }}</option>
      </select>
      <select v-model="filterStatus" class="admin-select">
        <option value="">全部状态</option>
        <option value="正常">正常</option>
        <option value="禁用">禁用</option>
      </select>
      <button class="admin-btn admin-btn-primary" @click="onSearch">
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
          <span>共 {{ filteredUsers.length }} 条</span>
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
            <tr v-for="user in filteredUsers" :key="user.id">
              <td><input type="checkbox" class="admin-check" :checked="selectedIds.includes(user.id)" @change="toggleSelect(user.id)" /></td>
              <td>
                <div class="admin-user-main">
                  <div class="admin-avatar">{{ user.name.charAt(0) }}</div>
                  <div class="admin-main-text">
                    <strong>{{ user.name }}</strong>
                    <span>{{ user.online ? '🟢 在线' : '⚫ 离线' }}</span>
                  </div>
                </div>
              </td>
              <td>{{ user.account }}</td>
              <td>{{ user.dept }}</td>
              <td><span class="admin-badge blue">{{ user.role }}</span></td>
              <td>{{ user.phone }}</td>
              <td>
                <span class="admin-badge" :class="user.status === '正常' ? 'green' : 'gray'">{{ user.status }}</span>
              </td>
              <td>{{ user.lastLogin }}</td>
              <td>
                <div class="admin-row-actions">
                  <button class="admin-row-btn" @click="openEditModal(user)"><Pencil :size="14" /> 编辑</button>
                  <button class="admin-row-btn" @click="openRoleModal(user)"><ShieldCheck :size="14" /> 角色</button>
                  <button class="admin-row-btn warning" @click="resetPassword(user)"><KeyRound :size="14" /> 重置</button>
                  <button class="admin-row-btn warning" @click="toggleStatus(user)">
                    <component :is="user.status === '正常' ? PauseCircle : PlayCircle" :size="14" />
                    {{ user.status === '正常' ? '禁用' : '启用' }}
                  </button>
                  <button class="admin-row-btn danger" @click="deleteUser(user)"><Trash2 :size="14" /> 删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredUsers.length === 0">
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
              <input v-model="formData.name" class="admin-input" placeholder="用户姓名" />
            </div>
            <div class="admin-form-item">
              <label>登录账号</label>
              <input v-model="formData.account" class="admin-input" placeholder="登录账号" />
            </div>
            <div class="admin-form-item">
              <label>手机号</label>
              <input v-model="formData.phone" class="admin-input" placeholder="手机号" />
            </div>
            <div class="admin-form-item">
              <label>部门</label>
              <select v-model="formData.dept" class="admin-select">
                <option v-for="d in deptOptions" :key="d" :value="d">{{ d }}</option>
              </select>
            </div>
            <div class="admin-form-item">
              <label>角色</label>
              <select v-model="formData.role" class="admin-select">
                <option v-for="r in roleOptions" :key="r" :value="r">{{ r }}</option>
              </select>
            </div>
            <div class="admin-form-item">
              <label>状态</label>
              <select v-model="formData.status" class="admin-select">
                <option value="正常">正常</option>
                <option value="禁用">禁用</option>
              </select>
            </div>
            <div class="admin-form-item full">
              <label>备注</label>
              <textarea v-model="formData.remark" class="admin-textarea" placeholder="备注"></textarea>
            </div>
          </div>
        </div>
        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" @click="showFormModal = false"><X :size="16" /> 取消</button>
          <button class="admin-btn admin-btn-primary" @click="saveUser"><Save :size="16" /> 保存</button>
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
            <select v-model="roleFormData.role" class="admin-select">
              <option v-for="r in roleOptions" :key="r" :value="r">{{ r }}</option>
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
import { ref, reactive, computed } from 'vue'
import '@/styles/users.css'
import {
  UsersRound, CheckCircle, PauseCircle, Wifi, Search, Trash2, UserPlus,
  RefreshCw, Pencil, ShieldCheck, KeyRound, PlayCircle, X, Check, Save
} from 'lucide-vue-next'

const deptOptions = ['设计部', '摄影部', '运营部', '数据部', '技术部']
const roleOptions = ['系统管理员', '设计主管', '摄影师', '数据审核员', '普通用户']

const users = ref([
  { id: 1, name: 'Alex', account: 'alex01', phone: '13800001001', dept: '设计部', role: '设计主管', status: '正常', online: true, lastLogin: '2026-05-23 09:15', remark: '' },
  { id: 2, name: 'Bella', account: 'bella02', phone: '13800001002', dept: '摄影部', role: '摄影师', status: '正常', online: true, lastLogin: '2026-05-23 08:42', remark: '' },
  { id: 3, name: 'Chris', account: 'chris03', phone: '13800001003', dept: '运营部', role: '普通用户', status: '禁用', online: false, lastLogin: '2026-05-20 17:30', remark: '账号异常' },
  { id: 4, name: 'Diana', account: 'diana04', phone: '13800001004', dept: '数据部', role: '数据审核员', status: '正常', online: false, lastLogin: '2026-05-22 14:10', remark: '' },
  { id: 5, name: 'Eric', account: 'eric05', phone: '13800001005', dept: '技术部', role: '系统管理员', status: '正常', online: true, lastLogin: '2026-05-23 10:05', remark: '' }
])

let nextId = 6

const searchKeyword = ref('')
const filterDept = ref('')
const filterStatus = ref('')
const selectedIds = ref([])

const filteredUsers = computed(() => {
  return users.value.filter(u => {
    const kw = searchKeyword.value.toLowerCase()
    const matchKw = !kw || u.name.toLowerCase().includes(kw) || u.account.toLowerCase().includes(kw) || u.phone.includes(kw)
    const matchDept = !filterDept.value || u.dept === filterDept.value
    const matchStatus = !filterStatus.value || u.status === filterStatus.value
    return matchKw && matchDept && matchStatus
  })
})

const isAllSelected = computed(() => {
  return filteredUsers.value.length > 0 && filteredUsers.value.every(u => selectedIds.value.includes(u.id))
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = filteredUsers.value.map(u => u.id)
  }
}

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const onSearch = () => {}

const showFormModal = ref(false)
const formMode = ref('add')
const formData = reactive({
  id: null,
  name: '',
  account: '',
  phone: '',
  dept: '设计部',
  role: '普通用户',
  status: '正常',
  remark: ''
})

const openAddModal = () => {
  formMode.value = 'add'
  formData.id = null
  formData.name = ''
  formData.account = ''
  formData.phone = ''
  formData.dept = '设计部'
  formData.role = '普通用户'
  formData.status = '正常'
  formData.remark = ''
  showFormModal.value = true
}

const openEditModal = (user) => {
  formMode.value = 'edit'
  formData.id = user.id
  formData.name = user.name
  formData.account = user.account
  formData.phone = user.phone
  formData.dept = user.dept
  formData.role = user.role
  formData.status = user.status
  formData.remark = user.remark
  showFormModal.value = true
}

const saveUser = () => {
  if (!formData.name.trim()) { alert('请输入用户姓名'); return }
  if (!formData.account.trim()) { alert('请输入登录账号'); return }
  if (!formData.phone.trim()) { alert('请输入手机号'); return }

  const accountExists = users.value.some(u => u.account === formData.account.trim() && u.id !== formData.id)
  if (accountExists) { alert('登录账号已存在'); return }

  if (formMode.value === 'add') {
    users.value.push({
      id: nextId++,
      name: formData.name.trim(),
      account: formData.account.trim(),
      phone: formData.phone.trim(),
      dept: formData.dept,
      role: formData.role,
      status: formData.status,
      online: false,
      lastLogin: '-',
      remark: formData.remark
    })
  } else {
    const user = users.value.find(u => u.id === formData.id)
    if (user) {
      user.name = formData.name.trim()
      user.account = formData.account.trim()
      user.phone = formData.phone.trim()
      user.dept = formData.dept
      user.role = formData.role
      user.status = formData.status
      user.remark = formData.remark
    }
  }
  showFormModal.value = false
}

const showRoleModal = ref(false)
const roleFormData = reactive({ id: null, role: '普通用户' })

const openRoleModal = (user) => {
  roleFormData.id = user.id
  roleFormData.role = user.role
  showRoleModal.value = true
}

const saveRole = () => {
  const user = users.value.find(u => u.id === roleFormData.id)
  if (user) {
    user.role = roleFormData.role
  }
  showRoleModal.value = false
}

const resetPassword = (user) => {
  if (!confirm(`确认重置用户 ${user.name} 的密码？`)) return
  alert(`用户 ${user.name} 的密码已重置`)
}

const toggleStatus = (user) => {
  const action = user.status === '正常' ? '禁用' : '启用'
  if (!confirm(`确认${action}用户 ${user.name}？`)) return
  user.status = user.status === '正常' ? '禁用' : '正常'
  if (user.status === '禁用') {
    user.online = false
  }
}

const deleteUser = (user) => {
  if (!confirm(`确认删除用户 ${user.name}？`)) return
  const idx = users.value.findIndex(u => u.id === user.id)
  if (idx >= 0) users.value.splice(idx, 1)
  selectedIds.value = selectedIds.value.filter(id => id !== user.id)
}

const onBatchDelete = () => {
  if (selectedIds.value.length === 0) return
  if (!confirm(`确认删除选中的 ${selectedIds.value.length} 个用户？`)) return
  users.value = users.value.filter(u => !selectedIds.value.includes(u.id))
  selectedIds.value = []
}
</script>
