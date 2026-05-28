import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import api from '@/services/api'

export interface Note {
  id: string
  title: string
  content: string
  createdAt: string
  archived: boolean
}

export interface Todo {
  id: string
  title: string
  completed: boolean
  isPriority: boolean
  dueDate: string | null
  createdAt: string
  archived: boolean
}

export type WidgetType = 'weather' | 'todos' | 'calendar' | 'notes' | 'pomodoro'

export interface Widget {
  id: string
  type: WidgetType
  enabled: boolean
  title: string
}

export const useDashboardStore = defineStore('dashboard', () => {
  const widgets = ref<Widget[]>([])
  const notes = ref<Note[]>([])
  const todos = ref<Todo[]>([])
  const archivedNotes = ref<Note[]>([])
  const archivedTodos = ref<Todo[]>([])
  const enabledWidgets = ref<Widget[]>([])

  watch(widgets, () => {
    enabledWidgets.value = widgets.value.filter(w => w.enabled)
  }, { deep: true, immediate: true })

  // --- Widgets ---

  async function updateWidgetOrder(newOrder: Widget[]) {
    widgets.value = newOrder
    await api.put('/widgets', { widgets: newOrder })
  }

  async function toggleWidget(widgetId: string) {
    const widget = widgets.value.find(w => w.id === widgetId)
    if (widget) {
      widget.enabled = !widget.enabled
      await api.put('/widgets', { widgets: widgets.value })
    }
  }

  async function resetWidgets() {
    const { data } = await api.post('/widgets/reset')
    widgets.value = data.widgets
  }

  // --- Notes ---

  async function addNote(title: string, content: string) {
    const { data } = await api.post('/notes', { title, content })
    notes.value.push(data)
  }

  async function updateNote(id: string, title: string, content: string) {
    const { data } = await api.put(`/notes/${id}`, { title, content })
    const idx = notes.value.findIndex(n => n.id === id)
    if (idx !== -1) notes.value[idx] = data
  }

  async function deleteNote(id: string) {
    await api.delete(`/notes/${id}`)
    notes.value = notes.value.filter(n => n.id !== id)
  }

  async function archiveNote(id: string) {
    await api.post(`/notes/${id}/archive`)
    const note = notes.value.find(n => n.id === id)
    if (note) {
      notes.value = notes.value.filter(n => n.id !== id)
      archivedNotes.value.push({ ...note, archived: true })
    }
  }

  async function unarchiveNote(id: string) {
    await api.post(`/notes/${id}/unarchive`)
    const note = archivedNotes.value.find(n => n.id === id)
    if (note) {
      archivedNotes.value = archivedNotes.value.filter(n => n.id !== id)
      notes.value.push({ ...note, archived: false })
    }
  }

  // --- Todos ---

  async function addTodo(title: string, dueDate: string | null = null) {
    const { data } = await api.post('/todos', { title, dueDate })
    todos.value.push(data)
  }

  async function updateTodo(id: string, updates: Partial<Omit<Todo, 'id' | 'createdAt'>>) {
    const { data } = await api.put(`/todos/${id}`, updates)
    const idx = todos.value.findIndex(t => t.id === id)
    if (idx !== -1) todos.value[idx] = data
  }

  async function toggleTodo(id: string) {
    const todo = todos.value.find(t => t.id === id)
    if (todo) await updateTodo(id, { completed: !todo.completed })
  }

  async function togglePriority(id: string): Promise<boolean> {
    const todo = todos.value.find(t => t.id === id)
    if (!todo) return false
    try {
      await updateTodo(id, { isPriority: !todo.isPriority })
      return true
    } catch (e: any) {
      if (e.response?.status === 400) return false
      throw e
    }
  }

  async function deleteTodo(id: string) {
    await api.delete(`/todos/${id}`)
    todos.value = todos.value.filter(t => t.id !== id)
  }

  async function archiveTodo(id: string) {
    await api.post(`/todos/${id}/archive`)
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      todos.value = todos.value.filter(t => t.id !== id)
      archivedTodos.value.push({ ...todo, archived: true })
    }
  }

  async function unarchiveTodo(id: string) {
    await api.post(`/todos/${id}/unarchive`)
    const todo = archivedTodos.value.find(t => t.id === id)
    if (todo) {
      archivedTodos.value = archivedTodos.value.filter(t => t.id !== id)
      todos.value.push({ ...todo, archived: false })
    }
  }

  async function initializeStore() {
    const [widgetsRes, notesRes, archivedNotesRes, todosRes, archivedTodosRes] = await Promise.all([
      api.get('/widgets'),
      api.get('/notes'),
      api.get('/notes/archived'),
      api.get('/todos'),
      api.get('/todos/archived'),
    ])
    widgets.value = widgetsRes.data.widgets
    notes.value = notesRes.data
    archivedNotes.value = archivedNotesRes.data
    todos.value = todosRes.data
    archivedTodos.value = archivedTodosRes.data
  }

  return {
    widgets,
    enabledWidgets,
    notes,
    todos,
    archivedNotes,
    archivedTodos,
    updateWidgetOrder,
    toggleWidget,
    resetWidgets,
    addNote,
    updateNote,
    deleteNote,
    archiveNote,
    unarchiveNote,
    addTodo,
    updateTodo,
    toggleTodo,
    togglePriority,
    deleteTodo,
    archiveTodo,
    unarchiveTodo,
    initializeStore,
  }
})
