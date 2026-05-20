    function getLogsHTML() {
      return `
    <div class="admin-page">
      <div class="admin-stats">
        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon blue"><i data-lucide="calendar-days"></i></div>
          <div class="admin-stat-info">
            <span>今日日志</span>
            <strong id="log-stat-today">0</strong>
          </div>
        </div>

        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon green"><i data-lucide="log-in"></i></div>
          <div class="admin-stat-info">
            <span>登录日志</span>
            <strong id="log-stat-login">0</strong>
          </div>
        </div>

        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon purple"><i data-lucide="mouse-pointer-click"></i></div>
          <div class="admin-stat-info">
            <span>操作日志</span>
            <strong id="log-stat-operate">0</strong>
          </div>
        </div>

        <div class="admin-card admin-stat-card">
          <div class="admin-stat-icon red"><i data-lucide="bug"></i></div>
          <div class="admin-stat-info">
            <span>异常日志</span>
            <strong id="log-stat-error">0</strong>
          </div>
        </div>
      </div>

      <div class="admin-card admin-toolbar">
        <div class="admin-search">
          <i data-lucide="search"></i>
          <input id="log-search-input" placeholder="搜索操作人、模块、IP..." />
        </div>

        <select class="admin-select" id="log-type-filter">
          <option value="all">全部类型</option>
          <option value="login">登录日志</option>
          <option value="operate">操作日志</option>
          <option value="error">异常日志</option>
        </select>

        <select class="admin-select" id="log-status-filter">
          <option value="all">全部状态</option>
          <option value="success">成功</option>
          <option value="failed">失败</option>
        </select>

        <button class="admin-btn admin-btn-ghost" id="log-btn-query">
          <i data-lucide="search"></i>
          查询
        </button>

        <div></div>

        <button class="admin-btn admin-btn-danger" id="log-btn-clear">
          <i data-lucide="trash-2"></i>
          清空日志
        </button>

        <button class="admin-btn admin-btn-primary" id="log-btn-export">
          <i data-lucide="download"></i>
          导出日志
        </button>
      </div>

      <div class="admin-card admin-table-card">
        <div class="admin-table-head">
          <div class="admin-table-title">
            <strong>日志列表</strong>
            <span id="log-table-count">共 0 条</span>
          </div>

          <button class="admin-btn admin-btn-ghost" id="log-btn-refresh">
            <i data-lucide="refresh-cw"></i>
            刷新
          </button>
        </div>

        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>操作人</th>
                <th>日志类型</th>
                <th>操作模块</th>
                <th>操作内容</th>
                <th>IP 地址</th>
                <th>状态</th>
                <th>操作时间</th>
                <th style="width:150px;">操作</th>
              </tr>
            </thead>
            <tbody id="logs-table-body"></tbody>
          </table>
        </div>
      </div>

      ${getLogModalsHTML()}
    </div>
  `;
    }

    function getLogModalsHTML() {
      return `
    <div class="admin-modal-mask" id="log-detail-modal">
      <div class="admin-modal large">
        <div class="admin-modal-head">
          <strong>日志详情</strong>
          <button class="admin-row-btn" data-close-admin-modal="log-detail-modal">
            <i data-lucide="x"></i>
          </button>
        </div>

        <div class="admin-modal-body">
          <div class="admin-detail-list" id="log-detail-content"></div>
        </div>

        <div class="admin-modal-foot">
          <button class="admin-btn admin-btn-ghost" data-close-admin-modal="log-detail-modal">关闭</button>
        </div>
      </div>
    </div>
  `;
    }

    var logState = {
      search: '',
      type: 'all',
      status: 'all'
    };

    var logListData = [
      { id: 1, user: 'Alex', type: 'login', module: '登录认证', content: '用户登录系统', ip: '192.168.1.24', status: 'success', time: '2025-02-15 10:24:18', detail: '账号 alex 登录成功，浏览器 Chrome，系统 macOS。' },
      { id: 2, user: 'Bella', type: 'operate', module: '择样图库', content: '上传图片资料', ip: '192.168.1.35', status: 'success', time: '2025-02-15 10:02:31', detail: '上传 6 张图片至图库，分类：春夏样衣。' },
      { id: 3, user: 'Chris', type: 'operate', module: '样品资料', content: '编辑样品信息', ip: '192.168.1.41', status: 'success', time: '2025-02-15 09:48:05', detail: '修改样品编号 SP-2025-001 的颜色、尺码信息。' },
      { id: 4, user: 'Diana', type: 'error', module: '角色管理', content: '权限保存失败', ip: '192.168.1.52', status: 'failed', time: '2025-02-15 09:12:44', detail: '保存权限时接口返回 500，错误信息：Permission service timeout。' },
      { id: 5, user: 'Eric', type: 'login', module: '登录认证', content: '密码错误', ip: '192.168.1.63', status: 'failed', time: '2025-02-14 18:26:10', detail: '账号 eric 登录失败，原因：密码错误。' }
    ];

    function initLogsPage() {
      renderLogsStats();
      renderLogsTable();
      bindLogEvents();
    }

    function getFilteredLogs() {
      var keyword = logState.search.toLowerCase();

      return logListData.filter(function(log) {
        var matchKeyword =
          !keyword ||
          log.user.toLowerCase().indexOf(keyword) > -1 ||
          log.module.toLowerCase().indexOf(keyword) > -1 ||
          log.ip.indexOf(keyword) > -1;

        var matchType = logState.type === 'all' || log.type === logState.type;
        var matchStatus = logState.status === 'all' || log.status === logState.status;

        return matchKeyword && matchType && matchStatus;
      });
    }

    function renderLogsStats() {
      setAdminText('log-stat-today', logListData.filter(function(l) {
        return l.time.indexOf('2025-02-15') === 0;
      }).length);

      setAdminText('log-stat-login', logListData.filter(function(l) { return l.type === 'login'; }).length);
      setAdminText('log-stat-operate', logListData.filter(function(l) { return l.type === 'operate'; }).length);
      setAdminText('log-stat-error', logListData.filter(function(l) { return l.type === 'error'; }).length);
    }

    function renderLogsTable() {
      var body = document.getElementById('logs-table-body');
      var count = document.getElementById('log-table-count');
      if (!body) return;

      var data = getFilteredLogs();
      if (count) count.textContent = '共 ' + data.length + ' 条';

      if (!data.length) {
        body.innerHTML = '<tr><td colspan="8"><div class="admin-empty">暂无匹配日志</div></td></tr>';
        return;
      }

      body.innerHTML = data.map(function(log) {
        return `
      <tr>
        <td>${escapeAdminHTML(log.user)}</td>
        <td>${renderLogType(log.type)}</td>
        <td>${escapeAdminHTML(log.module)}</td>
        <td>${escapeAdminHTML(log.content)}</td>
        <td>${escapeAdminHTML(log.ip)}</td>
        <td>${renderLogStatus(log.status)}</td>
        <td>${escapeAdminHTML(log.time)}</td>
        <td>
          <div class="admin-row-actions">
            <button class="admin-row-btn" data-log-detail="${log.id}">
              <i data-lucide="eye"></i>详情
            </button>
            <button class="admin-row-btn danger" data-log-delete="${log.id}">
              <i data-lucide="trash-2"></i>删除
            </button>
          </div>
        </td>
      </tr>
    `;
      }).join('');

      bindLogTableEvents();
      lucide.createIcons();
    }

    function renderLogType(type) {
      if (type === 'login') return '<span class="admin-badge green">登录日志</span>';
      if (type === 'operate') return '<span class="admin-badge blue">操作日志</span>';
      return '<span class="admin-badge red">异常日志</span>';
    }

    function renderLogStatus(status) {
      return status === 'success'
        ? '<span class="admin-badge green">成功</span>'
        : '<span class="admin-badge red">失败</span>';
    }

    function bindLogEvents() {
      var search = document.getElementById('log-search-input');
      if (search) {
        search.addEventListener('input', function() {
          logState.search = this.value.trim();
          renderLogsTable();
        });
      }

      var type = document.getElementById('log-type-filter');
      if (type) {
        type.addEventListener('change', function() {
          logState.type = this.value;
          renderLogsTable();
        });
      }

      var status = document.getElementById('log-status-filter');
      if (status) {
        status.addEventListener('change', function() {
          logState.status = this.value;
          renderLogsTable();
        });
      }

      bindAdminClick('log-btn-query', function() {
        renderLogsTable();
        adminToast('查询完成');
      });

      bindAdminClick('log-btn-refresh', function() {
        logState.search = '';
        logState.type = 'all';
        logState.status = 'all';

        var search = document.getElementById('log-search-input');
        var type = document.getElementById('log-type-filter');
        var status = document.getElementById('log-status-filter');

        if (search) search.value = '';
        if (type) type.value = 'all';
        if (status) status.value = 'all';

        renderLogsStats();
        renderLogsTable();
        adminToast('已刷新');
      });

      bindAdminClick('log-btn-clear', async function() {
        var ok = await adminConfirm('确定清空全部系统日志吗？', {
          title: '清空日志',
          type: 'danger',
          icon: 'trash-2'
        });

        if (!ok) return;

        logListData = [];
        renderLogsStats();
        renderLogsTable();
        adminToast('日志已清空');
      });

      bindAdminClick('log-btn-export', exportLogs);

      bindAdminModalClose();
    }

    function bindLogTableEvents() {
      var body = document.getElementById('logs-table-body');
      if (!body) return;

      body.onclick = function(e) {
        var detailBtn = e.target.closest('[data-log-detail]');
        if (detailBtn) return openLogDetail(Number(detailBtn.dataset.logDetail));

        var deleteBtn = e.target.closest('[data-log-delete]');
        if (deleteBtn) return deleteLog(Number(deleteBtn.dataset.logDelete));
      };
    }

    function openLogDetail(id) {
      var log = logListData.find(function(l) { return l.id === id; });
      if (!log) return;

      var box = document.getElementById('log-detail-content');
      if (!box) return;

      box.innerHTML = `
    ${renderAdminDetail('操作人', log.user)}
    ${renderAdminDetail('日志类型', log.type === 'login' ? '登录日志' : log.type === 'operate' ? '操作日志' : '异常日志')}
    ${renderAdminDetail('操作模块', log.module)}
    ${renderAdminDetail('操作内容', log.content)}
    ${renderAdminDetail('IP 地址', log.ip)}
    ${renderAdminDetail('状态', log.status === 'success' ? '成功' : '失败')}
    ${renderAdminDetail('操作时间', log.time)}
    ${renderAdminDetail('详细信息', log.detail)}
  `;

      openAdminModal('log-detail-modal');
    }

    async function deleteLog(id) {
      var log = logListData.find(function(l) { return l.id === id; });
      if (!log) return;

      var ok = await adminConfirm('确定删除这条日志吗？', {
        title: '删除日志',
        type: 'danger',
        icon: 'trash-2'
      });

      if (!ok) return;

      logListData = logListData.filter(function(l) { return l.id !== id; });

      renderLogsStats();
      renderLogsTable();
      adminToast('日志已删除');
    }

    function exportLogs() {
      var data = getFilteredLogs();

      if (!data.length) {
        adminToast('暂无日志可导出');
        return;
      }

      var text = data.map(function(log) {
        return [
          log.time,
          log.user,
          log.type,
          log.module,
          log.content,
          log.ip,
          log.status,
          log.detail
        ].join(' | ');
      }).join('\n');

      var blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
      var url = URL.createObjectURL(blob);

      var a = document.createElement('a');
      a.href = url;
      a.download = '系统日志.txt';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      URL.revokeObjectURL(url);

      adminToast('日志已导出');
    }
