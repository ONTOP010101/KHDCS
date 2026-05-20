// ==============================
// 角色管理页面模块
// 依赖: appToast, appConfirm (全局), lucide (全局 CDN)
// 来源: 拍照管理系统 - 主界面
// ==============================

function getRolesHTML() {
  return `
    <div class="roles-page">
      <div class="roles-stats">
        <div class="roles-card role-stat-card">
          <div class="role-stat-icon blue">
            <i data-lucide="shield-check"></i>
          </div>
          <div class="role-stat-info">
            <span>角色总数</span>
            <strong id="role-stat-total">0</strong>
          </div>
        </div>

        <div class="roles-card role-stat-card">
          <div class="role-stat-icon green">
            <i data-lucide="check-circle"></i>
          </div>
          <div class="role-stat-info">
            <span>启用角色</span>
            <strong id="role-stat-enabled">0</strong>
          </div>
        </div>

        <div class="roles-card role-stat-card">
          <div class="role-stat-icon orange">
            <i data-lucide="pause-circle"></i>
          </div>
          <div class="role-stat-info">
            <span>停用角色</span>
            <strong id="role-stat-disabled">0</strong>
          </div>
        </div>

        <div class="roles-card role-stat-card">
          <div class="role-stat-icon purple">
            <i data-lucide="users-round"></i>
          </div>
          <div class="role-stat-info">
            <span>关联用户</span>
            <strong id="role-stat-users">0</strong>
          </div>
        </div>
      </div>

      <div class="roles-card roles-toolbar">
        <div class="role-search">
          <i data-lucide="search"></i>
          <input id="role-search-input" placeholder="搜索角色名称、角色编码..." />
        </div>

        <select class="role-select" id="role-status-filter">
          <option value="all">全部状态</option>
          <option value="enabled">启用</option>
          <option value="disabled">停用</option>
        </select>

        <button class="role-btn role-btn-ghost" id="role-btn-query">
          <i data-lucide="search"></i>
          查询
        </button>

        <div></div>

        <button class="role-btn role-btn-danger" id="role-btn-batch-delete">
          <i data-lucide="trash-2"></i>
          批量删除
        </button>

        <button class="role-btn role-btn-primary" id="role-btn-add">
          <i data-lucide="plus"></i>
          新增角色
        </button>
      </div>

      <div class="roles-card roles-table-card">
        <div class="role-table-head">
          <div class="role-table-title">
            <strong>角色列表</strong>
            <span id="role-table-count">共 0 条</span>
          </div>

          <button class="role-btn role-btn-ghost" id="role-btn-refresh">
            <i data-lucide="refresh-cw"></i>
            刷新
          </button>
        </div>

        <div class="roles-table-wrap" id="roles-table-wrap">
          <table class="roles-table">
            <thead>
              <tr>
                <th style="width:44px;">
                  <input type="checkbox" class="role-check" id="role-check-all" />
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
            <tbody id="roles-table-body"></tbody>
          </table>
        </div>
      </div>

      ${getRoleModalsHTML()}
    </div>
  `;
}

