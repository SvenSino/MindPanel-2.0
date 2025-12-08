
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Checkbox from 'primevue/checkbox'
import Calendar from 'primevue/calendar'
import { useToast } from 'primevue/usetoast'

const dashboardStore = useDashboardStore()
const toast = useToast()

const newTodoTitle = ref('')
const newTodoDueDate = ref<Date | null>(null)
const showAllTodosDialog = ref(false)
const showArchiveDialog = ref(false)

const todos = computed(() => dashboardStore.todos)
const priorityTodos = computed(() => todos.value.filter(t => t.isPriority && !t.completed))
const regularTodos = computed(() => todos.value.filter(t => !t.isPriority && !t.completed))
const completedTodos = computed(() => todos.value.filter(t => t.completed))
const archivedTodos = computed(() => dashboardStore.archivedTodos)

// Maximal 5 Todos insgesamt (Prioritäten + reguläre)
const displayedRegularTodos = computed(() => {
  const maxRegular = Math.max(0, 5 - priorityTodos.value.length)
  return regularTodos.value.slice(0, maxRegular)
})
const hasMoreTodos = computed(() => {
  const totalDisplayed = priorityTodos.value.length + displayedRegularTodos.value.length
  return regularTodos.value.length + priorityTodos.value.length > totalDisplayed || completedTodos.value.length > 0
})

function addTodo() {
  if (!newTodoTitle.value.trim()) return

  dashboardStore.addTodo(
    newTodoTitle.value.trim(),
    newTodoDueDate.value?.toISOString() || null
  )

  newTodoTitle.value = ''
  newTodoDueDate.value = null
}

function toggleTodo(id: number) {
  dashboardStore.toggleTodo(id)
}

function togglePriority(id: number) {
  const success = dashboardStore.togglePriority(id)
  if (!success) {
    toast?.add({
      severity: 'warn',
      summary: 'Limit erreicht',
      detail: 'Maximal 3 Prioritäten erlaubt',
      life: 3000,
    })
  }
}

function deleteTodo(id: number) {
  dashboardStore.deleteTodo(id)
}

function archiveTodo(id: number) {
  dashboardStore.archiveTodo(id)
}

function unarchiveTodo(id: number) {
  dashboardStore.unarchiveTodo(id)
}

function formatDate(dateString: string | null): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function isOverdue(dateString: string | null): boolean {
  if (!dateString) return false
  const dueDate = new Date(dateString)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return dueDate < today
}
</script>

