<template>
  <div class="roles-page">
    <div class="roles-stats">
      <div class="roles-card role-stat-card">
        <div class="role-stat-icon blue">
          <ShieldCheck :size="22" />
        </div>
        <div class="role-stat-info">
          <span>角色总数</span>
          <strong>{{ roleList.length }}</strong>
        </div>
      </div>

      <div class="roles-card role-stat-card">
        <div class="role-stat-icon green">
          <CheckCircle :size="22" />
        </div>
        <div class="role-stat-info">
          <span>启用角色</span>
          <strong>{{ enabledCount }}</strong>
        </div>
      </div>

      <div class="roles-card role-stat-card">
        <div class="role-stat-icon orange">
          <PauseCircle :size="22" />
        </div>
        <div class="role-stat-info">
          <span>停用角色</span>
          <strong>{{ disabledCount }}</strong>
        </div>
      </div>

      <div class="roles-card role-stat-card">
        <div class="role-stat-icon purple">
          <UsersRound :size="22" />
        </div>
        <div class="role-stat-info">
          <span>关联用户</span>
          <strong>{{ totalUsers }}</strong>
        </div>
      </div>
    </div>

    <div class="roles-card roles-toolbar">
      <div class="role-search">
        <Search :size="16" />
        <input
          v-model="searchKeyword"
          placeholder="搜索角色名称、角色编码..."
        />
      </div>

      <select class="role-select" v-model="statusFilter">
        <option value="all">全部状态</option>
        <option value="enabled">启用</option>
        <option value="disabled">停用</option>
      </select>

      <button class="role-btn role-btn-ghost" @click="handleQuery">
        <Search :size="16" />
        查询
      </button>

      <div></div>

      <button class="role-btn role-btn-danger" @click="handleBatchDelete">
        <Trash2 :size="16" />
        批量删除
      </button>

      <button class="role-btn role-btn-primary" @click="openAddForm">
        <Plus :size="16" />
        新增角色
      </button>
    </div>

    <div class="roles-card roles-table-card">
      <div class="role-table-head">
        <div class="role-table-title">
          <strong>角色列表</strong>
          <span>共 {{ filteredRoles.length }} 条</span>
        </div>

        <button class="role-btn role-btn-ghost" @click="handleRefresh">
          <RefreshCw :size="16" />
          刷新
        </button>
      </div>

      <div class="roles-table-wrap">
        <table class="roles-table">
          <thead>
            <tr>
              <th style="width:44px;">
                <input
                  type="checkbox"
                  class="role-check"
                  :checked="isAllChecked"
                  :indeterminate="isIndeterminate"
                  @change="toggleCheckAll"
                />
              </th>
              <th>角色名称</th>
              <th>角色编码</th>
              <th>数据范围</th>
              <th>关联用户</th>
              <th>状态</th>
              <th>创建时间</th>
              <th style="width:330px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!filteredRoles.length">
              <td colspan="8">
                <div class="role-empty">暂无匹配角色</div>
              </td>
            </tr>
            <tr v-for="role in filteredRoles" :key="role.id">
              <td>
                <input
                  type="checkbox"
                  class="role-check"
                  :checked="selectedIds.has(role.id)"
                  @change="toggleCheck(role.id)"
                />
              </td>
              <td>
                <div class="role-name-main">
                  <div class="role-mini-icon">
                    <ShieldCheck :size="17" />
                  </div>
                  <div class="role-name-text">
                    <strong>{{ role.name }}</strong>
                    <span>{{ role.description || '暂无描述' }}</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="role-code">{{ role.code }}</span>
              </td>
              <td>{{ role.scope }}</td>
              <td>{{ role.users }} 人</td>
              <td>
                <span :class="['role-badge', role.status === 1 ? 'enabled' : 'disabled']">
                  {{ role.status === 1 ? '启用' : '停用' }}
                </span>
              </td>
              <td>{{ role.createTime }}</td>
              <td>
                <div class="role-row-actions">
                  <button class="role-row-btn" @click="openPermissionModal(role.id)">
                    <KeyRound :size="14" />
                    权限
                  </button>
                  <button class="role-row-btn" @click="openMembersModal(role.id)">
                    <UsersRound :size="14" />
                    成员
                  </button>
                  <button class="role-row-btn" @click="openEditForm(role.id)">
                    <Pencil :size="14" />
                    编辑
                  </button>
                  <button class="role-row-btn warning" @click="toggleRoleStatus(role.id)">
                    <component :is="role.status === 1 ? PauseCircle : PlayCircle" :size="14" />
                    {{ role.status === 1 ? '停用' : '启用' }}
                  </button>
                  <button class="role-row-btn danger" @click="deleteRole(role.id)">
                    <Trash2 :size="14" />
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div :class="['role-modal-mask', { show: formModalVisible }]">
      <div class="role-modal">
        <div class="role-modal-head">
          <strong>{{ editingId ? '编辑角色' : '新增角色' }}</strong>
          <button class="role-row-btn" @click="formModalVisible = false">
            <X :size="14" />
          </button>
        </div>

        <div class="role-modal-body">
          <div class="role-form-grid">
            <div class="role-form-item">
              <label>角色名称</label>
              <input class="role-input" v-model="formData.name" placeholder="例如：系统管理员" />
            </div>
            <div class="role-form-item">
              <label>角色编码</label>
              <input class="role-input" v-model="formData.code" placeholder="例如：admin" />
            </div>
            <div class="role-form-item">
              <label>状态</label>
              <select class="role-select" v-model="formData.status">
                <option :value="1">启用</option>
                <option :value="0">停用</option>
              </select>
            </div>
            <div class="role-form-item">
              <label>角色描述</label>
              <textarea class="role-textarea" v-model="formData.description" placeholder="请输入角色说明..."></textarea>
            </div>
          </div>
        </div>

        <div class="role-modal-foot">
          <button class="role-btn role-btn-ghost" @click="formModalVisible = false">取消</button>
          <button class="role-btn role-btn-primary" @click="submitForm">
            <Check :size="16" />
            保存
          </button>
        </div>
      </div>
    </div>

    <div :class="['role-modal-mask', { show: permissionModalVisible }]">
      <div class="role-modal large">
        <div class="role-modal-head">
          <strong>{{ permissionRoleName }} - 权限配置</strong>
          <button class="role-row-btn" @click="permissionModalVisible = false">
            <X :size="14" />
          </button>
        </div>

        <div class="role-modal-body">
          <div class="role-permission-layout">
            <div class="role-permission-left">
              <button
                :class="['role-permission-tab', { active: permissionTab === 'menu' }]"
                @click="permissionTab = 'menu'"
              >
                <LayoutDashboard :size="16" />
                菜单权限
              </button>
              <button
                :class="['role-permission-tab', { active: permissionTab === 'button' }]"
                @click="permissionTab = 'button'"
              >
                <MousePointerClick :size="16" />
                按钮权限
              </button>
              <button
                :class="['role-permission-tab', { active: permissionTab === 'data' }]"
                @click="permissionTab = 'data'"
              >
                <Database :size="16" />
                数据权限
              </button>
            </div>

            <div class="role-permission-right">
              <template v-if="permissionTab === 'menu'">
                <div class="role-permission-title">菜单权限</div>
                <div class="role-permission-group">
                  <div class="role-permission-group-title">请选择权限</div>
                  <div class="role-permission-checks">
                    <label class="role-check-item" v-for="item in menuPermissions" :key="item.id">
                      <input
                        type="checkbox"
                        class="role-permission-check"
                        :value="item.id"
                        v-model="permissionChecks"
                      />
                      <span>{{ item.name }}</span>
                    </label>
                  </div>
                </div>
              </template>

              <template v-if="permissionTab === 'button'">
                <div class="role-permission-title">按钮权限</div>
                <div class="role-permission-group">
                  <div class="role-permission-group-title">请选择权限</div>
                  <div class="role-permission-checks">
                    <label class="role-check-item" v-for="item in buttonPermissions" :key="item.id">
                      <input
                        type="checkbox"
                        class="role-permission-check"
                        :value="item.id"
                        v-model="permissionChecks"
                      />
                      <span>{{ item.name }}</span>
                    </label>
                  </div>
                </div>
              </template>

              <template v-if="permissionTab === 'data'">
                <div class="role-permission-title">数据权限</div>
                <div class="role-permission-group">
                  <div class="role-permission-group-title">请选择权限</div>
                  <div class="role-permission-checks">
                    <label class="role-check-item" v-for="item in dataPermissions" :key="item.id">
                      <input
                        type="checkbox"
                        class="role-permission-check"
                        :value="item.id"
                        v-model="permissionChecks"
                      />
                      <span>{{ item.name }}</span>
                    </label>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <div class="role-modal-foot">
          <button class="role-btn role-btn-ghost" @click="permissionModalVisible = false">取消</button>
          <button class="role-btn role-btn-primary" @click="savePermissions">
            <Save :size="16" />
            保存权限
          </button>
        </div>
      </div>
    </div>

    <div :class="['role-modal-mask', { show: membersModalVisible }]">
      <div class="role-modal">
        <div class="role-modal-head">
          <strong>{{ membersRoleName }} - 角色成员</strong>
          <button class="role-row-btn" @click="membersModalVisible = false">
            <X :size="14" />
          </button>
        </div>

        <div class="role-modal-body">
          <div class="role-member-list">
            <div class="role-member-item" v-for="member in membersList" :key="member.account">
              <div class="role-member-avatar">{{ member.name.slice(0, 1) }}</div>
              <div class="role-member-info">
                <strong>{{ member.name }}</strong>
                <span>{{ member.account }} · {{ member.dept }}</span>
              </div>
              <span class="role-badge enabled">正常</span>
            </div>
          </div>
        </div>

        <div class="role-modal-foot">
          <button class="role-btn role-btn-ghost" @click="membersModalVisible = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  ShieldCheck,
  CheckCircle,
  PauseCircle,
  UsersRound,
  Search,
  Trash2,
  Plus,
  RefreshCw,
  KeyRound,
  Pencil,
  PlayCircle,
  X,
  Check,
  Save,
  LayoutDashboard,
  MousePointerClick,
  Database
} from 'lucide-vue-next'
import { api } from '@/api'
import { appAlert, appConfirm, appToast } from '@/utils/dialog'
import '@/styles/roles.css'

