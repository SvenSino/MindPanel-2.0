import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import keycloak from '@/services/keycloak'
import api from '@/services/api'

export interface User {
  id: string
  username: string
  email: string
  firstName: string
  lastName: string
  street: string
  zipCode: string
  city: string
  country: string
  avatar: string | null
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const isAuthenticated = computed(() => !!user.value)

  async function initAuth() {
    if (!keycloak.authenticated) {
      user.value = null
      return
    }

    const token = keycloak.tokenParsed as Record<string, any>
    const baseUser: User = {
      id: token['sub'],
      username: token['preferred_username'] || '',
      email: token['email'] || '',
      firstName: token['given_name'] || '',
      lastName: token['family_name'] || '',
      street: '',
      zipCode: '',
      city: '',
      country: '',
      avatar: null,
    }

    try {
      const { data } = await api.get('/profile')
      user.value = {
        ...baseUser,
        street: data.street || '',
        zipCode: data.zipCode || '',
        city: data.city || '',
        country: data.country || '',
        avatar: data.avatar || null,
      }
    } catch {
      user.value = baseUser
    }
  }

  async function updateProfile(updates: Partial<Omit<User, 'id'>>) {
    if (!user.value) return
    const merged = { ...user.value, ...updates }
    await api.put('/profile', {
      street: merged.street,
      zipCode: merged.zipCode,
      city: merged.city,
      country: merged.country,
      avatar: merged.avatar,
    })
    user.value = merged
  }

  async function updateAvatar(avatarUrl: string) {
    await updateProfile({ avatar: avatarUrl || null })
  }

  function logout() {
    user.value = null
    keycloak.logout()
  }

  return {
    user,
    isAuthenticated,
    initAuth,
    updateProfile,
    updateAvatar,
    logout,
  }
})
