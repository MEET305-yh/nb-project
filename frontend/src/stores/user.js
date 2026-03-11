import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
    nickname: localStorage.getItem('nickname') || '',
    avatar: localStorage.getItem('avatar') || ''
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN',
    isMerchant: (state) => state.role === 'MERCHANT'
  },

  actions: {
    async login(loginData) {
      const res = await login(loginData)
      this.setUserInfo(res.data)
      return res
    },

    async register(registerData) {
      const res = await register(registerData)
      return res
    },

    setUserInfo(data) {
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.role = data.role
      this.nickname = data.nickname || data.username
      this.avatar = data.avatar || ''

      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)
      localStorage.setItem('nickname', data.nickname || data.username)
      localStorage.setItem('avatar', data.avatar || '')
    },

    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        this.nickname = res.data.nickname || this.username
        this.avatar = res.data.avatar || ''
        localStorage.setItem('nickname', this.nickname)
        localStorage.setItem('avatar', this.avatar)
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    },

    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.role = ''
      this.nickname = ''
      this.avatar = ''

      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('nickname')
      localStorage.removeItem('avatar')
    }
  }
})

