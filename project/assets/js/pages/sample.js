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

            <button class="sample-btn sample-btn-ghost" id="btn-reset" style="display:none;">
              <i data-lucide="rotate-ccw"></i>
              <span>重置</span>
            </button>

            <button class="sample-btn sample-btn-danger" id="btn-cancel" style="display:none;">
              <i data-lucide="x"></i>
              <span>取消</span>
            </button>

            <button class="sample-btn sample-btn-primary" id="btn-save" style="display:none;">
              <i data-lucide="save"></i>
              <span>保存</span>
            </button>
          </div>
        </div>

        <div class="sample-field-settings" id="field-settings-panel" style="display:none;">
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-bold text-slate-600">字段显示设置 — 可扩展为拖拽排序</span>
            <button class="text-xs text-slate-400 hover:text-blue-500" id="btn-close-field-settings">
              <i data-lucide="x" class="w-4 h-4"></i>
            </button>
          </div>
          <div class="flex flex-wrap gap-2">
            <span class="item-badge">公司编号</span>
            <span class="item-badge">出厂货号</span>
            <span class="item-badge">样品名称</span>
            <span class="item-badge">包装方式</span>
            <span class="item-badge">价格</span>
            <span class="item-badge">登记日期</span>
          </div>
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
        <div class="sample-toolbar-row search-row">
          <div class="sample-search">
            <i data-lucide="search"></i>
            <input type="text" placeholder="模糊搜索..." />
          </div>

          <button class="sample-btn sample-btn-primary">
            <i data-lucide="search"></i>
            查询
          </button>

          <span class="search-engine" title="MySQL LIKE 搜索">📋 MySQL</span>

          <div class="sample-search">
            <i data-lucide="crosshair"></i>
            <input type="text" placeholder="定位搜索..." />
          </div>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="locate"></i>
            定位
          </button>

          <div></div>
        </div>

        <div class="sample-toolbar-row action-row">
          <button class="sample-btn sample-btn-primary">
            <i data-lucide="plus"></i>
            添加
          </button>

          <button class="sample-btn sample-btn-ghost">
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

          <div class="sample-more-dropdown" id="more-dropdown">
            <button class="sample-btn sample-btn-ghost" id="btn-more-functions">
              <i data-lucide="more-horizontal"></i>
              其他功能
            </button>
          </div>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="columns"></i>
            列管理
          </button>

          <button class="sample-btn sample-btn-ghost">
            <i data-lucide="sliders-horizontal"></i>
            综合查询
          </button>
        </div>
      </div>

      <!-- 表格区 -->
      <div class="sample-card sample-table-card">
        <div class="sample-table-wrap">
          <table class="sample-data-table">
            <thead>
              <tr>
                <th><input type="checkbox" id="check-all" /></th>
                <th>ID</th>
                <th>公司编号</th>
                <th>出厂货号</th>
                <th>样品名称</th>
                <th>种类名称</th>
                <th>包装方式</th>
                <th>颜色</th>
                <th>出厂价</th>
                <th>价格</th>
                <th>登记人</th>
                <th>登记日期</th>
                <th>操作</th>
              </tr>
            </thead>

            <tbody id="sample-tbody">
              ${sampleRow(1, 'CMP001', 'FAC20240101', '陶瓷花瓶 A款', '陶瓷制品', '纸箱+泡沫', '白色', '¥28.00', '¥45.00', '张三', '2024-01-15')}
              ${sampleRow(2, 'CMP002', 'FAC20240102', '玻璃杯套装', '玻璃器皿', '气泡膜+纸盒', '透明', '¥12.50', '¥22.00', '李四', '2024-01-18')}
              ${sampleRow(3, 'CMP003', 'FAC20240103', '木质相框 M号', '木制工艺品', '独立纸盒', '原木色', '¥18.80', '¥35.00', '王五', '2024-01-22')}
              ${sampleRow(4, 'CMP004', 'FAC20240104', '不锈钢餐具组', '金属制品', 'PVC 盒装', '银色', '¥45.00', '¥88.00', '赵六', '2024-02-05')}
              ${sampleRow(5, 'CMP005', 'FAC20240105', '棉麻抱枕套', '家纺布艺', 'PE 袋', '米白', '¥8.60', '¥19.90', '钱七', '2024-02-10')}
              ${sampleRow(6, 'CMP006', 'FAC20240106', '香薰蜡烛礼盒', '香氛用品', '彩盒+丝带', '薰衣草紫', '¥25.00', '¥56.00', '孙八', '2024-02-15')}
              ${sampleRow(7, 'CMP007', 'FAC20240107', '竹编收纳篮', '竹制品', '无纺布袋', '浅棕', '¥15.30', '¥29.00', '周九', '2024-02-20')}
              ${sampleRow(8, 'CMP008', 'FAC20240108', '树脂摆件小熊', '树脂工艺品', 'EVA 内托', '棕色', '¥32.00', '¥65.00', '吴十', '2024-02-28')}
            </tbody>
          </table>
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
            已选择 <strong id="sample-selected-count">0</strong> 条 / 共 <strong>8</strong> 条记录
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
          <input type="${type || 'text'}" placeholder="${placeholder || ''}" class="sample-form-input" />
        </div>
      `;
}

function sampleRow(id, company, factory, name, category, pack, color, cost, price, user, date) {
  return `
        <tr>
          <td><input type="checkbox" /></td>
          <td>${id}</td>
          <td>${company}</td>
          <td>${factory}</td>
          <td>${name}</td>
          <td>${category}</td>
          <td>${pack}</td>
          <td>${color}</td>
          <td>${cost}</td>
          <td>${price}</td>
          <td>${user}</td>
          <td>${date}</td>
          <td>
            <button class="sample-table-action">编辑</button>
          </td>
        </tr>
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

      if (formCard) {
        formCard.classList.toggle('expanded', showingExtra);
      }

      extraFields.forEach(function(field) {
        field.style.display = showingExtra ? '' : 'none';
      });

      showMoreBtn.innerHTML = showingExtra
        ? '<i data-lucide="chevron-up"></i><span>收起字段</span>'
        : '<i data-lucide="chevron-down"></i><span>显示更多字段</span>';

      lucide.createIcons({
        nodes: [showMoreBtn]
      });
    });
  }

  var settingsBtn = document.getElementById('btn-settings');
  var settingsPanel = document.getElementById('field-settings-panel');
  var closeSettingsBtn = document.getElementById('btn-close-field-settings');

  if (settingsBtn && settingsPanel) {
    settingsBtn.addEventListener('click', function() {
      settingsPanel.style.display = settingsPanel.style.display === 'none' ? '' : 'none';
    });
  }

  if (closeSettingsBtn && settingsPanel) {
    closeSettingsBtn.addEventListener('click', function() {
      settingsPanel.style.display = 'none';
    });
  }

  var tbody = document.getElementById('sample-tbody');
  var checkAll = document.getElementById('check-all');
  var selectedCount = document.getElementById('sample-selected-count');

  var btnSelectAllBottom = document.getElementById('btn-select-all-bottom');
  var btnInvertSelect = document.getElementById('btn-invert-select');
  var btnClearSelect = document.getElementById('btn-clear-select');

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

        var selected = !row.classList.contains('selected');
        setRowSelected(row, selected);
        updateSelectedCount();
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

  if (btnSelectAllBottom && tbody) {
    btnSelectAllBottom.addEventListener('click', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, true);
      });

      updateSelectedCount();
    });
  }

  if (btnInvertSelect && tbody) {
    btnInvertSelect.addEventListener('click', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, !row.classList.contains('selected'));
      });

      updateSelectedCount();
    });
  }

  if (btnClearSelect && tbody) {
    btnClearSelect.addEventListener('click', function() {
      tbody.querySelectorAll('tr').forEach(function(row) {
        setRowSelected(row, false);
      });

      updateSelectedCount();
    });
  }

  var batchImageModal = null;
  var batchUploadFiles = [];

  function createBatchImageModal() {
    if (batchImageModal) return batchImageModal;

    var overlay = document.createElement('div');
    overlay.className = 'batch-image-modal-overlay';

    var modal = document.createElement('div');
    modal.className = 'batch-image-modal';

    modal.innerHTML =
      '<div class="batch-image-modal-header">' +
        '<strong>批量导入图片</strong>' +
        '<button class="modal-close-btn">&times;</button>' +
      '</div>' +
      '<div class="batch-image-modal-body">' +
        '<div class="upload-type-group">' +
          '<span class="upload-type-label">选择上传方式</span>' +
          '<div class="radio-group">' +
            '<label class="radio-item"><input type="radio" name="upload-type" value="company-code"> <span>公司编号</span></label>' +
            '<label class="radio-item"><input type="radio" name="upload-type" value="factory-code" checked> <span>出厂货号</span></label>' +
          '</div>' +
        '</div>' +

        '<div class="upload-area" id="batch-upload-area">' +
          '<div class="upload-icon">☁️</div>' +
          '<div class="upload-text">点击此处上传文件，可多选</div>' +
          '<div class="upload-hint">支持 png, jpg, jpeg, gif 格式，大于 1MB 自动压缩存储</div>' +
          '<input type="file" id="batch-file-input" accept=".png,.jpg,.jpeg,.gif" multiple style="display:none;" />' +
        '</div>' +

        '<div class="file-list" id="batch-file-list">' +
          '<div class="file-list-header">' +
            '<span>已选择文件</span>' +
            '<span class="file-count" id="batch-file-count">0 个文件</span>' +
          '</div>' +
          '<div class="file-list-items" id="batch-file-items"></div>' +
        '</div>' +

        '<div class="upload-type-group" style="margin-top:20px;">' +
          '<span class="image-mode-label">图片处理方式</span>' +
          '<div class="radio-group">' +
            '<label class="radio-item"><input type="radio" name="image-mode" value="cover"> <span>图片覆盖</span></label>' +
            '<label class="radio-item"><input type="radio" name="image-mode" value="append"> <span>图片追加</span></label>' +
            '<label class="radio-item"><input type="radio" name="image-mode" value="match" checked> <span>图片对应</span></label>' +
          '</div>' +
        '</div>' +
      '</div>' +
      '<div class="modal-footer">' +
        '<button class="sample-btn sample-btn-ghost" id="btn-batch-clear">清空</button>' +
        '<button class="sample-btn sample-btn-primary" id="btn-batch-confirm">确认导入</button>' +
        '<button class="sample-btn sample-btn-ghost" id="btn-batch-export-fail">导出失败图片</button>' +
      '</div>';

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    var uploadArea = modal.querySelector('#batch-upload-area');
    var fileInput = modal.querySelector('#batch-file-input');
    var fileList = modal.querySelector('#batch-file-list');
    var fileItems = modal.querySelector('#batch-file-items');
    var fileCount = modal.querySelector('#batch-file-count');

    uploadArea.onclick = function(e) {
      if (e.target === fileInput || e.target.closest('.file-item')) return;
      fileInput.click();
    };

    uploadArea.ondragover = function(e) {
      e.preventDefault();
      e.stopPropagation();
      uploadArea.classList.add('drag-over');
    };

    uploadArea.ondragleave = function(e) {
      e.preventDefault();
      e.stopPropagation();
      uploadArea.classList.remove('drag-over');
    };

    uploadArea.ondrop = function(e) {
      e.preventDefault();
      e.stopPropagation();
      uploadArea.classList.remove('drag-over');
      handleFiles(e.dataTransfer.files);
    };

    fileInput.onchange = function() {
      if (fileInput.files && fileInput.files.length) {
        handleFiles(fileInput.files);
      }
    };

    function handleFiles(files) {
      var validFiles = [];
      for (var i = 0; i < files.length; i++) {
        var file = files[i];
        var ext = file.name.split('.').pop().toLowerCase();
        if (['png', 'jpg', 'jpeg', 'gif'].indexOf(ext) !== -1) {
            validFiles.push(file);
        }
      }

      if (validFiles.length > 0) {
        batchUploadFiles = batchUploadFiles.concat(validFiles);
        renderFileList();
      } else if (files.length > 0) {
        alert('没有有效的图片文件（仅支持 png, jpg, jpeg, gif 格式）');
      }
    }

    function renderFileList() {
      if (batchUploadFiles.length === 0) {
        fileList.classList.remove('show');
        return;
      }

      fileList.classList.add('show');
      fileCount.textContent = batchUploadFiles.length + ' 个文件';
      fileItems.innerHTML = '';

      for (var i = 0; i < batchUploadFiles.length; i++) {
        (function(idx) {
          var file = batchUploadFiles[idx];
          var item = document.createElement('div');
          item.className = 'file-item';
          item.innerHTML =
            '<span class="file-item-icon">📷</span>' +
            '<span class="file-item-name" title="' + file.name + '">' + file.name + '</span>' +
            '<span class="file-item-size">' + (file.size / 1024).toFixed(1) + ' KB</span>' +
            '<span class="file-item-remove" data-idx="' + idx + '">&times;</span>';
          fileItems.appendChild(item);

          item.querySelector('.file-item-remove').onclick = function() {
            batchUploadFiles.splice(idx, 1);
            renderFileList();
          };
        })(i);
      }
    }

    modal.querySelector('#btn-batch-clear').onclick = function() {
      batchUploadFiles = [];
      renderFileList();
      if (fileInput) fileInput.value = '';
    };

    modal.querySelector('#btn-batch-confirm').onclick = function() {
      if (batchUploadFiles.length === 0) {
        alert('请先选择要上传的图片文件');
        return;
      }

      var uploadTypeEl = overlay.querySelector('input[name="upload-type"]:checked');
      var imageModeEl = overlay.querySelector('input[name="image-mode"]:checked');
      var uploadType = uploadTypeEl ? uploadTypeEl.value : 'factory-code';
      var imageMode = imageModeEl ? imageModeEl.value : 'match';

      var codeSet = {};
      var fileCodeMap = [];
      for (var i = 0; i < batchUploadFiles.length; i++) {
        var fileName = batchUploadFiles[i].name;
        var nameNoExt = fileName.replace(/\.[^.]+$/, '');
        var code = nameNoExt.replace(/[_-]\d+$/, '').replace(/[_-](front|back|top|bottom|side|left|right|main|detail|thumb|cover|pic|img|image|photo)$/i, '');
        fileCodeMap.push({ file: batchUploadFiles[i], code: code, fileName: fileName });
        codeSet[code] = true;
      }

      var codes = Object.keys(codeSet);

      var previewOverlay = createBatchPreviewModal();
      var previewBody = previewOverlay.querySelector('.preview-modal-body');

      previewBody.innerHTML = '<div class="preview-loading"><div class="spinner"></div><div>正在匹配样品编号...</div></div>';
      previewOverlay.style.display = 'flex';

      fetch('/samples/match-by-codes', {
        method: 'POST',
        headers: Object.assign(authHeader(), { 'Content-Type': 'application/json' }),
        body: JSON.stringify({ type: uploadType === 'company-code' ? 'sampleCode' : 'factoryCode', codes: codes })
      })
      .then(function(res) { return res.json(); })
      .then(function(result) {
        var matchedSamples = (result && result.data) ? result.data : [];
        var codeSampleMap = {};
        for (var k = 0; k < matchedSamples.length; k++) {
          var s = matchedSamples[k];
          var matchCode = uploadType === 'company-code' ? s.sampleCode : s.factoryCode;
          codeSampleMap[matchCode] = s;
        }

        var matchedCount = 0;
        var unmatchedCount = 0;
        var tableRows = '';
        for (var r = 0; r < fileCodeMap.length; r++) {
          var item = fileCodeMap[r];
          var sample = codeSampleMap[item.code];
          if (sample) {
            matchedCount++;
            tableRows += '<tr class="row-matched">' +
              '<td>' + item.fileName + '</td>' +
              '<td>' + item.code + '</td>' +
              '<td>' + sample.sampleName + '</td>' +
              '<td><span class="preview-status ok">√ 匹配成功</span></td></tr>';
          } else {
            unmatchedCount++;
            tableRows += '<tr class="row-unmatched">' +
              '<td>' + item.fileName + '</td>' +
              '<td>' + item.code + '</td>' +
              '<td>—</td>' +
              '<td><span class="preview-status fail">× 未找到</span></td></tr>';
          }
        }

        previewBody.innerHTML =
          '<div class="preview-summary">' +
            '<span class="preview-stat total">共 ' + fileCodeMap.length + ' 个文件</span>' +
            '<span class="preview-stat matched">匹配成功 ' + matchedCount + '</span>' +
            '<span class="preview-stat unmatched">未匹配 ' + unmatchedCount + '</span>' +
          '</div>' +
          '<table class="preview-table">' +
            '<thead><tr><th style="width:30%">文件名</th><th style="width:20%">匹配编号</th><th style="width:30%">样品名称</th><th style="width:20%">状态</th></tr></thead>' +
            '<tbody>' + tableRows + '</tbody>' +
          '</table>';

        previewOverlay._matchData = {
          fileCodeMap: fileCodeMap,
          codeSampleMap: codeSampleMap,
          uploadType: uploadType,
          imageMode: imageMode,
          matchedCount: matchedCount
        };
      })
      .catch(function(err) {
        previewBody.innerHTML = '<div style="text-align:center;padding:40px;color:#ff3b30;">匹配失败: ' + err.message + '</div>';
      });
    };

    modal.querySelector('#btn-batch-export-fail').onclick = function() {
      alert('导出失败图片功能开发中...');
    };

    modal.querySelector('.modal-close-btn').onclick = function() {
      overlay.style.display = 'none';
    };

    overlay.onclick = function(e) {
      if (e.target === overlay) {
        overlay.style.display = 'none';
      }
    };

    batchImageModal = overlay;
    return overlay;
  }

  var batchPreviewModal = null;
  var batchUploading = false;

  function createBatchPreviewModal() {
    if (batchPreviewModal) return batchPreviewModal;

    var overlay = document.createElement('div');
    overlay.className = 'preview-modal-overlay';

    var modal = document.createElement('div');
    modal.className = 'preview-modal';

    modal.innerHTML =
      '<div class="preview-modal-header">' +
        '<strong>导入预览</strong>' +
        '<button class="modal-close-btn">&times;</button>' +
      '</div>' +
      '<div class="preview-modal-body"></div>' +
      '<div class="modal-footer">' +
        '<button class="sample-btn sample-btn-ghost" id="btn-preview-back">返回修改</button>' +
        '<button class="sample-btn sample-btn-primary" id="btn-preview-confirm">确认导入</button>' +
      '</div>';

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    modal.querySelector('.modal-close-btn').onclick = function() {
      overlay.style.display = 'none';
    };

    overlay.onclick = function(e) {
      if (e.target === overlay) {
        overlay.style.display = 'none';
      }
    };

    modal.querySelector('#btn-preview-back').onclick = function() {
      overlay.style.display = 'none';
    };

    modal.querySelector('#btn-preview-confirm').onclick = function() {
      var data = overlay._matchData;
      if (!data) return;

      var matchedFiles = [];
      for (var i = 0; i < data.fileCodeMap.length; i++) {
        var item = data.fileCodeMap[i];
        if (data.codeSampleMap[item.code]) {
          matchedFiles.push(item);
        }
      }

      if (matchedFiles.length === 0) {
        alert('没有匹配成功的文件可以导入');
        return;
      }

      overlay.style.display = 'none';
      if (batchImageModal) batchImageModal.style.display = 'none';

      importBatchImages(matchedFiles, data);
    };

    batchPreviewModal = overlay;
    return overlay;
  }

  function importBatchImages(matchedFiles, matchData) {
    if (batchUploading) return;
    batchUploading = true;

    var total = matchedFiles.length;
    var done = 0;
    var failed = 0;
    var failedList = [];
    var successCount = 0;

    function uploadNext(index) {
      if (index >= matchedFiles.length) {
        batchUploading = false;
        var msg = '批量导入完成！成功: ' + successCount + ', 失败: ' + failed;
        if (failedList.length > 0) {
          msg += '\n失败文件:\n' + failedList.slice(0, 15).join('\n');
          if (failedList.length > 15) msg += '\n... 等共 ' + failedList.length + ' 个';
        }
        alert(msg);
        if (sampleGridApi) {
          sampleImageCache = {};
          sampleGridApi.purgeInfiniteCache();
          sampleGridApi.refreshCells({ columns: ['image'] });
        }
        return;
      }

      var item = matchedFiles[index];
      var sample = matchData.codeSampleMap[item.code];

      var formData = new FormData();
      formData.append('file', item.file);
      if (sample && sample.id) {
        formData.append('sampleId', sample.id);
      }

      fetch('/images/upload', {
        method: 'POST',
        headers: authHeader(),
        body: formData
      })
      .then(function(res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function(result) {
        done++;
        if (result && result.code === 200) {
          successCount++;
        } else {
          failed++;
          failedList.push(item.fileName + ' (' + (result ? result.message : '未知错误') + ')');
        }
        uploadNext(index + 1);
      })
      .catch(function(err) {
        done++;
        failed++;
        failedList.push(item.fileName + ' (' + err.message + ')');
        uploadNext(index + 1);
      });
    }

    uploadNext(0);
  }

  var btnMoreFunctions = document.getElementById('btn-more-functions');

  var moreMenuGroups = [
    {
      label: '批量操作',
      icon: 'layers',
      items: [
        { id: 'export-template', icon: 'download', label: '下载模板(Excel)', action: function() {
          var headers = ['厂商编号','公司编号','种类编号','种类名称','样品名称','英文名称','出厂货号','样品单位','样品英文单位','中文包装','英文包装','价格','样品长度','样品宽度','样品高度','样品毛重','样品净重','外箱长度','外箱宽度','外箱高度','外箱材积','外箱体积','内盒个数','外箱装量','装箱单位','外箱毛重','外箱净重','包装长度','包装宽度','包装高度','产品认证','认证总数','颜色','英文颜色','备注','英文备注','厂商名称','摊位号','联系人','电话','手机','传真','QQ','登记人','修改人','侵权','电池信息'];
          var csvContent = '\uFEFF' + headers.join(',') + '\n';
          var blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
          var url = URL.createObjectURL(blob);
          var a = document.createElement('a');
          a.href = url;
          a.download = '样品导入模板.csv';
          a.click();
          URL.revokeObjectURL(url);
        }},
        { id: 'batch-import-image', icon: 'image', label: '批量导入图片', action: function() {
          var modal = createBatchImageModal();
          modal.style.display = 'flex';
        }},
        { id: 'batch-set-price', icon: 'tag', label: '批量设置价格', action: function() {} },
        { id: 'import-excel', icon: 'upload', label: '导入(Excel)', action: function() {} },
        { id: 'export-excel', icon: 'file-spreadsheet', label: '导出(Excel)', action: function() {} },
        { id: 'print-with-img', icon: 'printer', label: '\u6253\u5370\u786e\u8ba4\u8868-\u6709\u56fe\u7247', action: function() {} },
        { id: 'print-no-img', icon: 'printer', label: '\u6253\u5370\u786e\u8ba4\u8868-\u65e0\u56fe\u7247', action: function() } ]
    },
    {
      label: '视频操作',
      icon: 'video',
      items: [
        { id: 'batch-import-video', icon: 'film', label: '批量导入视频', action: function() {
          var input = document.createElement('input');
          input.type = 'file';
          input.accept = '.mp4,.mov,.avi';
          input.multiple = true;
          input.click();
        }} ]
    },
    {
      label: '图片和视频操作',
      icon: 'image-plus',
      items: [] }
  ];

  var moreDropdownPanel = null;
  var moreDropdownSearchInput = null;
  var moreDropdownList = null;

  function createPortalPanel() {
    if (moreDropdownPanel) return;
    moreDropdownPanel = document.createElement('div');
    moreDropdownPanel.className = 'sample-more-dropdown-panel';
    moreDropdownPanel.style.display = 'none';
    moreDropdownPanel.innerHTML =
      '<div class="sample-more-dropdown-search">' +
        '<i data-lucide="search"></i>' +
        '<input type="text" placeholder="搜索功能..." id="more-dropdown-search-input" autocomplete="off" />' +
      '</div>' +
      '<div class="sample-more-dropdown-list" id="more-dropdown-list"></div>';
    document.body.appendChild(moreDropdownPanel);
    moreDropdownSearchInput = document.getElementById('more-dropdown-search-input');
    moreDropdownList = document.getElementById('more-dropdown-list');

    moreDropdownSearchInput.addEventListener('input', function() { renderMenu(moreDropdownSearchInput.value); });
    moreDropdownSearchInput.addEventListener('keydown', function(e) { e.stopPropagation(); });
    moreDropdownList.addEventListener('click', handleItemClick);
  }

  function renderMenu(filter) {
    var html = '';
    for (var g = 0; g < moreMenuGroups.length; g++) {
      var group = moreMenuGroups[g];
      if (!group.items || !group.items.length) continue;
      var filteredItems = [];
      for (var i = 0; i < group.items.length; i++) {
        if (!filter || group.items[i].label.indexOf(filter) !== -1) filteredItems.push(group.items[i]);
      }
      if (!filteredItems.length && filter) continue;
      html += '<div class="sample-more-group-label">';
      html += group.icon ? ('<i data-lucide="' + group.icon + '"></i>') : '';
      html += '<span>' + group.label + '</span></div>';
      for (var j = 0; j < filteredItems.length; j++) {
        var item = filteredItems[j];
        html += '<div class="sample-more-item" data-action="' + item.id + '">';
        html += item.icon ? ('<i data-lucide="' + item.icon + '"></i>') : '';
        html += '<span>' + item.label + '</span></div>';
      }
      if (g < moreMenuGroups.length - 1) html += '<div class="sample-more-sep"></div>';
    }
    if (!html) html = '<div class="sample-more-empty">无匹配功能</div>';
    moreDropdownList.innerHTML = html;
    if (window.lucide) window.lucide.createIcons();
  }

  function handleItemClick(e) {
    var el = e.target.closest('.sample-more-item');
    if (!el) return;
    var actionId = el.getAttribute('data-action');
    closeDropdown();
    for (var g = 0; g < moreMenuGroups.length; g++) {
      for (var i = 0; i < moreMenuGroups[g].items.length; i++) {
        if (moreMenuGroups[g].items[i].id === actionId) { moreMenuGroups[g].items[i].action(); return; }
      }
    }
  }

  function positionPanel() {
    var btnRect = btnMoreFunctions.getBoundingClientRect();
    moreDropdownPanel.style.top = (btnRect.bottom + window.scrollY + 6) + 'px';
    moreDropdownPanel.style.left = (btnRect.right - moreDropdownPanel.offsetWidth + window.scrollX) + 'px';
  }

  function openDropdown() {
    createPortalPanel(); positionPanel();
    moreDropdownPanel.style.display = 'block';
    moreDropdownSearchInput.value = ''; renderMenu();
    setTimeout(function() { moreDropdownSearchInput.focus(); }, 50);
  }

  function closeDropdown() {
    if (moreDropdownPanel) moreDropdownPanel.style.display = 'none';
  }

  if (btnMoreFunctions) {
    btnMoreFunctions.addEventListener('click', function(e) {
      e.stopPropagation();
      if (!moreDropdownPanel || moreDropdownPanel.style.display !== 'block') openDropdown();
      else closeDropdown();
    });

    document.addEventListener('click', function(e) {
      if (moreDropdownPanel && !document.getElementById('more-dropdown').contains(e.target) && !moreDropdownPanel.contains(e.target)) closeDropdown();
    });
  }
}
