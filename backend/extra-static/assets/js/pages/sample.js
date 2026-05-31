// ============================================================
// sample.js - 样品资料模块
// 依赖: lucide (全局图标库)
// 从 拍照管理系统 - 主界面 (1).html 提取 (2026-05-17)
// ============================================================

function getSampleHTML() {
  return `
    <div class="sample-page">
      <!-- 表单区 -->
      <div class="sample-card sample-form-card" id="sample-form-card">
        <div class="sample-form-top">
          <div class="sample-form-title">
            <div class="sample-form-title-icon">
              <i data-lucide="file-text" class="w-4 h-4"></i>
            </div>
            <div>
              <strong>数据表单区</strong>
              <span>主数据 / 详情录入</span>
            </div>
          </div>

          <div class="sample-form-actions">
            <span class="sample-mode-pill">只读模式</span>

            <button class="sample-btn sample-btn-ghost" id="btn-show-more-fields">
              <i data-lucide="chevron-down"></i>
              <span>显示更多字段</span>
            </button>

            <button class="sample-btn sample-btn-ghost" id="btn-settings">
              <i data-lucide="sliders-horizontal"></i>
              <span>字段设置</span>
            </button>

            <button class="sample-btn sample-btn-ghost" id="btn-reset">
              <i data-lucide="rotate-ccw"></i>
              <span>重置</span>
            </button>

            <button class="sample-btn sample-btn-danger" id="btn-cancel">
              <i data-lucide="x"></i>
              <span>取消</span>
            </button>

            <button class="sample-btn sample-btn-primary" id="btn-save">
              <i data-lucide="save"></i>
              <span>保存</span>
            </button>
          </div>
        </div>

        <div class="sample-field-settings" id="field-settings-panel" style="display:none;">
          <div class="field-settings-header">
            <span class="field-settings-title">字段显示设置</span>
            <button class="field-settings-close" id="btn-close-field-settings">
              <i data-lucide="x" class="w-4 h-4"></i>
            </button>
          </div>
          <div class="field-settings-grid" id="field-settings-grid"></div>
        </div>

        <div class="sample-form-scroll">
          <div class="sample-form-grid">
            ${sampleField('公司编号', '请输入公司编号')}
            ${sampleField('出厂货号', '请输入出厂货号')}
            ${sampleField('厂商编号', '请输入厂商编号', true)}
            ${sampleField('种类编号', '请输入种类编号', true)}
            ${sampleField('种类名称', '请输入种类名称', true)}
            ${sampleField('样品名称', '请输入样品名称', true)}
            ${sampleField('英文名称', '请输入英文名称', true)}
            ${sampleField('包装方式', '请输入包装方式', true)}
            ${sampleField('包装类型', '请输入包装类型', true)}
            ${sampleField('出厂价', '请输入出厂价', true)}
            ${sampleField('样品单位', '请输入样品单位', true)}
            ${sampleField('颜色', '请输入颜色', true)}
            ${sampleField('价格', '请输入价格', true)}
            ${sampleField('备注', '请输入备注', true)}
            ${sampleField('厂商名称', '请输入厂商名称', true)}
            ${sampleField('联系人', '请输入联系人', true)}
            ${sampleField('电话', '请输入电话', true)}
            ${sampleField('登记人', '请输入登记人', true)}
            ${sampleField('登记日期', '', true, 'date')}
          </div>
        </div>
      </div>

      <!-- 查询 / 操作区 -->
      <div class="sample-card sample-toolbar-card">
        <div class="sample-toolbar-row">
          <div class="sample-search">
            <i data-lucide="search"></i>
            <input type="text" placeholder="模糊搜索..." />
          </div>

          <button class="sample-btn sample-btn-primary">
            <i data-lucide="search"></i>
            查询
          </button>

          <div class="sample-search">
            <i data-lucide="crosshair"></i>
            <input type="text" placeholder="定位搜索..." />
          </div>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="locate"></i>
            定位
          </button>

          <span class="toolbar-sep"></span>

          <button class="sample-btn sample-btn-primary">
            <i data-lucide="plus"></i>
            添加
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-edit">
            <i data-lucide="pencil"></i>
            修改
          </button>

          <button class="sample-btn sample-btn-danger">
            <i data-lucide="trash-2"></i>
            删除
          </button>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="upload"></i>
            导入资料
          </button>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="file-spreadsheet"></i>
            导出模板
          </button>

          <button class="sample-btn sample-btn-primary">
            <i data-lucide="copy"></i>
            框选复制
          </button>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="sliders-horizontal"></i>
            综合查询
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-column-manager">
            <i data-lucide="columns"></i>
            列管理
          </button>
        </div>
      </div>

      <!-- 表格区 -->
      <div class="sample-card sample-table-card">
        <div class="sample-table-wrap">
          <div id="sample-grid" class="ag-theme-alpine sample-ag-grid"></div>
        </div>

        <div class="sample-statusbar">
          <button class="sample-btn sample-btn-ghost" id="btn-select-all-bottom">
            <i data-lucide="check-square"></i>
            全选
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-invert-select">
            <i data-lucide="shuffle"></i>
            反选
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-clear-select">
            <i data-lucide="square"></i>
            清除
          </button>

          <div class="sample-status-info">
            已选择 <strong id="sample-selected-count">0</strong> 条 / 共 <strong id="sample-total-count">0</strong> 条记录
          </div>

          <div class="sample-pagination">
            <button class="sample-btn sample-btn-ghost">上一页</button>
            <span class="sample-page-text">第 1 / 1 页</span>
            <button class="sample-btn sample-btn-ghost">下一页</button>
          </div>
        </div>
      </div>
    </div>
  `;
}

