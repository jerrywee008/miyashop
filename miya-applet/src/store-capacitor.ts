/**
 * Capacitor 安全版 user store — 使用 reactive 替代 Pinia
 */
import { reactive } from 'vue'

interface UserStore {
  token: string
  userInfo: any
  cartCount: number
  setToken: (newToken: string) => void
  setUserInfo: (info: any) => void
  setCartCount: (count: number) => void
  logout: () => void
}

const state = reactive<UserStore>({
  token: (typeof localStorage !== 'undefined' ? localStorage.getItem('token') : null) || '',
  userInfo: null,
  cartCount: 0,
  setToken(newToken: string) {
    state.token = newToken
    try { localStorage.setItem('token', newToken) } catch {}
  },
  setUserInfo(info: any) {
    state.userInfo = info
  },
  setCartCount(count: number) {
    state.cartCount = count
  },
  logout() {
    state.token = ''
    state.userInfo = null
    try { localStorage.removeItem('token') } catch {}
  }
})

export function useUserStore() {
  return state
}