function getRoleModalsHTML() {
  return `
    <div class="role-modal-mask" id="role-form-modal">
      <div class="role-modal">
        <div class="role-modal-head">
          <strong id="role-form-title">新增角色</strong>
          <button class="role-row-btn" data-close-role-modal="role-form-modal">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="role-modal-body">
          <div class="role-form-grid">
            <div class="role-form-item">
              <label>角色名称</label>
              <input class="role-input" id="role-form-name" placeholder="例如：系统管理员" />
            </div>

            <div class="role-form-item">
              <label>角色编码</label>
              <input class="role-input" id="role-form-code" placeholder="例如：admin" />
            </div>

            <div class="role-form-item">
              <label>状态</label>
              <select class="role-select" id="role-form-status">
                <option value="enabled">启用</option>
                <option value="disabled">停用</option>
              </select>
            </div>

            <div class="role-form-item">
              <label>数据范围</label>
              <select class="role-select" id="role-form-scope">
                <option value="全部数据">全部数据</option>
                <option value="本部门数据">本部门数据</option>
                <option value="本人数据">本人数据</option>
                <option value="自定义数据">自定义数据</option>
              </select>
            </div>

            <div class="role-form-item full">
              <label>角色描述</label>
              <textarea class="role-textarea" id="role-form-desc" placeholder="请输入角色说明..."></textarea>
            </div>
          </div>
        </div>

        <div class="role-modal-foot">
          <button class="role-btn role-btn-ghost" data-close-role-modal="role-form-modal">取消</button>
          <button class="role-btn role-btn-primary" id="role-form-submit">
            <i data-lucide="check"></i>
            保存
          </button>
        </div>
      </div>
    </div>

    <div class="role-modal-mask" id="role-permission-modal">
      <div class="role-modal large">
        <div class="role-modal-head">
          <strong id="role-permission-title">权限配置</strong>
          <button class="role-row-btn" data-close-role-modal="role-permission-modal">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="role-modal-body">
          <div class="role-permission-layout">
            <div class="role-permission-left">
              <button class="role-permission-tab active" data-permission-tab="menu">
                <i data-lucide="layout-dashboard"></i>
                菜单权限
              </button>
              <button class="role-permission-tab" data-permission-tab="button">
                <i data-lucide="mouse-pointer-click"></i>
                按钮权限
              </button>
              <button class="role-permission-tab" data-permission-tab="data">
                <i data-lucide="database"></i>
                数据权限
              </button>
            </div>

            <div class="role-permission-right">
              <div id="role-permission-content"></div>
            </div>
          </div>
        </div>

        <div class="role-modal-foot">
          <button class="role-btn role-btn-ghost" data-close-role-modal="role-permission-modal">取消</button>
          <button class="role-btn role-btn-primary" id="role-permission-save">
            <i data-lucide="save"></i>
            保存权限
          </button>
        </div>
      </div>
    </div>

    <div class="role-modal-mask" id="role-members-modal">
      <div class="role-modal">
        <div class="role-modal-head">
          <strong id="role-members-title">角色成员</strong>
          <button class="role-row-btn" data-close-role-modal="role-members-modal">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="role-modal-body">
          <div class="role-member-list" id="role-member-list"></div>
        </div>

        <div class="role-modal-foot">
          <button class="role-btn role-btn-ghost" data-close-role-modal="role-members-modal">关闭</button>
        </div>
      </div>
    </div>
  `;
}

var roleState = {
  search: '',
  status: 'all',
  editingId: null,
  permissionRoleId: null,
  permissionTab: 'menu',
  selectedIds: new Set()
};

var roleListData = [
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
];

var roleMemberMock = [
  { name: 'Alex', account: 'alex', dept: '设计部' },
  { name: 'Bella', account: 'bella', dept: '摄影部' },
  { name: 'Chris', account: 'chris', dept: '运营部' },
  { name: 'Diana', account: 'diana', dept: '数据部' },
  { name: 'Eric', account: 'eric', dept: '技术部' }
];

function initRolesPage() {
  renderRolesStats();
  renderRolesTable();
  bindRoleEvents();
}

function getFilteredRoles() {
  var keyword = (roleState.search || '').toLowerCase();

  return roleListData.filter(function(role) {
    var matchKeyword =
      !keyword ||
      role.name.toLowerCase().indexOf(keyword) > -1 ||
      role.code.toLowerCase().indexOf(keyword) > -1;

    var matchStatus =
      roleState.status === 'all' ||
      role.status === roleState.status;

    return matchKeyword && matchStatus;
  });
}

function renderRolesStats() {
  var total = roleListData.length;
  var enabled = roleListData.filter(function(r) { return r.status === 'enabled'; }).length;
  var disabled = roleListData.filter(function(r) { return r.status === 'disabled'; }).length;
  var users = roleListData.reduce(function(sum, r) { return sum + Number(r.users || 0); }, 0);

  setRoleText('role-stat-total', total);
  setRoleText('role-stat-enabled', enabled);
  setRoleText('role-stat-disabled', disabled);
  setRoleText('role-stat-users', users);
}