function sampleField(label, placeholder, extra, type) {
  return `
        <div class="sample-form-field ${extra ? 'sample-form-extra' : ''}" ${extra ? 'style="display:none;"' : ''} data-label="${label}">
          <label class="sample-form-label">${label}</label>
          <input type="${type || 'text'}" placeholder="${placeholder || ''}" class="sample-form-input" readonly />
        </div>
      `;
}


function initSamplePage() {
  var formCard = document.getElementById('sample-form-card');
  var showMoreBtn = document.getElementById('btn-show-more-fields');
  var extraFields = document.querySelectorAll('.sample-form-extra');
  var showingExtra = false;

  if (showMoreBtn) {
    showMoreBtn.addEventListener('click', function() {
      showingExtra = !showingExtra;
      if (formCard) formCard.classList.toggle('expanded', showingExtra);
      extraFields.forEach(function(f) { f.style.display = showingExtra ? '' : 'none'; });

      if (settingsGrid) {
        settingsGrid.querySelectorAll('input[type="checkbox"]').forEach(function(cb, i) {
          cb.checked = i < 2 || showingExtra;
        });
      }

      showMoreBtn.innerHTML = showingExtra
        ? '<i data-lucide="chevron-up"></i><span>收起字段</span>'
        : '<i data-lucide="chevron-down"></i><span>显示更多字段</span>';
      lucide.createIcons({ nodes: [showMoreBtn] });
    });
  }

  var settingsBtn = document.getElementById('btn-settings');
  var settingsPanel = document.getElementById('field-settings-panel');
  var settingsGrid = document.getElementById('field-settings-grid');
  var closeSettingsBtn = document.getElementById('btn-close-field-settings');

  if (settingsBtn && settingsPanel && settingsGrid) {
    var allFields = document.querySelectorAll('.sample-form-field');
    allFields.forEach(function(field) {
      var label = field.getAttribute('data-label');
      if (!label) return;

      var checkbox = document.createElement('label');
      checkbox.className = 'field-settings-chip';
      checkbox.innerHTML = '<input type="checkbox" checked /><span>' + label + '</span>';
      settingsGrid.appendChild(checkbox);

      var cb = checkbox.querySelector('input');
      cb.addEventListener('change', function() {
        field.style.display = cb.checked ? '' : 'none';
      });
    });

    settingsBtn.addEventListener('click', function() {
      settingsPanel.style.display = settingsPanel.style.display === 'none' ? '' : 'none';
    });
  }

  if (closeSettingsBtn && settingsPanel) {
    closeSettingsBtn.addEventListener('click', function() { settingsPanel.style.display = 'none'; });
  }

  var editMode = false;
  var allInputs = document.querySelectorAll('.sample-form-input');
  var modePill = document.querySelector('.sample-mode-pill');
  var btnEdit = document.getElementById('btn-edit');
  var btnCancel = document.getElementById('btn-cancel');
  var btnReset = document.getElementById('btn-reset');
  var btnSave = document.getElementById('btn-save');

  function setEditMode(on) {
    editMode = on;
    allInputs.forEach(function(input) { input.readOnly = !on; });
    if (modePill) {
      modePill.textContent = on ? '编辑模式' : '只读模式';
      modePill.style.background = on ? 'rgba(255,149,0,0.12)' : '';
      modePill.style.color = on ? '#ff9500' : '';
    }
    if (btnCancel) btnCancel.style.setProperty('display', on ? 'inline-flex' : 'none', 'important');
    if (btnReset) btnReset.style.setProperty('display', on ? 'inline-flex' : 'none', 'important');
    if (btnSave) btnSave.style.setProperty('display', on ? 'inline-flex' : 'none', 'important');
    if (showMoreBtn) showMoreBtn.style.setProperty('display', on ? 'none' : 'inline-flex', 'important');
    if (settingsBtn) settingsBtn.style.setProperty('display', on ? 'none' : 'inline-flex', 'important');
    if (btnEdit) btnEdit.style.setProperty('display', on ? 'none' : 'inline-flex', 'important');
  }

  if (btnEdit) {
    btnEdit.addEventListener('click', function() { setEditMode(true); });
  }

  if (btnCancel) {
    btnCancel.addEventListener('click', function() {
      allInputs.forEach(function(input) { input.value = ''; });
      setEditMode(false);
    });
  }

  if (btnReset) {
    btnReset.addEventListener('click', function() {
      allInputs.forEach(function(input) { input.value = ''; });
    });
  }

  if (btnSave) {
    btnSave.addEventListener('click', function() {
      setEditMode(false);
    });
  }

  setEditMode(false);

  var selectedCountEl = document.getElementById('sample-selected-count');
  var totalCountEl = document.getElementById('sample-total-count');
  var gridDiv = document.getElementById('sample-grid');
  var sampleGridApi = null;
  var PAGE_SIZE = 5000;

  if (gridDiv) {
    var columnDefs = [
      { headerCheckboxSelection: true, checkboxSelection: true, width: 50, pinned: 'left' },
      { field: 'id', headerName: '序号', width: 70, pinned: 'left' },
      { field: 'manufacturerCode', headerName: '厂商编号', width: 120 },
      { field: 'sampleCode', headerName: '公司编号', width: 120 },
      { field: 'category', headerName: '种类名称', width: 120 },
      { field: 'sampleName', headerName: '样品名称', width: 120 },
      { field: 'englishName', headerName: '英文名称', width: 120 },
      { field: 'factoryCode', headerName: '出厂货号', width: 120 },
      { field: 'sampleUnit', headerName: '样品单位', width: 120 },
      { field: 'sampleUnitEn', headerName: '样品英文单位', width: 120, hide: true },
      { field: 'packagingCn', headerName: '中文包装', width: 120, hide: true },
      { field: 'packagingEn', headerName: '英文包装', width: 120, hide: true },
      { field: 'factoryPrice', headerName: '出厂价', width: 120 },
      { field: 'taxPrice', headerName: '税点价', width: 120, hide: true },
      { field: 'sampleLength', headerName: '样品长度', width: 120, hide: true },
      { field: 'sampleWidth', headerName: '样品宽度', width: 120, hide: true },
      { field: 'sampleHeight', headerName: '样品高度', width: 120, hide: true },
      { field: 'sampleGrossWeight', headerName: '样品毛重', width: 120, hide: true },
      { field: 'sampleNetWeight', headerName: '样品净重', width: 120, hide: true },
      { field: 'cartonLength', headerName: '外箱长度', width: 120, hide: true },
      { field: 'cartonWidth', headerName: '外箱宽度', width: 120, hide: true },
      { field: 'cartonHeight', headerName: '外箱高度', width: 120, hide: true },
      { field: 'cartonMaterialVolume', headerName: '外箱材积', width: 120, hide: true },
      { field: 'cartonVolume', headerName: '外箱体积', width: 120, hide: true },
      { field: 'innerBoxCount', headerName: '内盒个数', width: 120, hide: true },
      { field: 'cartonCapacity', headerName: '外箱装量', width: 120, hide: true },
      { field: 'packingUnit', headerName: '装箱单位', width: 120, hide: true },
      { field: 'cartonGrossWeight', headerName: '外箱毛重', width: 120, hide: true },
      { field: 'cartonNetWeight', headerName: '外箱净重', width: 120, hide: true },
      { field: 'packageLength', headerName: '包装长度', width: 120, hide: true },
      { field: 'packageWidth', headerName: '包装宽度', width: 120, hide: true },
      { field: 'packageHeight', headerName: '包装高度', width: 120, hide: true },
      { field: 'certification', headerName: '产品认证', width: 120, hide: true },
      { field: 'certificationCount', headerName: '认证总数', width: 120, hide: true },
      { field: 'color', headerName: '颜色', width: 120 },
      { field: 'colorEn', headerName: '英文颜色', width: 120, hide: true },
      { field: 'remark', headerName: '备注', width: 120 },
      { field: 'remarkEn', headerName: '英文备注', width: 120, hide: true },
      { field: 'supplier', headerName: '厂商名称', width: 120 },
      { field: 'boothNo', headerName: '摊位号', width: 120, hide: true },
      { field: 'contactPerson', headerName: '联系人', width: 120 },
      { field: 'contactPhone', headerName: '电话', width: 120, hide: true },
      { field: 'mobile', headerName: '手机', width: 120, hide: true },
      { field: 'fax', headerName: '传真', width: 120, hide: true },
      { field: 'qq', headerName: 'QQ', width: 120, hide: true },
      { field: 'registrant', headerName: '登记人', width: 120, hide: true },
      { field: 'createTime', headerName: '登记日期', width: 120, valueFormatter: function(p) { return p.value ? p.value.substring(0, 10) : ''; } },
      { field: 'modifier', headerName: '修改人', width: 120, hide: true },
      { field: 'updateTime', headerName: '修改日期', width: 120, hide: true, valueFormatter: function(p) { return p.value ? p.value.substring(0, 10) : ''; } },
      { field: 'infringement', headerName: '侵权', width: 120, hide: true },
      { field: 'batteryInfo', headerName: '电池信息', width: 120, hide: true },
      { headerName: '操作', width: 88, pinned: 'right', cellRenderer: function() { return '<button class="sample-table-action">编辑</button>'; } }
    ];

    var dataSource = {
      getRows: function(params) {
        var current = Math.floor(params.startRow / PAGE_SIZE) + 1;
        var url = '/samples?current=' + current + '&size=' + PAGE_SIZE;

        var sortModel = params.sortModel;
        if (sortModel && sortModel.length > 0) {
          url += '&sortField=' + encodeURIComponent(sortModel[0].colId) + '&sortOrder=' + sortModel[0].sort;
        }

        fetch(url, { headers: authHeader() })
          .then(function(r) { return r.json(); })
          .then(function(res) {
            if (res.code === 200 && res.data) {
              var total = res.data.total || 0;
              var lastRow = total !== undefined ? total : -1;
              if (totalCountEl) totalCountEl.textContent = total;
              params.successCallback(res.data.records || [], lastRow);
            } else {
              params.failCallback();
            }
          })
          .catch(function() {
            params.failCallback();
          });
      }
    };

    var gridOptions = {
      columnDefs: columnDefs,
      rowModelType: 'infinite',
      serverSideSorting: true,
      datasource: dataSource,
      cacheBlockSize: PAGE_SIZE,
      maxBlocksInCache: 3,
      maxConcurrentDatasourceRequests: 1,
      rowHeight: 42,
      headerHeight: 42,
      rowSelection: 'multiple',
      suppressRowClickSelection: true,
      suppressMovableColumns: false,
      defaultColDef: { sortable: true, resizable: true, suppressSizeToFit: true },
      onSelectionChanged: function() {
        if (selectedCountEl && sampleGridApi) selectedCountEl.textContent = sampleGridApi.getSelectedRows().length;
      },
      onGridReady: function(params) {
        sampleGridApi = params.api;
      }
    };

    new agGrid.Grid(gridDiv, gridOptions);
  }

  var btnSelectAllBottom = document.getElementById('btn-select-all-bottom');
  var btnInvertSelect = document.getElementById('btn-invert-select');
  var btnClearSelect = document.getElementById('btn-clear-select');

  if (btnSelectAllBottom) {
    btnSelectAllBottom.addEventListener('click', function() {
      if (!sampleGridApi) return;
      sampleGridApi.selectAll('filtered');
    });
  }

  if (btnInvertSelect) {
    btnInvertSelect.addEventListener('click', function() {
      if (!sampleGridApi) return;
      var selected = sampleGridApi.getSelectedNodes().map(function(n) { return n.id; });
      var selectedSet = {};
      selected.forEach(function(id) { selectedSet[id] = true; });
      sampleGridApi.forEachNode(function(node) {
        node.setSelected(!selectedSet[node.id], false, true);
      });
    });
  }

  if (btnClearSelect) {
    btnClearSelect.addEventListener('click', function() {
      if (!sampleGridApi) return;
      sampleGridApi.deselectAll();
    });
  }

  var btnColManager = document.getElementById('btn-column-manager');
  var colPanel = null;
  var colPanelVisible = false;

  function repositionPanel() {
    if (!colPanel || !colPanelVisible) return;
    var gridRect = gridDiv.getBoundingClientRect();
    colPanel.style.height = gridRect.height + 'px';
    colPanel.style.top = gridRect.top + 'px';
    colPanel.style.right = (window.innerWidth - gridRect.right) + 'px';
  }

  if (btnColManager) {
    btnColManager.addEventListener('click', function() {
      if (!sampleGridApi) return;
      if (!colPanel) {
        colPanel = createColumnPanel();
        document.body.appendChild(colPanel);
      }
      colPanelVisible = !colPanelVisible;
      if (colPanelVisible) {
        repositionPanel();
        colPanel.style.display = 'block';
      } else {
        colPanel.style.display = 'none';
      }
    });
  }

  window.addEventListener('resize', repositionPanel);

  if (selectedCountEl) {
    selectedCountEl.textContent = '0';
  }

  function createColumnPanel() {
    var div = document.createElement('div');
    div.id = 'sample-col-panel';
    div.style.cssText = 'position:fixed;z-index:9999;width:260px;overflow:hidden;display:flex;flex-direction:column;background:#fff;border-radius:16px;box-shadow:0 20px 50px rgba(0,0,0,0.18);padding:16px;display:none;font-size:13px;font-family:inherit;';

    var hdr = document.createElement('div');
    hdr.style.cssText = 'display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;padding-bottom:10px;border-bottom:1px solid rgba(0,122,255,0.10);';
    hdr.innerHTML = '<strong style="font-size:14px;color:#1d1d1f;">列显示管理</strong>';

    var closeBtn = document.createElement('span');
    closeBtn.style.cssText = 'cursor:pointer;font-size:18px;color:rgba(29,29,31,0.4);line-height:1;';
    closeBtn.textContent = '\u00D7';
    closeBtn.onclick = function() { div.style.display = 'none'; colPanelVisible = false; };
    hdr.appendChild(closeBtn);
    div.appendChild(hdr);

    var list = document.createElement('div');
    list.className = 'sample-col-list';
    list.style.cssText = 'flex:1;overflow-y:scroll;min-height:0;padding-right:4px;';

    columnDefs.forEach(function(col) {
       if (!col.field) return;
       (function(colCaptured) {
         var row = document.createElement('label');
         row.style.cssText = 'display:flex;align-items:center;gap:8px;padding:5px 4px;cursor:pointer;border-radius:8px;transition:background 0.12s;';
         row.onmouseenter = function() { row.style.background = 'rgba(0,122,255,0.04)'; };
         row.onmouseleave = function() { row.style.background = ''; };

         var cb = document.createElement('input');
         cb.type = 'checkbox';
         cb.checked = !colCaptured.hide;
         cb.style.cssText = 'accent-color:#007aff;width:15px;height:15px;flex-shrink:0;';

         var label = document.createElement('span');
         label.textContent = colCaptured.headerName || colCaptured.field;
         row.appendChild(cb);
         row.appendChild(label);

         row.onclick = function(e) {
           if (e.target === cb) return;
           cb.checked = !cb.checked;
           var api = sampleGridApi;
           if (api) api.setColumnVisible(colCaptured.field, cb.checked);
         };
         cb.onchange = function() {
           var api = sampleGridApi;
           if (api) api.setColumnVisible(colCaptured.field, cb.checked);
         };

         list.appendChild(row);
       })(col);
     });

    div.appendChild(list);

    var btnRow = document.createElement('div');
    btnRow.style.cssText = 'display:flex;gap:8px;margin-top:12px;padding-top:10px;border-top:1px solid rgba(0,122,255,0.10);';

    var showAll = document.createElement('button');
    showAll.textContent = '全显示';
    showAll.className = 'sample-btn sample-btn-ghost';
    showAll.style.cssText = 'height:30px!important;min-height:30px!important;font-size:12px;padding:0 12px;flex:1;';
    showAll.onclick = function() {
      columnDefs.forEach(function(col) {
        if (!col.field) return;
        var api = sampleGridApi;
        if (api) api.setColumnVisible(col.field, true);
      });
      updatePanelCheckboxes(true);
    };
    btnRow.appendChild(showAll);

    var hideAll = document.createElement('button');
    hideAll.textContent = '全隐藏';
    hideAll.className = 'sample-btn sample-btn-ghost';
    hideAll.style.cssText = 'height:30px!important;min-height:30px!important;font-size:12px;padding:0 12px;flex:1;';
    hideAll.onclick = function() {
      columnDefs.forEach(function(col) {
        if (!col.field) return;
        var api = sampleGridApi;
        if (api) api.setColumnVisible(col.field, false);
      });
      updatePanelCheckboxes(false);
    };
    btnRow.appendChild(hideAll);

    div.appendChild(btnRow);

    function updatePanelCheckboxes(checked) {
      div.querySelectorAll('input[type="checkbox"]').forEach(function(cbInner) {
        cbInner.checked = checked;
      });
    }

    return div;
  }
}
