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
                    <span>{{ role.desc || '暂无描述' }}</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="role-code">{{ role.code }}</span>
              </td>
              <td>{{ role.scope }}</td>
              <td>{{ role.users }} 人</td>
              <td>
                <span :class="['role-badge', role.status === 'enabled' ? 'enabled' : 'disabled']">
                  {{ role.status === 'enabled' ? '启用' : '停用' }}
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
                    <component :is="role.status === 'enabled' ? PauseCircle : PlayCircle" :size="14" />
                    {{ role.status === 'enabled' ? '停用' : '启用' }}
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
                <option value="enabled">启用</option>
                <option value="disabled">停用</option>
              </select>
            </div>
            <div class="role-form-item">
              <label>数据范围</label>
              <select class="role-select" v-model="formData.scope">
                <option value="全部数据">全部数据</option>
                <option value="本部门数据">本部门数据</option>
                <option value="本人数据">本人数据</option>
                <option value="自定义数据">自定义数据</option>
              </select>
            </div>
            <div class="role-form-item full">
              <label>角色描述</label>
              <textarea class="role-textarea" v-model="formData.desc" placeholder="请输入角色说明..."></textarea>
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
                    <label class="role-check-item" v-for="item in menuPermissions" :key="item.key">
                      <input
                        type="checkbox"
                        class="role-permission-check"
                        :value="item.key"
                        v-model="permissionChecks"
                      />
                      <span>{{ item.label }}</span>
                    </label>
                  </div>
                </div>
              </template>

              <template v-if="permissionTab === 'button'">
                <div class="role-permission-title">按钮权限</div>
                <div class="role-permission-group">
                  <div class="role-permission-group-title">请选择权限</div>
                  <div class="role-permission-checks">
                    <label class="role-check-item" v-for="item in buttonPermissions" :key="item.key">
                      <input
                        type="checkbox"
                        class="role-permission-check"
                        :value="item.key"
                        v-model="permissionChecks"
                      />
                      <span>{{ item.label }}</span>
                    </label>
                  </div>
                </div>
              </template>

              <template v-if="permissionTab === 'data'">
                <div class="role-permission-title">数据权限</div>
                <div class="role-permission-group">
                  <div class="role-permission-group-title">请选择权限</div>
                  <div class="role-permission-checks">
                    <label class="role-check-item" v-for="item in dataPermissions" :key="item.key">
                      <input
                        type="checkbox"
                        class="role-permission-check"
                        :value="item.key"
                        v-model="permissionChecks"
                      />
                      <span>{{ item.label }}</span>
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
import { ref, reactive, computed } from 'vue'
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
import '@/styles/roles.css'

const roleList = reactive([
  {
    id: 1,
    name: '系统管理员',
    code: 'admin',
    scope: '全部数据',
    users: 3,
    status: 'enabled',
    desc: '拥有系统全部管理权限',
    createTime: '2024-01-12',
    permissions: ['dashboard', 'sample', 'gallery', 'friends', 'roles', 'users', 'add', 'edit', 'delete', 'export', 'all-data']
  },
  {
    id: 2,
    name: '设计主管',
    code: 'design_manager',
    scope: '本部门数据',
    users: 8,
    status: 'enabled',
    desc: '负责设计部门样品与图库管理',
    createTime: '2024-02-08',
    permissions: ['dashboard', 'sample', 'gallery', 'add', 'edit', 'export', 'dept-data']
  },
  {
    id: 3,
    name: '摄影师',
    code: 'photographer',
    scope: '本人数据',
    users: 12,
    status: 'enabled',
    desc: '负责图片上传与图片资料维护',
    createTime: '2024-03-16',
    permissions: ['gallery', 'add', 'edit', 'self-data']
  },
  {
    id: 4,
    name: '数据审核员',
    code: 'data_auditor',
    scope: '自定义数据',
    users: 5,
    status: 'disabled',
    desc: '负责样品数据审核与校验',
    createTime: '2024-04-21',
    permissions: ['sample', 'export', 'custom-data']
  }
])

const roleMemberMock = [
  { name: 'Alex', account: 'alex', dept: '设计部' },
  { name: 'Bella', account: 'bella', dept: '摄影部' },
  { name: 'Chris', account: 'chris', dept: '运营部' },
  { name: 'Diana', account: 'diana', dept: '数据部' },
  { name: 'Eric', account: 'eric', dept: '技术部' }
]

const menuPermissions = [
  { key: 'dashboard', label: '首页仪表盘' },
  { key: 'sample', label: '样品资料' },
  { key: 'gallery', label: '择样图库' },
  { key: 'friends', label: '好友列表' },
  { key: 'roles', label: '角色管理' },
  { key: 'users', label: '用户管理' }
]

const buttonPermissions = [
  { key: 'add', label: '新增' },
  { key: 'edit', label: '编辑' },
  { key: 'delete', label: '删除' },
  { key: 'export', label: '导出' },
  { key: 'import', label: '导入' },
  { key: 'audit', label: '审核' }
]

const dataPermissions = [
  { key: 'all-data', label: '全部数据' },
  { key: 'dept-data', label: '本部门数据' },
  { key: 'self-data', label: '本人数据' },
  { key: 'custom-data', label: '自定义数据' }
]

