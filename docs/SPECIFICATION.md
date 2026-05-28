# MindPanel - Projekt Spezifikation

**Version:** 0.0.0
**Datum:** 2025-12-06
**Status:** In Entwicklung

---

## 1. Projektübersicht

### 1.1 Projektname
**MindPanel**

### 1.2 Beschreibung
MindPanel ist eine moderne, offline-fähige Dashboard-Anwendung zur Förderung von Fokus und Produktivität. Die Anwendung bietet flexible, frei verschiebbare Widgets in einem Grid-Layout und ermöglicht es Nutzern, ihre täglichen Aufgaben, Notizen, Termine und weitere Aktivitäten an einem zentralen Ort zu verwalten.

### 1.3 Hauptmerkmale
- Frei verschiebbares Widget-System (Drag & Drop)
- Offline-first Architektur (alle Basisfunktionen ohne Backend)
- Dark/Light Mode
- Responsives Design
- Persistente Datenspeicherung (localStorage)
- Minimalistische, moderne UI
- Erweiterbare Architektur

---

## 2. Ziele & Anforderungen

### 2.1 Produktziele
- Bereitstellung eines leistungs- und erweiterungsfähigen Dashboard-Systems
- Förderung von Fokus, Produktivität und Übersichtlichkeit
- Flexible, frei verschiebbare Widgets (Drag & Drop Grid Layout)
- Minimalistische, moderne UI, Dark/Light-Modus
- Offline-fähig: alle Basisfunktionen laufen ohne Backend
- Erweiterbarkeit durch zukünftige Module (z. B. Habit Tracker, Timer, Analytics)

### 2.2 Zielgruppe
- Personen, die ihre Produktivität steigern möchten
- Remote-Arbeiter und Studenten
- Nutzer, die ein personalisiertes Dashboard bevorzugen
- Anwender, die Wert auf Datenschutz und Offline-Verfügbarkeit legen

---

## 3. Technische Architektur

### 3.1 Tech Stack

| Bereich | Technologie | Version |
|---------|-------------|---------|
| Frontend Framework | Vue 3 (Composition API) | ^3.5.24 |
| Build Tool | Vite | ^7.2.4 |
| Sprache | TypeScript | ~5.9.3 |
| UI-Komponenten | PrimeVue | ^4.5.1 |
| Theme System | @primeuix/themes (Aura) | ^2.0.2 |
| Styling | PrimeFlex + eigene CSS | ^4.0.0 |
| Icons | PrimeIcons | ^7.0.0 |
| State Management | Pinia | ^3.0.4 |
| Routing | Vue Router | ^4.6.3 |
| HTTP Client | Axios | ^1.13.2 |
| Grid Layout | (Geplant: vue-grid-layout-v3) | - |
| Wetter API | Open-Meteo API | - |
| Persistenz | localStorage (Browser) | - |

### 3.2 TypeScript Konfiguration

**Compiler Options:**
- Target: ES2020
- Module: ESNext
- Module Resolution: bundler
- Strict Mode: aktiviert
- JSX: preserve
- Path Alias: `@/*` → `./src/*`

**Linting:**
- noUnusedLocals: true
- noUnusedParameters: true
- noFallthroughCasesInSwitch: true

### 3.3 Build-Konfiguration

**Vite:**
- Vue Plugin aktiviert
- Path Alias: `@` → `./src`
- Dev Server: Standard Port
- Build: TypeScript Typecheck vor Build

### 3.4 Projektstruktur

```
mindpanel/
├── public/               # Statische Assets
├── src/
│   ├── assets/          # CSS, Bilder, Fonts
│   │   └── main.css
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppShell.vue
│   │   │   └── AppSidebar.vue
│   │   └── widgets/
│   │       ├── CalendarWidget.vue
│   │       ├── NotesWidget.vue
│   │       ├── NotesDialog.vue
│   │       ├── PomodoroWidget.vue
│   │       ├── TodosWidget.vue
│   │       ├── TodosDialog.vue
│   │       └── WeatherWidget.vue
│   ├── router/
│   │   └── index.ts
│   ├── services/
│   │   ├── api.ts
│   │   └── weatherService.ts
│   ├── stores/
│   │   ├── auth.ts
│   │   ├── dashboard.ts
│   │   ├── profile.ts
│   │   └── ui.ts
│   ├── views/
│   │   ├── DashboardView.vue
│   │   ├── LoginView.vue
│   │   ├── ProfileSettingsView.vue
│   │   ├── RegisterView.vue
│   │   └── SettingsView.vue
│   ├── App.vue
│   ├── main.ts
│   └── vite-env.d.ts
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## 4. Funktionale Spezifikationen

### 4.1 Dashboard

**Beschreibung:**
Das Dashboard zeigt alle Widgets in einem frei verschiebbaren Grid. Drag & Drop ermöglicht Benutzern, die Reihenfolge und Position zu ändern. Änderungen werden automatisch im localStorage gespeichert.

**Funktionen:**
- Widgets hinzufügen/entfernen
- Widgets frei verschieben
- Widgets in Größe veränderbar
- Persistentes Layout
- Dark/Light Mode Toggle
- Responsives Design

**Datenstrukturen:**

```typescript
interface WidgetLayout {
  id: string
  type: WidgetType
  x: number
  y: number
  w: number
  h: number
}

