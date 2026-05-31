// ==============================
// 用户管理页面模块
// 依赖: appToast, appConfirm (全局), lucide (全局 CDN)
// 来源: 拍照管理系统 - 主界面
// ==============================

function getUsersHTML() {
  return `
    <div class="admin-page">
      <div class="admin-stats">
        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon blue"><i data-lucide="users-round"></i></div>
          <div class="admin-stat-info">
            <span>用户总数</span>
            <strong id="user-stat-total">0</strong>
          </div>
        </div>

        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon green"><i data-lucide="check-circle"></i></div>
          <div class="admin-stat-info">
            <span>正常用户</span>
            <strong id="user-stat-enabled">0</strong>
          </div>
        </div>

        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon orange"><i data-lucide="pause-circle"></i></div>
          <div class="admin-stat-info">
            <span>禁用用户</span>
            <strong id="user-stat-disabled">0</strong>
          </div>
        </div>

        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon purple"><i data-lucide="wifi"></i></div>
          <div class="admin-stat-info">
            <span>在线用户</span>
            <strong id="user-stat-online">0</strong>
          </div>
        </div>
      </div>

      <div class="admin-card admin-toolbar">
        <div class="admin-search">
          <i data-lucide="search"></i>
          <input id="user-search-input" placeholder="搜索姓名、账号、手机号..." />
        </div>

        <select class="admin-select" id="user-dept-filter">
          <option value="all">全部部门</option>
          <option value="设计部">设计部</option>
          <option value="摄影部">摄影部</option>
          <option value="运营部">运营部</option>
          <option value="数据部">数据部</option>
          <option value="技术部">技术部</option>
        </select>

        <select class="admin-select" id="user-status-filter">
          <option value="all">全部状态</option>
          <option value="enabled">正常</option>
          <option value="disabled">禁用</option>
        </select>

        <button class="admin-btn admin-btn-ghost" id="user-btn-query">
          <i data-lucide="search"></i>
          查询
        </button>

        <div></div>

        <button class="admin-btn admin-btn-danger" id="user-btn-batch-delete">
          <i data-lucide="trash-2"></i>
          批量删除
        </button>

        <button class="admin-btn admin-btn-primary" id="user-btn-add">
          <i data-lucide="user-plus"></i>
          新增用户
        </button>
      </div>

      <div class="admin-card admin-table-card">
        <div class="admin-table-head">
          <div class="admin-table-title">
            <strong>用户列表</strong>
            <span id="user-table-count">共 0 条</span>
          </div>

          <button class="admin-btn admin-btn-ghost" id="user-btn-refresh">
            <i data-lucide="refresh-cw"></i>
            刷新
          </button>
        </div>

        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th style="width:44px;"><input type="checkbox" class="admin-check" id="user-check-all" /></th>
                <th>用户信息</th>
                <th>登录账号</th>
                <th>部门</th>
                <th>角色</th>
                <th>手机号</th>
                <th>状态</th>
                <th>最近登录</th>
                <th style="width:360px;">操作</th>
              </tr>
            </thead>
            <tbody id="users-table-body"></tbody>
          </table>
        </div>
      </div>

      ${getUserModalsHTML()}
    </div>
  `;
}

