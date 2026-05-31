function ensureAppDialogRoot() {
  var old = document.getElementById('app-dialog-mask');
  if (old) return old;

  var mask = document.createElement('div');
  mask.id = 'app-dialog-mask';
  mask.className = 'app-dialog-mask';

  mask.innerHTML = `
    <div class="app-dialog-box" role="dialog" aria-modal="true">
      <div class="app-dialog-head">
        <div class="app-dialog-icon info" id="app-dialog-icon">
          <i data-lucide="info"></i>
        </div>
        <div class="app-dialog-title" id="app-dialog-title">提示</div>
      </div>

      <div class="app-dialog-body" id="app-dialog-message"></div>

      <div class="app-dialog-foot" id="app-dialog-foot"></div>
    </div>
  `;

  document.body.appendChild(mask);
  return mask;
}

function appAlert(message, options) {
  options = options || {};

  return new Promise(function(resolve) {
    var mask = ensureAppDialogRoot();

    var iconBox = document.getElementById('app-dialog-icon');
    var titleEl = document.getElementById('app-dialog-title');
    var msgEl = document.getElementById('app-dialog-message');
    var footEl = document.getElementById('app-dialog-foot');

    var type = options.type || 'info';
    var title = options.title || '系统提示';
    var icon = options.icon || (
      type === 'success' ? 'check-circle' :
      type === 'warning' ? 'circle-alert' :
      type === 'danger' ? 'triangle-alert' :
      'info'
    );

    iconBox.className = 'app-dialog-icon ' + type;
    iconBox.innerHTML = '<i data-lucide="' + icon + '"></i>';

    titleEl.textContent = title;
    if (options.html) {
      msgEl.innerHTML = String(message || '');
    } else {
      msgEl.innerHTML = String(message || '').replace(/\n/g, '<br>');
    }

    footEl.innerHTML = `
      <button class="app-dialog-btn app-dialog-btn-primary" id="app-dialog-ok">
        确定
      </button>
    `;

    mask.classList.add('show');
    lucide.createIcons();

    var okBtn = document.getElementById('app-dialog-ok');

    function close() {
      mask.classList.remove('show');
      resolve(true);
    }

    okBtn.onclick = close;

    mask.onclick = function(e) {
      if (e.target === mask) close();
    };

    document.onkeydown = function(e) {
      if (!mask.classList.contains('show')) return;
      if (e.key === 'Escape' || e.key === 'Enter') {
        close();
      }
    };
  });
}

function appConfirm(message, options) {
  options = options || {};

  return new Promise(function(resolve) {
    var mask = ensureAppDialogRoot();

    var iconBox = document.getElementById('app-dialog-icon');
    var titleEl = document.getElementById('app-dialog-title');
    var msgEl = document.getElementById('app-dialog-message');
    var footEl = document.getElementById('app-dialog-foot');

    var type = options.type || 'warning';
    var title = options.title || '请确认';
    var icon = options.icon || (
      type === 'danger' ? 'triangle-alert' : 'circle-alert'
    );

    iconBox.className = 'app-dialog-icon ' + type;
    iconBox.innerHTML = '<i data-lucide="' + icon + '"></i>';

    titleEl.textContent = title;
    if (options.html) {
      msgEl.innerHTML = String(message || '');
    } else {
      msgEl.innerHTML = String(message || '').replace(/\n/g, '<br>');
    }

    footEl.innerHTML = `
      <button class="app-dialog-btn app-dialog-btn-ghost" id="app-dialog-cancel">
        取消
      </button>
      <button class="app-dialog-btn ${type === 'danger' ? 'app-dialog-btn-danger' : 'app-dialog-btn-primary'}" id="app-dialog-confirm">
        确定
      </button>
    `;

    mask.classList.add('show');
    lucide.createIcons();

    var cancelBtn = document.getElementById('app-dialog-cancel');
    var confirmBtn = document.getElementById('app-dialog-confirm');

    function close(result) {
      mask.classList.remove('show');
      resolve(result);
    }

    cancelBtn.onclick = function() {
      close(false);
    };

    confirmBtn.onclick = function() {
      close(true);
    };

    mask.onclick = function(e) {
      if (e.target === mask) close(false);
    };

    document.onkeydown = function(e) {
      if (!mask.classList.contains('show')) return;

      if (e.key === 'Escape') {
        close(false);
      }

      if (e.key === 'Enter') {
        close(true);
      }
    };
  });
}

function appToast(message, duration) {
  duration = duration || 1600;

  var wrap = document.getElementById('app-toast-wrap');

  if (!wrap) {
    wrap = document.createElement('div');
    wrap.id = 'app-toast-wrap';
    wrap.className = 'app-toast-wrap';
    document.body.appendChild(wrap);
  }

  wrap.innerHTML = '<div class="app-toast">' + String(message || '') + '</div>';

  clearTimeout(window.__appToastTimer);

  window.__appToastTimer = setTimeout(function() {
    wrap.innerHTML = '';
  }, duration);
}
