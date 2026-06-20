/**
 * 自定义弹窗工具 — 替代 alert / confirm / prompt
 * 样式定义在 @/styles/common.css (.app-dialog-*)
 */
import '@/styles/common.css'

/* ===== SVG 图标 ===== */
const ICONS = {
  info: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>`,
  success: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/></svg>`,
  warning: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
  danger: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`
}

/* ===== DOM 容器 ===== */
function ensureContainer() {
  let el = document.getElementById('app-dialog-root')
  if (!el) {
    el = document.createElement('div')
    el.id = 'app-dialog-root'
    document.body.appendChild(el)
  }
  return el
}

/* ===== 关闭弹窗 ===== */
function closeDialog(mask) {
  mask.classList.remove('show')
  setTimeout(() => mask.remove(), 280)
}

/* ===== Alert ===== */
export function appAlert(message, title = '提示', type = 'info') {
  return new Promise(resolve => {
    const root = ensureContainer()
    const mask = document.createElement('div')
    mask.className = 'app-dialog-mask'
    mask.innerHTML = `
      <div class="app-dialog-box">
        <div class="app-dialog-head">
          <div class="app-dialog-icon ${type}">${ICONS[type] || ICONS.info}</div>
          <div class="app-dialog-title">${title}</div>
        </div>
        <div class="app-dialog-body">${message}</div>
        <div class="app-dialog-foot">
          <button class="app-dialog-btn app-dialog-btn-primary" data-action="ok">确定</button>
        </div>
      </div>`
    root.appendChild(mask)
    requestAnimationFrame(() => mask.classList.add('show'))

    mask.querySelector('[data-action="ok"]').onclick = () => { closeDialog(mask); resolve(true) }
    mask.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === 'Escape') { closeDialog(mask); resolve(true) } })
    // 自动聚焦以便键盘操作
    setTimeout(() => { const btn = mask.querySelector('[data-action="ok"]'); if (btn) btn.focus() }, 50)
  })
}

/* ===== Confirm ===== */
export function appConfirm(message, title = '确认操作', type = 'warning') {
  return new Promise(resolve => {
    const root = ensureContainer()
    const mask = document.createElement('div')
    mask.className = 'app-dialog-mask'
    mask.innerHTML = `
      <div class="app-dialog-box">
        <div class="app-dialog-head">
          <div class="app-dialog-icon ${type}">${ICONS[type] || ICONS.warning}</div>
          <div class="app-dialog-title">${title}</div>
        </div>
        <div class="app-dialog-body">${message}</div>
        <div class="app-dialog-foot">
          <button class="app-dialog-btn app-dialog-btn-ghost" data-action="cancel">取消</button>
          <button class="app-dialog-btn ${type === 'danger' ? 'app-dialog-btn-danger' : 'app-dialog-btn-primary'}" data-action="ok">确定</button>
        </div>
      </div>`
    root.appendChild(mask)
    requestAnimationFrame(() => mask.classList.add('show'))

    mask.querySelector('[data-action="ok"]').onclick = () => { closeDialog(mask); resolve(true) }
    mask.querySelector('[data-action="cancel"]').onclick = () => { closeDialog(mask); resolve(false) }
    mask.addEventListener('keydown', e => { if (e.key === 'Escape') { closeDialog(mask); resolve(false) } })
    setTimeout(() => { const btn = mask.querySelector(type === 'danger' ? '[data-action="cancel"]' : '[data-action="ok"]'); if (btn) btn.focus() }, 50)
  })
}

/* ===== Prompt ===== */
export function appPrompt(message, defaultValue = '', title = '请输入', type = 'info') {
  return new Promise(resolve => {
    const root = ensureContainer()
    const mask = document.createElement('div')
    mask.className = 'app-dialog-mask'
    mask.innerHTML = `
      <div class="app-dialog-box">
        <div class="app-dialog-head">
          <div class="app-dialog-icon ${type}">${ICONS[type] || ICONS.info}</div>
          <div class="app-dialog-title">${title}</div>
        </div>
        <div class="app-dialog-body">
          <div style="margin-bottom:12px;font-weight:700;color:rgba(29,29,31,0.7)">${message}</div>
          <input class="app-dialog-input" type="text" value="${defaultValue.replace(/"/g, '&quot;')}" style="
            width:100%;height:40px;border-radius:14px;border:1px solid rgba(0,122,255,0.16);
            padding:0 14px;font-size:13px;font-weight:650;outline:none;background:rgba(255,255,255,0.88);
            box-sizing:border-box;color:#1d1d1f;
            transition:all .25s cubic-bezier(.4,0,.2,1);
          " />
        </div>
        <div class="app-dialog-foot">
          <button class="app-dialog-btn app-dialog-btn-ghost" data-action="cancel">取消</button>
          <button class="app-dialog-btn app-dialog-btn-primary" data-action="ok">确定</button>
        </div>
      </div>`
    root.appendChild(mask)
    requestAnimationFrame(() => mask.classList.add('show'))

    const input = mask.querySelector('.app-dialog-input')
    setTimeout(() => input.focus(), 80)

    mask.querySelector('[data-action="ok"]').onclick = () => {
      closeDialog(mask)
      resolve(input.value.trim() || null)
    }
    mask.querySelector('[data-action="cancel"]').onclick = () => { closeDialog(mask); resolve(null) }
    input.addEventListener('keydown', e => {
      if (e.key === 'Enter') { closeDialog(mask); resolve(input.value.trim() || null) }
      if (e.key === 'Escape') { closeDialog(mask); resolve(null) }
    })
  })
}

/* ===== Toast (轻提示，自动消失) ===== */
export function appToast(message, duration = 1800) {
  const root = ensureContainer()
  const wrap = document.createElement('div')
  wrap.className = 'app-toast-wrap'
  wrap.innerHTML = `<div class="app-toast">${message}</div>`
  root.appendChild(wrap)
  setTimeout(() => {
    wrap.style.transition = 'opacity 0.35s ease, transform 0.35s ease'
    wrap.style.opacity = '0'
    wrap.style.transform = 'translate(-50%, -45%) scale(0.96)'
    setTimeout(() => wrap.remove(), 350)
  }, duration)
}
