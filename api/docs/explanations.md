# MindPanel 2.0 — API Erklärung

Dieses Dokument erklärt das gesamte Backend-Projekt: Architektur, Technologien, Konzepte und Domänen.

---

## Inhaltsverzeichnis

1. [Gesamtsystem-Überblick](#1-gesamtsystem-überblick)
2. [Infrastruktur: Keycloak & MongoDB](#2-infrastruktur-keycloak--mongodb)
3. [OAuth 2.0 vs. OIDC — der Unterschied](#3-oauth-20-vs-oidc--der-unterschied)
4. [Wie JWT-Authentication hier funktioniert](#4-wie-jwt-authentication-hier-funktioniert)
5. [Authentication Principal](#5-authentication-principal)
6. [Spring Security Resource Server](#6-spring-security-resource-server)
7. [Schichtenarchitektur](#7-schichtenarchitektur)
8. [Domänen (Domain Objects)](#8-domänen-domain-objects)
9. [Repositories — Datenzugriff](#9-repositories--datenzugriff)
10. [Services — Businesslogik](#10-services--businesslogik)
11. [Controllers — HTTP-Schnittstelle](#11-controllers--http-schnittstelle)
12. [Datenisolation & Sicherheit](#12-datenisolation--sicherheit)
13. [Tests](#13-tests)
14. [Jest vs. JUnit — Testframeworks im Vergleich](#14-jest-vs-junit--testframeworks-im-vergleich)
15. [MongoDB — Datenspeicherung im Detail](#15-mongodb--datenspeicherung-im-detail)

---

## 1. Gesamtsystem-Überblick

```
┌─────────────────┐        JWT Token         ┌──────────────────────┐
│   Vue Frontend  │ ──────────────────────►  │  Spring Boot API     │
│  (Port 5173)    │ Authorization: Bearer ... │  (Port 8080)         │
└─────────────────┘                           └──────────┬───────────┘
         │                                               │
         │ Login / Token holen                           │ Validate Token
         ▼                                               ▼
┌─────────────────┐                           ┌──────────────────────┐
│    Keycloak     │ ◄─────────────────────── │  Keycloak OIDC       │
│  (Port 8180)    │   Public Key / Discovery  │  Discovery Endpoint  │
└─────────────────┘                           └──────────────────────┘
                                                         │
                                                         │ Daten lesen/schreiben
                                                         ▼
                                              ┌──────────────────────┐
                                              │      MongoDB         │
                                              │  (Port 27017)        │
                                              └──────────────────────┘
```

Das System besteht aus drei Diensten, die alle via Docker Compose lokal laufen:

| Dienst | Port | Aufgabe |
|--------|------|---------|
| **Spring Boot API** | 8080 | REST-Backend, Businesslogik, Datenzugriff |
| **Keycloak** | 8180 | Identity Provider — Login, Token-Ausgabe, User-Management |
| **MongoDB** | 27017 | NoSQL-Datenbank — speichert alle Anwendungsdaten |

---

## 2. Infrastruktur: Keycloak & MongoDB

### Keycloak

Keycloak ist ein Open-Source **Identity and Access Management** (IAM) System. Es übernimmt alles rund um Authentifizierung und Identität:

- **User-Verwaltung:** Registrierung, Login, Passwort-Reset
- **Realm:** Ein "Mandant" in Keycloak. Hier heißt der Realm `mindpanel`. Jeder Realm hat eigene User, Clients und Konfigurationen.
- **Client:** Eine registrierte Anwendung, die Keycloak nutzt. Der Client `mindpanel-api` repräsentiert das Frontend, das sich beim Backend authentifiziert.
- **Token-Ausgabe:** Nach erfolgreichem Login stellt Keycloak JWTs (JSON Web Tokens) aus.
- **Dev-Modus:** Im docker-compose läuft Keycloak mit `KC_DB: dev-mem`, d.h. alle Daten liegen im Arbeitsspeicher und werden beim Neustart gelöscht.
- **Realm & Client** werden automatisch wiederhergestellt, weil `keycloak/import/mindpanel-realm.json` beim Start importiert wird (`--import-realm`).
- **User werden nicht persistiert.** Sie liegen nur im Arbeitsspeicher und müssen nach jedem Neustart manuell neu angelegt werden. Grund: Passwort-Hashes werden nicht in den Realm-Export aufgenommen. Alternativen: Test-User mit festem Hash ins Import-JSON schreiben (Option A), oder Keycloak eine echte PostgreSQL-Datenbank geben (Option B).

### MongoDB

MongoDB ist eine **dokumentenorientierte NoSQL-Datenbank**. Statt Tabellen und Zeilen (wie SQL) werden JSON-ähnliche Dokumente in Collections gespeichert.

**Wichtige Unterschiede zu SQL:**

| SQL | MongoDB |
|-----|---------|
| Tabelle | Collection |
| Zeile | Dokument |
| Spalte | Feld |
| JOIN | Eingebettetes Dokument oder Referenz |
| Schema-Zwang | Flexibles Schema (optional) |

In diesem Projekt hat jede Domain-Klasse eine eigene Collection (z.B. `todos`, `notes`, `user_profiles`). MongoDB vergibt automatisch eine `_id` (ObjectId), die als String im Kotlin-Objekt gespeichert wird.

---

## 3. OAuth 2.0 vs. OIDC — der Unterschied

Das ist eines der häufigsten Missverständnisse in der Webentwicklung.

### OAuth 2.0 — Autorisierung

OAuth 2.0 ist ein **Autorisierungsprotokoll**. Es beantwortet die Frage:

> "Darf diese Anwendung auf meine Ressourcen zugreifen?"

OAuth 2.0 ist **kein Authentifizierungsprotokoll** — es sagt *nicht*, wer der User ist, sondern nur, was er darf. Es definiert verschiedene "Flows" (Grant Types):

- **Authorization Code Flow:** Der häufigste. User loggt sich ein, bekommt einen Code, der gegen ein Access Token getauscht wird. (Wird hier verwendet)
- **Client Credentials Flow:** Für Maschine-zu-Maschine-Kommunikation ohne User.
- **Implicit Flow:** Veraltet, für SPAs nicht mehr empfohlen.

**Was OAuth 2.0 ausgibt:**
- **Access Token:** Berechtigt zum Zugriff auf eine Ressource (z.B. die API)
- **Refresh Token:** Zum Erneuern des Access Tokens ohne erneuten Login

**Was OAuth 2.0 NICHT definiert:**
- Format des Tokens (kann alles sein)
- Was der Token über den User aussagt
- Wie der User authentifiziert wird

### OIDC — OpenID Connect — Authentifizierung

OpenID Connect (OIDC) ist eine **Erweiterung von OAuth 2.0**, die Authentifizierung hinzufügt. Es beantwortet:

> "Wer ist dieser User?"

OIDC ergänzt OAuth 2.0 um:

1. **ID Token:** Ein JWT, das Informationen über den User enthält (wer er ist, wann er sich eingeloggt hat)
2. **UserInfo Endpoint:** Ein standardisierter Endpunkt um User-Details abzurufen
3. **Discovery Endpoint:** `/.well-known/openid-configuration` — teilt automatisch mit, wo Public Keys, Token-Endpunkt etc. zu finden sind
4. **Standard Claims:** `sub` (Subject/User-ID), `email`, `name`, `iat`, `exp`, etc.

### Zusammenfassung

```
OAuth 2.0:  "Darf App X auf Ressource Y zugreifen?"  → Access Token
OIDC:       "Wer ist der eingeloggte User?"          → ID Token (JWT mit User-Daten)

OIDC baut auf OAuth 2.0 auf. Man kann OIDC nicht ohne OAuth 2.0 verwenden,
aber OAuth 2.0 ohne OIDC (dann hat man keine User-Identität).
```

### Wie es hier verwendet wird

- Das **Frontend** nutzt OIDC (Authorization Code Flow), um den User bei Keycloak einzuloggen
- Es bekommt ein **Access Token (JWT)**
- Das Access Token wird bei jedem API-Request als `Authorization: Bearer <token>` mitgeschickt
- Das **Backend** validiert dieses Access Token (agiert als OAuth 2.0 Resource Server)
- Das Backend liest die User-ID (`sub` claim) aus dem Token — das ist der Authentication Principal

---

## 4. Wie JWT-Authentication hier funktioniert

### Was ist ein JWT?

Ein **JSON Web Token** (JWT, sprich: "jot") ist ein kompaktes, URL-sicheres Token-Format. Es besteht aus drei Base64-codierten Teilen, getrennt durch Punkte:

```
eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyLWlkLTEyMyIsImV4cCI6MTcxNjY1NjAwMH0.SIGNATUR
      ▲                          ▲                                    ▲
   Header                     Payload                            Signature
(Algorithmus)          (Claims / User-Daten)              (Kryptographische Signatur)
```

**Header** — Metadaten:
```json
{ "alg": "RS256", "typ": "JWT" }
```

**Payload** — die eigentlichen Daten (Claims):
```json
{
  "sub": "a3f9c1d2-4e5b-4f6a-8c7d-9e0f1a2b3c4d",  // User-ID (Subject)
  "iss": "http://localhost:8180/realms/mindpanel",   // Aussteller (Issuer)
  "exp": 1716657000,                                 // Ablaufzeit (Unix Timestamp)
  "iat": 1716653400,                                 // Ausstellungszeit
  "email": "user@example.com",
  "name": "Max Mustermann"
}
```

**Signature** — verhindert Manipulation des Tokens:
- Keycloak signiert das Token mit seinem **Private Key** (RS256 = RSA + SHA-256)
- Die API kann die Signatur mit Keycloak's **Public Key** verifizieren
- Wenn jemand den Payload ändert, ist die Signatur ungültig

### Token-Validierungsablauf

```
1. Frontend schickt:  GET /api/todos
                      Authorization: Bearer eyJhbGc...

2. Spring Security intercepted den Request

3. Spring ruft Keycloak's Discovery Endpoint ab:
   http://localhost:8180/realms/mindpanel/.well-known/openid-configuration
   → Bekommt u.a. den "jwks_uri" (URL zu den Public Keys)

4. Spring holt Public Keys von Keycloak (wird gecacht):
   http://localhost:8180/realms/mindpanel/protocol/openid-connect/certs

5. Spring validiert:
   a) Signatur des JWTs mit dem Public Key
   b) Ablaufzeit (exp > jetzt?)
   c) Issuer (iss == konfigurierter issuer-uri?)

6. Wenn alles ok: Request wird weitergeleitet, JWT ist verfügbar als Authentication Principal

7. Controller extrahiert User-ID: jwt.subject == "a3f9c1d2-..."
```

Das Schöne: Die API muss **nie** bei Keycloak anfragen ob der User eingeloggt ist. Die Validierung funktioniert rein durch Kryptographie — der Public Key reicht.

---

## 5. Authentication Principal

Ein **Authentication Principal** (dt. "Authentifizierungssubjekt") ist das Objekt, das nach erfolgreicher Authentifizierung den **aktuell eingeloggten User** repräsentiert.

In Spring Security ist der Principal im `SecurityContext` gespeichert und pro Request verfügbar.

### In diesem Projekt

Da wir JWT/OAuth2 verwenden, ist der Principal vom Typ `org.springframework.security.oauth2.jwt.Jwt` — eine Klasse, die den dekodieren JWT-Token repräsentiert.

```kotlin
@GetMapping
fun getTodos(@AuthenticationPrincipal jwt: Jwt): List<Todo> {
    val userId = jwt.subject  // = jwt.getClaim("sub") = Keycloak User-ID
    return todoService.getTodos(userId)
}
```

**Was `@AuthenticationPrincipal` macht:**
- Spring injiziert automatisch das aktuelle `Jwt`-Objekt in den Method-Parameter
- Keine manuelle Abfrage des `SecurityContext` nötig
- Sauber und typsicher

**Wichtige `Jwt`-Methoden:**

| Methode | Was es ist | Beispielwert |
|---------|-----------|--------------|
| `jwt.subject` | Keycloak User-ID (sub claim) | `"a3f9c1d2-4e5b-..."` |
| `jwt.getClaim("email")` | E-Mail des Users | `"user@example.com"` |
| `jwt.getClaim("name")` | Anzeigename | `"Max Mustermann"` |
| `jwt.issuedAt` | Ausstellungszeit | `Instant` |
| `jwt.expiresAt` | Ablaufzeit | `Instant` |
| `jwt.issuer` | Keycloak Realm URL | `http://localhost:8180/realms/mindpanel` |

**Warum `sub` (Subject) als User-ID?**

Der `sub` Claim ist in OIDC/JWT standardisiert als eindeutige, unveränderliche User-Kennung. E-Mail-Adressen können sich ändern, Benutzernamen auch — aber die `sub` bleibt konstant. Daher wird sie als `userId` in allen MongoDB-Dokumenten gespeichert.

---

## 6. Spring Security Resource Server

Das Backend agiert als **OAuth 2.0 Resource Server**. Das bedeutet: Es stellt geschützte Ressourcen (die REST-Endpoints) bereit und vertraut auf Tokens, die von einem externen Authorization Server (Keycloak) ausgestellt wurden.

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
        return http.build()
    }
}
```

**Was jede Zeile bedeutet:**

- `csrf { it.disable() }` — CSRF-Schutz deaktiviert. CSRF-Angriffe funktionieren nur mit Session-Cookies. Da wir stateless JWT verwenden, gibt es keine Sessions und daher kein CSRF-Risiko.

- `anyRequest().authenticated()` — Jeder Request muss authentifiziert sein. Kein Endpoint ist öffentlich zugänglich (außer intern von Spring, z.B. Actuator wenn konfiguriert).

- `oauth2ResourceServer { oauth2 -> oauth2.jwt {} }` — Aktiviert JWT-Validierung. Spring konfiguriert sich selbst via `application.properties`:
  ```properties
  spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/mindpanel
  ```
  Spring nutzt den `issuer-uri` um den OIDC Discovery Endpoint aufzurufen und alles weitere (Public Keys, etc.) automatisch zu konfigurieren.

---

## 7. Schichtenarchitektur

Das Projekt folgt einer klassischen **Drei-Schichten-Architektur**:

```
┌─────────────────────────────────────────────────────────┐
│                   Controller Layer                       │
│   HTTP-Requests empfangen, Authentication auslesen,     │
│   Request-Daten validieren, Response zurückgeben        │
│   z.B. TodoController, NoteController                   │
├─────────────────────────────────────────────────────────┤
│                   Service Layer                          │
│   Businesslogik ausführen, Regeln durchsetzen,          │
│   Daten transformieren, Fehler werfen                   │
│   z.B. TodoService, NoteService                         │
├─────────────────────────────────────────────────────────┤
│                  Repository Layer                        │
│   Datenbankzugriffe, CRUD-Operationen,                  │
│   MongoDB-Queries                                        │
│   z.B. TodoRepository, NoteRepository                   │
├─────────────────────────────────────────────────────────┤
│                    MongoDB                               │
│   Collections: todos, notes, user_profiles, ...         │
└─────────────────────────────────────────────────────────┘
```

**Warum diese Trennung?**

- **Testbarkeit:** Services können mit gemockten Repositories getestet werden (keine echte DB nötig)
- **Austauschbarkeit:** Man könnte MongoDB gegen PostgreSQL tauschen, ohne Controllers zu ändern
- **Klarheit:** Jede Schicht hat eine definierte Aufgabe — kein vermischter Code

**Datenfluss Beispiel — `GET /api/todos`:**

```
1. TodoController.getTodos()
   └─ Liest userId aus JWT
   └─ Ruft todoService.getTodos(userId) auf

2. TodoService.getTodos(userId)
   └─ Ruft todoRepository.findByUserIdAndArchivedFalse(userId) auf

3. TodoRepository.findByUserIdAndArchivedFalse(userId)
   └─ MongoDB Query: { userId: "...", archived: false }
   └─ Gibt List<Todo> zurück

4. Rückweg: List<Todo> → Service → Controller → JSON Response
```

---

## 8. Domänen (Domain Objects)

Eine **Domäne** beschreibt einen fachlichen Bereich der Anwendung mit seinen Daten und Regeln. Jede Domäne hat ein oder mehrere **Domain Objects** (Kotlin data classes mit `@Document` Annotation für MongoDB).

### UserProfile

```kotlin
@Document(collection = "user_profiles")
data class UserProfile(
    @Id val id: String,
    val userId: String,    // Keycloak sub
    val street: String,
    val zipCode: String,
    val city: String,
    val country: String,
    val avatar: String?    // Optional: Avatar-URL
)
```

**Fachliche Bedeutung:** Persönliche Angaben des Users (Adresse, Avatar). Wird automatisch angelegt (leer) wenn der User das erste Mal sein Profil aufruft.

---

### Todo

```kotlin
@Document(collection = "todos")
data class Todo(
    @Id val id: String,
    val userId: String,
    val title: String,
    val completed: Boolean = false,
    val isPriority: Boolean = false,
    val dueDate: LocalDate? = null,
    val archived: Boolean = false,
    val createdAt: Instant = Instant.now()
)
```

**Fachliche Bedeutung:** Eine Aufgabe im To-Do-Widget.

**Businessregeln:**
- Max. **3 aktive Priority-Todos** pro User (400 BAD_REQUEST wenn überschritten)
- `archived = true` = Soft-Delete (Todo bleibt in DB, wird aber nicht im normalen View angezeigt)
- `completed` und `archived` sind unabhängig voneinander

---

### Note

```kotlin
@Document(collection = "notes")
data class Note(
    @Id val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val archived: Boolean = false,
    val createdAt: Instant = Instant.now()
)
```

**Fachliche Bedeutung:** Eine Notiz im Notes-Widget. Einfaches Titel + Inhalt Konzept.

---

### PomodoroSettings

```kotlin
@Document(collection = "pomodoro_settings")
data class PomodoroSettings(
    @Id val id: String,
    val userId: String,
    val focusDuration: Int = 25,
    val breakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val autoStart: Boolean = false
)
```

**Fachliche Bedeutung:** Persönliche Timer-Einstellungen für die Pomodoro-Technik. Wird mit Standardwerten (25/5/15 Minuten) angelegt, wenn noch keine existieren.

Die **Pomodoro-Technik** ist eine Zeitmanagement-Methode: 25 Minuten fokussiertes Arbeiten, dann 5 Minuten Pause. Nach 4 Pomodoros: 15 Minuten lange Pause.

---

### PomodoroStat

```kotlin
@Document(collection = "pomodoro_stats")
data class PomodoroStat(
    @Id val id: String,
    val userId: String,
    val date: LocalDate,
    val count: Int
)
```

**Fachliche Bedeutung:** Wie viele Pomodoros hat der User an einem bestimmten Tag abgeschlossen? Ein Dokument pro Tag pro User. Wird bei `POST /api/pomodoro/complete` inkrementiert (oder neu angelegt).

---

### WidgetConfig

```kotlin
@Document(collection = "widget_configs")
data class WidgetConfig(
    @Id val id: String,
    val userId: String,
    val widgets: List<Widget>
)

data class Widget(
    val id: String,       // z.B. "weather", "todos"
    val type: String,
    val enabled: Boolean,
    val title: String
)
```

**Fachliche Bedeutung:** Konfiguration des Dashboards — welche Widgets sind sichtbar, in welcher Reihenfolge. Default-Konfiguration:

| Widget-ID | Titel | Enabled |
|-----------|-------|---------|
| `weather` | Wetter | ✓ |
| `todos` | Aufgaben | ✓ |
| `calendar` | Kalender | ✓ |
| `notes` | Notizen | ✓ |
| `pomodoro` | Pomodoro | ✗ |

---

## 9. Repositories — Datenzugriff

Repositories sind **Interfaces** (keine Klassen), die von Spring Data MongoDB implementiert werden. Man definiert nur die Methoden-Signaturen — Spring generiert die MongoDB-Queries automatisch anhand des Methodennamens.

```kotlin
interface TodoRepository : MongoRepository<Todo, String> {
    // Spring generiert: { userId: ?, archived: false }
    fun findByUserIdAndArchivedFalse(userId: String): List<Todo>

    // Spring generiert: { _id: ?, userId: ? } — Ownership-Check!
    fun findByIdAndUserId(id: String, userId: String): Todo?

    // Spring generiert: { userId: ?, isPriority: true, archived: false }
    fun countByUserIdAndIsPriorityTrueAndArchivedFalse(userId: String): Int
}
```

**`MongoRepository<T, ID>` bietet automatisch:**
- `save(entity)` — Insert oder Update
- `findById(id)` — Suche nach ID
- `delete(entity)` — Löschen
- `findAll()` — Alle Dokumente
- ... und mehr

**Query-Methoden-Namenskonvention:**
- `findBy` + Feldname = WHERE-Klausel
- `And` = AND-Verknüpfung
- `True`/`False` = Boolean-Filter
- `count` statt `find` = Zählen statt Abrufen

---

## 10. Services — Businesslogik

Services sind `@Service`-annotierte Klassen, die die Businesslogik kapseln. Sie werden per **Dependency Injection** in Controllers und andere Services eingebunden.

### Ownership-Validation

Jede Schreib- und Leseoperation prüft, ob das Dokument dem aufrufenden User gehört:

```kotlin
fun getTodo(userId: String, todoId: String): Todo {
    return todoRepository.findByIdAndUserId(todoId, userId)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found")
}
```

Durch `findByIdAndUserId` (statt nur `findById`) wird sichergestellt, dass User A niemals auf Todos von User B zugreifen kann — auch wenn er die ID kennt.

### Fehlerbehandlung

Services werfen `ResponseStatusException` mit dem passenden HTTP-Status:

```kotlin
// 404: Ressource nicht gefunden oder gehört nicht dem User
throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found")

// 400: Businessregel verletzt
throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 3 priority todos allowed")
```

Spring wandelt diese Exceptions automatisch in entsprechende HTTP-Responses um.

### Auto-Erstellung bei erstem Zugriff ("Get or Create")

Für Settings und Configs wird beim ersten Abruf ein Default-Objekt angelegt:

```kotlin
fun getSettings(userId: String): PomodoroSettings {
    return pomodoroSettingsRepository.findByUserId(userId)
        ?: pomodoroSettingsRepository.save(
            PomodoroSettings(
                id = ObjectId.get().toHexString(),
                userId = userId,
                // Default-Werte aus dem data class
            )
        )
}
```

---

## 11. Controllers — HTTP-Schnittstelle

Controllers sind `@RestController`-annotierte Klassen. Sie definieren REST-Endpoints und verbinden HTTP mit der Businesslogik.

### Aufbau eines Controllers

```kotlin
@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {

    @GetMapping
    fun getTodos(@AuthenticationPrincipal jwt: Jwt): List<Todo> {
        return todoService.getTodos(jwt.subject)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTodo(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateTodoRequest
    ): Todo {
        return todoService.createTodo(jwt.subject, request.title, request.dueDate, request.isPriority)
    }
}
```

**Wichtige Annotationen:**

| Annotation | Bedeutung |
|-----------|----------|
| `@RestController` | Kombiniert `@Controller` + `@ResponseBody` (jede Methode gibt JSON zurück) |
| `@RequestMapping("/api/todos")` | Basis-URL für alle Endpoints in diesem Controller |
| `@GetMapping` | HTTP GET |
| `@PostMapping` | HTTP POST |
| `@PutMapping("/{id}")` | HTTP PUT mit Path-Variable |
| `@DeleteMapping("/{id}")` | HTTP DELETE |
| `@ResponseStatus(CREATED)` | Gibt 201 statt 200 zurück |
| `@RequestBody` | Deserialisiert den Request-Body als JSON → Kotlin-Objekt |
| `@PathVariable` | Wert aus der URL (z.B. `{todoId}`) |
| `@AuthenticationPrincipal jwt: Jwt` | Injiziert den validierten JWT-Token |

### REST-Endpoints Übersicht

| Domäne | Endpoint | Methode | Beschreibung |
|--------|----------|---------|--------------|
| Todos | `GET /api/todos` | GET | Aktive Todos |
| Todos | `GET /api/todos/archived` | GET | Archivierte Todos |
| Todos | `POST /api/todos` | POST | Todo erstellen |
| Todos | `PUT /api/todos/{id}` | PUT | Todo updaten |
| Todos | `POST /api/todos/{id}/archive` | POST | Todo archivieren |
| Todos | `POST /api/todos/{id}/unarchive` | POST | Todo wiederherstellen |
| Todos | `DELETE /api/todos/{id}` | DELETE | Todo löschen |
| Notes | `GET /api/notes` | GET | Aktive Notizen |
| Notes | `GET /api/notes/archived` | GET | Archivierte Notizen |
| Notes | `POST /api/notes` | POST | Notiz erstellen |
| Notes | `PUT /api/notes/{id}` | PUT | Notiz updaten |
| Notes | `POST /api/notes/{id}/archive` | POST | Archivieren |
| Notes | `POST /api/notes/{id}/unarchive` | POST | Wiederherstellen |
| Notes | `DELETE /api/notes/{id}` | DELETE | Löschen |
| Pomodoro | `GET /api/pomodoro/settings` | GET | Einstellungen abrufen |
| Pomodoro | `PUT /api/pomodoro/settings` | PUT | Einstellungen speichern |
| Pomodoro | `GET /api/pomodoro/stats?days=7` | GET | Statistiken (Map Date→Anzahl) |
| Pomodoro | `POST /api/pomodoro/complete` | POST | Pomodoro als erledigt markieren |
| Profil | `GET /api/profile` | GET | Profil abrufen |
| Profil | `PUT /api/profile` | PUT | Profil updaten |
| Widgets | `GET /api/widgets` | GET | Widget-Konfiguration |
| Widgets | `PUT /api/widgets` | PUT | Widgets anpassen |
| Widgets | `POST /api/widgets/reset` | POST | Auf Standardwerte zurücksetzen |

---

## 12. Datenisolation & Sicherheit

### Warum keine User-Daten durchsickern können

Jedes MongoDB-Dokument enthält ein `userId`-Feld (Keycloak `sub`). Repository-Queries filtern immer nach dieser ID:

```
User A (userId: "aaa") fragt GET /api/todos
  → todoRepository.findByUserIdAndArchivedFalse("aaa")
  → MongoDB: { userId: "aaa", archived: false }
  → Gibt nur Todos von User A zurück ✓

User A versucht Todo von User B zu löschen:
  → DELETE /api/todos/{todo_b_id}
  → todoRepository.findByIdAndUserId(todo_b_id, "aaa")
  → MongoDB findet kein Dokument (ID + userId passen nicht zusammen)
  → 404 NOT_FOUND ✓ (kein 403, damit User A nicht weiß, ob die ID existiert)
```

### Stateless Authentication

Die API speichert **keine Sessions** (kein `HttpSession`, kein JSESSIONID-Cookie). Jeder Request muss einen gültigen JWT mitschicken. Das macht die API:

- **Horizontal skalierbar:** Mehrere Server-Instanzen möglich, kein Session-Sharing nötig
- **Einfacher:** Kein State, der verwaltet werden muss
- **Sicher:** Kein Session-Hijacking möglich

---

## 13. Tests

Das Projekt hat zwei Arten von Tests:

### Unit Tests (Service-Tests)

Testen die Businesslogik **isoliert**, ohne echte MongoDB:

```kotlin
@ExtendWith(MockitoExtension::class)
class TodoServiceTest {
    @Mock private lateinit var todoRepository: TodoRepository  // Mock-Objekt
    @InjectMocks private lateinit var todoService: TodoService // Echter Service

    @Test
    fun `createTodo throws when priority limit reached`() {
        whenever(todoRepository.countByUserIdAndIsPriorityTrueAndArchivedFalse("user1"))
            .thenReturn(3) // Simuliere: 3 Priority-Todos existieren bereits

        assertThrows<ResponseStatusException> {
            todoService.createTodo("user1", "Neues Todo", null, isPriority = true)
        }
    }
}
```

- Mocks simulieren das Repository-Verhalten
- Schnell (keine DB-Verbindung nötig)
- Testen spezifische Businessregeln

### Integration Tests (Controller-Tests)

Testen den vollständigen HTTP-Request-Cycle **mit echter Spring Security**:

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {
    @Test
    fun `GET todos returns 401 without token`() {
        mockMvc.get("/api/todos").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `GET todos returns todos for authenticated user`() {
        mockMvc.get("/api/todos") {
            with(jwt().jwt { it.subject("user1") })  // Simulierter JWT
        }.andExpect {
            status { isOk() }
        }
    }
}
```

- `@AutoConfigureMockMvc` — Echter Spring-Context, aber ohne HTTP-Server
- `jwt()` aus `spring-security-test` — Simuliert einen validierten JWT (kein echter Keycloak nötig)
- Testen HTTP-Status-Codes, JSON-Serialisierung, Autorisierung

---

## 14. Jest vs. JUnit — Testframeworks im Vergleich

**Jest wird in diesem Projekt nicht verwendet.** Das Frontend (`/ui`) hat aktuell keine Tests. Das Backend nutzt JUnit 5 + Mockito.

Jest ist ein **JavaScript-Testframework** (von Meta), das vor allem im React/Vue-Ökosystem verbreitet ist. JUnit 5 ist das Äquivalent für die JVM-Welt. Beide lösen dasselbe Problem — automatisiertes Prüfen, ob Code das tut, was er soll.

### Konzept-Vergleich

| Konzept | Jest (JavaScript) | JUnit 5 (Kotlin/Java) |
|---------|-------------------|----------------------|
| Test definieren | `test('beschreibung', () => {})` | `@Test fun \`beschreibung\`()` |
| Erwartung prüfen | `expect(x).toBe(y)` | `assertEquals(y, x)` |
| Fehler erwarten | `expect(() => fn()).toThrow()` | `assertThrows<Exception> { fn() }` |
| Setup vor jedem Test | `beforeEach(() => {})` | `@BeforeEach fun setup()` |
| Mocking | `jest.fn()` / `jest.mock(...)` | `@Mock` + Mockito (`whenever(...).thenReturn(...)`) |
| Tests starten | `npm test` | `./gradlew test` |

### Mocking — das zentrale Konzept

Ein **Mock** ist ein Fake-Objekt, das echte Abhängigkeiten ersetzt. Ziel: eine Einheit (z.B. Service) isoliert testen, ohne ihre Abhängigkeiten (z.B. Datenbank) wirklich aufzurufen.

**Jest:**
```javascript
const todoRepo = { findByUserId: jest.fn().mockResolvedValue([]) }
```

**Mockito (Kotlin):**
```kotlin
@Mock private lateinit var todoRepository: TodoRepository
whenever(todoRepository.findByUserIdAndArchivedFalse("user1")).thenReturn(emptyList())
```

Beide sagen dasselbe: "Wenn diese Methode aufgerufen wird, gib diesen Wert zurück — ohne echte Logik auszuführen."

### Wenn das Frontend Tests bekäme

Für Vue + Vite wäre **Vitest** die natürliche Wahl — es ist Jest-kompatibel, aber für das Vite-Ökosystem optimiert (gleiche API, deutlich schneller). Dazu käme `@vue/test-utils` zum Rendern und Interagieren mit Komponenten.

---

## 15. MongoDB — Datenspeicherung im Detail

### Was MongoDB ist

MongoDB ist eine **dokumentenorientierte NoSQL-Datenbank**. Statt Zeilen in Tabellen speichert sie **Dokumente** im BSON-Format (Binary JSON — intern wie JSON, aber effizienter gespeichert).

Jedes Dokument ist ein eigenständiges Objekt, das beliebig verschachtelt sein kann:

```json
{
  "_id": ObjectId("6650f3a2c1234567890abcde"),
  "userId": "a3f9c1d2-4e5b-4f6a-8c7d-9e0f1a2b3c4d",
  "title": "Dokumentation schreiben",
  "completed": false,
  "isPriority": true,
  "dueDate": "2026-05-30",
  "archived": false,
  "createdAt": ISODate("2026-05-25T10:00:00Z")
}
```

### Grundbegriffe

| Begriff | Bedeutung | SQL-Analogie |
|---------|-----------|-------------|
| **Database** | Oberste Organisationseinheit, hier: `mindpanel` | Database |
| **Collection** | Gruppe gleichartiger Dokumente, z.B. `todos` | Tabelle |
| **Document** | Ein einzelner Datensatz als JSON/BSON | Zeile |
| **Field** | Ein einzelner Wert im Dokument, z.B. `title` | Spalte |
| **`_id`** | Eindeutiger Pflicht-Bezeichner jedes Dokuments | Primary Key |

**Wichtiger Unterschied zu SQL:** MongoDB erzwingt kein festes Schema. Zwei Dokumente in derselben Collection könnten unterschiedliche Felder haben. In Spring wird das durch die Kotlin-Datenklassen de facto einheitlich gehalten.

### Collections in diesem Projekt

| Collection | Kotlin-Klasse | Gespeicherte Daten |
|-----------|--------------|-------------------|
| `todos` | `Todo` | Aufgaben mit Status, Priorität, Fälligkeit |
| `notes` | `Note` | Notizen mit Titel und Inhalt |
| `user_profiles` | `UserProfile` | Adresse und Avatar |
| `pomodoro_settings` | `PomodoroSettings` | Timer-Konfiguration |
| `pomodoro_stats` | `PomodoroStat` | Tägliche Pomodoro-Zähler |
| `widget_configs` | `WidgetConfig` | Dashboard-Layout |

### Die `_id` und `@Id`

MongoDB vergibt für jedes neue Dokument automatisch eine **ObjectId** — ein 12-Byte-Wert, der als 24-stelliger Hex-String dargestellt wird, z.B. `6650f3a2c1234567890abcde`.

In Kotlin wird das Feld mit `@Id` markiert. Spring Data konvertiert die ObjectId automatisch in einen `String`:

```kotlin
@Document(collection = "todos")
data class Todo(
    @Id val id: String,   // In MongoDB: _id: ObjectId("6650f3a2...")
    ...
)
```

Beim Erstellen wird die ID manuell generiert:
```kotlin
id = ObjectId.get().toHexString()   // z.B. "6650f3a2c1234567890abcde"
```

---

### Wo die Daten physisch gespeichert werden

#### Im Docker-Container mit Volume

In `docker-compose.yml` ist MongoDB so konfiguriert:

```yaml
mongodb:
  image: mongo:8
  volumes:
    - mongodb_data:/data/db   # Named Volume

volumes:
  mongodb_data:               # Von Docker verwaltet
```

MongoDB speichert alle Daten intern unter `/data/db`. Das **Named Volume** `mongodb_data` wird von Docker verwaltet und liegt auf dem Host-System unter:

```
/var/lib/docker/volumes/mindpanel-20_mongodb_data/_data/
```

Das Volume überlebt Container-Neustarts — Daten gehen also nicht verloren, wenn der Container gestoppt wird. Nur `docker compose down -v` (mit `-v`) löscht das Volume und damit alle Daten.

**Keycloak** dagegen nutzt `KC_DB: dev-mem` (in-memory), d.h. Keycloak-Daten (User, Realm-Konfiguration) werden bei jedem Neustart zurückgesetzt.

---

### Wie Spring mit MongoDB interagiert

Spring Data MongoDB übernimmt die gesamte Kommunikation mit der Datenbank. Der Ablauf von Code zu Datenbank:

```
Kotlin Code (Service)
      │
      │  todoRepository.save(todo)
      ▼
Spring Data MongoDB
      │  Konvertiert Kotlin-Objekt → BSON-Dokument
      │  Führt MongoDB-Operationen aus (insertOne / replaceOne)
      ▼
MongoDB Driver (java-driver)
      │  Sendet BSON über TCP
      ▼
MongoDB (Port 27017)
      │  Schreibt in Collection "todos"
      ▼
Datei auf Disk (/data/db)
```

#### Verbindungskonfiguration

Die Verbindung wird in `application.properties` konfiguriert:

```properties
spring.data.mongodb.uri=mongodb://admin:adminnimda@localhost:27017/mindpanel?authSource=admin
```

Aufgeschlüsselt:

| Teil | Wert | Bedeutung |
|------|------|-----------|
| `mongodb://` | — | Protokoll |
| `admin:adminnimda` | Credentials | Username:Passwort (aus docker-compose) |
| `localhost:27017` | Host:Port | MongoDB-Adresse |
| `/mindpanel` | Database | Datenbankname |
| `?authSource=admin` | Parameter | Auth erfolgt gegen die `admin`-Datenbank |

#### Was Spring Data automatisch macht

Spring Data MongoDB generiert zur Laufzeit Implementierungen für alle Repository-Interfaces. Man schreibt nur:

```kotlin
interface TodoRepository : MongoRepository<Todo, String> {
    fun findByUserIdAndArchivedFalse(userId: String): List<Todo>
}
```

Spring leitet daraus automatisch die MongoDB-Query ab:
```json
{ "userId": "<wert>", "archived": false }
```

Für komplexere Queries gibt es `@Query`:
```kotlin
@Query("{ 'userId': ?0, 'completed': true }")
fun findCompletedByUserId(userId: String): List<Todo>
```

#### Object Mapping

Spring Data übernimmt die Konvertierung zwischen Kotlin-Objekten und BSON-Dokumenten:

```
Kotlin Todo(id="abc", title="Test", completed=false)
      ↕  (automatisches Mapping)
BSON  { _id: ObjectId("abc"), title: "Test", completed: false }
```

Sonderfall: `LocalDate` und `Instant` werden als String bzw. Date in BSON gespeichert. Spring konfiguriert die nötigen Converter automatisch über den `jackson-module-kotlin`.

---

### Auf die Datenbank zugreifen

#### Option 1: MongoDB Compass (GUI)

MongoDB Compass ist die offizielle Desktop-App für MongoDB. Download unter [mongodb.com/products/compass](https://www.mongodb.com/products/compass).

Verbindungsstring eingeben:
```
mongodb://admin:adminnimda@localhost:27017/?authSource=admin
```

Dann sind alle Collections visuell durchsuchbar, Dokumente können bearbeitet, gefiltert und exportiert werden.

#### Option 2: mongosh (CLI)

```bash
# In den Container einloggen
docker exec -it mindpanel-mongodb mongosh -u admin -p adminnimda --authenticationDatabase admin

# Datenbank wechseln
use mindpanel

# Alle Todos anzeigen
db.todos.find()

# Gefiltert
db.todos.find({ userId: "a3f9c1d2-..." })
db.todos.find({ completed: false, archived: false })

# Ein Dokument
db.todos.findOne({ _id: ObjectId("6650f3a2c1234567890abcde") })

# Collections auflisten
show collections

# Anzahl Dokumente
db.todos.countDocuments()
```

#### Option 3: IntelliJ Ultimate (Database Tool)

IntelliJ IDEA Ultimate hat ein eingebautes Database Tool (nicht in der Community Edition). Dort MongoDB als Datenquelle hinzufügen mit demselben Connection String wie oben.

#### Option 4: direkt über die API

Der einfachste Weg während der Entwicklung: REST-Calls über die API (z.B. mit Bruno, Postman oder curl) — dann sieht man die Daten so, wie sie die App ausgibt.