// ===== 数据状态 =====
const roleList = ref([])
const totalRecords = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const tableLoading = ref(false)

const searchKeyword = ref('')
const statusFilter = ref('all')
const selectedIds = reactive(new Set())

// ===== 弹窗状态 =====
const formModalVisible = ref(false)
const permissionModalVisible = ref(false)
const membersModalVisible = ref(false)
const formLoading = ref(false)

const editingId = ref(null)
const formData = reactive({
  name: '',
  code: '',
  status: 1,
  description: ''
})

// ===== 权限相关 =====
const permissionRoleId = ref(null)
const permissionTab = ref('menu')
const permissionChecks = reactive([])
const allPermissions = ref([])
const permissionsLoading = ref(false)

// ===== 成员相关 =====
const membersRoleName = ref('')
const membersList = ref([])

// ===== 计算属性 =====
const enabledCount = computed(() => roleList.value.filter(r => r.status === 1).length)
const disabledCount = computed(() => roleList.value.filter(r => r.status === 0).length)
const totalUsers = computed(() => roleList.value.reduce((sum, r) => sum + Number(r.userCount || 0), 0))

const filteredRoles = computed(() => {
  let data = roleList.value
  if (statusFilter.value !== 'all') {
    const statusVal = statusFilter.value === 'enabled' ? 1 : 0
    data = data.filter(r => r.status === statusVal)
  }
  return data
})

