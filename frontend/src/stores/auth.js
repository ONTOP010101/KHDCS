import { reactive } from 'vue'

const state = reactive({
  token: sessionStorage.getItem('token') || localStorage.getItem('token') || '',
  userInfo: null
})

function loadUserInfo() {
  const raw = sessionStorage.getItem('userInfo') || localStorage.getItem('userInfo')
  if (raw) {
    try {
      state.userInfo = JSON.parse(raw)
    } catch {
      state.userInfo = null
    }
  }
}

loadUserInfo()

export function useAuth() {
  function setAuth(data, remember = false) {
    const storage = remember ? localStorage : sessionStorage
    storage.setItem('token', data.token)
    const info = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      roleId: data.roleId,
      roleName: data.roleName,
      permissions: data.permissions
    }
    storage.setItem('userInfo', JSON.stringify(info))
    state.token = data.token
    state.userInfo = info
  }

  function clearAuth() {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    state.token = ''
    state.userInfo = null
  }

  function isLoggedIn() {
    return !!state.token
  }

  function getUserInitials() {
    if (!state.userInfo) return 'U'
    const name = state.userInfo.realName || state.userInfo.username || 'U'
    return name.substring(0, 2).toUpperCase()
  }

  return {
    state,
    setAuth,
    clearAuth,
    isLoggedIn,
    getUserInitials
  }
}
