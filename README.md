# MindPanel

Ein modernes, offline-fähiges Produktivitäts-Dashboard mit frei verschiebbaren Widgets.

## Features

- **Drag & Drop**: Verschiebe Widgets per Drag & Drop
- **5 leistungsstarke Widgets**:
  - 📋 Aufgaben-Manager mit Prioritäten
  - 📝 Notizen mit Editor
  - 🌤️ Wetter (Open-Meteo API)
  - 📅 Kalender
  - ⏱️ Pomodoro-Timer
- **Dark Mode**: Elegantes dunkles Theme
- **Offline-fähig**: Alle Daten werden lokal gespeichert
- **Responsive**: Funktioniert auf Desktop, Tablet und Mobile
- **Minimalistisch**: Modernes, cleanes Design mit PrimeVue

## Tech Stack

- **Frontend**: Vue 3 (Composition API) + TypeScript
- **Build**: Vite
- **UI**: PrimeVue + PrimeFlex
- **State**: Pinia
- **Router**: Vue Router
- **Drag & Drop**: VueDraggable
- **HTTP**: Axios

## Projekt-Setup

### Dependencies installieren

```bash
npm install
```

### Development Server starten

```bash
npm run dev
```

Die App läuft dann auf [http://localhost:5173](http://localhost:5173)

### Production Build erstellen

```bash
npm run build
```

### Production Preview

```bash
npm run preview
```

## Projektstruktur

```
src/
├── assets/          # CSS und statische Assets
├── components/      # Vue Komponenten
│   ├── layout/     # Layout-Komponenten (AppShell, Sidebar)
│   └── widgets/    # Widget-Komponenten
├── router/          # Vue Router Konfiguration
├── services/        # API Services (z.B. Weather)
├── stores/          # Pinia Stores
├── views/           # View-Komponenten (Pages)
├── App.vue          # Root-Komponente
└── main.ts          # App Entry Point
```

## Widgets

### 📋 Aufgaben-Widget

- Aufgaben erstellen und verwalten
- Bis zu 3 Prioritäten markieren
- Fälligkeitsdatum setzen
- Aufgaben abhaken und löschen

### 📝 Notizen-Widget

- Notizen mit Titel und Inhalt erstellen
- Notizen bearbeiten und löschen
- Übersichtliche Darstellung

### 🌤️ Wetter-Widget

- Aktuelle Wetterdaten via Open-Meteo API
- Stadtsuche mit Geocoding
- Temperatur und Wetterbeschreibung
- Windgeschwindigkeit

### 📅 Kalender-Widget

- Monatsansicht
- Datum auswählen
- Event-Verwaltung (geplant)

### ⏱️ Pomodoro-Widget

- Fokus- und Pausen-Timer
- Anpassbare Dauern
- Automatischer Wechsel
- Pomodoro-Zähler
- Browser-Benachrichtigungen

## Einstellungen

In den Einstellungen kannst du:

- Dark/Light Mode umschalten
- Widgets aktivieren/deaktivieren
- Layout zurücksetzen
- Alle Daten löschen

## Datenspeicherung

Alle Daten werden lokal im Browser gespeichert (localStorage):

- `mindpanel_widgets` - Widget-Konfiguration
- `mindpanel_todos` - Aufgaben
- `mindpanel_notes` - Notizen
- `mindpanel_darkmode` - Theme-Einstellung
- `mindpanel_pomodoro_settings` - Pomodoro-Einstellungen
- `mindpanel_weather_city` - Letzte gewählte Stadt

## Widget-System

Das Widget-System ist bewusst einfach gehalten:

- Widgets werden als Array gespeichert
- Reihenfolge bestimmt die Anzeige
- Drag & Drop ändert die Array-Reihenfolge
- Keine komplexen x/y/w/h Koordinaten
- Responsives CSS Grid Layout

## Zukünftige Features

- Cloud-Sync über Backend
- Erweiterte Kalender-Funktionen
- Habit Tracker
- Analytics Dashboard
- Custom Widgets
- Export/Import

## Lizenz

MIT

## Entwickelt mit ❤️

Viel Spaß mit MindPanel!
