// ============================================================
// gallery.js - 择样图库模块
// 依赖: lucide (全局图标库), openTab (全局路由函数), appAlert/appConfirm/appToast (全局弹窗)
// 从 拍照管理系统 - 主界面 (1).html 提取 (2026-05-17)
// ============================================================

var galleryDetailStore = {};
var currentGalleryDetailSize = 'small';

function getGalleryDetailPageId(code) {
  return 'gallery-detail-' + encodeURIComponent(code);
}

function isGalleryDetailPage(page) {
  return page && page.indexOf('gallery-detail-') === 0;
}

function getGalleryHTML() {
  return `
    <div class="gallery-page">
      <!-- 数据表单区 -->
      <div class="gallery-card gallery-form-card">
        <div class="gallery-form-top">
          <div class="gallery-section-title">
            <div class="gallery-section-icon">
              <i data-lucide="file-text"></i>
            </div>
            <div>
              <strong>数据表单区</strong>
              <span>择样代号 / 客户信息</span>
            </div>
          </div>

          <div class="gallery-form-actions">
            <span class="gallery-mode-pill">新增模式</span>
            <button class="gallery-btn gallery-btn-ghost" id="gallery-form-reset-btn">
              <i data-lucide="rotate-ccw"></i>
              重置
            </button>
            <button class="gallery-btn gallery-btn-primary" id="gallery-form-save-btn">
              <i data-lucide="save"></i>
              保存
            </button>
          </div>
        </div>

        <div class="gallery-form-grid">
          <div class="gallery-form-field">
            <label class="gallery-form-label">择样日期 <span class="text-red-400">*</span></label>
            <input type="datetime-local" class="gallery-form-input" />
          </div>

          <div class="gallery-form-field">
            <label class="gallery-form-label">本次代号 <span class="text-red-400">*</span></label>
            <input type="text" placeholder="请输入代号" class="gallery-form-input" />
          </div>

          <div class="gallery-form-field">
            <label class="gallery-form-label">客户名称 <span class="text-red-400">*</span></label>
            <input type="text" placeholder="请输入客户名称" class="gallery-form-input" />
          </div>

          <div class="gallery-form-field">
            <label class="gallery-form-label">拍摄人员</label>
            <input type="text" placeholder="请输入拍摄人员" class="gallery-form-input" />
          </div>

          <div class="gallery-form-field full">
            <label class="gallery-form-label">备注</label>
            <textarea placeholder="请输入备注信息" class="gallery-form-textarea"></textarea>
          </div>
        </div>
      </div>

      <!-- 查询 / 操作区 -->
      <div class="gallery-card gallery-toolbar-card">
        <div class="gallery-toolbar-row search-row">
          <div class="gallery-search">
            <i data-lucide="search"></i>
            <input type="text" placeholder="模糊搜索..." />
          </div>

          <input type="date" class="gallery-date-input" />
          <span class="gallery-date-separator">至</span>
          <input type="date" class="gallery-date-input" />

          <button class="gallery-btn gallery-btn-primary">
            <i data-lucide="search"></i>
            查询
          </button>

          <div></div>
        </div>

        <div class="gallery-toolbar-row action-row">
          <button class="gallery-btn gallery-btn-primary">
            <i data-lucide="plus"></i>
            添加代号
          </button>

          <button class="gallery-btn gallery-btn-danger">
            <i data-lucide="trash-2"></i>
            删除代号
          </button>

          <button class="gallery-btn gallery-btn-ghost">
            <i data-lucide="download"></i>
            导出列表
          </button>
        </div>
      </div>

      <!-- 表格区 -->
      <div class="gallery-card gallery-table-card">
        <div class="gallery-table-wrap">
          <table class="gallery-data-table">
            <thead>
              <tr>
                <th><input type="checkbox" id="gallery-check-all" /></th>
                <th>ID</th>
                <th>择样日期</th>
                <th>代号</th>
                <th>客户名称</th>
                <th>拍摄人员</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>

            <tbody id="gallery-tbody">
              ${galleryRow(1, '2024-03-15 09:30', 'A001', 'ABC 贸易公司', '张三', false)}
              ${galleryRow(2, '2024-03-14 14:00', 'B002', 'XYZ 进出口', '李四', true)}
              ${galleryRow(3, '2024-03-13 11:20', 'C003', '阳光实业', '王五', false)}
              ${galleryRow(4, '2024-03-12 10:10', 'D004', '蓝海贸易', '赵六', false)}
              ${galleryRow(5, '2024-03-11 16:40', 'E005', '星河采购', '钱七', true)}
              ${galleryRow(6, '2024-03-10 09:15', 'F006', '东方家居', '孙八', false)}
            </tbody>
          </table>
        </div>

        <div class="gallery-statusbar">
          <button class="gallery-btn gallery-btn-ghost" id="gallery-select-all-btn">
            <i data-lucide="check-square"></i>
            全选
          </button>

          <button class="gallery-btn gallery-btn-ghost" id="gallery-invert-btn">
            <i data-lucide="shuffle"></i>
            反选
          </button>

          <button class="gallery-btn gallery-btn-ghost" id="gallery-clear-btn">
            <i data-lucide="square"></i>
            清除
          </button>

          <div class="gallery-status-info">
            已选择 <strong id="gallery-selected-count">0</strong> 条 / 共 <strong>6</strong> 条记录
          </div>

          <select class="gallery-page-size">
            <option>10条</option>
            <option>20条</option>
            <option>50条</option>
            <option selected>1000条</option>
          </select>

          <button class="gallery-btn gallery-btn-ghost">上一页</button>
          <span class="sample-page-text">第 1 / 1 页</span>
          <button class="gallery-btn gallery-btn-ghost">下一页</button>
        </div>
      </div>
    </div>
  `;
}

