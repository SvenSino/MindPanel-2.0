# MindPanel 2.0

Ein modernes Produktivitäts-Dashboard mit frei verschiebbaren Widgets, REST-Backend und Benutzerauthentifizierung.

## Features

- **Drag & Drop**: Widgets per Drag & Drop verschieben und anordnen
- **5 Widgets**: Aufgaben-Manager, Notizen, Wetter, Kalender, Pomodoro-Timer
- **Dark Mode**: Elegantes dunkles Theme
- **Responsive**: Funktioniert auf Desktop, Tablet und Mobile
- **Benutzerauthentifizierung**: Login via Keycloak (OAuth2 / OIDC)
- **Persistenz**: Alle Daten werden pro Nutzer in MongoDB gespeichert
- **Admin-Panel**: Nutzerverwaltung für Admins

## Tech Stack

| Bereich | Technologie |
|---------|-------------|
| Frontend | Vue 3 (Composition API) + TypeScript + Vite |
| UI | PrimeVue + PrimeFlex |
| State | Pinia |
| HTTP | Axios |
| Backend | Spring Boot 3 + Kotlin |
| Datenbank | MongoDB |
| Auth | Keycloak 26 (OAuth2 / OIDC) |
| Build | Gradle |

## Voraussetzungen

- Node.js 18+
- JDK 21+
- Docker + Docker Compose

## Installation & Start

### 1. Repository klonen

```bash
git clone <repo-url>
cd MindPanel-2.0
```

### 2. Infrastruktur starten (MongoDB + Keycloak)

```bash
docker compose up -d
```

Startet:
- **MongoDB** auf Port `27017`
- **Keycloak** auf Port `8180` (Admin: `admin` / `admin`)

### 3. Keycloak einrichten

Keycloak nutzt eine In-Memory-Datenbank — der Realm `mindpanel` und der Client `mindpanel-api` werden beim Start automatisch importiert. Du musst nur einmalig einen Nutzer anlegen:

1. `http://localhost:8180` öffnen → mit `admin` / `admin` einloggen
2. Realm **mindpanel** auswählen
3. **Users** → "Create new user" → Username + Vor-/Nachname + E-Mail eintragen → "Create"
4. Tab **Credentials** → Passwort setzen → "Temporary" auf `OFF`

### 4. Backend starten

```bash
cd api
./gradlew bootRun
```

Die API läuft auf `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 5. Frontend starten

```bash
cd ui
npm install
npm run dev
```

Das Frontend läuft auf `http://localhost:5173`.

## Projektstruktur

```
MindPanel-2.0/
├── docker-compose.yml       # MongoDB + Keycloak
├── keycloak/
│   └── import/              # Realm-Konfiguration (auto-import)
├── api/                     # Spring Boot Backend
│   └── src/main/kotlin/
│       └── com/mindpanel/api/
│           ├── controller/  # REST Endpoints
│           ├── service/     # Businesslogik
│           ├── repository/  # MongoDB Zugriff
│           ├── model/       # Domain-Objekte
│           ├── security/    # OAuth2 / JWT Konfiguration
│           └── config/      # OpenAPI / Swagger
└── ui/                      # Vue 3 Frontend
    └── src/
        ├── components/      # UI-Komponenten
        ├── views/           # Seiten
        ├── stores/          # Pinia Stores
        └── services/        # API + Keycloak
```

## API Endpoints

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| GET | `/api/todos` | Aktive Todos |
| POST | `/api/todos` | Todo erstellen |
| PUT | `/api/todos/{id}` | Todo aktualisieren |
| DELETE | `/api/todos/{id}` | Todo löschen |
| GET | `/api/notes` | Aktive Notizen |
| POST | `/api/notes` | Notiz erstellen |
| GET | `/api/profile` | Nutzerprofil abrufen |
| PUT | `/api/profile` | Nutzerprofil aktualisieren |
| GET | `/api/pomodoro/settings` | Pomodoro-Einstellungen |
| GET | `/api/pomodoro/stats` | Statistiken |
| GET | `/api/widgets` | Widget-Konfiguration |
| GET | `/api/admin/users` | Alle Nutzer (nur Admin) |

Vollständige Dokumentation: `http://localhost:8080/swagger-ui/index.html`

## Admin-Rolle

Um einem Nutzer Admin-Rechte zu geben:

1. Keycloak öffnen → Realm `mindpanel` → **Users** → Nutzer auswählen
2. Tab **Role mapping** → "Assign role" → Filter auf "Filter by realm roles" → `admin` zuweisen
3. Neu einloggen

## Tests

```bash
cd api
./gradlew test
```
