# Backend Architektur

## Tech Stack

- **Kotlin** + **Spring Boot 3.5**
- **MongoDB** (Spring Data MongoDB) — App-Daten
- **Keycloak** — Authentifizierung & User-Verwaltung
- **Spring Security OAuth2 Resource Server** — JWT-Validierung

## Schichtenarchitektur

```
Controller  →  Service  →  Repository  →  MongoDB
    ↑
  JWT (Keycloak)
```

## Warum MongoDB?

Die Daten sind dokument-orientiert und user-scoped — jeder User hat seine eigenen Notes, Todos, Widget-Configs. Keine komplexen Joins nötig. MongoDB ist bereits per Docker Compose eingerichtet.

## Collections

| Collection          | Beschreibung                              |
|---------------------|-------------------------------------------|
| `notes`             | Notizen (aktiv + archiviert)              |
| `todos`             | Aufgaben (aktiv + archiviert)             |
| `widget_configs`    | Widget-Reihenfolge und -Status pro User   |
| `pomodoro_settings` | Timer-Einstellungen pro User              |
| `pomodoro_stats`    | Tägliche Pomodoro-Statistiken pro User    |

## User-Daten

User-Daten (Name, E-Mail, Passwort, Rollen) werden **ausschließlich in Keycloak** gespeichert. MongoDB enthält nur die `userId` (= Keycloak `sub`-Claim) zur Verknüpfung.

## API Endpoints

### Notes
| Methode | Pfad                       | Beschreibung            |
|---------|----------------------------|-------------------------|
| GET     | /api/notes                 | Aktive Notizen          |
| GET     | /api/notes/archived        | Archivierte Notizen     |
| POST    | /api/notes                 | Neue Notiz              |
| PUT     | /api/notes/{id}            | Notiz aktualisieren     |
| POST    | /api/notes/{id}/archive    | Archivieren             |
| POST    | /api/notes/{id}/unarchive  | Wiederherstellen        |
| DELETE  | /api/notes/{id}            | Löschen                 |

### Todos
| Methode | Pfad                       | Beschreibung            |
|---------|----------------------------|-------------------------|
| GET     | /api/todos                 | Aktive Todos            |
| GET     | /api/todos/archived        | Archivierte Todos       |
| POST    | /api/todos                 | Neues Todo              |
| PUT     | /api/todos/{id}            | Todo aktualisieren      |
| POST    | /api/todos/{id}/archive    | Archivieren             |
| POST    | /api/todos/{id}/unarchive  | Wiederherstellen        |
| DELETE  | /api/todos/{id}            | Löschen                 |

### Widgets
| Methode | Pfad               | Beschreibung                |
|---------|--------------------|-----------------------------|
| GET     | /api/widgets       | Widget-Config laden         |
| PUT     | /api/widgets       | Widget-Reihenfolge speichern|
| POST    | /api/widgets/reset | Auf Standard zurücksetzen   |

### Pomodoro
| Methode | Pfad                    | Beschreibung              |
|---------|-------------------------|---------------------------|
| GET     | /api/pomodoro/settings  | Einstellungen laden       |
| PUT     | /api/pomodoro/settings  | Einstellungen speichern   |
| GET     | /api/pomodoro/stats     | Tagesstatistiken (7 Tage) |
| POST    | /api/pomodoro/complete  | Pomodoro aufzeichnen      |

## Authentifizierung

Alle Requests benötigen einen gültigen Keycloak JWT im Header:
```
Authorization: Bearer <token>
```

Die User-ID wird aus dem `sub`-Claim des JWT extrahiert.