function getUserModalsHTML() {
  return `
    <div class="admin-modal-mask" id="user-form-modal">
      <div class="admin-modal">
        <div class="admin-modal-head">
          <strong id="user-form-title">新增用户</strong>
          <button class="admin-row-btn" data-close-admin-modal="user-form-modal">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="admin-modal-body">
          <div class="admin-form-grid">
            <div class="admin-form-item">
              <label>用户姓名</label>
              <input class="admin-input" id="user-form-name" placeholder="请输入用户姓名" />
            </div>

            <div class="admin-form-item">
              <label>登录账号</label>
              <input class="admin-input" id="user-form-account" placeholder="请输入登录账号" />
            </div>

            <div class="admin-form-item">
              <label>手机号</label>
              <input class="admin-input" id="user-form-phone" placeholder="请输入手机号" />
            </div>

            <div class="admin-form-item">
              <label>部门</label>
              <select class="admin-select" id="user-form-dept">
                <option value="设计部">设计部</option>
                <option value="摄影部">摄影部</option>
                <option value="运营部">运营部</option>
                <option value="数据部">数据部</option>
                <option value="技术部">技术部</option>
              </select>
            </div>

            <div class="admin-form-item">
              <label>角色</label>
              <select class="admin-select" id="user-form-role">
                <option value="系统管理员">系统管理员</option>
                <option value="设计主管">设计主管</option>
                <option value="摄影师">摄影师</option>
                <option value="数据审核员">数据审核员</option>
                <option value="普通用户">普通用户</option>
              </select>
            </div>

            <div class="admin-form-item">
              <label>状态</label>
              <select class="admin-select" id="user-form-status">
                <option value="enabled">正常</option>
                <option value="disabled">禁用</option>
              </select>
            </div>

            <div class="admin-form-item full">
              <label>备注</label>
              <textarea class="admin-textarea" id="user-form-remark" placeholder="请输入备注..."></textarea>
            </div>
          </div>
        </div>

        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" data-close-admin-modal="user-form-modal">取消</button>
          <button class="admin-btn admin-btn-primary" id="user-form-submit">
            <i data-lucide="check"></i>
            保存
          </button>
        </div>
      </div>
    </div>

    <div class="admin-modal-mask" id="user-role-modal">
      <div class="admin-modal">
        <div class="admin-modal-head">
          <strong id="user-role-title">分配角色</strong>
          <button class="admin-row-btn" data-close-admin-modal="user-role-modal">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="admin-modal-body">
          <div class="admin-form-item">
            <label>选择角色</label>
            <select class="admin-select" id="user-role-select">
              <option value="系统管理员">系统管理员</option>
              <option value="设计主管">设计主管</option>
              <option value="摄影师">摄影师</option>
              <option value="数据审核员">数据审核员</option>
              <option value="普通用户">普通用户</option>
            </select>
          </div>
        </div>

        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" data-close-admin-modal="user-role-modal">取消</button>
          <button class="admin-btn admin-btn-primary" id="user-role-submit">
            <i data-lucide="save"></i>
            保存角色
          </button>
        </div>
      </div>
    </div>
  `;
}

var userState = {
  search: '',
  dept: 'all',
  status: 'all',
  editingId: null,
  roleUserId: null,
  selectedIds: new Set()
};

var userListData = [
  { id: 1, name: 'Alex', account: 'alex', phone: '13800000001', dept: '设计部', role: '系统管理员', status: 'enabled', online: true, lastLogin: '2025-02-15 10:24', remark: '系统管理员' },
  { id: 2, name: 'Bella', account: 'bella', phone: '13800000002', dept: '摄影部', role: '摄影师', status: 'enabled', online: true, lastLogin: '2025-02-15 09:42', remark: '' },
  { id: 3, name: 'Chris', account: 'chris', phone: '13800000003', dept: '运营部', role: '普通用户', status: 'enabled', online: false, lastLogin: '2025-02-14 18:10', remark: '' },
  { id: 4, name: 'Diana', account: 'diana', phone: '13800000004', dept: '数据部', role: '数据审核员', status: 'disabled', online: false, lastLogin: '2025-02-12 15:35', remark: '临时停用' },
  { id: 5, name: 'Eric', account: 'eric', phone: '13800000005', dept: '技术部', role: '设计主管', status: 'enabled', online: false, lastLogin: '2025-02-13 11:20', remark: '' }
];

function initUsersPage() {
  renderUsersStats();
  renderUsersTable();
  bindUserEvents();
}

function getFilteredUsers() {
  var keyword = userState.search.toLowerCase();

  return userListData.filter(function(user) {
    var matchKeyword =
      !keyword ||
      user.name.toLowerCase().indexOf(keyword) > -1 ||
      user.account.toLowerCase().indexOf(keyword) > -1 ||
      user.phone.indexOf(keyword) > -1;

    var matchDept = userState.dept === 'all' || user.dept === userState.dept;
    var matchStatus = userState.status === 'all' || user.status === userState.status;

    return matchKeyword && matchDept && matchStatus;
  });
}

function renderUsersStats() {
  setAdminText('user-stat-total', userListData.length);
  setAdminText('user-stat-enabled', userListData.filter(function(u) { return u.status === 'enabled'; }).length);
  setAdminText('user-stat-disabled', userListData.filter(function(u) { return u.status === 'disabled'; }).length);
  setAdminText('user-stat-online', userListData.filter(function(u) { return u.online; }).length);
}