const searchKeyword = ref('')
const statusFilter = ref('all')
const selectedIds = reactive(new Set())

const formModalVisible = ref(false)
const permissionModalVisible = ref(false)
const membersModalVisible = ref(false)

const editingId = ref(null)
const formData = reactive({
  name: '',
  code: '',
  status: 'enabled',
  scope: '本部门数据',
  desc: ''
})

const permissionRoleId = ref(null)
const permissionTab = ref('menu')
const permissionChecks = reactive([])

const membersRoleName = ref('')
const membersList = ref([])

const enabledCount = computed(() => roleList.filter(r => r.status === 'enabled').length)
const disabledCount = computed(() => roleList.filter(r => r.status === 'disabled').length)
const totalUsers = computed(() => roleList.reduce((sum, r) => sum + Number(r.users || 0), 0))

const filteredRoles = computed(() => {
  const keyword = (searchKeyword.value || '').toLowerCase()
  return roleList.filter(role => {
    const matchKeyword = !keyword || role.name.toLowerCase().includes(keyword) || role.code.toLowerCase().includes(keyword)
    const matchStatus = statusFilter.value === 'all' || role.status === statusFilter.value
    return matchKeyword && matchStatus
  })
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
  const role = roleList.find(r => r.id === permissionRoleId.value)
  return role ? role.name : ''
})

function handleQuery() {
  alert('查询完成')
}

function handleRefresh() {
  searchKeyword.value = ''
  statusFilter.value = 'all'
  selectedIds.clear()
  alert('已刷新')
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
  formData.status = 'enabled'
  formData.scope = '本部门数据'
  formData.desc = ''
  formModalVisible.value = true
}

function openEditForm(id) {
  const role = roleList.find(r => r.id === id)
  if (!role) return
  editingId.value = id
  formData.name = role.name
  formData.code = role.code
  formData.status = role.status
  formData.scope = role.scope
  formData.desc = role.desc || ''
  formModalVisible.value = true
}

function submitForm() {
  const name = formData.name.trim()
  const code = formData.code.trim()
  if (!name) {
    alert('请输入角色名称')
    return
  }
  if (!code) {
    alert('请输入角色编码')
    return
  }
  const duplicated = roleList.some(role => role.code === code && role.id !== editingId.value)
  if (duplicated) {
    alert('角色编码已存在')
    return
  }
  if (editingId.value) {
    const role = roleList.find(r => r.id === editingId.value)
    if (role) {
      role.name = name
      role.code = code
      role.status = formData.status
      role.scope = formData.scope
      role.desc = formData.desc.trim()
    }
    alert('角色已更新')
  } else {
    roleList.unshift({
      id: Date.now(),
      name,
      code,
      status: formData.status,
      scope: formData.scope,
      desc: formData.desc.trim(),
      users: 0,
      createTime: new Date().toISOString().slice(0, 10),
      permissions: []
    })
    alert('角色已新增')
  }
  formModalVisible.value = false
}

async function toggleRoleStatus(id) {
  const role = roleList.find(r => r.id === id)
  if (!role) return
  const nextStatus = role.status === 'enabled' ? 'disabled' : 'enabled'
  const actionText = nextStatus === 'enabled' ? '启用' : '停用'
  const ok = confirm(`确定要${actionText}角色「${role.name}」吗？`)
  if (!ok) return
  role.status = nextStatus
  alert(`角色已${actionText}`)
}

async function deleteRole(id) {
  const role = roleList.find(r => r.id === id)
  if (!role) return
  const ok = confirm(`确定删除角色「${role.name}」吗？删除后不可恢复。`)
  if (!ok) return
  const idx = roleList.findIndex(r => r.id === id)
  if (idx > -1) roleList.splice(idx, 1)
  selectedIds.delete(id)
  alert('角色已删除')
}

async function handleBatchDelete() {
  if (selectedIds.size === 0) {
    alert('请先选择需要删除的角色')
    return
  }
  const ok = confirm(`确定删除选中的 ${selectedIds.size} 个角色吗？`)
  if (!ok) return
  const idsToDelete = new Set(selectedIds)
  for (let i = roleList.length - 1; i >= 0; i--) {
    if (idsToDelete.has(roleList[i].id)) {
      roleList.splice(i, 1)
    }
  }
  selectedIds.clear()
  alert('已删除选中角色')
}

function openPermissionModal(id) {
  const role = roleList.find(r => r.id === id)
  if (!role) {
    alert('未找到角色')
    return
  }
  permissionRoleId.value = id
  permissionTab.value = 'menu'
  permissionChecks.splice(0, permissionChecks.length, ...role.permissions)
  permissionModalVisible.value = true
}

function savePermissions() {
  const role = roleList.find(r => r.id === permissionRoleId.value)
  if (!role) {
    alert('未找到角色')
    return
  }
  role.permissions = [...permissionChecks]
  permissionModalVisible.value = false
  alert('权限已保存')
}

function openMembersModal(id) {
  const role = roleList.find(r => r.id === id)
  if (!role) return
  membersRoleName.value = role.name
  membersList.value = roleMemberMock.slice(0, Math.max(1, Math.min(role.users, roleMemberMock.length)))
  membersModalVisible.value = true
}
</script>
