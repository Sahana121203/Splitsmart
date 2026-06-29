import { create } from 'zustand'
import { login as loginApi, register as registerApi, getMe } from '../api/auth'

const useAuthStore = create((set) => ({
  user: null,
  token: localStorage.getItem('access_token'),
  loading: false,
  error: null,

  login: async (phoneOrEmail, password) => {
    set({ loading: true, error: null })
    try {
      const res = await loginApi({ phoneOrEmail, password })
      const { accessToken, ...user } = res.data
      localStorage.setItem('access_token', accessToken)
      set({ user, token: accessToken, loading: false })
      return true
    } catch (err) {
      set({
        error: err.response?.data?.error
          || err.message
          || 'Login failed',
        loading: false
      })
      return false
    }
  },

  register: async (data) => {
    set({ loading: true, error: null })
    try {
      const res = await registerApi(data)
      const { accessToken, ...user } = res.data
      localStorage.setItem('access_token', accessToken)
      set({ user, token: accessToken, loading: false })
      return true
    } catch (err) {
      set({
        error: err.response?.data?.error
          || err.message
          || 'Registration failed',
        loading: false
      })
      return false
    }
  },

  logout: () => {
    localStorage.removeItem('access_token')
    set({ user: null, token: null })
  },

  fetchMe: async () => {
    try {
      const res = await getMe()
      set({ user: res.data })
    } catch {
      localStorage.removeItem('access_token')
      set({ user: null, token: null })
    }
  },

  clearError: () => set({ error: null }),
}))

export default useAuthStore