function galleryRow(id, date, code, customer, photographer, encrypted) {
  var isEncrypted = !!encrypted;

  return `
    <tr
      data-code="${code}"
      data-customer="${customer}"
      data-photographer="${photographer}"
      data-encrypted="${isEncrypted ? '1' : '0'}"
    >
      <td><input type="checkbox" /></td>
      <td>${id}</td>
      <td>${date}</td>
      <td>
        <a href="javascript:void(0)" class="gallery-code-link" onclick="event.stopPropagation(); enterGalleryDetail('${code}', '${customer}', '${photographer}')">
          ${code}
        </a>
      </td>
      <td>${customer}</td>
      <td>${photographer}</td>
      <td>
        ${
          isEncrypted
            ? '<span class="gallery-encrypt-badge locked">已加密</span>'
            : '<span class="gallery-encrypt-badge open">未加密</span>'
        }
      </td>
      <td>
        <div class="gallery-row-actions">
          <button class="gallery-row-btn" onclick="event.stopPropagation(); enterGalleryDetail('${code}', '${customer}', '${photographer}')">
            <i data-lucide="eye"></i>
            详情
          </button>

          <button class="gallery-row-btn" onclick="event.stopPropagation();">
            <i data-lucide="pencil"></i>
            编辑
          </button>

          <button class="gallery-row-btn danger" onclick="event.stopPropagation();">
            <i data-lucide="trash-2"></i>
            删除
          </button>

          ${
            isEncrypted
              ? `
                <button class="gallery-row-btn unlock" onclick="event.stopPropagation(); toggleGalleryEncrypt(this);">
                  <i data-lucide="unlock"></i>
                  解密
                </button>
              `
              : `
                <button class="gallery-row-btn lock" onclick="event.stopPropagation(); toggleGalleryEncrypt(this);">
                  <i data-lucide="lock"></i>
                  加密
                </button>
              `
          }
        </div>
      </td>
    </tr>
  `;
}

function toggleGalleryEncrypt(btn) {
  var row = btn.closest('tr');
  if (!row) return;

  var encrypted = row.dataset.encrypted === '1';
  var nextEncrypted = !encrypted;

  row.dataset.encrypted = nextEncrypted ? '1' : '0';

  var statusCell = row.children[6];
  var actionCell = row.children[7];

  if (!statusCell || !actionCell) return;

  statusCell.innerHTML = nextEncrypted
    ? '<span class="gallery-encrypt-badge locked">已加密</span>'
    : '<span class="gallery-encrypt-badge open">未加密</span>';

  btn.classList.toggle('lock', !nextEncrypted);
  btn.classList.toggle('unlock', nextEncrypted);

  btn.innerHTML = nextEncrypted
    ? '<i data-lucide="unlock"></i> 解密'
    : '<i data-lucide="lock"></i> 加密';

  lucide.createIcons();
}

