# Pomodoro Widget - Ausführliche Code-Erklärung

Diese Datei erklärt den kompletten Code des Pomodoro Widgets, alle verwendeten Vue-Konzepte und wie alles zusammenarbeitet.

## Inhaltsverzeichnis

1. [Store-Architektur (pomodoro.ts)](#store-architektur)
2. [Vue-Konzepte erklärt](#vue-konzepte)
3. [Component-Struktur (PomodoroWidget.vue)](#component-struktur)
4. [Datenfluss und Interaktion](#datenfluss)

---

## Store-Architektur

### Was ist ein Store?

Ein Store ist ein zentraler Datenspeicher für deine Anwendung. Statt dass jede Komponente ihre eigenen Daten verwaltet, kannst du Daten im Store ablegen, die dann von mehreren Komponenten genutzt werden können.

**Warum brauchen wir einen Store für den Pomodoro Timer?**
- Der Timer-Zustand (läuft er gerade, wie viel Zeit ist noch übrig) muss erhalten bleiben, auch wenn die Komponente neu gerendert wird
- Verschiedene Teile der App könnten auf die Pomodoro-Daten zugreifen wollen
- Wir wollen die Einstellungen in LocalStorage speichern - das ist im Store einfacher zu verwalten

### Store-Struktur (`src/stores/pomodoro.ts`)

```typescript
import { defineStore } from 'pinia'
import { ref, computed, watch, nextTick } from 'vue'
```

**Imports erklärt:**
- `defineStore`: Pinia-Funktion zum Erstellen eines Stores
- `ref`: Erstellt eine reaktive Variable (wenn sich der Wert ändert, aktualisiert sich die UI)
- `computed`: Erstellt einen berechneten Wert, der sich automatisch aktualisiert
- `watch`: Beobachtet Variablen und führt Code aus, wenn sie sich ändern
- `nextTick`: Wartet bis Vue die DOM-Updates durchgeführt hat

### Refs (Reaktive Variablen)

```typescript
const focusDuration = ref(25) // minutes
const breakDuration = ref(5) // minutes
const longBreakDuration = ref(15) // minutes
```

**Was ist ein `ref`?**
- Ein `ref` ist wie eine Box, die einen Wert enthält
- Vue "beobachtet" diese Box
- Wenn du den Wert änderst, merkt Vue das und aktualisiert automatisch alle Stellen in der UI, die diesen Wert anzeigen

**Beispiel:**
```typescript
const count = ref(0)  // Erstellt eine ref mit Wert 0
count.value = 5       // Ändert den Wert auf 5 - Vue aktualisiert die UI automatisch
```

Im Code:
- `focusDuration` = Wie lange eine Fokus-Phase dauert (Standard: 25 Min)
- `breakDuration` = Wie lange eine kurze Pause dauert (Standard: 5 Min)
- `longBreakDuration` = Wie lange eine lange Pause dauert (Standard: 15 Min)

```typescript
const currentMode = ref<TimerMode>('focus')
const timeLeft = ref(0) // seconds
const isRunning = ref(false)
```

- `currentMode`: In welchem Modus sind wir? ('focus', 'break', oder 'longBreak')
- `timeLeft`: Wie viele Sekunden sind noch übrig?
- `isRunning`: Läuft der Timer gerade?

```typescript
const completedPomodoros = ref(0)
const currentFlow = ref(1) // 1-4
const autoStart = ref(false)
```

- `completedPomodoros`: Wie viele Pomodoros wurden insgesamt abgeschlossen?
- `currentFlow`: Wo sind wir im Flow? (1-4, nach 4 kommt lange Pause)
- `autoStart`: Soll der nächste Timer automatisch starten?

### Computed Properties (Berechnete Werte)

```typescript
const currentDuration = computed(() => {
  if (currentMode.value === 'focus') return focusDuration.value
  if (currentMode.value === 'longBreak') return longBreakDuration.value
  return breakDuration.value
})
```

**Was ist ein `computed`?**
- Ein `computed` ist wie eine Formel in Excel
- Es berechnet einen Wert basierend auf anderen Werten
- Wenn sich die Quellwerte ändern, wird das Ergebnis automatisch neu berechnet
- **WICHTIG**: Der Wert wird gecacht - die Berechnung läuft nur, wenn sich die Abhängigkeiten ändern

**Im Kontext:**
`currentDuration` gibt die aktuelle Dauer zurück, je nachdem in welchem Modus wir sind:
- Fokus → focusDuration (25 Min)
- Lange Pause → longBreakDuration (15 Min)
- Kurze Pause → breakDuration (5 Min)

**Warum computed statt Funktion?**
```typescript
// ❌ Normale Funktion - wird bei jedem Zugriff neu berechnet
function getCurrentDuration() {
  if (currentMode.value === 'focus') return focusDuration.value
  // ...
}

// ✅ Computed - wird nur neu berechnet wenn sich currentMode ändert
const currentDuration = computed(() => {
  if (currentMode.value === 'focus') return focusDuration.value
  // ...
})
```

```typescript
const totalSeconds = computed(() => currentDuration.value * 60)
```

Wandelt die Minuten in Sekunden um. Nutzt `currentDuration` (das selbst computed ist).

**Chain von Computed Properties:**
1. `currentMode` ändert sich von 'focus' zu 'break'
2. `currentDuration` wird neu berechnet (25 → 5)
3. `totalSeconds` wird neu berechnet (1500 → 300)
4. Alle UI-Elemente, die diese Werte nutzen, aktualisieren sich automatisch

```typescript
const progress = computed(() => {
  if (totalSeconds.value === 0) return 0
  return ((totalSeconds.value - timeLeft.value) / totalSeconds.value) * 100
})
```

Berechnet den Fortschritt in Prozent für die Progressbar:
- Wenn 10 von 25 Minuten vergangen sind: (1500-900)/1500 * 100 = 40%

```typescript
const displayMinutes = computed(() => Math.floor(timeLeft.value / 60))
const displaySeconds = computed(() => timeLeft.value % 60)
```

Wandelt die Sekunden in Minuten:Sekunden Format:
- `timeLeft = 125` Sekunden
- `displayMinutes = 2` (125 / 60 = 2.08, abgerundet)
- `displaySeconds = 5` (125 % 60 = 5, Rest der Division)
- Anzeige: "02:05"

### Watchers (Beobachter)

```typescript
watch([focusDuration, breakDuration, longBreakDuration], () => {
  if (!isRunning.value) {
    timeLeft.value = totalSeconds.value
  }
})
```

**Was ist ein `watch`?**
- Ein `watch` beobachtet eine oder mehrere reaktive Variablen
- Wenn sich eine davon ändert, wird die angegebene Funktion ausgeführt
- **Wichtig**: Ein watch hat Seiteneffekte - es ändert aktiv Daten

**Im Kontext:**
Dieser Watcher beobachtet die drei Dauer-Einstellungen:
1. User ändert `focusDuration` von 25 auf 20 Minuten
2. Der Watcher merkt das
3. Wenn der Timer nicht läuft (`!isRunning.value`), wird `timeLeft` auf die neue Dauer gesetzt
4. Die Anzeige springt sofort von "25:00" auf "20:00"

**Warum dieser Check?**
```typescript
if (!isRunning.value) {
```
Wenn der Timer läuft, wollen wir nicht plötzlich die Zeit ändern. Das wäre verwirrend für den User.

```typescript
watch([focusDuration, breakDuration, longBreakDuration, completedPomodoros, currentFlow, autoStart], saveSettings)
```

Dieser Watcher speichert die Einstellungen automatisch in LocalStorage, sobald sich eine davon ändert.

**Unterschied Computed vs Watch:**

```typescript
// ✅ Computed - gibt einen Wert zurück, keine Seiteneffekte
const doubled = computed(() => count.value * 2)

// ✅ Watch - führt Aktionen aus (Seiteneffekte)
watch(count, () => {
  localStorage.setItem('count', count.value.toString())
  console.log('Count changed!')
})
```

### Timer-Funktionen

```typescript
function start() {
  if (timeLeft.value === 0) {
    timeLeft.value = totalSeconds.value
  }

  isRunning.value = true

  if (intervalId !== null) {
    clearInterval(intervalId)
  }

  intervalId = window.setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      complete()
    }
  }, 1000)
}
```

**Schritt-für-Schritt:**
1. **Initialisierung**: Wenn `timeLeft` 0 ist, setze es auf die volle Dauer
2. **Status setzen**: `isRunning = true`
3. **Alte Intervals löschen**: Sicherheit, falls noch ein alter Interval läuft
4. **Interval starten**:
   - Jede Sekunde (1000ms) wird die Funktion ausgeführt
   - `timeLeft` wird um 1 reduziert
   - Wenn `timeLeft` 0 erreicht, wird `complete()` aufgerufen

**Was ist ein Interval?**
```typescript
// Führt die Funktion alle 1000ms (1 Sekunde) aus
intervalId = window.setInterval(() => {
  console.log('Tick!')
}, 1000)

// Stoppt das Interval
clearInterval(intervalId)
```

```typescript
function pause() {
  isRunning.value = false
  if (intervalId !== null) {
    clearInterval(intervalId)
    intervalId = null
  }
}
```

Stoppt den Timer:
1. `isRunning` auf false setzen
2. Interval löschen (sonst läuft es weiter im Hintergrund)

```typescript
function reset() {
  pause()
  currentMode.value = 'focus'
  currentFlow.value = 1
  timeLeft.value = totalSeconds.value
}
```

Reset-Button:
1. Timer pausieren
2. Zurück zum Fokus-Modus
3. Flow auf 1/4 setzen
4. Zeit auf volle Dauer setzen

```typescript
function complete() {
  pause()

  if (currentMode.value === 'focus') {
    completedPomodoros.value++

    // Statistik aktualisieren
    const today = new Date().toISOString().split('T')[0]
    dailyStats.value[today] = (dailyStats.value[today] || 0) + 1
    saveDailyStats()

    // Nach 4 Fokus-Sessions eine lange Pause
    if (currentFlow.value >= 4) {
      currentMode.value = 'longBreak'
      currentFlow.value = 1
    } else {
      currentMode.value = 'break'
      currentFlow.value++
    }
  } else {
    currentMode.value = 'focus'
  }

  // Set timeLeft to new mode duration
  timeLeft.value = totalSeconds.value

  // Optional: Notification
  if ('Notification' in window && Notification.permission === 'granted') {
    const body = currentMode.value === 'focus'
      ? 'Zurück zur Arbeit!'
      : currentMode.value === 'longBreak'
      ? 'Zeit für eine lange Pause!'
      : 'Zeit für eine kurze Pause!'

    new Notification('Pomodoro Timer', { body })
  }

  // Auto-start nächster Timer
  if (autoStart.value) {
    setTimeout(() => start(), 1000)
  }
}
```

**Komplexe Logik - Schritt für Schritt:**

1. **Timer stoppen**
   ```typescript
   pause()
   ```

2. **Wenn eine Fokus-Phase abgeschlossen wurde:**
   ```typescript
   if (currentMode.value === 'focus') {
     completedPomodoros.value++  // Zähler erhöhen
   ```

3. **Statistik speichern:**
   ```typescript
   const today = new Date().toISOString().split('T')[0]  // "2025-12-07"
   dailyStats.value[today] = (dailyStats.value[today] || 0) + 1
   ```
   - Holt das heutige Datum
   - Erhöht den Zähler für heute (oder setzt ihn auf 1, wenn noch nicht vorhanden)

4. **Nächste Phase bestimmen:**
   ```typescript
   if (currentFlow.value >= 4) {
     currentMode.value = 'longBreak'  // Lange Pause nach 4 Pomodoros
     currentFlow.value = 1            // Flow zurücksetzen
   } else {
     currentMode.value = 'break'      // Kurze Pause
     currentFlow.value++              // Flow erhöhen (1→2, 2→3, 3→4)
   }
   ```

5. **Nach Pause → zurück zu Fokus:**
   ```typescript
   } else {
     currentMode.value = 'focus'
   }
   ```

6. **Zeit für neue Phase setzen:**
   ```typescript
   timeLeft.value = totalSeconds.value
   ```
   Wichtig: `currentMode` hat sich geändert, also gibt `totalSeconds` jetzt die Dauer der neuen Phase zurück!

7. **Browser-Benachrichtigung:**
   ```typescript
   if ('Notification' in window && Notification.permission === 'granted') {
     new Notification('Pomodoro Timer', { body })
   }
   ```

8. **Auto-Start:**
   ```typescript
   if (autoStart.value) {
     setTimeout(() => start(), 1000)  // Wartet 1 Sekunde, dann start()
   }
   ```

### LocalStorage-Persistierung

```typescript
function saveSettings() {
  if (typeof window === 'undefined') return  // SSR-Check
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      focusDuration: focusDuration.value,
      breakDuration: breakDuration.value,
      longBreakDuration: longBreakDuration.value,
      completedPomodoros: completedPomodoros.value,
      currentFlow: currentFlow.value,
      autoStart: autoStart.value,
    })
  )
}
```

**Was passiert hier?**
1. Erstellt ein JavaScript-Objekt mit allen Einstellungen
2. `JSON.stringify()` wandelt es in einen String um
3. `localStorage.setItem()` speichert es im Browser
4. Die Daten bleiben erhalten, auch wenn der Browser geschlossen wird

**LocalStorage:**
```typescript
// Daten speichern
localStorage.setItem('name', 'Max')

// Daten laden
const name = localStorage.getItem('name')  // "Max"

// Daten löschen
localStorage.removeItem('name')
```

```typescript
function loadSettings() {
  if (typeof window === 'undefined') return
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      const data = JSON.parse(saved)
      focusDuration.value = data.focusDuration || 25
      breakDuration.value = data.breakDuration || 5
      longBreakDuration.value = data.longBreakDuration || 15
      completedPomodoros.value = data.completedPomodoros || 0
      currentFlow.value = data.currentFlow || 1
      autoStart.value = data.autoStart ?? false
    } catch (e) {
      console.warn('Could not load pomodoro settings')
    }
  }
}
```

**Laden der Daten:**
1. Holt den String aus LocalStorage
2. `JSON.parse()` wandelt ihn zurück in ein Objekt
3. Setzt alle Werte (mit Fallback-Werten falls nicht vorhanden)
4. `try/catch` fängt Fehler ab (z.B. wenn der String kaputt ist)

**Wichtig:** `??` vs `||`
```typescript
autoStart.value = data.autoStart ?? false  // ✅ Nur wenn undefined/null
autoStart.value = data.autoStart || false  // ❌ Auch wenn false!

// Beispiel:
const value = false
console.log(value || 'default')   // 'default' (falsch!)
console.log(value ?? 'default')   // false (richtig!)
```

### Statistik

```typescript
const statsData = computed(() => {
  const data: { label: string; value: number; date: string }[] = []
  const now = new Date()

  for (let i = 6; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]

    const label = date.toLocaleDateString('de-DE', { weekday: 'short' })

    data.push({
      label,
      value: dailyStats.value[dateStr] || 0,
      date: dateStr
    })
  }

  return data
})
```

**Was passiert hier?**

1. **Array für 7 Tage erstellen:**
   ```typescript
   for (let i = 6; i >= 0; i--)
   ```
   - i=6: vor 6 Tagen
   - i=5: vor 5 Tagen
   - ...
   - i=0: heute

2. **Datum berechnen:**
   ```typescript
   const date = new Date(now)
   date.setDate(date.getDate() - i)
   ```
   - Kopiert das aktuelle Datum
   - Zieht i Tage ab

3. **Datum formatieren:**
   ```typescript
   const dateStr = date.toISOString().split('T')[0]  // "2025-12-07"
   const label = date.toLocaleDateString('de-DE', { weekday: 'short' })  // "So"
   ```

4. **Wert für diesen Tag holen:**
   ```typescript
   value: dailyStats.value[dateStr] || 0
   ```
   - Schaut in `dailyStats` nach dem Datum
   - Wenn nicht vorhanden: 0

**Ergebnis:**
```javascript
[
  { label: "Mo", value: 3, date: "2025-12-01" },
  { label: "Di", value: 5, date: "2025-12-02" },
  { label: "Mi", value: 2, date: "2025-12-03" },
  { label: "Do", value: 4, date: "2025-12-04" },
  { label: "Fr", value: 6, date: "2025-12-05" },
  { label: "Sa", value: 1, date: "2025-12-06" },
  { label: "So", value: 0, date: "2025-12-07" }
]
```

---

## Vue-Konzepte

### Template Syntax

#### Interpolation (Mustache Syntax)

```vue
<div>{{ pomodoroStore.completedPomodoros }}</div>
```

**Was ist `{{ }}`?**
- Zeigt den Wert einer Variable im HTML an
- Vue ersetzt `{{ ... }}` automatisch durch den aktuellen Wert
- Aktualisiert sich automatisch, wenn sich der Wert ändert

```vue
<div class="timer-display">
  {{ formatTime(pomodoroStore.displayMinutes) }}:{{ formatTime(pomodoroStore.displaySeconds) }}
</div>
```

**Funktionsaufrufe in Templates:**
- Du kannst Funktionen direkt im Template aufrufen
- `formatTime()` wird bei jedem Re-Render ausgeführt
- Deshalb: Funktionen im Template sollten einfach und schnell sein

#### v-bind (oder `:`)

```vue
<Button
  :severity="pomodoroStore.currentMode === 'focus' ? 'primary' : 'secondary'"
  :outlined="pomodoroStore.currentMode !== 'focus'"
  :disabled="pomodoroStore.isRunning"
/>
```

**Was ist `:`?**
- Kurzform für `v-bind`
- Bindet ein Attribut an einen JavaScript-Ausdruck
- Ohne `:` wäre es ein statischer String

```vue
<!-- Statischer String -->
<Button severity="primary" />

<!-- Dynamischer Wert -->
<Button :severity="mode" />

<!-- JavaScript-Ausdruck -->
<Button :severity="isActive ? 'primary' : 'secondary'" />
```

**Im Kontext:**
- Wenn `currentMode === 'focus'`: Button ist primary (blau)
- Sonst: Button ist secondary (grau)
- Wenn `isRunning`: Button ist disabled (nicht klickbar)

#### v-model

```vue
<InputNumber
  v-model="pomodoroStore.focusDuration"
  :min="1"
  :max="60"
/>
```

**Was ist `v-model`?**
- Two-Way Data Binding
- Verknüpft ein Input-Element mit einer Variable
- Wenn User den Input ändert → Variable ändert sich
- Wenn Variable sich ändert → Input aktualisiert sich

```vue
<script setup>
const text = ref('Hallo')
</script>

<template>
  <input v-model="text" />
  <p>{{ text }}</p>
</template>
```

**Was passiert hier?**
1. User tippt "Welt" ins Input
2. `text.value` ändert sich auf "Welt"
3. Das `<p>` zeigt automatisch "Welt" an

**Unter der Haube ist v-model:**
```vue
<!-- Das hier: -->
<input v-model="text" />

<!-- Ist das gleiche wie: -->
<input
  :value="text"
  @input="text = $event.target.value"
/>
```

#### v-if / v-else

```vue
<Button
  v-if="!pomodoroStore.isRunning"
  icon="pi pi-play"
  label="Start"
/>
<Button
  v-else
  icon="pi pi-pause"
  label="Pause"
/>
```

**Was ist `v-if`?**
- Bedingtes Rendering
- Element wird nur im DOM erstellt, wenn die Bedingung wahr ist
- Wenn die Bedingung sich ändert, wird das Element hinzugefügt/entfernt

**Im Kontext:**
- Timer läuft nicht (`!isRunning`) → "Start"-Button wird angezeigt
- Timer läuft (`isRunning`) → "Pause"-Button wird angezeigt
- Es existiert immer nur EINER der beiden Buttons im DOM

**v-if vs v-show:**
```vue
<!-- v-if: Element wird aus dem DOM entfernt -->
<div v-if="show">Ich existiere nicht im DOM wenn show=false</div>

<!-- v-show: Element bleibt im DOM, wird nur versteckt (display: none) -->
<div v-show="show">Ich bin im DOM, aber unsichtbar wenn show=false</div>
```

**Wann welches?**
- `v-if`: Wenn Element selten wechselt oder aufwändig ist
- `v-show`: Wenn Element häufig ein/ausgeblendet wird

#### v-for

```vue
<div
  v-for="n in 4"
  :key="n"
  class="flow-dot"
/>
```

**Was ist `v-for`?**
- Wiederholt ein Element mehrfach
- Kann über Arrays, Zahlen oder Objekte iterieren

**Beispiele:**
```vue
<!-- Über Zahl iterieren -->
<div v-for="n in 5" :key="n">{{ n }}</div>
<!-- Erstellt: 1, 2, 3, 4, 5 -->

<!-- Über Array iterieren -->
<div v-for="item in items" :key="item.id">{{ item.name }}</div>

<!-- Über Array mit Index -->
<div v-for="(item, index) in items" :key="index">
  {{ index }}: {{ item.name }}
</div>
```

**Im Kontext:**
```vue
<div v-for="n in 4" :key="n" class="flow-dot"></div>
```
Erstellt 4 Dots für den Flow-Indikator (1/4, 2/4, 3/4, 4/4)

#### :key - Sehr wichtig!

**Was ist `:key`?**
- Gibt Vue einen eindeutigen Identifier für jedes Element
- Vue nutzt den Key, um Elemente zu tracken
- Wichtig für Performance und korrekte Updates

**Beispiel ohne Key:**
```vue
<div v-for="item in items">{{ item.name }}</div>
```

Wenn sich `items` ändert, weiß Vue nicht, welches Element welches ist:
```
Vorher: [Alice, Bob, Charlie]
Nachher: [Bob, Charlie, Dave]
```
Vue denkt: "Ändere Alice → Bob, Bob → Charlie, Charlie → Dave"

**Beispiel mit Key:**
```vue
<div v-for="item in items" :key="item.id">{{ item.name }}</div>
```

Vue erkennt: "Alice wurde entfernt, Dave wurde hinzugefügt, Rest gleich"

**Im Kontext:**
```vue
<div
  v-for="item in pomodoroStore.statsData"
  :key="item.date"
  class="chart-bar-wrapper"
>
```

- Jeder Tag hat ein eindeutiges Datum als Key
- Vue kann die Bars korrekt aktualisieren
- Wenn sich die Daten ändern, weiß Vue genau welcher Bar welcher Tag ist

**Schlechte Keys:**
```vue
<!-- ❌ Index als Key - kann zu Problemen führen -->
<div v-for="(item, index) in items" :key="index">

<!-- ✅ Eindeutige ID als Key -->
<div v-for="item in items" :key="item.id">
```

#### Event Handling (@click, etc.)

```vue
<Button
  @click="pomodoroStore.start"
  label="Start"
/>
```

**Was ist `@`?**
- Kurzform für `v-on`
- Bindet Event-Listener
- Führt Code aus, wenn das Event ausgelöst wird

```vue
<!-- Kurzform -->
<button @click="handleClick">Click</button>

<!-- Langform -->
<button v-on:click="handleClick">Click</button>
```

**Event-Typen:**
```vue
<input @input="handleInput" />      <!-- Bei jeder Eingabe -->
<input @change="handleChange" />    <!-- Bei Blur nach Änderung -->
<div @mouseenter="handleHover" />   <!-- Mouse Over -->
<form @submit="handleSubmit" />     <!-- Form Submit -->
```

**Inline Expressions:**
```vue
<!-- Funktion aufrufen -->
<button @click="increment">+</button>

<!-- Inline Code -->
<button @click="count++">+</button>

<!-- Mit Parameter -->
<button @click="setCount(5)">Set to 5</button>

<!-- Mit Event-Objekt -->
<button @click="handleClick($event)">Click</button>
```

#### Directives (v-tooltip, etc.)

```vue
<Button
  v-tooltip.top="'Zurücksetzen'"
  icon="pi pi-refresh"
/>
```

**Was sind Directives?**
- Spezielle Attribute, die Vue-Funktionalität hinzufügen
- Beginnen mit `v-`
- Können Modifiers haben (`.top`, `.prevent`, etc.)

**Built-in Directives:**
- `v-if` / `v-else` / `v-show`
- `v-for`
- `v-model`
- `v-bind` (`:`)
- `v-on` (`@`)

**Custom Directives (von Libraries):**
- `v-tooltip` (PrimeVue)
- Fügt Tooltip-Funktionalität hinzu

**Modifiers:**
```vue
<!-- Position des Tooltips -->
<Button v-tooltip.top="'Oben'" />
<Button v-tooltip.bottom="'Unten'" />
<Button v-tooltip.left="'Links'" />

<!-- Event Modifiers -->
<form @submit.prevent="handleSubmit">  <!-- preventDefault() -->
<input @keyup.enter="submit">          <!-- Nur bei Enter -->
<div @click.stop="handle">             <!-- stopPropagation() -->
```

#### Template Slots

```vue
<Card>
  <template #header>
    <div class="widget-header">...</div>
  </template>

  <template #content>
    <div class="widget-content">...</div>
  </template>
</Card>
```

**Was sind Slots?**
- Erlauben es, Inhalte in Komponenten einzufügen
- Wie Platzhalter in der Komponente

**Card-Komponente intern:**
```vue
<!-- Card.vue -->
<div class="card">
  <div class="card-header">
    <slot name="header"></slot>
  </div>
  <div class="card-content">
    <slot name="content"></slot>
  </div>
</div>
```

**Nutzung:**
```vue
<Card>
  <template #header>
    <h1>Titel</h1>
  </template>
  <template #content>
    <p>Inhalt</p>
  </template>
</Card>
```

**Ergebnis:**
```html
<div class="card">
  <div class="card-header">
    <h1>Titel</h1>
  </div>
  <div class="card-content">
    <p>Inhalt</p>
  </div>
</div>
```

### Reactive Classes

```vue
<div
  class="flow-dot"
  :class="{
    'flow-dot-active': n < pomodoroStore.currentFlow,
    'flow-dot-current': n === pomodoroStore.currentFlow,
  }"
></div>
```

**Dynamische Klassen:**
- Object-Syntax: Key = Klassenname, Value = Bedingung
- Wenn Bedingung true → Klasse wird hinzugefügt

**Im Kontext:**
```
currentFlow = 3

Dot 1 (n=1): n < 3 → flow-dot-active ✅
Dot 2 (n=2): n < 3 → flow-dot-active ✅
Dot 3 (n=3): n === 3 → flow-dot-current ✅
Dot 4 (n=4): keine Bedingung true → nur flow-dot
```

**Andere Syntaxen:**
```vue
<!-- Array-Syntax -->
<div :class="[isActive ? 'active' : '', 'base-class']"></div>

<!-- String-Syntax -->
<div :class="className"></div>

<!-- Kombiniert -->
<div class="static" :class="{ dynamic: condition }"></div>
```

### Reactive Styles

```vue
<div
  class="chart-bar"
  :style="{
    height: item.value > 0 ? `${(item.value / pomodoroStore.maxValue) * 100}%` : '2px',
    backgroundColor: 'var(--primary-color)',
    opacity: item.value > 0 ? 0.8 : 0.2
  }"
>
```

**Dynamische Styles:**
- Object-Syntax: CSS-Properties als camelCase
- Werte als Strings

**Im Kontext:**
```javascript
// Beispiel: item.value = 5, maxValue = 10
height: (5 / 10) * 100 = 50%
opacity: 5 > 0 → 0.8

// Beispiel: item.value = 0
height: 2px (Mindesthöhe)
opacity: 0.2 (durchsichtig)
```

**CSS Variables:**
```vue
backgroundColor: 'var(--primary-color)'
```
Nutzt CSS Custom Properties aus dem Theme.

---

## Component-Struktur

### Script Setup

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { usePomodoroStore } from '@/stores/pomodoro'

const pomodoroStore = usePomodoroStore()
const showStatsDialog = ref(false)

function formatTime(num: number): string {
  return num.toString().padStart(2, '0')
}
</script>
```

**Was ist `<script setup>`?**
- Moderne, kürzere Syntax für Composition API
- Alles was hier definiert wird, ist automatisch im Template verfügbar
- Kein `return` nötig

**Ohne setup (alte Syntax):**
```vue
<script>
export default {
  setup() {
    const count = ref(0)

    return {
      count  // Muss explizit returned werden
    }
  }
}
</script>
```

**Mit setup (neue Syntax):**
```vue
<script setup>
const count = ref(0)  // Automatisch verfügbar
</script>
```

**Store nutzen:**
```typescript
const pomodoroStore = usePomodoroStore()
```
- Holt die Store-Instanz
- Alle Store-Werte und Funktionen sind über `pomodoroStore` verfügbar

**Lokale Refs:**
```typescript
const showStatsDialog = ref(false)
```
- Komponenten-lokaler State (nicht im Store)
- Nur für UI-State, der nicht global sein muss

**Helper-Funktionen:**
```typescript
function formatTime(num: number): string {
  return num.toString().padStart(2, '0')
}
```
- `padStart(2, '0')`: Füllt String mit Nullen auf
- `5` → `"05"`
- `12` → `"12"`

### Template-Struktur

```vue
<template>
  <Card class="widget-card h-full">
    <template #header>
      <!-- Header-Inhalt -->
    </template>

    <template #content>
      <!-- Main-Inhalt -->
    </template>
  </Card>

  <!-- Dialoge außerhalb der Card -->
  <Dialog v-model:visible="showStatsDialog">
    <!-- Dialog-Inhalt -->
  </Dialog>
</template>
```

**Struktur-Hierarchie:**
```
Card (äußerer Container)
├── Header Slot
│   ├── Titel & Icon
│   └── Statistik-Button
└── Content Slot
    ├── Mode Selector (Fokus/Pause Buttons)
    ├── Flow Indicator (1/4 Dots)
    ├── Timer Display (25:00)
    ├── Progress Bar
    ├── Controls (Start/Pause, Reset, Skip)
    └── Settings (InputNumbers)

Dialog (separater Overlay)
└── Statistik-Chart
```

### Conditional Rendering im Detail

```vue
<Button
  v-if="!pomodoroStore.isRunning"
  icon="pi pi-play"
  label="Start"
  @click="pomodoroStore.start"
/>
<Button
  v-else
  icon="pi pi-pause"
  label="Pause"
  @click="pomodoroStore.pause"
/>
```

**Lifecycle dieser Buttons:**
1. Initial: `isRunning = false`
   - Start-Button wird erstellt
   - Pause-Button existiert nicht im DOM

2. User klickt "Start": `isRunning = true`
   - Start-Button wird aus DOM entfernt
   - Pause-Button wird erstellt und eingefügt

3. User klickt "Pause": `isRunning = false`
   - Pause-Button wird aus DOM entfernt
   - Start-Button wird erstellt und eingefügt

**Wichtig:** Jedes Mal ein komplett neuer Button, nicht nur Label-Änderung!

### Component Communication

**Parent → Child (Props):**
```vue
<!-- Parent -->
<ChildComponent :message="text" />

<!-- Child -->
<script setup>
const props = defineProps<{
  message: string
}>()
</script>
```

**Child → Parent (Events):**
```vue
<!-- Child -->
<script setup>
const emit = defineEmits<{
  update: [value: string]
}>()

function handleClick() {
  emit('update', 'new value')
}
</script>

<!-- Parent -->
<ChildComponent @update="handleUpdate" />
```

**Im Pomodoro Widget:**
Wir nutzen den Store, daher keine direkte Component Communication nötig!

---

## Datenfluss

### Gesamter Flow eines Pomodoro-Zyklus

```
1. User klickt "Start"
   ↓
2. @click="pomodoroStore.start" wird ausgeführt
   ↓
3. start() Funktion im Store:
   - timeLeft.value = 1500 (25 Min * 60)
   - isRunning.value = true
   - Interval startet
   ↓
4. Jede Sekunde:
   - timeLeft.value-- (1500 → 1499 → 1498 ...)
   - displayMinutes computed aktualisiert sich (25:00 → 24:59 → 24:58)
   - Template re-rendert automatisch
   - Progress Bar aktualisiert sich
   ↓
5. Wenn timeLeft === 0:
   - complete() wird aufgerufen
   - completedPomodoros++
   - Statistik aktualisiert
   - currentMode: 'focus' → 'break'
   - currentFlow++ (1 → 2)
   - timeLeft = 300 (5 Min Pause)
   - Notification wird angezeigt
   - Wenn autoStart: start() nach 1 Sekunde
   ↓
6. Watch in Store bemerkt Änderungen:
   - saveSettings() wird aufgerufen
   - Daten werden in LocalStorage gespeichert
```

### Reaktivitäts-Kette

**User ändert Fokus-Dauer von 25 auf 20:**

```
1. InputNumber Component
   v-model="pomodoroStore.focusDuration"
   ↓
2. focusDuration.value ändert sich: 25 → 20
   ↓
3. Watcher [focusDuration, ...] wird getriggert
   ↓
4. if (!isRunning.value): timeLeft.value = totalSeconds.value
   ↓
5. totalSeconds ist computed, berechnet neu:
   currentDuration.value (20) * 60 = 1200
   ↓
6. timeLeft.value = 1200
   ↓
7. displayMinutes computed berechnet neu:
   Math.floor(1200 / 60) = 20
   ↓
8. Template aktualisiert sich:
   "25:00" → "20:00"
   ↓
9. Zweiter Watcher wird getriggert:
   saveSettings() speichert neue Einstellung
```

### Computed Dependencies

```
focusDuration (ref)
breakDuration (ref)
longBreakDuration (ref)
currentMode (ref)
    ↓
currentDuration (computed)
    ↓
totalSeconds (computed)
    ↓
progress (computed)

timeLeft (ref)
    ↓
displayMinutes (computed)
displaySeconds (computed)
```

**Beispiel-Kaskade:**
```typescript
// Initialer Zustand
currentMode = 'focus'
focusDuration = 25
timeLeft = 1500

// User wechselt zu 'break'
currentMode = 'break'
  → currentDuration berechnet neu: 5 (statt 25)
  → totalSeconds berechnet neu: 300 (statt 1500)
  → progress berechnet neu: 0 (da timeLeft noch 1500)

// switchMode() setzt timeLeft
timeLeft = 300
  → displayMinutes: 5
  → displaySeconds: 0
  → progress: 0
```

### Event Flow

**User klickt "Skip":**

```
1. Template: @click="pomodoroStore.skip"
   ↓
2. Store: skip() Funktion
   ↓
3. pause() wird aufgerufen
   - isRunning = false
   - Interval wird gelöscht
   ↓
4. Wenn currentMode === 'focus':
   - completedPomodoros++
   - dailyStats aktualisiert
   - saveDailyStats()
   - Flow-Logik (break oder longBreak)
   - currentFlow aktualisiert
   Sonst:
   - currentMode = 'focus'
   ↓
5. timeLeft = totalSeconds.value (neue Phase)
   ↓
6. Wenn autoStart:
   - setTimeout(() => start(), 1000)
   ↓
7. Watchers reagieren:
   - saveSettings() speichert neue Werte
```

---

## Häufige Patterns

### Loading Initial Data

```typescript
// Store Initialization
loadSettings()
loadDailyStats()

if (timeLeft.value === 0) {
  timeLeft.value = totalSeconds.value
}
```

**Warum in dieser Reihenfolge?**
1. Erst Settings laden (damit focusDuration etc. korrekt sind)
2. Dann timeLeft setzen (damit es die geladene Dauer nutzt)

### Auto-Save Pattern

```typescript
watch([
  focusDuration,
  breakDuration,
  longBreakDuration,
  completedPomodoros,
  currentFlow,
  autoStart
], saveSettings)
```

Jede Änderung an diesen Werten triggert automatisch ein Speichern.

### Timer Pattern

```typescript
let intervalId: number | null = null

function start() {
  // Alte Intervals aufräumen
  if (intervalId !== null) {
    clearInterval(intervalId)
  }

  // Neues Interval starten
  intervalId = window.setInterval(() => {
    // Timer-Logik
  }, 1000)
}

function pause() {
  // Interval stoppen
  if (intervalId !== null) {
    clearInterval(intervalId)
    intervalId = null
  }
}
```

**Wichtig:**
- Immer alte Intervals aufräumen
- `intervalId` auf `null` setzen nach `clearInterval`

### Notification Pattern

```typescript
// Berechtigung anfragen (einmalig)
if ('Notification' in window && Notification.permission === 'default') {
  Notification.requestPermission()
}

// Notification anzeigen (wenn erlaubt)
if ('Notification' in window && Notification.permission === 'granted') {
  new Notification('Titel', { body: 'Nachricht' })
}
```

### Conditional Computed Logic

```typescript
const currentDuration = computed(() => {
  if (currentMode.value === 'focus') return focusDuration.value
  if (currentMode.value === 'longBreak') return longBreakDuration.value
  return breakDuration.value
})
```

Elegante Alternative zu großen if-else Blöcken.

---

## Best Practices

### 1. Refs vs Computed

```typescript
// ❌ Schlecht - manuelles Update nötig
const totalSeconds = ref(0)
watch(currentDuration, () => {
  totalSeconds.value = currentDuration.value * 60
})

// ✅ Gut - automatische Berechnung
const totalSeconds = computed(() => currentDuration.value * 60)
```

### 2. Watch nur für Seiteneffekte

```typescript
// ✅ Gut - Seiteneffekt (LocalStorage)
watch(focusDuration, saveSettings)

// ❌ Schlecht - sollte computed sein
watch(focusDuration, () => {
  totalMinutes.value = focusDuration.value * 60
})
```

### 3. Eindeutige Keys in v-for

```typescript
// ❌ Schlecht
<div v-for="(item, index) in items" :key="index">

// ✅ Gut
<div v-for="item in items" :key="item.id">
```

### 4. Template-Logik einfach halten

```vue
<!-- ❌ Zu komplex im Template -->
<div>{{ items.filter(i => i.active).map(i => i.name).join(', ') }}</div>

<!-- ✅ Computed Property nutzen -->
<div>{{ activeItemNames }}</div>

<script setup>
const activeItemNames = computed(() =>
  items.value.filter(i => i.active).map(i => i.name).join(', ')
)
</script>
```

### 5. Props sind Read-Only

```vue
<!-- ❌ Schlecht -->
<script setup>
const props = defineProps<{ count: number }>()
props.count++ // Fehler!
</script>

<!-- ✅ Gut - lokale Kopie für Änderungen -->
<script setup>
const props = defineProps<{ count: number }>()
const localCount = ref(props.count)
localCount.value++
</script>
```

---

## Performance-Tipps

### 1. Computed Caching

Computed Properties werden gecacht:
```typescript
// Wird nur bei Änderung von focusDuration neu berechnet
const totalSeconds = computed(() => {
  console.log('Berechnung läuft')
  return focusDuration.value * 60
})
```

### 2. v-show vs v-if

```vue
<!-- Häufiges Toggling: v-show -->
<div v-show="isVisible">Inhalt</div>

<!-- Seltenes Toggling: v-if -->
<div v-if="isVisible">Aufwändiger Inhalt</div>
```

### 3. Event Handler

```vue
<!-- ❌ Neue Funktion bei jedem Render -->
<button @click="() => handleClick(id)">Click</button>

<!-- ✅ Stabile Referenz -->
<button @click="handleClick">Click</button>
```

---

## Debugging-Tipps

### 1. Vue DevTools

- Browser Extension für Chrome/Firefox
- Zeigt Component Tree
- Inspect Store State
- Track Events

### 2. Console Logging in Computed

```typescript
const totalSeconds = computed(() => {
  const result = currentDuration.value * 60
  console.log('totalSeconds berechnet:', result)
  return result
})
```

### 3. Watch für Debugging

```typescript
watch(timeLeft, (newValue, oldValue) => {
  console.log(`timeLeft: ${oldValue} → ${newValue}`)
})
```

### 4. Template Debugging

```vue
<div>{{ console.log('currentMode:', pomodoroStore.currentMode) }}</div>
<pre>{{ JSON.stringify(pomodoroStore.statsData, null, 2) }}</pre>
```

---

## Zusammenfassung

### Datenfluss-Übersicht

```
User Interaction (Click, Input)
    ↓
Event Handler (@click, v-model)
    ↓
Store State ändert sich (ref)
    ↓
Computed Properties berechnen neu
    ↓
Watchers reagieren (Seiteneffekte)
    ↓
Template re-rendert
    ↓
DOM Update
    ↓
User sieht Änderung
```

### Vue Reaktivitäts-System

1. **Refs** - Reaktive Variablen
2. **Computed** - Abgeleitete Werte (gecacht)
3. **Watch** - Seiteneffekte bei Änderungen
4. **Template** - Deklarative UI basierend auf State

### Key Takeaways

- **Refs** für Daten, die sich ändern
- **Computed** für berechnete Werte (automatisch aktualisiert, gecacht)
- **Watch** für Seiteneffekte (LocalStorage, API Calls)
- **v-model** für Two-Way Binding
- **:key** immer bei v-for (eindeutige IDs nutzen)
- **:class** und **:style** für dynamisches Styling
- **@event** für Event Handling

---

## Erweiterte Konzepte

### Composition API vs Options API

**Options API (alt):**
```vue
<script>
export default {
  data() {
    return {
      count: 0
    }
  },
  computed: {
    doubled() {
      return this.count * 2
    }
  },
  methods: {
    increment() {
      this.count++
    }
  }
}
</script>
```

**Composition API (modern):**
```vue
<script setup>
const count = ref(0)
const doubled = computed(() => count.value * 2)
const increment = () => count.value++
</script>
```

**Vorteile Composition API:**
- Bessere TypeScript-Unterstützung
- Einfacher zu organisieren (nach Feature statt nach Type)
- Wiederverwendbare Logik (Composables)
- Weniger Boilerplate

### Lifecycle Hooks

```typescript
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  console.log('Component wurde gemounted')
  // z.B. API Call, Event Listener hinzufügen
})

onUnmounted(() => {
  console.log('Component wird entfernt')
  // z.B. Event Listener entfernen, Intervals clearen
})
```

**Wichtig für Timer:**
```typescript
onUnmounted(() => {
  if (intervalId !== null) {
    clearInterval(intervalId)
  }
})
```

### Composables (Wiederverwendbare Logik)

```typescript
// useLocalStorage.ts
export function useLocalStorage(key: string, defaultValue: any) {
  const stored = localStorage.getItem(key)
  const value = ref(stored ? JSON.parse(stored) : defaultValue)

  watch(value, (newValue) => {
    localStorage.setItem(key, JSON.stringify(newValue))
  })

  return value
}

// Nutzung
const count = useLocalStorage('count', 0)
```

---

Diese Erklärung deckt alle wichtigen Konzepte ab, die im Pomodoro Widget verwendet werden. Jedes Konzept wird im Kontext der tatsächlichen Implementierung erklärt, sodass du nicht nur die Theorie verstehst, sondern auch siehst, wie es in der Praxis angewendet wird.
