<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import Button from 'primevue/button'
import Avatar from 'primevue/avatar'
import Menu from 'primevue/menu'
import AppSidebar from './AppSidebar.vue'
import MindPanelLogo from '@/components/MindPanelLogo.vue'

const router = useRouter()
const uiStore = useUiStore()
const authStore = useAuthStore()

const isMobile = ref(false)
const sidebarVisible = ref(false)
const userMenu = ref()

const userInitials = computed(() => {
  const first = authStore.user?.firstName?.charAt(0) || ''
  const last = authStore.user?.lastName?.charAt(0) || ''
  if (first && last) return (first + last).toUpperCase()
  if (first) return first.toUpperCase()
  if (last) return last.toUpperCase()
  return authStore.user?.username?.substring(0, 2).toUpperCase() || 'U'
})

const menuItems = computed(() => [
  {
    label: authStore.user?.username || 'Benutzer',
    items: [
      {
        label: 'Profil-Einstellungen',
        icon: 'pi pi-user',
        command: () => router.push('/profile-settings'),
      },
      {
        separator: true,
      },
      {
        label: 'Abmelden',
        icon: 'pi pi-sign-out',
        command: () => {
          authStore.logout()
          router.push('/login')
        },
      },
    ],
  },
])

function checkMobile() {
  isMobile.value = window.innerWidth < 1024
  if (!isMobile.value) {
    sidebarVisible.value = false
  }
}

function toggleSidebar() {
  if (isMobile.value) {
    sidebarVisible.value = !sidebarVisible.value
  }
}

function toggleUserMenu(event: Event) {
  userMenu.value.toggle(event)
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="flex align-items-center justify-content-between px-4 py-3">
        <div class="flex align-items-center gap-3">
          <Button
            v-if="isMobile"
            icon="pi pi-bars"
            text
            rounded
            @click="toggleSidebar"
            aria-label="Menu"
          />
          <div class="cursor-pointer" @click="router.push('/')">
            <MindPanelLogo size="medium" />
          </div>
        </div>

        <div class="flex align-items-center gap-2">
          <Button
            :icon="uiStore.isDarkMode ? 'pi pi-sun' : 'pi pi-moon'"
            text
            rounded
            @click="uiStore.toggleDarkMode"
            v-tooltip.bottom="uiStore.isDarkMode ? 'Light Mode' : 'Dark Mode'"
            aria-label="Toggle Dark Mode"
          />
          <Button
            icon="pi pi-cog"
            text
            rounded
            @click="router.push('/settings')"
            v-tooltip.bottom="'Einstellungen'"
            aria-label="Einstellungen"
          />

          <div v-if="authStore.isAuthenticated" class="user-section">
            <div class="flex align-items-center gap-2 cursor-pointer" @click="toggleUserMenu">
              <Avatar
                v-if="authStore.user?.avatar"
                :image="authStore.user.avatar"
                shape="circle"
                class="user-avatar"
              />
              <Avatar
                v-else
                :label="userInitials"
                shape="circle"
                class="user-avatar"
              />
              <span v-if="!isMobile" class="font-semibold">{{ authStore.user?.username }}</span>
            </div>
            <Menu ref="userMenu" :model="menuItems" popup />
          </div>
        </div>
      </div>
    </header>

    <div class="app-body">
      <aside v-if="!isMobile" class="desktop-sidebar surface-card">
        <AppSidebar :visible="true" :is-desktop="true" />
      </aside>

      <AppSidebar v-if="isMobile" v-model:visible="sidebarVisible" :is-desktop="false" />

      <main class="app-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--surface-ground);
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--surface-card);
  border-bottom: 1px solid var(--surface-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.desktop-sidebar {
  width: 260px;
  background: var(--surface-card);
  border-right: 2px solid var(--surface-border);
  position: sticky;
  top: 65px;
  height: calc(100vh - 65px);
  overflow-y: auto;
  flex-shrink: 0;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
}

.app-content {
  flex: 1;
  padding: 1.5rem;
  max-width: 1920px;
  margin: 0 auto;
  width: 100%;
  overflow-x: hidden;
}

@media (max-width: 1024px) {
  .app-content {
    padding: 1rem;
  }
}

.user-avatar {
  width: 40px;
  height: 40px;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-section:hover .user-avatar {
  transform: scale(1.05);
}
</style>
