<script setup lang="ts">
import { RouterView } from 'vue-router'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
</script>

<template>
  <div id="app">
    <RouterView />
    <Toast />
    <ConfirmDialog />
  </div>
</template>

<style scoped>
#app {
  min-height: 100vh;
}
</style>