type WidgetType = 'weather' | 'todo' | 'calendar' | 'notes' | 'pomodoro'
```

**Standard-Layout:**
```json
[
  { "id": "todo", "type": "todo", "x": 0, "y": 0, "w": 3, "h": 8 },
  { "id": "calendar", "type": "calendar", "x": 3, "y": 0, "w": 3, "h": 7 },
  { "id": "notes", "type": "notes", "x": 6, "y": 0, "w": 3, "h": 7 },
  { "id": "weather", "type": "weather", "x": 9, "y": 0, "w": 3, "h": 4 },
  { "id": "pomodoro", "type": "pomodoro", "x": 0, "y": 8, "w": 3, "h": 4 }
]
```

### 4.2 Widgets

#### 4.2.1 Todo Widget

**Funktionen:**
- Aufgabe hinzufügen
- Aufgabe abhaken/erledigen
- Aufgabe als Priorität markieren (max. 3 Prioritäten)
- Fälligkeitsdatum setzen (Due Date)
- Erledigungsdatum setzen (Do Date)
- Aufgabe löschen
- Persistenz im localStorage
- Scrollbarer Bereich innerhalb des Widgets

**Datenstruktur:**
```typescript
interface Todo {
  id: number
  title: string
  completed: boolean
  isPriority: boolean
  dueDate: string | null
  doDate: string | null
}
```

**localStorage Key:** `focusboard_todos`

#### 4.2.2 Notes Widget

**Funktionen:**
- Notiz erstellen (Titel + Inhalt)
- Notiz bearbeiten
- Notiz löschen
- Persistenz im localStorage
- Dialog-basierte Bearbeitung

**Datenstruktur:**
```typescript
interface Note {
  id: number
  title: string
  content: string
}
```

**localStorage Key:** `focusboard_notes`

#### 4.2.3 Kalender Widget

**Funktionen:**
- Lokale Datumauswahl
- Darstellung eines Monatskalenders
- Speicherung der Auswahl
- Noch keine Backend-Events (geplant)

**Status:** Implementiert

#### 4.2.4 Wetter Widget (Open-Meteo)

**Funktionen:**
- Stadtname eingeben
- Open-Meteo Geocoding API nutzen
- Open-Meteo Forecast API nutzen
- Temperatur + Wetterbeschreibung anzeigen
- Fehlerbehandlung
- Persistenz der letzten Stadt

**API Endpoints:**
- Geocoding: `https://geocoding-api.open-meteo.com/v1/search`
- Forecast: `https://api.open-meteo.com/v1/forecast`

**localStorage Key:** `focusboard_weather_city`

#### 4.2.5 Pomodoro Widget

**Funktionen:**
- Fokus-Zeit einstellen (Standard: 25 Min)
- Pausen-Zeit einstellen (Standard: 5 Min)
- Timer starten/pausieren/zurücksetzen
- Automatischer Wechsel Fokus → Pause → Fokus
- Fortschrittsbalken
- Anzeige abgeschlossener Pomodoros
- Speichern von Einstellungen im localStorage

**localStorage Key:** `focusboard_pomodoro_settings`

### 4.3 Einstellungen (Settings)

**Bereiche:**

1. **Personalisierung**
   - Dark/Light Mode Toggle
   - Widgets an/aus
   - Layout zurücksetzen

2. **Profil** (ProfileSettingsView)
   - Benutzerprofil-Verwaltung
   - (Optional, für zukünftige Backend-Integration)

3. **App-Informationen**
   - App-Version
   - Lizenzinformationen

---

## 5. State Management (Pinia)

### 5.1 Dashboard Store (`stores/dashboard.ts`)

**Zuständigkeit:**
- Verwaltung aller Widgets und deren Layout
- Todo-Management
- Notes-Management
- localStorage Persistierung

**State:**
```typescript
{
  notes: Note[]
  todos: Todo[]
  widgets: WidgetLayout[]
}
```