<template>
  <Card class="widget-card h-full">
    <template #header>
      <div class="widget-header drag-handle">
        <div class="flex align-items-center gap-2">
          <i class="pi pi-check-square text-xl"></i>
          <h3 class="m-0 font-semibold">Aufgaben</h3>
        </div>
        <div class="flex align-items-center gap-2">
          <span class="text-sm text-color-secondary">
            {{ todos.filter(t => !t.completed).length }} offen
          </span>
          <Button
            icon="pi pi-inbox"
            rounded
            text
            @click="showArchiveDialog = true"
            v-tooltip.top="'Archiv'"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div class="widget-content">
        <div class="flex flex-column gap-2 mb-4">
          <InputText
            v-model="newTodoTitle"
            placeholder="Neue Aufgabe..."
            @keyup.enter="addTodo"
            class="w-full"
          />
          <div class="flex gap-2">
            <Calendar
              v-model="newTodoDueDate"
              placeholder="Fälligkeitsdatum"
              dateFormat="dd.mm.yy"
              showIcon
              class="calendar-field"
            />
            <Button
              icon="pi pi-plus"
              @click="addTodo"
              :disabled="!newTodoTitle.trim()"
              class="add-todo-btn"
            />
          </div>
        </div>

        <div v-if="priorityTodos.length > 0" class="mb-4">
          <div class="flex align-items-center gap-2 mb-2">
            <i class="pi pi-star-fill text-yellow-500"></i>
            <span class="font-semibold text-sm">Prioritäten</span>
          </div>
          <div class="flex flex-column gap-2">
            <div
              v-for="todo in priorityTodos"
              :key="todo.id"
              class="todo-item p-3 border-round"
            >
              <div class="flex align-items-start gap-3">
                <Checkbox
                  :modelValue="todo.completed"
                  @update:modelValue="toggleTodo(todo.id)"
                  binary
                />
                <div class="flex-1">
                  <p class="m-0" :class="{ 'line-through text-color-secondary': todo.completed }">
                    {{ todo.title }}
                  </p>
                  <p
                    v-if="todo.dueDate"
                    class="text-xs m-0 mt-1"
                    :class="isOverdue(todo.dueDate) ? 'text-red-500' : 'text-color-secondary'"
                  >
                    <i class="pi pi-calendar mr-1"></i>
                    {{ formatDate(todo.dueDate) }}
                  </p>
                </div>
                <div class="flex gap-1">
                  <Button
                    icon="pi pi-star-fill"
                    text
                    rounded
                    size="small"
                    class="text-yellow-500"
                    @click="togglePriority(todo.id)"
                    v-tooltip.top="'Priorität entfernen'"
                  />
                  <Button
                    icon="pi pi-inbox"
                    text
                    rounded
                    size="small"
                    @click="archiveTodo(todo.id)"
                    v-tooltip.top="'Archivieren'"
                  />
                  <Button
                    icon="pi pi-trash"
                    text
                    rounded
                    size="small"
                    severity="danger"
                    @click="deleteTodo(todo.id)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="regularTodos.length > 0" class="mb-4">
          <span class="font-semibold text-sm mb-2 block">Aufgaben</span>
          <div class="flex flex-column gap-2">
            <div
              v-for="todo in displayedRegularTodos"
              :key="todo.id"
              class="todo-item p-3 border-round"
            >
              <div class="flex align-items-start gap-3">
                <Checkbox
                  :modelValue="todo.completed"
                  @update:modelValue="toggleTodo(todo.id)"
                  binary
                />
                <div class="flex-1">
                  <p class="m-0" :class="{ 'line-through text-color-secondary': todo.completed }">
                    {{ todo.title }}
                  </p>
                  <p
                    v-if="todo.dueDate"
                    class="text-xs m-0 mt-1"
                    :class="isOverdue(todo.dueDate) ? 'text-red-500' : 'text-color-secondary'"
                  >
                    <i class="pi pi-calendar mr-1"></i>
                    {{ formatDate(todo.dueDate) }}
                  </p>
                </div>
                <div class="flex gap-1">
                  <Button
                    icon="pi pi-star"
                    text
                    rounded
                    size="small"
                    @click="togglePriority(todo.id)"
                    v-tooltip.top="'Als Priorität markieren'"
                  />
                  <Button
                    icon="pi pi-inbox"
                    text
                    rounded
                    size="small"
                    @click="archiveTodo(todo.id)"
                    v-tooltip.top="'Archivieren'"
                  />
                  <Button
                    icon="pi pi-trash"
                    text
                    rounded
                    size="small"
                    severity="danger"
                    @click="deleteTodo(todo.id)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <Button
          v-if="hasMoreTodos"
          label="Mehr anzeigen"
          icon="pi pi-chevron-down"
          text
          class="w-full"
          @click="showAllTodosDialog = true"
        />

        <div v-if="todos.length === 0" class="text-center py-6">
          <i class="pi pi-check-circle text-4xl text-color-secondary mb-3"></i>
          <p class="text-color-secondary m-0">Keine Aufgaben vorhanden</p>
        </div>
      </div>
    </template>
  </Card>

  <Dialog
    v-model:visible="showAllTodosDialog"
    header="Alle Aufgaben"
    :modal="true"
    :style="{ width: '90vw', maxWidth: '800px' }"
  >
    <div style="max-height: 60vh; overflow-y: auto;">
      <div v-if="priorityTodos.length > 0" class="mb-4">
        <div class="flex align-items-center gap-2 mb-2">
          <i class="pi pi-star-fill text-yellow-500"></i>
          <span class="font-semibold text-sm">Prioritäten</span>
        </div>
        <div class="flex flex-column gap-2">
          <div
            v-for="todo in priorityTodos"
            :key="todo.id"
            class="todo-item p-3 border-round"
          >
            <div class="flex align-items-start gap-3">
              <Checkbox
                :modelValue="todo.completed"
                @update:modelValue="toggleTodo(todo.id)"
                binary
              />
              <div class="flex-1">
                <p class="m-0" :class="{ 'line-through text-color-secondary': todo.completed }">
                  {{ todo.title }}
                </p>
                <p
                  v-if="todo.dueDate"
                  class="text-xs m-0 mt-1"
                  :class="isOverdue(todo.dueDate) ? 'text-red-500' : 'text-color-secondary'"
                >
                  <i class="pi pi-calendar mr-1"></i>
                  {{ formatDate(todo.dueDate) }}
                </p>
              </div>
              <div class="flex gap-1">
                <Button
                  icon="pi pi-star-fill"
                  text
                  rounded
                  size="small"
                  class="text-yellow-500"
                  @click="togglePriority(todo.id)"
                  v-tooltip.top="'Priorität entfernen'"
                />
                <Button
                  icon="pi pi-trash"
                  text
                  rounded
                  size="small"
                  severity="danger"
                  @click="deleteTodo(todo.id)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="regularTodos.length > 0" class="mb-4">
        <span class="font-semibold text-sm mb-2 block">Aufgaben</span>
        <div class="flex flex-column gap-2">
          <div
            v-for="todo in regularTodos"
            :key="todo.id"
            class="todo-item p-3 border-round"
          >
            <div class="flex align-items-start gap-3">
              <Checkbox
                :modelValue="todo.completed"
                @update:modelValue="toggleTodo(todo.id)"
                binary
              />
              <div class="flex-1">
                <p class="m-0" :class="{ 'line-through text-color-secondary': todo.completed }">
                  {{ todo.title }}
                </p>
                <p
                  v-if="todo.dueDate"
                  class="text-xs m-0 mt-1"
                  :class="isOverdue(todo.dueDate) ? 'text-red-500' : 'text-color-secondary'"
                >
                  <i class="pi pi-calendar mr-1"></i>
                  {{ formatDate(todo.dueDate) }}
                </p>
              </div>
              <div class="flex gap-1">
                <Button
                  icon="pi pi-star"
                  text
                  rounded
                  size="small"
                  @click="togglePriority(todo.id)"
                  v-tooltip.top="'Als Priorität markieren'"
                />
                <Button
                  icon="pi pi-trash"
                  text
                  rounded
                  size="small"
                  severity="danger"
                  @click="deleteTodo(todo.id)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="completedTodos.length > 0">
        <span class="font-semibold text-sm mb-2 block text-color-secondary">Erledigt</span>
        <div class="flex flex-column gap-2">
          <div
            v-for="todo in completedTodos"
            :key="todo.id"
            class="todo-item p-3 border-round"
          >
            <div class="flex align-items-start gap-3">
              <Checkbox
                :modelValue="todo.completed"
                @update:modelValue="toggleTodo(todo.id)"
                binary
              />
              <div class="flex-1">
                <p class="m-0 line-through text-color-secondary">
                  {{ todo.title }}
                </p>
              </div>
              <Button
                icon="pi pi-trash"
                text
                rounded
                size="small"
                severity="danger"
                @click="deleteTodo(todo.id)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Dialog>

  <Dialog
    v-model:visible="showArchiveDialog"
    header="Archivierte Aufgaben"
    :modal="true"
    :style="{ width: '90vw', maxWidth: '800px' }"
  >
    <div v-if="archivedTodos.length > 0" style="max-height: 60vh; overflow-y: auto;">
      <div class="flex flex-column gap-2">
        <div
          v-for="todo in archivedTodos"
          :key="todo.id"
          class="todo-item p-3 border-round"
        >
          <div class="flex align-items-start gap-3">
            <Checkbox
              :modelValue="todo.completed"
              @update:modelValue="toggleTodo(todo.id)"
              binary
            />
            <div class="flex-1">
              <p class="m-0" :class="{ 'line-through text-color-secondary': todo.completed }">
                {{ todo.title }}
              </p>
              <p
                v-if="todo.dueDate"
                class="text-xs m-0 mt-1"
                :class="isOverdue(todo.dueDate) ? 'text-red-500' : 'text-color-secondary'"
              >
                <i class="pi pi-calendar mr-1"></i>
                {{ formatDate(todo.dueDate) }}
              </p>
            </div>
            <div class="flex gap-1">
              <Button
                icon="pi pi-replay"
                text
                rounded
                size="small"
                @click="unarchiveTodo(todo.id)"
                v-tooltip.top="'Wiederherstellen'"
              />
              <Button
                icon="pi pi-trash"
                text
                rounded
                size="small"
                severity="danger"
                @click="deleteTodo(todo.id)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="text-center py-6">
      <i class="pi pi-inbox text-4xl text-color-secondary mb-3"></i>
      <p class="text-color-secondary m-0">Keine archivierten Aufgaben</p>
    </div>
  </Dialog>
</template>

<style scoped>
.todo-item {
  transition: all 0.2s ease;
  border: 1px solid var(--surface-border);
  background: var(--surface-card);
}

.todo-item:hover {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

@media (max-width: 450px) {
  .add-todo-btn :deep(.p-button-label) {
    display: none;
  }
}

.add-todo-btn {
  min-width: 2.5rem;
  flex-shrink: 0;
}

.calendar-field {
  flex: 1 1 auto;
  min-width: 0;
}

.calendar-field :deep(.p-inputtext) {
  min-width: 0;
  flex: 1 1 auto;
}

.calendar-field :deep(.p-datepicker-trigger) {
  flex-shrink: 0;
}
</style>