function renderUsersTable() {
  var body = document.getElementById('users-table-body');
  var count = document.getElementById('user-table-count');
  if (!body) return;

  var data = getFilteredUsers();
  if (count) count.textContent = '共 ' + data.length + ' 条';

  if (!data.length) {
    body.innerHTML = '<tr><td colspan="9"><div class="admin-empty">暂无匹配用户</div></td></tr>';
    return;
  }

  body.innerHTML = data.map(function(user) {
    return `
      <tr>
        <td>
          <input type="checkbox" class="admin-check user-row-check" data-id="${user.id}" ${userState.selectedIds.has(user.id) ? 'checked' : ''} />
        </td>

        <td>
          <div class="admin-user-main">
            <div class="admin-avatar">${escapeAdminHTML(user.name.slice(0, 1))}</div>
            <div class="admin-main-text">
              <strong>${escapeAdminHTML(user.name)}</strong>
              <span>${user.online ? '在线' : '离线'}</span>
            </div>
          </div>
        </td>

        <td>${escapeAdminHTML(user.account)}</td>
        <td>${escapeAdminHTML(user.dept)}</td>
        <td><span class="admin-badge blue">${escapeAdminHTML(user.role)}</span></td>
        <td>${escapeAdminHTML(user.phone)}</td>

        <td>
          <span class="admin-badge ${user.status === 'enabled' ? 'green' : 'gray'}">
            ${user.status === 'enabled' ? '正常' : '禁用'}
          </span>
        </td>

        <td>${escapeAdminHTML(user.lastLogin)}</td>

        <td>
          <div class="admin-row-actions">
            <button class="admin-row-btn" data-user-edit="${user.id}">
              <i data-lucide="pencil"></i>编辑
            </button>

            <button class="admin-row-btn" data-user-role="${user.id}">
              <i data-lucide="shield-check"></i>角色
            </button>

            <button class="admin-row-btn warning" data-user-reset="${user.id}">
              <i data-lucide="key-round"></i>重置
            </button>

            <button class="admin-row-btn warning" data-user-toggle="${user.id}">
              <i data-lucide="${user.status === 'enabled' ? 'pause-circle' : 'play-circle'}"></i>
              ${user.status === 'enabled' ? '禁用' : '启用'}
            </button>

            <button class="admin-row-btn danger" data-user-delete="${user.id}">
              <i data-lucide="trash-2"></i>删除
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  bindUserTableEvents();
  syncUserCheckAll();
  lucide.createIcons();
}

function setAdminText(id, text) {
  var el = document.getElementById(id);
  if (el) el.textContent = text;
}

function escapeAdminHTML(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function renderAdminDetail(label, value) {
  return `
    <div class="admin-detail-item">
      <span>${escapeAdminHTML(label)}</span>
      <strong>${escapeAdminHTML(value)}</strong>
    </div>
  `;
}

function bindAdminClick(id, fn) {
  var el = document.getElementById(id);
  if (el) el.addEventListener('click', fn);
}

function adminToast(message) {
  if (typeof appToast === 'function') {
    appToast(message);
  } else {
    alert(message);
  }
}

function adminConfirm(message, options) {
  if (typeof appConfirm === 'function') {
    return appConfirm(message, options || {});
  }
  return Promise.resolve(confirm(message));
}

function bindAdminModalClose() {
  document.querySelectorAll('[data-close-admin-modal]').forEach(function(btn) {
    btn.addEventListener('click', function() {
      closeAdminModal(this.dataset.closeAdminModal);
    });
  });

  document.querySelectorAll('.admin-modal-mask').forEach(function(mask) {
    mask.addEventListener('click', function(e) {
      if (e.target === mask) closeAdminModal(mask.id);
    });
  });
}

function openAdminModal(id) {
  var modal = document.getElementById(id);
  if (modal) modal.classList.add('show');
  lucide.createIcons();
}

function closeAdminModal(id) {
  var modal = document.getElementById(id);
  if (modal) modal.classList.remove('show');
}

function bindUserEvents() {
  var search = document.getElementById('user-search-input');
  if (search) {
    search.addEventListener('input', function() {
      userState.search = this.value.trim();
      renderUsersTable();
    });
  }

  var dept = document.getElementById('user-dept-filter');
  if (dept) {
    dept.addEventListener('change', function() {
      userState.dept = this.value;
      renderUsersTable();
    });
  }

  var status = document.getElementById('user-status-filter');
  if (status) {
    status.addEventListener('change', function() {
      userState.status = this.value;
      renderUsersTable();
    });
  }

  bindAdminClick('user-btn-query', function() {
    renderUsersTable();
    adminToast('查询完成');
  });

  bindAdminClick('user-btn-refresh', function() {
    userState.search = '';
    userState.dept = 'all';
    userState.status = 'all';
    userState.selectedIds.clear();

    var search = document.getElementById('user-search-input');
    var dept = document.getElementById('user-dept-filter');
    var status = document.getElementById('user-status-filter');

    if (search) search.value = '';
    if (dept) dept.value = 'all';
    if (status) status.value = 'all';

    renderUsersStats();
    renderUsersTable();
    adminToast('已刷新');
  });

  bindAdminClick('user-btn-add', function() {
    openUserFormModal();
  });

  bindAdminClick('user-form-submit', submitUserForm);
  bindAdminClick('user-role-submit', submitUserRole);

  bindAdminClick('user-btn-batch-delete', async function() {
    if (!userState.selectedIds.size) {
      adminToast('请先选择用户');
      return;
    }

    var ok = await adminConfirm('确定删除选中的 ' + userState.selectedIds.size + ' 个用户吗？', {
      title: '批量删除',
      type: 'danger',
      icon: 'trash-2'
    });

    if (!ok) return;

    userListData = userListData.filter(function(user) {
      return !userState.selectedIds.has(user.id);
    });

    userState.selectedIds.clear();

    renderUsersStats();
    renderUsersTable();
    adminToast('已删除选中用户');
  });

  var checkAll = document.getElementById('user-check-all');
  if (checkAll) {
    checkAll.addEventListener('change', function() {
      var data = getFilteredUsers();

      if (this.checked) {
        data.forEach(function(u) { userState.selectedIds.add(u.id); });
      } else {
        data.forEach(function(u) { userState.selectedIds.delete(u.id); });
      }

      renderUsersTable();
    });
  }

  bindAdminModalClose();
}

function bindUserTableEvents() {
  var body = document.getElementById('users-table-body');
  if (!body) return;

  body.onclick = function(e) {
    var check = e.target.closest('.user-row-check');
    if (check) {
      var checkId = Number(check.dataset.id);
      if (check.checked) userState.selectedIds.add(checkId);
      else userState.selectedIds.delete(checkId);
      syncUserCheckAll();
      return;
    }

    var editBtn = e.target.closest('[data-user-edit]');
    if (editBtn) return openUserFormModal(Number(editBtn.dataset.userEdit));

    var roleBtn = e.target.closest('[data-user-role]');
    if (roleBtn) return openUserRoleModal(Number(roleBtn.dataset.userRole));

    var resetBtn = e.target.closest('[data-user-reset]');
    if (resetBtn) return resetUserPassword(Number(resetBtn.dataset.userReset));

    var toggleBtn = e.target.closest('[data-user-toggle]');
    if (toggleBtn) return toggleUserStatus(Number(toggleBtn.dataset.userToggle));

    var deleteBtn = e.target.closest('[data-user-delete]');
    if (deleteBtn) return deleteUser(Number(deleteBtn.dataset.userDelete));
  };
}

function syncUserCheckAll() {
  var checkAll = document.getElementById('user-check-all');
  if (!checkAll) return;

  var data = getFilteredUsers();

  if (!data.length) {
    checkAll.checked = false;
    checkAll.indeterminate = false;
    return;
  }

  var checkedCount = data.filter(function(u) { return userState.selectedIds.has(u.id); }).length;
  checkAll.checked = checkedCount === data.length;
  checkAll.indeterminate = checkedCount > 0 && checkedCount < data.length;
}

function openUserFormModal(id) {
  userState.editingId = id || null;

  var title = document.getElementById('user-form-title');
  var name = document.getElementById('user-form-name');
  var account = document.getElementById('user-form-account');
  var phone = document.getElementById('user-form-phone');
  var dept = document.getElementById('user-form-dept');
  var role = document.getElementById('user-form-role');
  var status = document.getElementById('user-form-status');
  var remark = document.getElementById('user-form-remark');

  if (userState.editingId) {
    var user = userListData.find(function(u) { return u.id === userState.editingId; });
    if (!user) return;

    if (title) title.textContent = '编辑用户';
    if (name) name.value = user.name;
    if (account) account.value = user.account;
    if (phone) phone.value = user.phone;
    if (dept) dept.value = user.dept;
    if (role) role.value = user.role;
    if (status) status.value = user.status;
    if (remark) remark.value = user.remark || '';
  } else {
    if (title) title.textContent = '新增用户';
    if (name) name.value = '';
    if (account) account.value = '';
    if (phone) phone.value = '';
    if (dept) dept.value = '设计部';
    if (role) role.value = '普通用户';
    if (status) status.value = 'enabled';
    if (remark) remark.value = '';
  }

  openAdminModal('user-form-modal');
}

function submitUserForm() {
  var name = document.getElementById('user-form-name').value.trim();
  var account = document.getElementById('user-form-account').value.trim();
  var phone = document.getElementById('user-form-phone').value.trim();
  var dept = document.getElementById('user-form-dept').value;
  var role = document.getElementById('user-form-role').value;
  var status = document.getElementById('user-form-status').value;
  var remark = document.getElementById('user-form-remark').value.trim();

  if (!name) { adminToast('请输入用户姓名'); return; }
  if (!account) { adminToast('请输入登录账号'); return; }
  if (!phone) { adminToast('请输入手机号'); return; }

  var duplicated = userListData.some(function(u) { return u.account === account && u.id !== userState.editingId; });
  if (duplicated) { adminToast('登录账号已存在'); return; }

  if (userState.editingId) {
    var user = userListData.find(function(u) { return u.id === userState.editingId; });
    if (user) {
      user.name = name; user.account = account; user.phone = phone;
      user.dept = dept; user.role = role; user.status = status; user.remark = remark;
    }
    adminToast('用户已更新');
  } else {
    userListData.unshift({
      id: Date.now(), name: name, account: account, phone: phone,
      dept: dept, role: role, status: status, online: false,
      lastLogin: '-', remark: remark
    });
    adminToast('用户已新增');
  }

  closeAdminModal('user-form-modal');
  renderUsersStats();
  renderUsersTable();
}

function openUserRoleModal(id) {
  userState.roleUserId = id;
  var user = userListData.find(function(u) { return u.id === id; });
  if (!user) return;

  setAdminText('user-role-title', user.name + ' - 分配角色');
  var select = document.getElementById('user-role-select');
  if (select) select.value = user.role;

  openAdminModal('user-role-modal');
}

function submitUserRole() {
  var user = userListData.find(function(u) { return u.id === userState.roleUserId; });
  if (!user) { adminToast('未找到用户'); return; }

  var select = document.getElementById('user-role-select');
  user.role = select ? select.value : user.role;

  closeAdminModal('user-role-modal');
  renderUsersTable();
  adminToast('角色已更新');
}

async function resetUserPassword(id) {
  var user = userListData.find(function(u) { return u.id === id; });
  if (!user) return;

  var ok = await adminConfirm('确定重置用户「' + user.name + '」的密码吗？', {
    title: '重置密码', type: 'warning', icon: 'key-round'
  });

  if (!ok) return;
  adminToast('密码已重置为默认密码');
}

async function toggleUserStatus(id) {
  var user = userListData.find(function(u) { return u.id === id; });
  if (!user) return;

  var next = user.status === 'enabled' ? 'disabled' : 'enabled';
  var actionText = next === 'enabled' ? '启用' : '禁用';

  var ok = await adminConfirm('确定要' + actionText + '用户「' + user.name + '」吗？', {
    title: actionText + '用户', type: next === 'enabled' ? 'warning' : 'danger',
    icon: next === 'enabled' ? 'play-circle' : 'pause-circle'
  });

  if (!ok) return;

  user.status = next;
  renderUsersStats();
  renderUsersTable();
  adminToast('用户已' + actionText);
}

async function deleteUser(id) {
  var user = userListData.find(function(u) { return u.id === id; });
  if (!user) return;

  var ok = await adminConfirm('确定删除用户「' + user.name + '」吗？删除后不可恢复。', {
    title: '删除用户', type: 'danger', icon: 'trash-2'
  });

  if (!ok) return;

  userListData = userListData.filter(function(u) { return u.id !== id; });
  userState.selectedIds.delete(id);

  renderUsersStats();
  renderUsersTable();
  adminToast('用户已删除');
}