function initGalleryPage() {
  var tbody = document.getElementById('gallery-tbody');
  var checkAll = document.getElementById('gallery-check-all');
  var selectedCount = document.getElementById('gallery-selected-count');

  var btnSelectAll = document.getElementById('gallery-select-all-btn');
  var btnInvert = document.getElementById('gallery-invert-btn');
  var btnClear = document.getElementById('gallery-clear-btn');

  function updateSelectedCount() {
    if (!tbody || !selectedCount) return;

    var count = tbody.querySelectorAll('tr.selected').length;
    selectedCount.textContent = count;

    if (checkAll) {
      var total = tbody.querySelectorAll('tr').length;
      checkAll.checked = count === total && total > 0;
      checkAll.indeterminate = count > 0 && count < total;
    }
  }

  function setRowSelected(row, selected) {
    row.classList.toggle('selected', selected);

    var checkbox = row.querySelector('input[type="checkbox"]');
    if (checkbox) {
      checkbox.checked = selected;
    }
  }

  if (tbody) {
    tbody.querySelectorAll('tr').forEach(function(row) {
      row.addEventListener('click', function(e) {
        if (e.target.closest('button')) return;
        if (e.target.closest('a')) return;

        var code = row.dataset.code;
        var customer = row.dataset.customer;
        var photographer = row.dataset.photographer;

        enterGalleryDetail(code, customer, photographer);
      });

      var checkbox = row.querySelector('input[type="checkbox"]');

      if (checkbox) {
        checkbox.addEventListener('click', function(e) {
          e.stopPropagation();
          setRowSelected(row, checkbox.checked);
          updateSelectedCount();
        });
      }
    });
  }

  if (checkAll && tbody) {
    checkAll.addEventListener('change', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, checkAll.checked);
      });

      updateSelectedCount();
    });
  }

  if (btnSelectAll && tbody) {
    btnSelectAll.addEventListener('click', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, true);
      });

      updateSelectedCount();
    });
  }

  if (btnInvert && tbody) {
    btnInvert.addEventListener('click', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, !row.classList.contains('selected'));
      });

      updateSelectedCount();
    });
  }

  if (btnClear && tbody) {
    btnClear.addEventListener('click', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, false);
      });

      updateSelectedCount();
    });
  }
}

function enterGalleryDetail(code, customer, photographer) {
  var page = getGalleryDetailPageId(code);

  galleryDetailStore[page] = {
    code: code,
    customer: customer || '未知客户',
    photographer: photographer || '未知拍摄人'
  };

  openTab(
    page,
    '择样详情-' + code,
    'images'
  );
}

function backToGalleryList() {
  openTab('gallery', '择样图库', 'image');
}

