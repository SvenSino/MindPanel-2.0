import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export interface Note {
  id: number
  title: string
  content: string
  createdAt: string
  archived?: boolean
}

export interface Todo {
  id: number
  title: string
  completed: boolean
  isPriority: boolean
  dueDate: string | null
  createdAt: string
  archived?: boolean
}

export type WidgetType = 'weather' | 'todos' | 'calendar' | 'notes' | 'pomodoro'

export interface Widget {
  id: string
  type: WidgetType
  enabled: boolean
  title: string
}

const WIDGET_LAYOUT_KEY = 'mindpanel_widgets'
const NOTES_KEY = 'mindpanel_notes'
const TODOS_KEY = 'mindpanel_todos'
const ARCHIVED_NOTES_KEY = 'mindpanel_archived_notes'
const ARCHIVED_TODOS_KEY = 'mindpanel_archived_todos'

const getDefaultWidgets = (): Widget[] => [
  { id: 'todos', type: 'todos', enabled: true, title: 'Aufgaben' },
  { id: 'notes', type: 'notes', enabled: true, title: 'Notizen' },
  { id: 'calendar', type: 'calendar', enabled: true, title: 'Kalender' },
  { id: 'weather', type: 'weather', enabled: true, title: 'Wetter' },
  { id: 'pomodoro', type: 'pomodoro', enabled: true, title: 'Pomodoro' },
]

