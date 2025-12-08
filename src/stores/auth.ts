import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
  createdAt: string
}

const MOCK_USERS = [
  {
    username: 'admin',
    password: 'admin123',
    user: {
      id: 'user_001',
      username: 'admin',
      email: 'admin@mindpanel.de',
      firstName: 'Max',
      lastName: 'Mustermann',
      street: 'Musterstraße 123',
      zipCode: '44137',
      city: 'Dortmund',
      country: 'Deutschland',
      avatar: null,
      createdAt: '2024-01-15T10:30:00.000Z',
    }
  },
  {
    username: 'demo',
    password: 'demo123',
    user: {
      id: 'user_002',
      username: 'demo',
      email: 'demo@mindpanel.de',
      firstName: 'Anna',
      lastName: 'Schmidt',
      street: 'Demoweg 456',
      zipCode: '10115',
      city: 'Berlin',
      country: 'Deutschland',
      avatar: null,
      createdAt: '2024-02-20T14:15:00.000Z',
    }
  }
]

const USER_KEY = 'mindpanel_user'
const USERS_DATA_KEY = 'mindpanel_users_data' // Speichert Daten für alle User

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const isAuthenticated = computed(() => user.value !== null)

  function loadUser() {
    if (typeof window === 'undefined') return
    const raw = window.localStorage.getItem(USER_KEY)
    if (!raw) return
    try {
      const parsed = JSON.parse(raw) as User
      user.value = parsed
    } catch (e) {
      console.warn('Could not load user:', e)
    }
  }

  function saveUser() {
    if (typeof window === 'undefined') return
    if (user.value) {
      // Speichere aktuellen User
      window.localStorage.setItem(USER_KEY, JSON.stringify(user.value))

      // Speichere User-Daten in globaler User-Datenbank
      const usersDataRaw = window.localStorage.getItem(USERS_DATA_KEY)
      let usersData: Record<string, User> = {}

      if (usersDataRaw) {
        try {
          usersData = JSON.parse(usersDataRaw)
        } catch (e) {
          console.warn('Could not parse users data')
        }
      }

      usersData[user.value.username] = user.value
      window.localStorage.setItem(USERS_DATA_KEY, JSON.stringify(usersData))
    } else {
      window.localStorage.removeItem(USER_KEY)
    }
  }

  function login(username: string, password: string): boolean {
    const mockUser = MOCK_USERS.find(
      user => user.username === username && user.password === password
    )

    if (mockUser) {
      // Lade gespeicherte User-Daten für diesen Username
      const usersDataRaw = typeof window !== 'undefined' ? window.localStorage.getItem(USERS_DATA_KEY) : null
      let savedUser: User | null = null

      if (usersDataRaw) {
        try {
          const usersData: Record<string, User> = JSON.parse(usersDataRaw)
          savedUser = usersData[username] || null
        } catch (e) {
          console.warn('Could not parse users data')
        }
      }

      // Wenn gespeicherte Daten für diesen User existieren, diese verwenden
      // Ansonsten die Standard-Daten aus MOCK_USERS
      if (savedUser) {
        user.value = savedUser
      } else {
        user.value = { ...mockUser.user }
      }

      saveUser()
      return true
    }

    return false

  }

  function register(username: string, email: string, password: string): boolean {
    // Prüfe ob Username bereits existiert
    if (MOCK_USERS.find(user => user.username === username)) {
      return false
    }

    // Mock register - in real app, this would call an API
    const newUser: User = {
      id: Math.random().toString(36).substring(7),
      username,
      email,
      firstName: '',
      lastName: '',
      street: '',
      zipCode: '',
      city: '',
      country: '',
      avatar: null,
      createdAt: new Date().toISOString(),
    }
    user.value = newUser
    saveUser()
    return true
  }

  function logout() {
    user.value = null
    saveUser()
  }

  function updateProfile(updates: Partial<Omit<User, 'id' | 'createdAt'>>) {
    if (user.value) {
      user.value = { ...user.value, ...updates }
      saveUser()
    }
  }

  function updateAvatar(avatarUrl: string) {
    if (user.value) {
      user.value.avatar = avatarUrl
      saveUser()
    }
  }

  function initAuth() {
    loadUser()
  }

  return {
    user,
    isAuthenticated,
    login,
    register,
    logout,
    updateProfile,
    updateAvatar,
    initAuth,
  }
})
