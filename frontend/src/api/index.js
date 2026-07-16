const isElectron = !!(window.electronAPI)
const API_BASE = isElectron ? 'http://localhost:8080' : ''

function getToken() {
  return sessionStorage.getItem('token') || localStorage.getItem('token')
}

export function authHeader() {
  const token = getToken()
  return token ? { Authorization: 'Bearer ' + token } : {}
}

export async function safeJson(res) {
  const text = await res.text()
  if (!text) return {}
  try {
    return JSON.parse(text)
  } catch {
    return {}
  }
}

export function api(endpoint, options = {}) {
  const token = getToken()
  const headers = { ...options.headers }
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  const fetchOptions = {
    method: options.method || 'GET',
    headers,
    body: options.body,
    signal: options.signal
  }
  if (options.keepalive) {
    fetchOptions.keepalive = true
  }
  return fetch(API_BASE + endpoint, fetchOptions).then(async res => {
    if (res.status === 401) {
      sessionStorage.removeItem('token')
      localStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      localStorage.removeItem('userInfo')
      window.location.href = '/#/login'
      return Promise.reject(new Error('Unauthorized'))
    }
    const data = await safeJson(res)
    if (!res.ok) {
      const msg = data?.message || data?.msg || `请求失败 (${res.status})`
      return Promise.reject(new Error(msg))
    }
    return data
  })
}

export function login(username, password) {
  return fetch(API_BASE + '/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  }).then(safeJson)
}

// ==================== 短信 API ====================

/** 获取模板列表 */
export function getSmsTemplates(type) {
  const params = type ? `?type=${encodeURIComponent(type)}` : ''
  return api('/api/sms/templates' + params)
}

/** 创建模板 */
export function createSmsTemplate(data) {
  return api('/api/sms/templates', { method: 'POST', body: JSON.stringify(data) })
}

/** 更新模板 */
export function updateSmsTemplate(id, data) {
  return api('/api/sms/templates/' + id, { method: 'PUT', body: JSON.stringify(data) })
}

/** 删除模板 */
export function deleteSmsTemplate(id) {
  return api('/api/sms/templates/' + id, { method: 'DELETE' })
}

/** 同步模板状态 */
export function syncSmsTemplateStatus(id) {
  return api('/api/sms/templates/' + id + '/sync-status', { method: 'POST' })
}

/** 发送短信 */
export function sendSms(data) {
  return api('/api/sms/send', { method: 'POST', body: JSON.stringify(data) })
}

/** 个性短信发送 */
export function sendPersonalSms(data) {
  return api('/api/sms/send-personal', { method: 'POST', body: JSON.stringify(data) })
}

/** 企业微信发送 */
export function sendWework(data) {
  return api('/api/sms/send-wework', { method: 'POST', body: JSON.stringify(data) })
}

/** 查询发送记录 */
export function getSmsRecords(params) {
  const qs = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') qs.append(k, v)
  })
  return api('/api/sms/records?' + qs.toString())
}

/** 手动同步发送状态（从联麓拉取报告） */
export function syncSmsStatus(codeName) {
  return api('/api/sms/sync-status?codeName=' + encodeURIComponent(codeName), { method: 'POST' })
}

/** 查询余额 */
export function getSmsBalance(smsType = '通知') {
  return api('/api/sms/balance?smsType=' + encodeURIComponent(smsType))
}

/** 查询签名列表 */
export function getSmsSigns(smsType = '通知') {
  return api('/api/sms/signs?smsType=' + encodeURIComponent(smsType))
}

/** 查询代号下的厂商总数 */
export function getSmsManufacturerCount(codeName) {
  return api('/api/sms/manufacturer-count?codeName=' + encodeURIComponent(codeName))
}

// ==================== 企业微信绑定管理 ====================

/** 查询厂商微信绑定列表 */
export function getWeworkBindings(manufacturerId) {
  return api('/api/wework/bindings?manufacturerId=' + manufacturerId)
}

/** 解绑微信 */
export function unbindWework(manufacturerId, bindingId) {
  return api('/api/wework/bindings/' + bindingId + '?manufacturerId=' + manufacturerId, { method: 'DELETE' })
}

/** 检查绑定超限 */
export function checkWeworkOverLimit(manufacturerId) {
  return api('/api/wework/bindings/check-overlimit?manufacturerId=' + manufacturerId)
}

/** 企微绑定查询（概览）：按手机号搜索厂商绑定状态，支持分页 */
export function getWeworkBindingOverview(phone, page = 1, pageSize = 20) {
  let params = `?page=${page}&pageSize=${pageSize}`
  if (phone) params += '&phone=' + encodeURIComponent(phone)
  return api('/api/wework/bindings/overview' + params)
}
