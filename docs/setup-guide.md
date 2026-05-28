# MindPanel Backend Setup Guide

## Stack
- **ui/** — Vue 3 + TypeScript (Frontend)
- **api/** — Kotlin + Spring Boot 3.5 (Backend)
- **MongoDB** — Datenbank
- **Keycloak** — Identity & Access Management (IAM), stellt JWTs aus
- **Docker Compose** — startet MongoDB + Keycloak lokal

---

## Projektstruktur

```
MindPanel-2.0/
├── ui/                  # Vue Frontend
├── api/                 # Spring Boot Backend
├── docker-compose.yml   # MongoDB + Keycloak
└── docs/
    └── setup-guide.md   # diese Datei
```

---

## Erledigte Schritte

### ✅ Schritt 1 — Projektstruktur
Bestehender Frontend-Code wurde in `ui/` verschoben.
`.gitignore` wurde neu und sauber aufgesetzt.

### ✅ Schritt 2 — Spring Boot Projekt
`api/` wurde via Spring Initializr generiert mit:
- `spring-boot-starter-web` — REST API
- `spring-boot-starter-data-mongodb` — MongoDB Anbindung
- `spring-boot-starter-security` — Security-Layer
- `spring-boot-starter-oauth2-resource-server` — JWT-Validierung via Keycloak

### ✅ Schritt 3 — Docker Compose
`docker-compose.yml` im Root mit:
- **MongoDB** auf Port `27017` (User: admin / admin, DB: mindpanel)
- **Keycloak** auf Port `8080` (User: admin / admin, im Dev-Modus)

---

## Offene Schritte

### 🔲 Schritt 4 — application.properties konfigurieren
Datei: `api/src/main/resources/application.properties`

Hier trägt Spring Boot die Verbindungsdaten ein:
- MongoDB URI (wo läuft die DB, welcher User, welche DB?)
- Keycloak Issuer URI (wohin schickt Spring Security die JWT-Validierung?)

### 🔲 Schritt 5 — Docker Compose starten
MongoDB und Keycloak lokal hochfahren und prüfen ob alles läuft.

### 🔲 Schritt 6 — Keycloak Realm einrichten
In der Keycloak Admin-Oberfläche:
- Realm `mindpanel` anlegen
- Client für das Frontend anlegen
- Einen Test-User anlegen

### 🔲 Schritt 7 — Spring Security konfigurieren
Eine `SecurityConfig.kt` anlegen, die:
- Alle Requests gegen Keycloak-JWTs absichert
- CORS für das Frontend erlaubt

### 🔲 Schritt 8 — Ersten REST Endpoint bauen
Einen einfachen geschützten Endpoint als Smoke-Test.

### 🔲 Schritt 9 — Frontend mit Keycloak verbinden
`keycloak-js` in `ui/` einbinden, Login-Flow implementieren.