export const useDashboardStore = defineStore('dashboard', () => {
  // State
  const widgets = ref<Widget[]>(getDefaultWidgets())
  const notes = ref<Note[]>([])
  const todos = ref<Todo[]>([])
  const archivedNotes = ref<Note[]>([])
  const archivedTodos = ref<Todo[]>([])

  let todoIdCounter = 1
  let noteIdCounter = 1


  function loadWidgets() {
    if (typeof window === 'undefined') return
    const raw = window.localStorage.getItem(WIDGET_LAYOUT_KEY)
    if (!raw) {
      widgets.value = getDefaultWidgets()
      return
    }
    try {
      const parsed = JSON.parse(raw) as Widget[]
      if (Array.isArray(parsed)) {
        const defaultWidgets = getDefaultWidgets()
        const mergedWidgets = [...parsed]

        defaultWidgets.forEach(defaultWidget => {
          if (!mergedWidgets.find(w => w.id === defaultWidget.id)) {
            mergedWidgets.push(defaultWidget)
          }
        })

        widgets.value = mergedWidgets
        saveWidgets()
      }
    } catch (e) {
      console.warn('Konnte Widgets nicht laden:', e)
      widgets.value = getDefaultWidgets()
    }
  }

  function saveWidgets() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(WIDGET_LAYOUT_KEY, JSON.stringify(widgets.value))
  }

  function updateWidgetOrder(newOrder: Widget[]) {
    widgets.value = newOrder
    saveWidgets()
  }

  function toggleWidget(widgetId: string) {
    const widget = widgets.value.find(w => w.id === widgetId)
    if (widget) {
      widget.enabled = !widget.enabled
      saveWidgets()
    }
  }

  function resetWidgets() {
    widgets.value = getDefaultWidgets()
    saveWidgets()
  }

  // Computed: enabled widgets only
  const enabledWidgets = ref<Widget[]>([])
  watch(widgets, () => {
    enabledWidgets.value = widgets.value.filter(w => w.enabled)
  }, { deep: true, immediate: true })


  function loadNotes() {
    if (typeof window === 'undefined') return
    const raw = window.localStorage.getItem(NOTES_KEY)
    if (!raw) return
    try {
      const parsed = JSON.parse(raw) as Note[]
      if (Array.isArray(parsed)) {
        notes.value = parsed
        if (parsed.length > 0) {
          noteIdCounter = Math.max(...parsed.map(n => n.id)) + 1
        }
      }
    } catch (e) {
      console.warn('Konnte Notizen nicht laden:', e)
    }
  }

  function saveNotes() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(NOTES_KEY, JSON.stringify(notes.value))
  }

  function addNote(title: string, content: string) {
    notes.value.push({
      id: noteIdCounter++,
      title,
      content,
      createdAt: new Date().toISOString(),
    })
  }

  function updateNote(id: number, title: string, content: string) {
    const note = notes.value.find(n => n.id === id)
    if (note) {
      note.title = title
      note.content = content
    }
  }

  function deleteNote(id: number) {
    notes.value = notes.value.filter(n => n.id !== id)
  }

  function archiveNote(id: number) {
    const note = notes.value.find(n => n.id === id)
    if (note) {
      note.archived = true
      archivedNotes.value.push({ ...note })
      notes.value = notes.value.filter(n => n.id !== id)
      saveArchivedNotes()
    }
  }

  function unarchiveNote(id: number) {
    const note = archivedNotes.value.find(n => n.id === id)
    if (note) {
      note.archived = false
      notes.value.push({ ...note })
      archivedNotes.value = archivedNotes.value.filter(n => n.id !== id)
      saveArchivedNotes()
    }
  }

  function loadArchivedNotes() {
    if (typeof window === 'undefined') return
    const raw = window.localStorage.getItem(ARCHIVED_NOTES_KEY)
    if (!raw) return
    try {
      const parsed = JSON.parse(raw) as Note[]
      if (Array.isArray(parsed)) {
        archivedNotes.value = parsed
      }
    } catch (e) {
      console.warn('Konnte archivierte Notizen nicht laden:', e)
    }
  }

  function saveArchivedNotes() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(ARCHIVED_NOTES_KEY, JSON.stringify(archivedNotes.value))
  }


  function loadTodos() {
    if (typeof window === 'undefined') return
    const raw = window.localStorage.getItem(TODOS_KEY)
    if (!raw) return
    try {
      const parsed = JSON.parse(raw) as Todo[]
      if (Array.isArray(parsed)) {
        todos.value = parsed
        if (parsed.length > 0) {
          todoIdCounter = Math.max(...parsed.map(t => t.id)) + 1
        }
      }
    } catch (e) {
      console.warn('Konnte Todos nicht laden:', e)
    }
  }

  function saveTodos() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(TODOS_KEY, JSON.stringify(todos.value))
  }

  function addTodo(title: string, dueDate: string | null = null) {
    todos.value.push({
      id: todoIdCounter++,
      title,
      completed: false,
      isPriority: false,
      dueDate,
      createdAt: new Date().toISOString(),
    })
  }

  function updateTodo(id: number, updates: Partial<Omit<Todo, 'id' | 'createdAt'>>) {
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      Object.assign(todo, updates)
    }
  }

  function toggleTodo(id: number) {
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      todo.completed = !todo.completed
    }
  }

  function togglePriority(id: number): boolean {
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      const priorityCount = todos.value.filter(t => t.isPriority && t.id !== id).length
      if (!todo.isPriority && priorityCount >= 3) {
        return false
      }
      todo.isPriority = !todo.isPriority
      return true
    }
    return false
  }

  function deleteTodo(id: number) {
    todos.value = todos.value.filter(t => t.id !== id)
  }

  function archiveTodo(id: number) {
    const todo = todos.value.find(t => t.id === id)
    if (todo) {
      todo.archived = true
      archivedTodos.value.push({ ...todo })
      todos.value = todos.value.filter(t => t.id !== id)
      saveArchivedTodos()
    }
  }

  function unarchiveTodo(id: number) {
    const todo = archivedTodos.value.find(t => t.id === id)
    if (todo) {
      todo.archived = false
      todos.value.push({ ...todo })
      archivedTodos.value = archivedTodos.value.filter(t => t.id !== id)
      saveArchivedTodos()
    }
  }

  function loadArchivedTodos() {
    if (typeof window === 'undefined') return
    const raw = window.localStorage.getItem(ARCHIVED_TODOS_KEY)
    if (!raw) return
    try {
      const parsed = JSON.parse(raw) as Todo[]
      if (Array.isArray(parsed)) {
        archivedTodos.value = parsed
      }
    } catch (e) {
      console.warn('Konnte archivierte Todos nicht laden:', e)
    }
  }

  function saveArchivedTodos() {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(ARCHIVED_TODOS_KEY, JSON.stringify(archivedTodos.value))
  }


  function initializeStore() {
    loadWidgets()
    loadNotes()
    loadTodos()
    loadArchivedNotes()
    loadArchivedTodos()
  }

  // Watcher für Änderungen und automatisches Speichern
  watch(notes, saveNotes, { deep: true })
  watch(todos, saveTodos, { deep: true })
  watch(archivedNotes, saveArchivedNotes, { deep: true })
  watch(archivedTodos, saveArchivedTodos, { deep: true })

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