function getGalleryDetailHTML(code, customer, photographer) {
  return `
    <div class="gallery-detail-page">
      <!-- 详情工具条 -->
      <div class="gallery-card gallery-detail-top">
        <div class="gallery-detail-left">
          <button class="gallery-btn gallery-btn-ghost" onclick="backToGalleryList()">
            <i data-lucide="arrow-left"></i>
            返回列表
          </button>

          <div class="gallery-detail-context">
            <span class="gallery-detail-chip">
              <i data-lucide="hash"></i>
              ${code}
            </span>
            <span class="gallery-detail-chip">
              <i data-lucide="building-2"></i>
              ${customer}
            </span>
            <span class="gallery-detail-chip">
              <i data-lucide="camera"></i>
              ${photographer}
            </span>
          </div>
        </div>

        <div class="gallery-detail-right">
          <button class="gallery-btn gallery-btn-ghost" id="d-size-s">
            <i data-lucide="square"></i>
            小图
          </button>

          <button class="gallery-btn gallery-btn-ghost" id="d-size-m">
            <i data-lucide="layout-grid"></i>
            中图
          </button>

          <button class="gallery-btn gallery-btn-ghost" id="d-size-l">
            <i data-lucide="maximize-2"></i>
            大图
          </button>

          <button class="gallery-btn gallery-btn-primary">
            <i data-lucide="archive"></i>
            导出图片
          </button>

          <button class="gallery-btn gallery-btn-primary">
            <i data-lucide="file-spreadsheet"></i>
            导出表格
          </button>

          <button class="gallery-btn gallery-btn-ghost">
            <i data-lucide="file-up"></i>
            按编号查询排序
          </button>
        </div>
      </div>

      <div class="gallery-detail-main">
        <!-- 基本信息 -->
        <div class="gallery-info-grid">
          ${galleryInfoCard('calendar', '择样日期', '2024-03-15 09:30:00')}
          ${galleryInfoCard('hash', '本次代号', code)}
          ${galleryInfoCard('building-2', '客户名称', customer)}
          ${galleryInfoCard('camera', '拍摄人员', photographer)}
          ${galleryInfoCard('clock', '创建时间', '2024-03-15 09:30:00')}
          ${galleryInfoCard('refresh-cw', '修改时间', '2024-03-15 09:30:00')}
        </div>

        <!-- 统计 -->
        <div class="gallery-stat-grid">
          <div class="gallery-stat-card">
            <div class="gallery-stat-icon green">
              <i data-lucide="download"></i>
            </div>
            <div class="gallery-stat-text">
              <span>已导出数据</span>
              <strong>12</strong>
            </div>
          </div>

          <div class="gallery-stat-card">
            <div class="gallery-stat-icon orange">
              <i data-lucide="circle-alert"></i>
            </div>
            <div class="gallery-stat-text">
              <span>未导出数据</span>
              <strong>8</strong>
            </div>
          </div>
        </div>

        <!-- 图片资料 -->
        <div class="gallery-card gallery-image-table-card">
          <div class="gallery-image-table-head">
            <h3>
              <i data-lucide="image" class="w-4 h-4 text-blue-500"></i>
              图片资料 <span class="text-slate-400 font-semibold">(<span id="d-img-count">18</span> 张)</span>
            </h3>

            <button class="gallery-btn gallery-btn-primary">
              <i data-lucide="upload"></i>
              上传图片
            </button>
          </div>

          <div class="gallery-detail-scroll">
            <table class="gallery-image-table">
              <thead>
                <tr>
                  <th style="width:50px;"><input type="checkbox" /></th>
                  <th style="width:70px;">序号</th>
                  <th style="width:110px;">公司编号</th>
                  <th style="width:130px;">出厂货号</th>
                  <th style="width:980px;">图片区</th>
                  <th style="width:90px;">拍摄人</th>
                  <th style="width:90px;">修改人</th>
                  <th style="width:160px;">上传时间</th>
                  <th style="width:160px;">修改时间</th>
                  <th class="gallery-sticky-action" style="width:130px;">操作</th>
                </tr>
              </thead>

              <tbody>
                ${galleryImageRow(1, 'CMP001', 'FAC20240101', photographer)}
                ${galleryImageRow(2, 'CMP002', 'FAC20240102', '李四')}
                ${galleryImageRow(3, 'CMP003', 'FAC20240103', '王五')}
              </tbody>
            </table>
          </div>

          <div class="gallery-detail-footer">
            <button class="gallery-btn gallery-btn-ghost">
              <i data-lucide="check-square"></i>
              全选
            </button>

            <button class="gallery-btn gallery-btn-ghost">
              <i data-lucide="shuffle"></i>
              反选
            </button>

            <button class="gallery-btn gallery-btn-ghost">
              <i data-lucide="square"></i>
              清除
            </button>

            <div class="gallery-status-info">
              当前共 <strong>3</strong> 条图片记录，图片总数 <strong id="d-total-count">18</strong> 张
            </div>

            <select class="gallery-page-size">
              <option>10条</option>
              <option selected>20条</option>
              <option>50条</option>
              <option>100条</option>
            </select>

            <button class="gallery-btn gallery-btn-ghost">上一页</button>
            <span class="sample-page-text">第 1 / 1 页</span>
            <button class="gallery-btn gallery-btn-ghost">下一页</button>
          </div>
        </div>
      </div>
    </div>
  `;
}

