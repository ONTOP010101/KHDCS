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
            <input type="text" placeholder="模糊搜索..." id="search-input" />
          </div>

          <button class="sample-btn sample-btn-primary" id="btn-search">
            <i data-lucide="search"></i>
            查询
          </button>

          <div class="sample-search">
            <i data-lucide="crosshair"></i>
            <input type="text" placeholder="定位搜索..." id="locate-input" />
          </div>

          <button class="sample-btn sample-btn-ghost" id="btn-locate">
            <i data-lucide="locate"></i>
            定位
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-clear-search" style="display:none;">
            <i data-lucide="x"></i>
            清除
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

          <button class="sample-btn sample-btn-ghost" id="btn-import">
            <i data-lucide="upload"></i>
            导入资料
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-export-template">
            <i data-lucide="file-spreadsheet"></i>
            导出模板
          </button>

          <button class="sample-btn sample-btn-primary">
            <i data-lucide="copy"></i>
            框选复制
          </button>

          <button class="sample-btn sample-btn-ghost" id="btn-advanced-search">
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

          <div class="sample-row-select-box">
            <span class="sample-row-select-label">按行勾选</span>
            <input type="text" placeholder="例: 1-5,8,10-15" id="row-select-input" />
            <button class="sample-btn sample-btn-ghost" id="btn-row-select">确定</button>
          </div>

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
  var searchKeyword = '';
  var locateRowId = null;

  if (gridDiv) {
    var columnDefs = [
      { checkboxSelection: true, width: 50, pinned: 'left' },
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
      { field: 'factoryPrice', headerName: '出厂价', width: 120, headerClass: 'sample-col-red-header', cellRenderer: function(p) { return '<span style="color:#ff3b30;font-weight:600;">' + (p.value !== null && p.value !== undefined ? p.value : '') + '</span>'; } },
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

        var sortField = '';
        var sortOrder = '';
        var sortModel = params.sortModel;
        if (sortModel && sortModel.length > 0) {
          sortField = sortModel[0].colId;
          sortOrder = sortModel[0].sort;
        }

        if (advSearchActive && advSearchParams) {
          var url = '/samples/search?current=' + current + '&size=' + PAGE_SIZE;
          if (sortField) url += '&sortField=' + encodeURIComponent(sortField) + '&sortOrder=' + sortOrder;
          fetch(url, {
            method: 'POST',
            headers: Object.assign(authHeader(), { 'Content-Type': 'application/json' }),
            body: advSearchParams
          })
          .then(function(r) { return r.json(); })
          .then(function(res) {
            if (res.code === 200 && res.data) {
              var total = res.data.total || 0;
              var lastRow = total !== undefined ? total : -1;
              if (totalCountEl) totalCountEl.textContent = total;
              var matchInfo = document.getElementById('advsearch-match-info');
              if (matchInfo) { matchInfo.textContent = '匹配 ' + total + ' 条记录'; matchInfo.style.display = 'inline'; }
              params.successCallback(res.data.records || [], lastRow);
            } else {
              params.failCallback();
            }
          })
          .catch(function() { params.failCallback(); });
          return;
        }

        var url = '/samples?current=' + current + '&size=' + PAGE_SIZE;

        if (searchKeyword) {
          url += '&keyword=' + encodeURIComponent(searchKeyword);
        }

        if (sortField) {
          url += '&sortField=' + encodeURIComponent(sortField) + '&sortOrder=' + sortOrder;
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
      getRowId: function(params) { return String(params.data.id); },
      suppressRowClickSelection: true,
      suppressMovableColumns: false,
      getRowStyle: function(params) {
        if (locateRowId && params.data && locateRowId == params.data.id) {
          return { backgroundColor: 'rgba(255, 204, 0, 0.35)' };
        }
        return null;
      },
      defaultColDef: { sortable: true, resizable: true, minWidth: 80 },
      onSelectionChanged: function() {
        if (selectedCountEl && sampleGridApi) selectedCountEl.textContent = sampleGridApi.getSelectedRows().length;
      },
      onGridReady: function(params) {
        sampleGridApi = params.api;
        params.api.sizeColumnsToFit();
      },
      onFirstDataRendered: function(params) {
        params.api.sizeColumnsToFit();
      },
      onRowClicked: function(event) {
        if (!event.data) return;
        var data = event.data;
        var fieldLabelToDataField = {
          '公司编号': 'sampleCode',
          '出厂货号': 'factoryCode',
          '厂商编号': 'manufacturerCode',
          '种类编号': 'category',
          '种类名称': 'category',
          '样品名称': 'sampleName',
          '英文名称': 'englishName',
          '出厂价': 'factoryPrice',
          '样品单位': 'sampleUnit',
          '颜色': 'color',
          '备注': 'remark',
          '厂商名称': 'supplier',
          '联系人': 'contactPerson',
          '电话': 'contactPhone',
          '登记人': 'registrant',
          '登记日期': 'createTime'
        };
        var formFields = document.querySelectorAll('.sample-form-field');
        formFields.forEach(function(field) {
          var label = field.getAttribute('data-label');
          var fieldName = fieldLabelToDataField[label];
          if (fieldName) {
            var input = field.querySelector('.sample-form-input');
            if (input) {
              var val = data[fieldName];
              if (val === null || val === undefined) val = '';
              if (fieldName === 'createTime' && val && val.length >= 10) {
                val = val.substring(0, 10);
              }
              input.value = val;
            }
          }
        });
      }
    };

    new agGrid.Grid(gridDiv, gridOptions);
  }

  var btnSelectAllBottom = document.getElementById('btn-select-all-bottom');
  var btnInvertSelect = document.getElementById('btn-invert-select');
  var btnClearSelect = document.getElementById('btn-clear-select');
  var rowSelectInput = document.getElementById('row-select-input');
  var btnRowSelect = document.getElementById('btn-row-select');

  if (btnSelectAllBottom) {
    btnSelectAllBottom.addEventListener('click', function() {
      if (!sampleGridApi) return;
      var total = parseInt(totalCountEl.textContent) || 0;
      if (total === 0) return;
      sampleGridApi.deselectAll();
      try {
        sampleGridApi.selectAll();
      } catch (e) {
        sampleGridApi.forEachNode(function(node) { node.setSelected(true); });
      }
      if (selectedCountEl) {
        var selected = sampleGridApi.getSelectedNodes().length;
        selectedCountEl.textContent = Math.min(selected, total);
      }
    });
  }

  if (btnInvertSelect) {
    btnInvertSelect.addEventListener('click', function() {
      if (!sampleGridApi) return;
      var total = parseInt(totalCountEl.textContent) || 0;
      if (total === 0) return;
      sampleGridApi.forEachNode(function(node) {
        node.setSelected(!node.isSelected());
      });
      if (selectedCountEl) {
        var count = sampleGridApi.getSelectedNodes().length;
        selectedCountEl.textContent = Math.min(count, total);
      }
    });
  }

  if (btnClearSelect) {
    btnClearSelect.addEventListener('click', function() {
      if (!sampleGridApi) return;
      sampleGridApi.deselectAll();
      if (selectedCountEl) selectedCountEl.textContent = '0';
    });
  }

  if (btnRowSelect && rowSelectInput) {
    btnRowSelect.addEventListener('click', doRowSelect);
  }
  if (rowSelectInput) {
    rowSelectInput.addEventListener('keydown', function(e) {
      if (e.key === 'Enter') doRowSelect();
    });
  }

  function doRowSelect() {
    if (!sampleGridApi) return;
    if (!rowSelectInput) return;
    var val = rowSelectInput.value.trim();
    if (!val) return;

    var total = parseInt(totalCountEl.textContent) || 0;
    if (total === 0) return;

    var ranges = parseRowRanges(val, total);
    if (ranges.length === 0) {
      alert('请输入正确的行号，如: 1-5,8,10-15 或 1,3,5');
      return;
    }

    sampleGridApi.deselectAll();

    sampleGridApi.forEachNode(function(node) {
      if (node.rowIndex !== null && node.rowIndex !== undefined) {
        var rowNum = node.rowIndex + 1;
        if (isInRanges(rowNum, ranges)) {
          node.setSelected(true);
        }
      }
    });

    if (selectedCountEl) {
      var count = sampleGridApi.getSelectedNodes().length;
      selectedCountEl.textContent = Math.min(count, total);
    }

    function parseRowRanges(input, maxRow) {
      var result = [];
      var parts = input.split(',').map(function(s) { return s.trim(); }).filter(function(s) { return s.length > 0; });
      for (var i = 0; i < parts.length; i++) {
        var part = parts[i];
        if (/^\d+$/.test(part)) {
          var n = parseInt(part);
          if (n >= 1 && n <= maxRow) result.push([n, n]);
        } else if (/^(\d+)\s*-\s*(\d+)$/.test(part)) {
          var match = part.match(/^(\d+)\s*-\s*(\d+)$/);
          var start = parseInt(match[1]);
          var end = parseInt(match[2]);
          if (start <= end && start >= 1 && end <= maxRow) {
            result.push([start, end]);
          }
        }
      }
      return result;
    }

    function isInRanges(rowNum, ranges) {
      for (var i = 0; i < ranges.length; i++) {
        if (rowNum >= ranges[i][0] && rowNum <= ranges[i][1]) return true;
      }
      return false;
    }
  }

  var btnColManager = document.getElementById('btn-column-manager');
  var colPanel = null;
  var colPanelVisible = false;

  function repositionPanel() {
    if (!colPanel || !colPanelVisible) return;
    var gridRect = gridDiv.getBoundingClientRect();
    colPanel.style.height = (gridRect.height - 10) + 'px';
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
        colPanel.style.display = 'flex';
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
    div.style.cssText = 'position:fixed;z-index:9999;width:260px;overflow:hidden;display:none;flex-direction:column;background:#fff;border-radius:16px;box-shadow:0 20px 50px rgba(0,0,0,0.18);padding:16px;font-size:13px;font-family:inherit;';

    var hdr = document.createElement('div');
    hdr.style.cssText = 'display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;padding-bottom:10px;border-bottom:1px solid rgba(0,122,255,0.10);flex-shrink:0;';
    hdr.innerHTML = '<strong style="font-size:14px;color:#1d1d1f;">列显示管理</strong>';

    var closeBtn = document.createElement('span');
    closeBtn.style.cssText = 'cursor:pointer;font-size:18px;color:rgba(29,29,31,0.4);line-height:1;';
    closeBtn.textContent = '\u00D7';
    closeBtn.onclick = function() { div.style.display = 'none'; colPanelVisible = false; };
    hdr.appendChild(closeBtn);
    div.appendChild(hdr);

    var list = document.createElement('div');
    list.className = 'sample-col-list';
    list.style.cssText = 'flex:1 1 auto;overflow-y:auto;min-height:100px;padding-right:8px;';

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
    btnRow.style.cssText = 'display:flex;gap:8px;margin-top:12px;padding-top:10px;border-top:1px solid rgba(0,122,255,0.10);flex-shrink:0;';

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

  var importModal = null;
  var importResultData = null;

  function createImportModal() {
    var overlay = document.createElement('div');
    overlay.id = 'sample-import-modal';
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.35);z-index:10000;display:none;align-items:center;justify-content:center;backdrop-filter:blur(4px);';

    var box = document.createElement('div');
    box.style.cssText = 'width:560px;max-height:80vh;background:#fff;border-radius:20px;box-shadow:0 24px 60px rgba(0,0,0,0.22);display:flex;flex-direction:column;overflow:hidden;';

    var header = document.createElement('div');
    header.style.cssText = 'display:flex;justify-content:space-between;align-items:center;padding:20px 24px 16px;border-bottom:1px solid rgba(0,122,255,0.10);flex-shrink:0;';
    header.innerHTML = '<strong style="font-size:16px;color:#1d1d1f;">导入资料</strong>';

    var closeBtn = document.createElement('span');
    closeBtn.style.cssText = 'cursor:pointer;font-size:20px;color:rgba(29,29,31,0.4);line-height:1;';
    closeBtn.textContent = '\u00D7';
    closeBtn.onclick = function() { overlay.style.display = 'none'; };
    header.appendChild(closeBtn);
    box.appendChild(header);

    var body = document.createElement('div');
    body.style.cssText = 'flex:1;overflow-y:auto;padding:20px 24px;min-height:0;';

    var uploadArea = document.createElement('div');
    uploadArea.id = 'import-upload-area';
    uploadArea.style.cssText = 'border:2px dashed rgba(0,122,255,0.20);border-radius:16px;padding:36px 20px;text-align:center;cursor:pointer;transition:all 0.2s;background:rgba(0,122,255,0.02);';
    uploadArea.innerHTML = '<div style="font-size:36px;color:rgba(0,122,255,0.35);margin-bottom:10px;">\u2607</div><div style="font-size:14px;color:rgba(29,29,31,0.55);margin-bottom:6px;">点击或拖拽上传 .xlsx 文件</div><div style="font-size:12px;color:rgba(29,29,31,0.35);">表头需与系统列名对应</div>';

    var fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = '.xlsx,.xls';
    fileInput.style.display = 'none';
    uploadArea.appendChild(fileInput);

    uploadArea.onclick = function() { fileInput.click(); };
    uploadArea.ondragover = function(e) { e.preventDefault(); uploadArea.style.borderColor = 'rgba(0,122,255,0.50)'; uploadArea.style.background = 'rgba(0,122,255,0.06)'; };
    uploadArea.ondragleave = function() { uploadArea.style.borderColor = 'rgba(0,122,255,0.20)'; uploadArea.style.background = 'rgba(0,122,255,0.02)'; };
    uploadArea.ondrop = function(e) { e.preventDefault(); uploadArea.style.borderColor = 'rgba(0,122,255,0.20)'; uploadArea.style.background = 'rgba(0,122,255,0.02)'; if (e.dataTransfer.files.length) { fileInput.files = e.dataTransfer.files; handleFileSelect(); } };

    body.appendChild(uploadArea);

    var fileInfo = document.createElement('div');
    fileInfo.id = 'import-file-info';
    fileInfo.style.cssText = 'display:none;margin-top:12px;padding:10px 14px;border-radius:12px;background:rgba(0,122,255,0.05);font-size:13px;color:#1d1d1f;';
    body.appendChild(fileInfo);

    var progressArea = document.createElement('div');
    progressArea.id = 'import-progress';
    progressArea.style.cssText = 'display:none;margin-top:16px;text-align:center;padding:20px 0;';
    progressArea.innerHTML = '<div style="font-size:14px;color:rgba(29,29,31,0.65);">正在导入，请稍候...</div><div style="margin-top:12px;height:4px;border-radius:999px;background:rgba(0,122,255,0.10);overflow:hidden;"><div style="width:40%;height:100%;background:linear-gradient(90deg,#007aff,#5ac8fa);border-radius:999px;animation:importSlide 1.5s ease-in-out infinite;"></div></div><style>@keyframes importSlide{0%{width:10%;margin-left:0}50%{width:50%;margin-left:25%}100%{width:10%;margin-left:90%}}</style>';
    body.appendChild(progressArea);

    var resultArea = document.createElement('div');
    resultArea.id = 'import-result';
    resultArea.style.cssText = 'display:none;margin-top:16px;';
    body.appendChild(resultArea);

    box.appendChild(body);

    var footer = document.createElement('div');
    footer.id = 'import-footer';
    footer.style.cssText = 'display:none;padding:14px 24px;border-top:1px solid rgba(0,122,255,0.10);display:flex;gap:10px;justify-content:flex-end;flex-shrink:0;';

    var btnExportFail = document.createElement('button');
    btnExportFail.className = 'sample-btn sample-btn-ghost';
    btnExportFail.id = 'btn-export-fail';
    btnExportFail.innerHTML = '<i data-lucide="download"></i><span>导出失败记录</span>';
    btnExportFail.style.display = 'none';
    btnExportFail.onclick = function() { exportFailedRows(); };
    footer.appendChild(btnExportFail);

    var btnConfirm = document.createElement('button');
    btnConfirm.className = 'sample-btn sample-btn-primary';
    btnConfirm.innerHTML = '<span>确定</span>';
    btnConfirm.onclick = function() {
      overlay.style.display = 'none';
      if (sampleGridApi) sampleGridApi.refreshInfiniteCache();
    };
    footer.appendChild(btnConfirm);

    box.appendChild(footer);
    overlay.appendChild(box);

    overlay.onclick = function(e) { if (e.target === overlay) overlay.style.display = 'none'; };

    fileInput.onchange = function() { handleFileSelect(); };

    function handleFileSelect() {
      if (!fileInput.files || !fileInput.files.length) return;
      var file = fileInput.files[0];
      if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
        fileInfo.style.display = 'block';
        fileInfo.innerHTML = '<span style="color:#ff3b30;">仅支持 .xlsx 或 .xls 格式</span>';
        return;
      }

      fileInfo.style.display = 'block';
      fileInfo.innerHTML = '<span style="color:#007aff;">' + file.name + '</span> <span style="color:rgba(29,29,31,0.45);">(' + (file.size / 1024).toFixed(1) + ' KB)</span>';
      uploadArea.style.display = 'none';
      progressArea.style.display = 'block';
      resultArea.style.display = 'none';
      footer.style.display = 'none';

      var formData = new FormData();
      formData.append('file', file);

      fetch('/samples/import', {
        method: 'POST',
        headers: authHeader(),
        body: formData
      })
      .then(function(r) { return r.json(); })
      .then(function(res) {
        progressArea.style.display = 'none';
        footer.style.display = 'flex';

        if (res.code === 200 && res.data) {
          importResultData = res.data;
          renderResult(res.data);
        } else {
          resultArea.style.display = 'block';
          resultArea.innerHTML = '<div style="padding:16px;border-radius:12px;background:rgba(255,59,48,0.06);color:#ff3b30;font-size:13px;">导入失败：' + (res.message || '未知错误') + '</div>';
          btnExportFail.style.display = 'none';
        }
      })
      .catch(function(err) {
        progressArea.style.display = 'none';
        footer.style.display = 'flex';
        resultArea.style.display = 'block';
        resultArea.innerHTML = '<div style="padding:16px;border-radius:12px;background:rgba(255,59,48,0.06);color:#ff3b30;font-size:13px;">网络错误：' + err.message + '</div>';
        btnExportFail.style.display = 'none';
      });
    }

    function renderResult(data) {
      var total = data.totalCount || 0;
      var success = data.successCount || 0;
      var fail = data.failCount || 0;
      var failedRows = data.failedRows || [];

      var html = '<div style="display:flex;gap:12px;margin-bottom:16px;">';
      html += '<div style="flex:1;padding:14px;border-radius:14px;background:rgba(0,122,255,0.05);text-align:center;">';
      html += '<div style="font-size:24px;font-weight:700;color:#007aff;">' + total + '</div>';
      html += '<div style="font-size:12px;color:rgba(29,29,31,0.50);margin-top:4px;">总条数</div></div>';
      html += '<div style="flex:1;padding:14px;border-radius:14px;background:rgba(52,199,89,0.06);text-align:center;">';
      html += '<div style="font-size:24px;font-weight:700;color:#34c759;">' + success + '</div>';
      html += '<div style="font-size:12px;color:rgba(29,29,31,0.50);margin-top:4px;">导入成功</div></div>';
      html += '<div style="flex:1;padding:14px;border-radius:14px;background:rgba(255,59,48,0.06);text-align:center;">';
      html += '<div style="font-size:24px;font-weight:700;color:#ff3b30;">' + fail + '</div>';
      html += '<div style="font-size:12px;color:rgba(29,29,31,0.50);margin-top:4px;">导入失败</div></div>';
      html += '</div>';

      if (failedRows.length > 0) {
        btnExportFail.style.display = 'inline-flex';
        html += '<div style="font-size:13px;font-weight:600;color:#1d1d1f;margin-bottom:8px;">失败记录 (' + fail + ' 条)</div>';
        html += '<div style="max-height:200px;overflow-y:auto;border-radius:12px;border:1px solid rgba(0,122,255,0.10);">';

        var keys = Object.keys(failedRows[0]).filter(function(k) { return k !== 'row'; });
        html += '<table style="width:100%;border-collapse:collapse;font-size:12px;">';
        html += '<thead><tr style="background:rgba(0,122,255,0.04);">';
        html += '<th style="padding:8px 10px;text-align:left;color:rgba(29,29,31,0.55);font-weight:600;position:sticky;top:0;background:rgba(0,122,255,0.04);">行号</th>';
        html += '<th style="padding:8px 10px;text-align:left;color:rgba(29,29,31,0.55);font-weight:600;position:sticky;top:0;background:rgba(0,122,255,0.04);">失败原因</th>';
        html += '</tr></thead><tbody>';
        failedRows.forEach(function(row) {
          html += '<tr style="border-top:1px solid rgba(0,122,255,0.06);">';
          html += '<td style="padding:6px 10px;color:#1d1d1f;">第' + (row.row || '') + '行</td>';
          html += '<td style="padding:6px 10px;color:#ff3b30;">' + (row['失败原因'] || '') + '</td>';
          html += '</tr>';
        });
        html += '</tbody></table></div>';
      } else {
        btnExportFail.style.display = 'none';
      }

      resultArea.innerHTML = html;
      resultArea.style.display = 'block';
    }

    function exportFailedRows() {
      if (!importResultData || !importResultData.failedRows || importResultData.failedRows.length === 0) return;

      var rows = importResultData.failedRows;
      var allKeys = [];
      rows.forEach(function(row) {
        Object.keys(row).forEach(function(k) {
          if (allKeys.indexOf(k) === -1) allKeys.push(k);
        });
      });

      var csvContent = '\uFEFF';
      csvContent += allKeys.join(',') + '\n';
      rows.forEach(function(row) {
        var line = allKeys.map(function(k) {
          var val = row[k] || '';
          if (val.indexOf(',') >= 0 || val.indexOf('"') >= 0 || val.indexOf('\n') >= 0) {
            val = '"' + val.replace(/"/g, '""') + '"';
          }
          return val;
        }).join(',');
        csvContent += line + '\n';
      });

      var blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      var url = URL.createObjectURL(blob);
      var a = document.createElement('a');
      a.href = url;
      a.download = '导入失败记录_' + new Date().toISOString().slice(0, 10) + '.csv';
      a.click();
      URL.revokeObjectURL(url);
    }

    return overlay;
  }

  var btnImport = document.getElementById('btn-import');
  if (btnImport) {
    btnImport.addEventListener('click', function() {
      if (!importModal) {
        importModal = createImportModal();
        document.body.appendChild(importModal);
        lucide.createIcons({ nodes: [importModal] });
      }
      var uploadArea = document.getElementById('import-upload-area');
      var fileInfo = document.getElementById('import-file-info');
      var progressArea = document.getElementById('import-progress');
      var resultArea = document.getElementById('import-result');
      var footer = document.getElementById('import-footer');
      if (uploadArea) uploadArea.style.display = 'block';
      if (fileInfo) fileInfo.style.display = 'none';
      if (progressArea) progressArea.style.display = 'none';
      if (resultArea) resultArea.style.display = 'none';
      if (footer) footer.style.display = 'none';
      var fileInput = importModal.querySelector('input[type="file"]');
      if (fileInput) fileInput.value = '';
      importModal.style.display = 'flex';
    });
  }

  var btnExportTemplate = document.getElementById('btn-export-template');
  if (btnExportTemplate) {
    btnExportTemplate.addEventListener('click', function() {
      var headers = ['厂商编号','公司编号','种类编号','种类名称','样品名称','英文名称','出厂货号','样品单位','样品英文单位','中文包装','英文包装','价格','样品长度','样品宽度','样品高度','样品毛重','样品净重','外箱长度','外箱宽度','外箱高度','外箱材积','外箱体积','内盒个数','外箱装量','装箱单位','外箱毛重','外箱净重','包装长度','包装宽度','包装高度','产品认证','认证总数','颜色','英文颜色','备注','英文备注','厂商名称','摊位号','联系人','电话','手机','传真','QQ','登记人','修改人','侵权','电池信息'];
      var csvContent = '\uFEFF' + headers.join(',') + '\n';
      var blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      var url = URL.createObjectURL(blob);
      var a = document.createElement('a');
      a.href = url;
      a.download = '样品导入模板.csv';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  var advSearchModal = null;
  var advSearchActive = false;

  var advSearchFields = [
    { label: '厂商编号', field: 'manufacturerCode' },
    { label: '公司编号', field: 'sampleCode' },
    { label: '种类名称', field: 'category' },
    { label: '样品名称', field: 'sampleName' },
    { label: '英文名称', field: 'englishName' },
    { label: '出厂货号', field: 'factoryCode' },
    { label: '样品单位', field: 'sampleUnit' },
    { label: '样品英文单位', field: 'sampleUnitEn' },
    { label: '中文包装', field: 'packagingCn' },
    { label: '英文包装', field: 'packagingEn' },
    { label: '出厂价', field: 'factoryPrice' },
    { label: '税点价', field: 'taxPrice' },
    { label: '样品长度', field: 'sampleLength' },
    { label: '样品宽度', field: 'sampleWidth' },
    { label: '样品高度', field: 'sampleHeight' },
    { label: '样品毛重', field: 'sampleGrossWeight' },
    { label: '样品净重', field: 'sampleNetWeight' },
    { label: '外箱长度', field: 'cartonLength' },
    { label: '外箱宽度', field: 'cartonWidth' },
    { label: '外箱高度', field: 'cartonHeight' },
    { label: '外箱材积', field: 'cartonMaterialVolume' },
    { label: '外箱体积', field: 'cartonVolume' },
    { label: '内盒个数', field: 'innerBoxCount' },
    { label: '外箱装量', field: 'cartonCapacity' },
    { label: '装箱单位', field: 'packingUnit' },
    { label: '外箱毛重', field: 'cartonGrossWeight' },
    { label: '外箱净重', field: 'cartonNetWeight' },
    { label: '包装长度', field: 'packageLength' },
    { label: '包装宽度', field: 'packageWidth' },
    { label: '包装高度', field: 'packageHeight' },
    { label: '产品认证', field: 'certification' },
    { label: '认证总数', field: 'certificationCount' },
    { label: '颜色', field: 'color' },
    { label: '英文颜色', field: 'colorEn' },
    { label: '备注', field: 'remark' },
    { label: '英文备注', field: 'remarkEn' },
    { label: '厂商名称', field: 'supplier' },
    { label: '摊位号', field: 'boothNo' },
    { label: '联系人', field: 'contactPerson' },
    { label: '电话', field: 'contactPhone' },
    { label: '手机', field: 'mobile' },
    { label: '传真', field: 'fax' },
    { label: 'QQ', field: 'qq' },
    { label: '登记人', field: 'registrant' },
    { label: '修改人', field: 'modifier' },
    { label: '侵权', field: 'infringement' },
    { label: '电池信息', field: 'batteryInfo' }
  ];

  function createAdvSearchModal() {
    var overlay = document.createElement('div');
    overlay.id = 'sample-advsearch-modal';
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.35);z-index:10000;display:none;align-items:center;justify-content:center;backdrop-filter:blur(4px);';

    var box = document.createElement('div');
    box.style.cssText = 'width:720px;max-height:82vh;background:#fff;border-radius:20px;box-shadow:0 24px 60px rgba(0,0,0,0.22);display:flex;flex-direction:column;overflow:hidden;';

    var header = document.createElement('div');
    header.style.cssText = 'display:flex;justify-content:space-between;align-items:center;padding:20px 24px 16px;border-bottom:1px solid rgba(0,122,255,0.10);flex-shrink:0;';
    header.innerHTML = '<strong style="font-size:16px;color:#1d1d1f;">综合查询</strong><span style="font-size:12px;color:rgba(29,29,31,0.40);">多条件组合搜索（条件之间为「与」关系）</span>';

    var closeBtn = document.createElement('span');
    closeBtn.style.cssText = 'cursor:pointer;font-size:20px;color:rgba(29,29,31,0.4);line-height:1;';
    closeBtn.textContent = '\u00D7';
    closeBtn.onclick = function() { overlay.style.display = 'none'; advSearchActive = false; };
    header.appendChild(closeBtn);
    box.appendChild(header);

    var body = document.createElement('div');
    body.style.cssText = 'flex:1;overflow-y:auto;padding:16px 24px;min-height:0;display:flex;flex-wrap:wrap;gap:8px 12px;align-content:flex-start;';

    advSearchFields.forEach(function(f) {
      var wrap = document.createElement('div');
      wrap.style.cssText = 'display:flex;align-items:center;gap:6px;width:200px;flex-shrink:0;';

      var label = document.createElement('label');
      label.style.cssText = 'font-size:12px;color:rgba(29,29,31,0.55);white-space:nowrap;width:60px;text-align:right;flex-shrink:0;';
      label.textContent = f.label;
      wrap.appendChild(label);

      var input = document.createElement('input');
      input.type = 'text';
      input.placeholder = f.label;
      input.setAttribute('data-field', f.field);
      input.style.cssText = 'flex:1;height:30px;min-width:0;border:1px solid rgba(0,122,255,0.12);border-radius:8px;padding:0 8px;font-size:12px;color:#1d1d1f;background:rgba(255,255,255,0.70);outline:none;transition:border-color 0.15s;';
      wrap.appendChild(input);

      body.appendChild(wrap);
    });

    box.appendChild(body);

    var footer = document.createElement('div');
    footer.style.cssText = 'padding:14px 24px;border-top:1px solid rgba(0,122,255,0.10);display:flex;gap:10px;justify-content:space-between;align-items:center;flex-shrink:0;';

    var matchInfo = document.createElement('span');
    matchInfo.id = 'advsearch-match-info';
    matchInfo.style.cssText = 'font-size:12px;color:rgba(29,29,31,0.45);display:none;';
    footer.appendChild(matchInfo);

    var rightBtns = document.createElement('div');
    rightBtns.style.cssText = 'display:flex;gap:10px;';

    var btnReset = document.createElement('button');
    btnReset.className = 'sample-btn sample-btn-ghost';
    btnReset.innerHTML = '<span>重置</span>';
    btnReset.onclick = function() {
      body.querySelectorAll('input').forEach(function(inp) { inp.value = ''; });
      matchInfo.style.display = 'none';
    };
    rightBtns.appendChild(btnReset);

    var btnSearch = document.createElement('button');
    btnSearch.className = 'sample-btn sample-btn-primary';
    btnSearch.innerHTML = '<i data-lucide="search"></i><span>查询</span>';
    btnSearch.onclick = function() {
      var params = {};
      var hasCondition = false;
      body.querySelectorAll('input').forEach(function(inp) {
        var val = inp.value.trim();
        if (val) {
          params[inp.getAttribute('data-field')] = val;
          hasCondition = true;
        }
      });

      if (!hasCondition) return;

      advSearchActive = true;
      advSearchParams = JSON.stringify(params);
      matchInfo.style.display = 'none';
      if (sampleGridApi) sampleGridApi.refreshInfiniteCache();
    };
    rightBtns.appendChild(btnSearch);

    var btnClear = document.createElement('button');
    btnClear.className = 'sample-btn sample-btn-ghost';
    btnClear.innerHTML = '<i data-lucide="x"></i><span>清除</span>';
    btnClear.onclick = function() {
      advSearchActive = false;
      advSearchParams = null;
      body.querySelectorAll('input').forEach(function(inp) { inp.value = ''; });
      matchInfo.style.display = 'none';
      if (sampleGridApi) sampleGridApi.refreshInfiniteCache();
    };
    rightBtns.appendChild(btnClear);

    footer.appendChild(rightBtns);
    box.appendChild(footer);
    overlay.appendChild(box);

    overlay.onclick = function(e) { if (e.target === overlay) { overlay.style.display = 'none'; advSearchActive = false; } };

    return overlay;
  }

  var advSearchParams = null;

  var btnAdvSearch = document.getElementById('btn-advanced-search');
  if (btnAdvSearch) {
    btnAdvSearch.addEventListener('click', function() {
      if (!advSearchModal) {
        advSearchModal = createAdvSearchModal();
        document.body.appendChild(advSearchModal);
        lucide.createIcons({ nodes: [advSearchModal] });
      }
      advSearchModal.style.display = 'flex';
    });
  }

  var searchInput = document.getElementById('search-input');
  var btnSearch = document.getElementById('btn-search');
  var locateInput = document.getElementById('locate-input');
  var btnLocate = document.getElementById('btn-locate');
  var btnClearSearch = document.getElementById('btn-clear-search');

  function doSearch() {
    var val = searchInput ? searchInput.value.trim() : '';
    searchKeyword = val;
    locateRowId = null;
    advSearchActive = false;
    advSearchParams = null;
    if (btnClearSearch) btnClearSearch.style.display = val ? 'inline-flex' : 'none';
    if (sampleGridApi) {
      sampleGridApi.refreshInfiniteCache();
    }
  }

  function doLocate() {
    if (!sampleGridApi) return;
    var val = locateInput ? locateInput.value.trim() : '';
    if (!val) return;

    searchKeyword = val;
    if (btnClearSearch) btnClearSearch.style.display = 'inline-flex';

    fetch('/samples?keyword=' + encodeURIComponent(val) + '&current=1&size=1', { headers: authHeader() })
      .then(function(r) { return r.json(); })
      .then(function(res) {
        if (res.code === 200 && res.data && res.data.total > 0) {
          if (searchInput) searchInput.value = '';
          sampleGridApi.refreshInfiniteCache();
          sampleGridApi.ensureIndexVisible(0, 'top');
          setTimeout(function() {
            sampleGridApi.forEachNode(function(node, index) {
              if (index === 0 && node.data) {
                locateRowId = node.data.id;
                sampleGridApi.redrawRows();
                if (gridDiv) {
                  gridDiv.classList.add('sample-locate-highlight');
                  setTimeout(function() { gridDiv.classList.remove('sample-locate-highlight'); }, 2500);
                }
              }
            });
          }, 400);
        } else {
          searchKeyword = '';
          if (btnClearSearch) btnClearSearch.style.display = 'none';
          if (typeof sample_alert === 'function') {
            sample_alert('未找到匹配数据: ' + val);
          } else {
            alert('未找到匹配数据: ' + val);
          }
        }
      })
      .catch(function() {
        searchKeyword = '';
        if (btnClearSearch) btnClearSearch.style.display = 'none';
      });
  }

  function doClearSearch() {
    searchKeyword = '';
    locateRowId = null;
    advSearchActive = false;
    advSearchParams = null;
    if (searchInput) searchInput.value = '';
    if (locateInput) locateInput.value = '';
    if (btnClearSearch) btnClearSearch.style.display = 'none';
    if (sampleGridApi) {
      sampleGridApi.refreshInfiniteCache();
    }
  }

  if (btnSearch) {
    btnSearch.addEventListener('click', doSearch);
  }
  if (searchInput) {
    searchInput.addEventListener('keydown', function(e) {
      if (e.key === 'Enter') doSearch();
    });
  }
  if (btnLocate) {
    btnLocate.addEventListener('click', doLocate);
  }
  if (locateInput) {
    locateInput.addEventListener('keydown', function(e) {
      if (e.key === 'Enter') doLocate();
    });
  }
  if (btnClearSearch) {
    btnClearSearch.addEventListener('click', doClearSearch);
  }
}
