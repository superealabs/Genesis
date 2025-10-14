// stores/useAuthenticationStore.ts
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { User } from '@/models/auth/UserModel.ts'

export const useAuthenticationStore = defineStore('authenticationStore', () => {
  // Try to load from localStorage
  const storedUser = localStorage.getItem('currentUser')
  const storedToken = localStorage.getItem('userToken')

  const currentUser = ref<User>(
    storedUser ? JSON.parse(storedUser) : User.voidUser()
  )
  const userToken = ref<string>(
    storedToken ??
    ''
  )

  const setToken = (token: string) => {
    userToken.value = token
    localStorage.setItem('userToken', token)
  }

  const getToken = () => {
    return userToken.value
  }

  const setCurrentUser = (user: User) => {
    currentUser.value = user
    localStorage.setItem('currentUser', JSON.stringify(user))
  }

  const getCurrentUser = () => {
    return currentUser.value
  }

  const getCurrentUsername = computed((): string => {
    const user = getCurrentUser()
    return user?.username || 'John Doe'
  })

  // Optional: a clear function (logout)
  const clear = () => {
    currentUser.value = User.voidUser()
    userToken.value = ''
    localStorage.removeItem('currentUser')
    localStorage.removeItem('userToken')
  }

  return {
    setToken,
    getToken,
    getCurrentUser,
    setCurrentUser,
    getCurrentUsername,
    clear,
  }
})
