<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import type { Note } from '@/stores/dashboard'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'

const dashboardStore = useDashboardStore()

const notes = computed(() => dashboardStore.notes)
const displayedNotes = computed(() => notes.value.slice(0, 4))
const hasMoreNotes = computed(() => notes.value.length > 4)

const showDialog = ref(false)
const showAllNotesDialog = ref(false)
const showArchiveDialog = ref(false)
const editingNote = ref<Note | null>(null)
const noteTitle = ref('')
const noteContent = ref('')

const archivedNotes = computed(() => dashboardStore.archivedNotes)

function openNewNoteDialog() {
  editingNote.value = null
  noteTitle.value = ''
  noteContent.value = ''
  showDialog.value = true
}

function openEditNoteDialog(note: Note) {
  editingNote.value = note
  noteTitle.value = note.title
  noteContent.value = note.content
  showDialog.value = true
}

function saveNote() {
  if (!noteTitle.value.trim()) return

  if (editingNote.value) {
    dashboardStore.updateNote(editingNote.value.id, noteTitle.value.trim(), noteContent.value.trim())
  } else {
    dashboardStore.addNote(noteTitle.value.trim(), noteContent.value.trim())
  }

  showDialog.value = false
  noteTitle.value = ''
  noteContent.value = ''
  editingNote.value = null
}

function deleteNote(id: number) {
  dashboardStore.deleteNote(id)
}

function archiveNote(id: number) {
  dashboardStore.archiveNote(id)
}

function unarchiveNote(id: number) {
  dashboardStore.unarchiveNote(id)
}

function truncate(text: string, length: number): string {
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}
</script>

