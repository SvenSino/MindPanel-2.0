<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import Sidebar from 'primevue/sidebar'

const props = defineProps<{
  visible: boolean
  isDesktop?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const router = useRouter()
const route = useRoute()

const menuItems = [
  { label: 'Dashboard', icon: 'pi pi-th-large', to: '/' },
  { label: 'Profil', icon: 'pi pi-user', to: '/profile-settings' },
  { label: 'Einstellungen', icon: 'pi pi-cog', to: '/settings' },
]

function navigateTo(path: string) {
  router.push(path)
  if (!props.isDesktop) {
    emit('update:visible', false)
  }
}

function isActive(path: string) {
  return route.path === path
}
</script>

<template>
  <div v-if="isDesktop" class="desktop-menu">


    <div class="flex flex-column gap-2 p-3 mt-3">
      <div
        v-for="item in menuItems"
        :key="item.to"
        class="menu-item p-3 border-round cursor-pointer transition-colors transition-duration-150"
        :class="{ 'menu-item-active': isActive(item.to) }"
        @click="navigateTo(item.to)"
      >
        <div class="flex align-items-center gap-3">
          <i :class="item.icon" class="text-xl"></i>
          <span class="font-medium">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <div class="sidebar-footer">
      <div class="text-center text-sm text-color-secondary p-3">
        <p class="m-0">MindPanel v0.1.0</p>
        <p class="m-0 mt-1">Made with ❤️</p>
      </div>
    </div>
  </div>

  <!-- Mobile Sidebar (drawer) -->
  <Sidebar
    v-else
    :visible="visible"
    @update:visible="emit('update:visible', $event)"
    position="left"
  >
    <template #header>
      <div class="flex align-items-center gap-2">
        <i class="pi pi-bars text-2xl"></i>
        <span class="font-bold text-xl">Menu</span>
      </div>
    </template>

    <div class="flex flex-column gap-2">
      <div
        v-for="item in menuItems"
        :key="item.to"
        class="menu-item p-3 border-round cursor-pointer transition-colors transition-duration-150"
        :class="{ 'menu-item-active': isActive(item.to) }"
        @click="navigateTo(item.to)"
      >
        <div class="flex align-items-center gap-3">
          <i :class="item.icon" class="text-xl"></i>
          <span class="font-medium">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="text-center text-sm text-color-secondary p-3">
        <p class="m-0">MindPanel v0.1.0</p>
        <p class="m-0 mt-1">Made with ❤️</p>
      </div>
    </template>
  </Sidebar>
</template>

<style scoped>
.desktop-menu {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-footer {
  margin-top: auto;
  border-top: 1px solid var(--surface-border);
}

.menu-item {
  transition: all 0.15s ease;
}

.menu-item:hover {
  background: var(--surface-hover);
}

.menu-item-active {
  background: var(--p-button-primary-hover-background);
  color: var(--primary-color-text);
}

.menu-item-active:hover {
  background: var(--primary-color);
  background: var(--p-button-primary-hover-background);
}
</style>