function renderRolesTable() {
  var body = document.getElementById('roles-table-body');
  var count = document.getElementById('role-table-count');

  if (!body) return;

  var data = getFilteredRoles();

  if (count) {
    count.textContent = '共 ' + data.length + ' 条';
  }

  if (!data.length) {
    body.innerHTML = `
      <tr>
        <td colspan="8">
          <div class="role-empty">暂无匹配角色</div>
        </td>
      </tr>
    `;
    return;
  }

  body.innerHTML = data.map(function(role) {
    return `
      <tr>
        <td>
          <input type="checkbox" class="role-check role-row-check" data-id="${role.id}" ${roleState.selectedIds.has(role.id) ? 'checked' : ''} />
        </td>

        <td>
          <div class="role-name-main">
            <div class="role-mini-icon">
              <i data-lucide="shield-check"></i>
            </div>
            <div class="role-name-text">
              <strong>${escapeRoleHTML(role.name)}</strong>
              <span>${escapeRoleHTML(role.desc || '暂无描述')}</span>
            </div>
          </div>
        </td>

        <td>
          <span class="role-code">${escapeRoleHTML(role.code)}</span>
        </td>

        <td>${escapeRoleHTML(role.scope)}</td>

        <td>${role.users} 人</td>

        <td>
          <span class="role-badge ${role.status === 'enabled' ? 'enabled' : 'disabled'}">
            ${role.status === 'enabled' ? '启用' : '停用'}
          </span>
        </td>

        <td>${role.createTime}</td>

        <td>
          <div class="role-row-actions">
            <button class="role-row-btn" data-role-permission="${role.id}">
              <i data-lucide="key-round"></i>
              权限
            </button>

            <button class="role-row-btn" data-role-members="${role.id}">
              <i data-lucide="users-round"></i>
              成员
            </button>

            <button class="role-row-btn" data-role-edit="${role.id}">
              <i data-lucide="pencil"></i>
              编辑
            </button>

            <button class="role-row-btn warning" data-role-toggle="${role.id}">
              <i data-lucide="${role.status === 'enabled' ? 'pause-circle' : 'play-circle'}"></i>
              ${role.status === 'enabled' ? '停用' : '启用'}
            </button>

            <button class="role-row-btn danger" data-role-delete="${role.id}">
              <i data-lucide="trash-2"></i>
              删除
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  bindRoleTableEvents();
  syncRoleCheckAll();
  lucide.createIcons();
}

function setRoleText(id, text) {
  var el = document.getElementById(id);
  if (el) el.textContent = text;
}

function escapeRoleHTML(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function bindRoleEvents() {
  var search = document.getElementById('role-search-input');
  if (search) {
    search.addEventListener('input', function() {
      roleState.search = this.value.trim();
      renderRolesTable();
    });
  }

  var status = document.getElementById('role-status-filter');
  if (status) {
    status.addEventListener('change', function() {
      roleState.status = this.value;
      renderRolesTable();
    });
  }

  bindRoleClick('role-btn-query', function() {
    renderRolesTable();
    appToast('查询完成');
  });

  bindRoleClick('role-btn-refresh', function() {
    roleState.search = '';
    roleState.status = 'all';
    roleState.selectedIds.clear();

    var search = document.getElementById('role-search-input');
    var status = document.getElementById('role-status-filter');

    if (search) search.value = '';
    if (status) status.value = 'all';

    renderRolesStats();
    renderRolesTable();
    appToast('已刷新');
  });

  bindRoleClick('role-btn-add', function() {
    openRoleFormModal();
  });

  bindRoleClick('role-btn-batch-delete', async function() {
    if (roleState.selectedIds.size === 0) {
      appToast('请先选择需要删除的角色');
      return;
    }

    var ok = await appConfirm('确定删除选中的 ' + roleState.selectedIds.size + ' 个角色吗？', {
      title: '批量删除',
      type: 'danger',
      icon: 'trash-2'
    });

    if (!ok) return;

    roleListData = roleListData.filter(function(role) {
      return !roleState.selectedIds.has(role.id);
    });

    roleState.selectedIds.clear();

    renderRolesStats();
    renderRolesTable();

    appToast('已删除选中角色');
  });

  var checkAll = document.getElementById('role-check-all');
  if (checkAll) {
    checkAll.addEventListener('change', function() {
      var data = getFilteredRoles();

      if (this.checked) {
        data.forEach(function(role) {
          roleState.selectedIds.add(role.id);
        });
      } else {
        data.forEach(function(role) {
          roleState.selectedIds.delete(role.id);
        });
      }

      renderRolesTable();
    });
  }

  document.querySelectorAll('[data-close-role-modal]').forEach(function(btn) {
    btn.addEventListener('click', function() {
      closeRoleModal(this.dataset.closeRoleModal);
    });
  });

  document.querySelectorAll('.role-modal-mask').forEach(function(mask) {
    mask.addEventListener('click', function(e) {
      if (e.target === mask) {
        closeRoleModal(mask.id);
      }
    });
  });

  bindRoleClick('role-form-submit', function() {
    submitRoleForm();
  });

  bindRoleClick('role-permission-save', function() {
    saveRolePermissions();
  });

  document.querySelectorAll('.role-permission-tab').forEach(function(tab) {
    tab.addEventListener('click', function() {
      document.querySelectorAll('.role-permission-tab').forEach(function(t) {
        t.classList.remove('active');
      });

      this.classList.add('active');
      roleState.permissionTab = this.dataset.permissionTab;
      renderRolePermissionContent();
      lucide.createIcons();
    });
  });
}

function bindRoleTableEvents() {
  var body = document.getElementById('roles-table-body');
  if (!body) return;

  body.onclick = function(e) {
    var check = e.target.closest('.role-row-check');

    if (check) {
      var checkId = Number(check.dataset.id);

      if (check.checked) {
        roleState.selectedIds.add(checkId);
      } else {
        roleState.selectedIds.delete(checkId);
      }

      syncRoleCheckAll();
      return;
    }

    var permissionBtn = e.target.closest('[data-role-permission]');
    if (permissionBtn) {
      var id = Number(permissionBtn.dataset.rolePermission);
      openRolePermissionModal(id);
      return;
    }

    var membersBtn = e.target.closest('[data-role-members]');
    if (membersBtn) {
      var id = Number(membersBtn.dataset.roleMembers);
      openRoleMembersModal(id);
      return;
    }

    var editBtn = e.target.closest('[data-role-edit]');
    if (editBtn) {
      var id = Number(editBtn.dataset.roleEdit);
      openRoleFormModal(id);
      return;
    }

    var toggleBtn = e.target.closest('[data-role-toggle]');
    if (toggleBtn) {
      var id = Number(toggleBtn.dataset.roleToggle);
      toggleRoleStatus(id);
      return;
    }

    var deleteBtn = e.target.closest('[data-role-delete]');
    if (deleteBtn) {
      var id = Number(deleteBtn.dataset.roleDelete);
      deleteRole(id);
      return;
    }
  };
}

function bindRoleClick(id, fn) {
  var el = document.getElementById(id);
  if (el) el.addEventListener('click', fn);
}

function syncRoleCheckAll() {
  var checkAll = document.getElementById('role-check-all');
  if (!checkAll) return;

  var data = getFilteredRoles();

  if (!data.length) {
    checkAll.checked = false;
    checkAll.indeterminate = false;
    return;
  }

  var checkedCount = data.filter(function(role) {
    return roleState.selectedIds.has(role.id);
  }).length;

  checkAll.checked = checkedCount === data.length;
  checkAll.indeterminate = checkedCount > 0 && checkedCount < data.length;
}

function openRoleModal(id) {
  var modal = document.getElementById(id);
  if (modal) modal.classList.add('show');
  lucide.createIcons();
}

function closeRoleModal(id) {
  var modal = document.getElementById(id);
  if (modal) modal.classList.remove('show');
}

function roleToast(message) {
  if (typeof appToast === 'function') {
    appToast(message);
  } else {
    alert(message);
  }
}

function roleConfirm(message, options) {
  if (typeof appConfirm === 'function') {
    return appConfirm(message, options || {});
  }

  return Promise.resolve(confirm(message));
}

function openRoleFormModal(id) {
  roleState.editingId = id || null;

  var title = document.getElementById('role-form-title');
  var name = document.getElementById('role-form-name');
  var code = document.getElementById('role-form-code');
  var status = document.getElementById('role-form-status');
  var scope = document.getElementById('role-form-scope');
  var desc = document.getElementById('role-form-desc');

  if (roleState.editingId) {
    var role = roleListData.find(function(r) {
      return r.id === roleState.editingId;
    });

    if (!role) return;

    if (title) title.textContent = '编辑角色';
    if (name) name.value = role.name;
    if (code) code.value = role.code;
    if (status) status.value = role.status;
    if (scope) scope.value = role.scope;
    if (desc) desc.value = role.desc || '';
  } else {
    if (title) title.textContent = '新增角色';
    if (name) name.value = '';
    if (code) code.value = '';
    if (status) status.value = 'enabled';
    if (scope) scope.value = '本部门数据';
    if (desc) desc.value = '';
  }

  openRoleModal('role-form-modal');
}

function submitRoleForm() {
  var name = document.getElementById('role-form-name').value.trim();
  var code = document.getElementById('role-form-code').value.trim();
  var status = document.getElementById('role-form-status').value;
  var scope = document.getElementById('role-form-scope').value;
  var desc = document.getElementById('role-form-desc').value.trim();

  if (!name) {
    appToast('请输入角色名称');
    return;
  }

  if (!code) {
    appToast('请输入角色编码');
    return;
  }

  var duplicated = roleListData.some(function(role) {
    return role.code === code && role.id !== roleState.editingId;
  });

  if (duplicated) {
    appToast('角色编码已存在');
    return;
  }

  if (roleState.editingId) {
    var role = roleListData.find(function(r) {
      return r.id === roleState.editingId;
    });

    if (role) {
      role.name = name;
      role.code = code;
      role.status = status;
      role.scope = scope;
      role.desc = desc;
    }

    appToast('角色已更新');
  } else {
    var newId = Date.now();

    roleListData.unshift({
      id: newId,
      name: name,
      code: code,
      status: status,
      scope: scope,
      desc: desc,
      users: 0,
      createTime: new Date().toISOString().slice(0, 10),
      permissions: []
    });

    appToast('角色已新增');
  }

  closeRoleModal('role-form-modal');
  renderRolesStats();
  renderRolesTable();
}

function openRoleMembersModal(id) {
  var role = roleListData.find(function(r) {
    return r.id === id;
  });

  if (!role) return;

  setRoleText('role-members-title', role.name + ' - 角色成员');

  var list = document.getElementById('role-member-list');
  if (!list) return;

  var members = roleMemberMock.slice(0, Math.max(1, Math.min(role.users, roleMemberMock.length)));

  list.innerHTML = members.map(function(m) {
    return `
      <div class="role-member-item">
        <div class="role-member-avatar">${m.name.slice(0, 1)}</div>
        <div class="role-member-info">
          <strong>${m.name}</strong>
          <span>${m.account} · ${m.dept}</span>
        </div>
        <span class="role-badge enabled">正常</span>
      </div>
    `;
  }).join('');

  openRoleModal('role-members-modal');
}

async function toggleRoleStatus(id) {
  var role = roleListData.find(function(r) {
    return r.id === id;
  });

  if (!role) return;

  var nextStatus = role.status === 'enabled' ? 'disabled' : 'enabled';
  var actionText = nextStatus === 'enabled' ? '启用' : '停用';

  var ok = await appConfirm('确定要' + actionText + '角色「' + role.name + '」吗？', {
    title: actionText + '角色',
    type: nextStatus === 'enabled' ? 'warning' : 'danger',
    icon: nextStatus === 'enabled' ? 'play-circle' : 'pause-circle'
  });

  if (!ok) return;

  role.status = nextStatus;

  renderRolesStats();
  renderRolesTable();

  appToast('角色已' + actionText);
}

async function deleteRole(id) {
  var role = roleListData.find(function(r) {
    return r.id === id;
  });

  if (!role) return;

  var ok = await appConfirm('确定删除角色「' + role.name + '」吗？删除后不可恢复。', {
    title: '删除角色',
    type: 'danger',
    icon: 'trash-2'
  });

  if (!ok) return;

  roleListData = roleListData.filter(function(r) {
    return r.id !== id;
  });

  roleState.selectedIds.delete(id);

  renderRolesStats();
  renderRolesTable();

  appToast('角色已删除');
}

function openRolePermissionModal(id) {
  roleState.permissionRoleId = id;
  roleState.permissionTab = 'menu';

  var role = roleListData.find(function(r) {
    return r.id === id;
  });

  if (!role) {
    appToast('未找到角色');
    return;
  }

  setRoleText('role-permission-title', role.name + ' - 权限配置');

  document.querySelectorAll('.role-permission-tab').forEach(function(tab) {
    tab.classList.toggle('active', tab.dataset.permissionTab === 'menu');
  });

  renderRolePermissionContent();
  openRoleModal('role-permission-modal');
}

function renderRolePermissionContent() {
  var box = document.getElementById('role-permission-content');
  if (!box) return;

  var role = roleListData.find(function(r) {
    return r.id === roleState.permissionRoleId;
  });

  if (!role) return;

  if (roleState.permissionTab === 'menu') {
    box.innerHTML = renderPermissionGroup('菜单权限', [
      ['dashboard', '首页仪表盘'],
      ['sample', '样品资料'],
      ['gallery', '择样图库'],
      ['friends', '好友列表'],
      ['roles', '角色管理'],
      ['users', '用户管理']
    ], role.permissions);
  }

  if (roleState.permissionTab === 'button') {
    box.innerHTML = renderPermissionGroup('按钮权限', [
      ['add', '新增'],
      ['edit', '编辑'],
      ['delete', '删除'],
      ['export', '导出'],
      ['import', '导入'],
      ['audit', '审核']
    ], role.permissions);
  }

  if (roleState.permissionTab === 'data') {
    box.innerHTML = renderPermissionGroup('数据权限', [
      ['all-data', '全部数据'],
      ['dept-data', '本部门数据'],
      ['self-data', '本人数据'],
      ['custom-data', '自定义数据']
    ], role.permissions);
  }
}

function renderPermissionGroup(title, items, current) {
  current = current || [];

  return `
    <div class="role-permission-title">${title}</div>

    <div class="role-permission-group">
      <div class="role-permission-group-title">请选择权限</div>

      <div class="role-permission-checks">
        ${items.map(function(item) {
          var key = item[0];
          var label = item[1];

          return `
            <label class="role-check-item">
              <input
                type="checkbox"
                class="role-permission-check"
                value="${key}"
                ${current.indexOf(key) > -1 ? 'checked' : ''}
              />
              <span>${label}</span>
            </label>
          `;
        }).join('')}
      </div>
    </div>
  `;
}

function saveRolePermissions() {
  var role = roleListData.find(function(r) {
    return r.id === roleState.permissionRoleId;
  });

  if (!role) {
    appToast('未找到角色');
    return;
  }

  var checks = document.querySelectorAll('.role-permission-check:checked');
  var permissions = [];

  checks.forEach(function(check) {
    permissions.push(check.value);
  });

  role.permissions = permissions;

  closeRoleModal('role-permission-modal');
  renderRolesTable();

  appToast('权限已保存');
}