<template>
  <Card class="widget-card h-full">
    <template #header>
      <div class="widget-header drag-handle">
        <div class="flex align-items-center gap-2">
          <i class="pi pi-book text-xl"></i>
          <h3 class="m-0 font-semibold">Notizen</h3>
        </div>
        <div class="flex gap-1">
          <Button
            icon="pi pi-inbox"
            rounded
            text
            @click="showArchiveDialog = true"
            v-tooltip.top="'Archiv'"
          />
          <Button
            icon="pi pi-plus"
            rounded
            text
            @click="openNewNoteDialog"
            v-tooltip.top="'Neue Notiz'"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div class="widget-content">
        <div v-if="notes.length > 0" class="flex flex-column gap-3">
          <div
            v-for="note in displayedNotes"
            :key="note.id"
            class="note-card p-3 border-round cursor-pointer"
            @click="openEditNoteDialog(note)"
          >
            <div class="flex justify-content-between align-items-start mb-2">
              <h4 class="m-0 font-semibold">{{ note.title }}</h4>
              <div class="flex gap-1">
                <Button
                  icon="pi pi-inbox"
                  text
                  rounded
                  size="small"
                  @click.stop="archiveNote(note.id)"
                  v-tooltip.top="'Archivieren'"
                />
                <Button
                  icon="pi pi-trash"
                  text
                  rounded
                  size="small"
                  severity="danger"
                  @click.stop="deleteNote(note.id)"
                />
              </div>
            </div>
            <p class="m-0 text-sm text-color-secondary line-height-3">
              {{ truncate(note.content, 120) }}
            </p>
            <p class="text-xs text-color-secondary m-0 mt-2">
              <i class="pi pi-clock mr-1"></i>
              {{ new Date(note.createdAt).toLocaleDateString('de-DE') }}
            </p>
          </div>

          <Button
            v-if="hasMoreNotes"
            label="Mehr anzeigen"
            icon="pi pi-chevron-down"
            text
            class="w-full"
            @click="showAllNotesDialog = true"
          />
        </div>

        <div v-else class="text-center py-6">
          <i class="pi pi-book text-4xl text-color-secondary mb-3"></i>
          <p class="text-color-secondary m-0 mb-3">Keine Notizen vorhanden</p>
          <Button
            label="Erste Notiz erstellen"
            icon="pi pi-plus"
            @click="openNewNoteDialog"
          />
        </div>
      </div>
    </template>
  </Card>

  <Dialog
    v-model:visible="showAllNotesDialog"
    header="Alle Notizen"
    :modal="true"
    :style="{ width: '90vw', maxWidth: '800px' }"
  >
    <div class="flex flex-column gap-3" style="max-height: 60vh; overflow-y: auto;">
      <div
        v-for="note in notes"
        :key="note.id"
        class="note-card p-3 border-round cursor-pointer"
        @click="openEditNoteDialog(note); showAllNotesDialog = false"
      >
        <div class="flex justify-content-between align-items-start mb-2">
          <h4 class="m-0 font-semibold">{{ note.title }}</h4>
          <Button
            icon="pi pi-trash"
            text
            rounded
            size="small"
            severity="danger"
            @click.stop="deleteNote(note.id)"
          />
        </div>
        <p class="m-0 text-sm text-color-secondary line-height-3">
          {{ truncate(note.content, 200) }}
        </p>
        <p class="text-xs text-color-secondary m-0 mt-2">
          <i class="pi pi-clock mr-1"></i>
          {{ new Date(note.createdAt).toLocaleDateString('de-DE') }}
        </p>
      </div>
    </div>
  </Dialog>

  <Dialog
    v-model:visible="showDialog"
    :header="editingNote ? 'Notiz bearbeiten' : 'Neue Notiz'"
    :modal="true"
    :style="{ width: '90vw', maxWidth: '600px' }"
  >
    <div class="flex flex-column gap-3">
      <div>
        <label for="note-title" class="block mb-2 font-semibold">Titel</label>
        <InputText
          id="note-title"
          v-model="noteTitle"
          placeholder="Notiz-Titel..."
          class="w-full"
          autofocus
        />
      </div>
      <div>
        <label for="note-content" class="block mb-2 font-semibold">Inhalt</label>
        <Textarea
          id="note-content"
          v-model="noteContent"
          placeholder="Notiz-Inhalt..."
          rows="10"
          class="w-full"
        />
      </div>
    </div>

    <template #footer>
      <Button
        label="Abbrechen"
        text
        @click="showDialog = false"
      />
      <Button
        :label="editingNote ? 'Speichern' : 'Erstellen'"
        @click="saveNote"
        :disabled="!noteTitle.trim()"
      />
    </template>
  </Dialog>

  <Dialog
    v-model:visible="showArchiveDialog"
    header="Archivierte Notizen"
    :modal="true"
    :style="{ width: '90vw', maxWidth: '800px' }"
  >
    <div v-if="archivedNotes.length > 0" class="flex flex-column gap-3" style="max-height: 60vh; overflow-y: auto;">
      <div
        v-for="note in archivedNotes"
        :key="note.id"
        class="note-card p-3 border-round"
      >
        <div class="flex justify-content-between align-items-start mb-2">
          <h4 class="m-0 font-semibold">{{ note.title }}</h4>
          <div class="flex gap-1">
            <Button
              icon="pi pi-replay"
              text
              rounded
              size="small"
              @click="unarchiveNote(note.id)"
              v-tooltip.top="'Wiederherstellen'"
            />
            <Button
              icon="pi pi-trash"
              text
              rounded
              size="small"
              severity="danger"
              @click="deleteNote(note.id)"
            />
          </div>
        </div>
        <p class="m-0 text-sm text-color-secondary line-height-3">
          {{ truncate(note.content, 200) }}
        </p>
        <p class="text-xs text-color-secondary m-0 mt-2">
          <i class="pi pi-clock mr-1"></i>
          {{ new Date(note.createdAt).toLocaleDateString('de-DE') }}
        </p>
      </div>
    </div>
    <div v-else class="text-center py-6">
      <i class="pi pi-inbox text-4xl text-color-secondary mb-3"></i>
      <p class="text-color-secondary m-0">Keine archivierten Notizen</p>
    </div>
  </Dialog>
</template>

<style scoped>
.note-card {
  transition: all 0.2s ease;
  border: 1px solid var(--surface-border);
  background: var(--surface-card);
}

.note-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
}
</style>