**Actions:**
- `addNote(title, content)`
- `updateNote(id, title, content)`
- `deleteNote(id)`
- `addTodo(title, dueDate, doDate)`
- `updateTodo(id, updates)`
- `toggleTodo(id)`
- `togglePriority(id)` - Max. 3 Prioritäten
- `deleteTodo(id)`
- `updateWidgetLayout(layout)`
- `moveWidget(fromIndex, toIndex)`
- `toggleWidget(widgetId)`
- `resetLayout()`
- `initializeStore()`

**Persistierung:**
- Auto-save bei Änderungen (Vue watch)
- Auto-load beim App-Start

### 5.2 UI Store (`stores/ui.ts`)

**Zuständigkeit:**
- Theme-Management (Dark/Light Mode)
- UI-Zustand

**localStorage Key:** `focusboard_darkmode`

### 5.3 Auth Store (`stores/auth.ts`)

**Zuständigkeit:**
- Authentifizierung (optional für Backend)
- Token-Management
- User-Info

**Status:** Vorbereitet für zukünftige Backend-Integration

### 5.4 Profile Store (`stores/profile.ts`)

**Zuständigkeit:**
- Benutzerprofil-Daten
- Profil-Einstellungen

**Status:** Vorbereitet für zukünftige Backend-Integration

---

## 6. Routing Spezifikation

### 6.1 Routen

| Route | View | Beschreibung | Guard |
|-------|------|--------------|-------|
| `/` | DashboardView | Hauptübersicht mit Widgets | - |
| `/settings` | SettingsView | App-Einstellungen | - |
| `/profile` | ProfileSettingsView | Profil-Einstellungen | - |
| `/login` | LoginView | Login (Optional für Backend) | - |
| `/register` | RegisterView | Registrierung (Optional für Backend) | - |

### 6.2 Router Konfiguration

**Router Mode:** HTML5 History Mode
**Base URL:** `/`

---

## 7. Persistenz & Datenspeicherung

### 7.1 localStorage Keys

| Key | Beschreibung | Datentyp |
|-----|--------------|----------|
| `focusboard_widget_layout` | Widget-Layout Konfiguration | WidgetLayout[] |
| `focusboard_darkmode` | Theme-Einstellung | boolean |
| `focusboard_todos` | Todo-Liste | Todo[] |
| `focusboard_notes` | Notizen-Liste | Note[] |
| `focusboard_pomodoro_settings` | Pomodoro-Einstellungen | Object |
| `focusboard_weather_city` | Letzte gewählte Stadt | string |

### 7.2 Daten-Migration

**Strategie:**
- Beim Laden werden Daten validiert
- Fehlende Properties werden mit Defaults ergänzt
- Bei Strukturänderungen wird eine Migration durchgeführt
- Layout-Reset-Funktion verfügbar

---

## 8. Nicht-funktionale Anforderungen

### 8.1 Performance

- Laden unter 1s (SPA)
- Keine großen externen Bibliotheken
- Lazy Loading für Views
- Optimiertes Bundle-Splitting
- Schnelle Reaktionszeiten bei Widget-Interaktionen

### 8.2 Accessibility (A11y)

- Fokus-Markierungen
- ARIA-Rollen für:
  - Widgets
  - Buttons
  - Drag-and-Drop Bereiche
- Keyboard-Navigation
- Semantisches HTML
- Kontrastverhältnisse (WCAG 2.1 AA)

### 8.3 Sicherheit

- JWT-basierte Auth (optional, wenn Backend kommt)
- Keine sensiblen Daten im localStorage
- XSS-Schutz durch Vue
- Content Security Policy (geplant)
- Input-Validierung

### 8.4 Responsiveness

**Breakpoints:**
- Mobile: < 600px (1 Spalte)
- Tablet: 600px - 800px (2 Spalten)
- Desktop: > 800px (3-4 Spalten)

**Design-Prinzipien:**
- Mobile-First Approach
- Touch-optimierte Controls
- Responsive Grid-Layout
- Flexible Widget-Größen

### 8.5 Browser-Unterstützung

**Unterstützte Browser:**
- Chrome/Edge (letzte 2 Versionen)
- Firefox (letzte 2 Versionen)
- Safari (letzte 2 Versionen)

**Mindestanforderungen:**
- ES2020 Support
- localStorage Support
- CSS Grid Support

---

## 9. UI/UX Design

### 9.1 Design-System

**Theme:**
- PrimeVue Aura Theme
- Anpassbares Dark/Light Mode
- Konsistente Farbpalette
- Moderne, minimalistische Ästhetik

**Komponenten:**
- PrimeVue UI-Komponenten
- Custom Widget-Komponenten
- Responsive Layout-Komponenten

### 9.2 Layout-Prinzipien

- Grid-basiertes Layout
- Drag & Drop Interaktionen
- Card-basierte Widgets
- Konsistente Abstände (PrimeFlex)
- Smooth Transitions