const isAllChecked = computed(() => {
  const data = filteredRoles.value
  return data.length > 0 && data.every(role => selectedIds.has(role.id))
})

const isIndeterminate = computed(() => {
  const data = filteredRoles.value
  const checkedCount = data.filter(role => selectedIds.has(role.id)).length
  return checkedCount > 0 && checkedCount < data.length
})

const permissionRoleName = computed(() => {
  const role = roleList.value.find(r => r.id === permissionRoleId.value)
  return role ? role.name : ''
})

// 按类型分组的权限列表
const menuPermissions = computed(() => allPermissions.value.filter(p => p.type === 1))
const buttonPermissions = computed(() => allPermissions.value.filter(p => p.type === 2))
const dataPermissions = computed(() => allPermissions.value.filter(p => p.type === 3))

// ===== 数据加载 =====
const loadRoles = async () => {
  tableLoading.value = true
  try {
    const params = new URLSearchParams({
      current: currentPage.value,
      size: pageSize.value
    })
    if (searchKeyword.value) params.set('keyword', searchKeyword.value)
    const res = await api(`/roles?${params.toString()}`)
    const data = res.data || res
    roleList.value = (data.records || []).map(r => ({
      ...r,
      userCount: 0,
      scope: r.description || '-'
    }))
    totalRecords.value = data.total || 0
  } catch (e) {
    console.error('加载角色列表失败:', e)
    roleList.value = []
  } finally {
    tableLoading.value = false
  }
}

const loadAllPermissions = async () => {
  if (allPermissions.value.length > 0) return // 缓存：只加载一次
  permissionsLoading.value = true
  try {
    const res = await api('/roles/permissions')
    const list = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
    allPermissions.value = list
  } catch (e) {
    console.error('加载权限列表失败:', e)
    allPermissions.value = []
  } finally {
    permissionsLoading.value = false
  }
}

// ===== 操作方法 =====
function handleQuery() {
  currentPage.value = 1
  loadRoles()
}

function handleRefresh() {
  searchKeyword.value = ''
  statusFilter.value = 'all'
  selectedIds.clear()
  currentPage.value = 1
  loadRoles()
}

function toggleCheckAll(e) {
  const data = filteredRoles.value
  if (e.target.checked) {
    data.forEach(role => selectedIds.add(role.id))
  } else {
    data.forEach(role => selectedIds.delete(role.id))
  }
}