function galleryInfoCard(icon, label, value) {
  return `
    <div class="gallery-info-card">
      <div class="gallery-info-label">
        <i data-lucide="${icon}"></i>
        ${label}
      </div>
      <div class="gallery-info-value">${value}</div>
    </div>
  `;
}

function galleryImageRow(id, company, factory, user) {
  return `
    <tr>
      <td><input type="checkbox" /></td>
      <td>${id}</td>
      <td>${company}</td>
      <td>${factory}</td>
      <td class="gallery-img-strip" id="gallery-img-row-${id}"></td>
      <td>${user}</td>
      <td>${user}</td>
      <td>2024-03-15 09:30:00</td>
      <td>2024-03-15 09:30:00</td>
      <td class="gallery-sticky-action">
        <button class="sample-table-action">
          编辑
        </button>
        <button class="sample-table-action" style="color:#ff3b30;">
          删除
        </button>
      </td>
    </tr>
  `;
}

function renderGalleryDetailImages() {
  var small = currentGalleryDetailSize === 'small';
  var medium = currentGalleryDetailSize === 'medium';
  var large = currentGalleryDetailSize === 'large';

  var size = large ? 128 : medium ? 82 : 60;

  var colors = [
    '#007aff',
    '#34c759',
    '#ff9500',
    '#ff3b30',
    '#af52de',
    '#5856d6',
    '#ff2d55',
    '#00c7be'
  ];

  for (var row = 1; row <= 3; row++) {
    var cell = document.getElementById('gallery-img-row-' + row);
    if (!cell) continue;

    var html = '';

    for (var j = 1; j <= 6; j++) {
      var label = 'FAC2024010' + row + '-' + j;
      var bgColor = colors[(row + j) % colors.length];

      html += `
        <div class="gallery-thumb-item" onclick="alert('图片放大预览')">
          <div class="gallery-thumb-box" style="width:${size}px;height:${size}px;background:linear-gradient(135deg, ${bgColor}cc, ${bgColor}66);">
            <svg width="${Math.max(size / 3, 20)}" height="${Math.max(size / 3, 20)}" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
              <rect width="18" height="18" x="3" y="3" rx="2" ry="2"></rect>
              <circle cx="9" cy="9" r="2"></circle>
              <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"></path>
            </svg>
          </div>
          <div class="gallery-thumb-label">${label}</div>
        </div>
      `;
    }

    cell.innerHTML = html;
  }

  var countEl = document.getElementById('d-img-count');
  var totalEl = document.getElementById('d-total-count');

  if (countEl) countEl.textContent = 18;
  if (totalEl) totalEl.textContent = 18;

  var btnS = document.getElementById('d-size-s');
  var btnM = document.getElementById('d-size-m');
  var btnL = document.getElementById('d-size-l');

  [btnS, btnM, btnL].forEach(function(btn) {
    if (btn) btn.classList.remove('gallery-btn-primary');
    if (btn) btn.classList.add('gallery-btn-ghost');
  });

  var activeBtn = currentGalleryDetailSize === 'large'
    ? btnL
    : currentGalleryDetailSize === 'medium'
      ? btnM
      : btnS;

  if (activeBtn) {
    activeBtn.classList.remove('gallery-btn-ghost');
    activeBtn.classList.add('gallery-btn-primary');
  }
}

function initGalleryDetailPage(page) {
  var btnS = document.getElementById('d-size-s');
  var btnM = document.getElementById('d-size-m');
  var btnL = document.getElementById('d-size-l');

  if (btnS) {
    btnS.addEventListener('click', function() {
      currentGalleryDetailSize = 'small';
      renderGalleryDetailImages();
    });
  }

  if (btnM) {
    btnM.addEventListener('click', function() {
      currentGalleryDetailSize = 'medium';
      renderGalleryDetailImages();
    });
  }

  if (btnL) {
    btnL.addEventListener('click', function() {
      currentGalleryDetailSize = 'large';
      renderGalleryDetailImages();
    });
  }
}