### 9.3 Typografie

- System-Fonts für Performance
- Klare Hierarchie
- Lesbare Schriftgrößen
- Responsive Typography

---

## 10. API & Services

### 10.1 Weather Service (`services/weatherService.ts`)

**Zweck:** Integration mit Open-Meteo API

**Methoden:**
- `searchCity(cityName)` - Geocoding
- `getForecast(latitude, longitude)` - Wettervorhersage

**Error Handling:** Try-catch mit Benutzer-Feedback

### 10.2 API Service (`services/api.ts`)

**Zweck:** Axios-Konfiguration für zukünftige Backend-Calls

**Features:**
- Base URL Konfiguration
- Request/Response Interceptors
- Error Handling
- Token-Management (vorbereitet)

---

## 11. Deployment & Build

### 11.1 Build-Prozess

**Entwicklung:**
```bash
npm run dev
```

**Production Build:**
```bash
npm run build
```

**Preview:**
```bash
npm run preview
```

### 11.2 Build-Output

- Optimierte Bundles
- Code-Splitting
- Minifizierung
- Source Maps (dev only)
- Asset-Optimierung

### 11.3 Deployment-Optionen

- Statisches Hosting (Netlify, Vercel, GitHub Pages)
- CDN-Delivery
- Keine Server-Anforderungen
- Offline-fähig (Service Worker geplant)

---

## 12. Zukünftige Erweiterungen

### 12.1 Geplante Features

**Phase 2:**
- Cloud-Sync über Backend (RestAPI oder Supabase)
- Multi-Device Synchronisation
- Erweiterte Widget-Konfiguration
- Widget-Sharing

**Phase 3:**
- AI-Features (Task-Zusammenfassungen, Kalender-Analysen)
- Habit Tracker Widget
- Analytics Widget (Zeitnutzung, Pomodoro-Statistiken)
- Goal Tracking

**Phase 4:**
- Widget-Marktplatz (Plugin-System)
- Custom Widget-Entwicklung
- Import/Export von Layouts
- Team-Collaboration Features

### 12.2 Technische Erweiterungen

- Progressive Web App (PWA)
- Service Worker für Offline-Support
- Push-Benachrichtigungen
- Internationalisierung (i18n)
- E2E Testing (Playwright/Cypress)
- Unit Tests (Vitest)
- Docker-Container
- CI/CD Pipeline

---

## 13. Entwicklungs-Richtlinien

### 13.1 Code-Style

- TypeScript Strict Mode
- Vue 3 Composition API
- Composables für wiederverwendbare Logik
- Single File Components (SFC)
- ESLint + Prettier (geplant)

### 13.2 Naming Conventions

**Dateien:**
- Components: PascalCase (z.B. `TodoWidget.vue`)
- Composables: camelCase mit `use` Prefix
- Stores: camelCase mit `use` Prefix + `Store` Suffix
- Services: camelCase mit `Service` Suffix

**Code:**
- Variables: camelCase
- Constants: UPPER_SNAKE_CASE
- Interfaces/Types: PascalCase
- Functions: camelCase

### 13.3 Git Workflow

**Branches:**
- `main` - Production-ready Code
- `develop` - Entwicklungs-Branch
- `feature/*` - Feature-Branches
- `bugfix/*` - Bugfix-Branches

**Commits:**
- Conventional Commits Format
- Klare, beschreibende Commit-Messages

---

## 14. Lizenzierung & Rechtliches

### 14.1 Lizenz
**MIT License** (empfohlen)

### 14.2 Open Source Dependencies
Alle verwendeten Bibliotheken sind Open Source und MIT-kompatibel.

### 14.3 Branding
- Projektname: MindPanel
- Logo: (Optional)
- Farbschema: Aura Theme Default

---

## 15. Anhänge

### 15.1 Glossar

| Begriff | Beschreibung |
|---------|--------------|
| Widget | Modulares UI-Element im Dashboard |
| Grid Layout | Raster-basiertes Anordnungssystem |
| Pomodoro | Zeitmanagement-Technik (25 Min Fokus, 5 Min Pause) |
| SPA | Single Page Application |
| PWA | Progressive Web App |

### 15.2 Referenzen

- Vue 3: https://vuejs.org/
- Vite: https://vitejs.dev/
- PrimeVue: https://primevue.org/
- Pinia: https://pinia.vuejs.org/
- Open-Meteo API: https://open-meteo.com/

---

## 16. Versions-Historie

| Version | Datum | Änderungen |
|---------|-------|------------|
| 0.0.0 | 2025-12-06 | Initiale Spezifikation |

---

**Ende der Spezifikation**

---

*Erstellt für das MindPanel Projekt*
*Letzte Aktualisierung: 2025-12-06*