function toggleCheck(id) {
  if (selectedIds.has(id)) {
    selectedIds.delete(id)
  } else {
    selectedIds.add(id)
  }
}

function openAddForm() {
  editingId.value = null
  formData.name = ''
  formData.code = ''
  formData.status = 1
  formData.description = ''
  formModalVisible.value = true
}

function openEditForm(id) {
  const role = roleList.value.find(r => r.id === id)
  if (!role) return
  editingId.value = id
  formData.name = role.name
  formData.code = role.code
  formData.status = role.status
  formData.description = role.description || ''
  formModalVisible.value = true
}

async function submitForm() {
  const name = formData.name.trim()
  const code = formData.code.trim()
  if (!name) { appAlert('请输入角色名称', '表单验证', 'warning'); return }
  if (!code) { appAlert('请输入角色编码', '表单验证', 'warning'); return }

  formLoading.value = true
  try {
    if (editingId.value) {
      await api(`/roles/${editingId.value}`, {
        method: 'PUT',
        body: JSON.stringify({
          name,
          description: formData.description.trim(),
          status: formData.status
        })
      })
      appToast('角色已更新')
    } else {
      await api('/roles', {
        method: 'POST',
        body: JSON.stringify({
          name,
          code,
          description: formData.description.trim(),
          status: formData.status
        })
      })
      appToast('角色已新增')
    }
    formModalVisible.value = false
    loadRoles()
  } catch (e) {
    appAlert(e.message || '操作失败', '操作失败', 'danger')
  } finally {
    formLoading.value = false
  }
}

async function toggleRoleStatus(id) {
  const role = roleList.value.find(r => r.id === id)
  if (!role) return
  const nextStatus = role.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '停用'
  const ok = await appConfirm(`确定要${actionText}角色「${role.name}」吗？`, `${actionText}角色`, 'warning')
  if (!ok) return
  try {
    await api(`/roles/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ status: nextStatus })
    })
    role.status = nextStatus
  } catch (e) {
    appAlert(e.message || '操作失败', '操作失败', 'danger')
  }
}

async function deleteRole(id) {
  const role = roleList.value.find(r => r.id === id)
  if (!role) return
  const ok = await appConfirm(`确定删除角色「${role.name}」吗？<br/><small style="color:rgba(29,29,31,0.45)">删除后不可恢复</small>`, '删除角色', 'danger')
  if (!ok) return
  try {
    await api(`/roles/${id}`, { method: 'DELETE' })
    selectedIds.delete(id)
    loadRoles()
  } catch (e) {
    appAlert(e.message || '删除失败', '删除失败', 'danger')
  }
}

async function handleBatchDelete() {
  if (selectedIds.size === 0) { appAlert('请先选择需要删除的角色', '提示', 'warning'); return }
  const ok = await appConfirm(`确定删除选中的 ${selectedIds.size} 个角色吗？<br/><small style="color:rgba(29,29,31,0.45)">删除后不可恢复</small>`, '批量删除', 'danger')
  if (!ok) return
  try {
    await api('/roles/batch-delete', {
      method: 'POST',
      body: JSON.stringify([...selectedIds])
    })
    selectedIds.clear()
    loadRoles()
  } catch (e) {
    appAlert(e.message || '批量删除失败', '批量删除失败', 'danger')
  }
}

async function openPermissionModal(id) {
  const role = roleList.value.find(r => r.id === id)
  if (!role) return
  permissionRoleId.value = id
  permissionTab.value = 'menu'
  permissionChecks.splice(0, permissionChecks.length)

  // 并行加载权限列表和角色已有权限
  await loadAllPermissions()
  try {
    const res = await api(`/roles/${id}/permissions`)
    const ids = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
    permissionChecks.splice(0, permissionChecks.length, ...ids)
  } catch (e) {
    console.error('加载角色权限失败:', e)
    // 权限加载失败时仍打开弹窗，只是不勾选
  }
  permissionModalVisible.value = true
}

async function savePermissions() {
  try {
    await api(`/roles/${permissionRoleId.value}/permissions`, {
      method: 'PUT',
      body: JSON.stringify([...permissionChecks])
    })
    permissionModalVisible.value = false
    loadRoles()
    appToast('权限已保存')
  } catch (e) {
    appAlert(e.message || '保存权限失败', '保存权限失败', 'danger')
  }
}

function openMembersModal(id) {
  const role = roleList.value.find(r => r.id === id)
  if (!role) return
  membersRoleName.value = role.name
  membersList.value = []
  membersModalVisible.value = true
}

// ===== 生命周期 =====
onMounted(() => {
  loadRoles()
})
</script>
