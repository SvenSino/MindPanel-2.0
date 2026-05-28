# MindPanel - Erweiterte Konzepte und Architektur

Diese Dokumentation erklärt alle wichtigen Konzepte der MindPanel-Anwendung, einschließlich JavaScript-Grundlagen, Vue.js-Architektur, Routing, Authentifizierung und API-Integration.

## Inhaltsverzeichnis

1. [JavaScript Grundlagen](#javascript-grundlagen)
2. [Views vs Components](#views-vs-components)
3. [Vue Router - Navigation](#vue-router)
4. [Authentifizierung](#authentifizierung)
5. [Services - Weather API](#services)
6. [State Management - Pinia Stores](#state-management)
7. [Anwendungs-Initialisierung](#anwendungs-initialisierung)
8. [Layouts und Komponenten-Hierarchie](#layouts)
9. [Drag & Drop System](#drag-and-drop)
10. [LocalStorage Persistierung](#localstorage)

---

## JavaScript Grundlagen

### Promises (Versprechen)

Ein Promise ist ein JavaScript-Objekt, das eine asynchrone Operation repräsentiert. Es ist wie ein "Versprechen", dass ein Wert in der Zukunft verfügbar sein wird.

**Zustände eines Promise:**
- **Pending** (wartend): Die Operation läuft noch
- **Fulfilled** (erfüllt): Die Operation war erfolgreich
- **Rejected** (abgelehnt): Die Operation ist fehlgeschlagen

**Grundlegendes Beispiel:**

```javascript
// Promise erstellen
const myPromise = new Promise((resolve, reject) => {
  // Simuliere asynchrone Operation (z.B. API-Call)
  setTimeout(() => {
    const success = true

    if (success) {
      resolve('Daten erfolgreich geladen!')  // Erfolg
    } else {
      reject('Fehler beim Laden!')  // Fehler
    }
  }, 2000)  // 2 Sekunden warten
})

// Promise verwenden
myPromise
  .then(result => {
    console.log(result)  // "Daten erfolgreich geladen!"
  })
  .catch(error => {
    console.error(error)  // Falls Fehler
  })
```

**Mehrere Promises verketten:**

```javascript
fetch('https://api.example.com/user')
  .then(response => response.json())  // Wandelt Response in JSON um
  .then(data => {
    console.log(data.name)
    return fetch(`https://api.example.com/user/${data.id}/posts`)
  })
  .then(response => response.json())
  .then(posts => {
    console.log(posts)
  })
  .catch(error => {
    console.error('Fehler:', error)
  })
```

### Async/Await

`async/await` ist eine moderne Syntax, um mit Promises zu arbeiten. Es macht asynchronen Code lesbarer und sieht aus wie synchroner Code.

**Grundprinzip:**

```javascript
// ❌ Mit Promises (verschachtelt)
function loadUser() {
  fetch('/api/user')
    .then(response => response.json())
    .then(user => {
      console.log(user)
      return fetch(`/api/posts/${user.id}`)
    })
    .then(response => response.json())
    .then(posts => {
      console.log(posts)
    })
    .catch(error => {
      console.error(error)
    })
}

// ✅ Mit async/await (linear und lesbar)
async function loadUser() {
  try {
    const response = await fetch('/api/user')
    const user = await response.json()
    console.log(user)

    const postsResponse = await fetch(`/api/posts/${user.id}`)
    const posts = await postsResponse.json()
    console.log(posts)
  } catch (error) {
    console.error(error)
  }
}
```

**Wichtige Regeln:**

1. **`await` nur in `async` Funktionen:**
```javascript
// ❌ Fehler - await außerhalb async
function getData() {
  const data = await fetch('/api/data')  // Fehler!
}

// ✅ Korrekt
async function getData() {
  const data = await fetch('/api/data')  // OK
}
```

2. **`async` Funktionen geben immer ein Promise zurück:**
```javascript
async function getNumber() {
  return 42
}

// Ist das gleiche wie:
function getNumber() {
  return Promise.resolve(42)
}

// Nutzung:
const num = await getNumber()  // 42
// oder
getNumber().then(num => console.log(num))  // 42
```

3. **Fehlerbehandlung mit try/catch:**
```javascript
async function fetchData() {
  try {
    const response = await fetch('/api/data')
    if (!response.ok) {
      throw new Error('API Fehler')
    }
    const data = await response.json()
    return data
  } catch (error) {
    console.error('Fehler beim Laden:', error)
    // Fallback-Wert zurückgeben oder Fehler weiterwerfen
    return null
  }
}
```

**Im MindPanel WeatherService:**

```typescript
// src/services/weatherService.ts
export async function searchCity(query: string): Promise<GeocodingResult[]> {
  try {
    // await wartet bis axios.get fertig ist
    const response = await axios.get(GEOCODING_API, {
      params: {
        name: query,
        count: 5,
        language: 'de',
      },
    })
    // Gibt die Ergebnisse zurück
    return response.data.results || []
  } catch (error) {
    console.error('Geocoding error:', error)
    // Wirft einen neuen Fehler, der vom Aufrufer gefangen werden kann
    throw new Error('Stadt konnte nicht gefunden werden')
  }
}
```

**Verwendung im Component:**

```vue
<script setup>
import { searchCity } from '@/services/weatherService'

async function handleSearch(cityName) {
  try {
    const cities = await searchCity(cityName)
    console.log('Gefundene Städte:', cities)
  } catch (error) {
    console.error('Fehler bei Suche:', error.message)
  }
}
</script>
```

### Parallele Promises

**Problem: Sequentiell (langsam):**
```javascript
// Dauert 6 Sekunden (3 + 3)
async function loadData() {
  const user = await fetch('/api/user')  // 3 Sekunden
  const posts = await fetch('/api/posts')  // 3 Sekunden
}
```

**Lösung: Parallel (schnell):**
```javascript
// Dauert 3 Sekunden (beide gleichzeitig)
async function loadData() {
  const [userResponse, postsResponse] = await Promise.all([
    fetch('/api/user'),   // Beide starten gleichzeitig
    fetch('/api/posts')
  ])

  const user = await userResponse.json()
  const posts = await postsResponse.json()
}
```

**Promise.all erklärt:**
```javascript
// Wartet bis ALLE Promises erfüllt sind
const results = await Promise.all([
  fetch('/api/user'),
  fetch('/api/posts'),
  fetch('/api/comments')
])
// results ist ein Array: [userResponse, postsResponse, commentsResponse]

// Wenn EINES fehlschlägt, wird der gesamte Promise.all abgebrochen
```

**Promise.race (erster gewinnt):**
```javascript
// Nimmt das Ergebnis des schnellsten Promise
const fastest = await Promise.race([
  fetch('/api/server1'),
  fetch('/api/server2'),
  fetch('/api/server3')
])
// fastest ist die Antwort des schnellsten Servers
```

### Arrow Functions (Pfeilfunktionen)

```javascript
// Traditionelle Funktion
function add(a, b) {
  return a + b
}

// Arrow Function (kurz)
const add = (a, b) => a + b

// Arrow Function (mit Block)
const add = (a, b) => {
  const result = a + b
  return result
}

// Ein Parameter - Klammern optional
const double = x => x * 2

// Kein Parameter - Klammern erforderlich
const sayHello = () => console.log('Hello')
```

**Wichtiger Unterschied - `this` Kontext:**

```javascript
// Traditionelle Funktion - eigenes `this`
const obj = {
  count: 0,
  increment: function() {
    setTimeout(function() {
      this.count++  // ❌ `this` ist undefined oder window
    }, 1000)
  }
}

// Arrow Function - behält `this` vom umgebenden Scope
const obj = {
  count: 0,
  increment: function() {
    setTimeout(() => {
      this.count++  // ✅ `this` ist obj
    }, 1000)
  }
}
```

### Destructuring (Destrukturierung)

**Array Destructuring:**
```javascript
// Alte Methode
const arr = [1, 2, 3]
const first = arr[0]
const second = arr[1]

// Destructuring
const [first, second, third] = [1, 2, 3]
console.log(first)   // 1
console.log(second)  // 2

// Rest Operator
const [head, ...tail] = [1, 2, 3, 4, 5]
console.log(head)  // 1
console.log(tail)  // [2, 3, 4, 5]

// Überspringen von Werten
const [first, , third] = [1, 2, 3]
console.log(first)   // 1
console.log(third)   // 3
```

**Object Destructuring:**
```javascript
// Alte Methode
const user = { name: 'Max', age: 25, city: 'Berlin' }
const name = user.name
const age = user.age

// Destructuring
const { name, age, city } = user
console.log(name)  // 'Max'
console.log(age)   // 25

// Umbenennen
const { name: userName, age: userAge } = user
console.log(userName)  // 'Max'

// Default-Werte
const { name, country = 'Deutschland' } = user
console.log(country)  // 'Deutschland' (da nicht in user vorhanden)

// Rest Operator
const { name, ...rest } = user
console.log(rest)  // { age: 25, city: 'Berlin' }
```

**In Funktionsparametern:**
```javascript
// Ohne Destructuring
function greet(user) {
  console.log(`Hallo ${user.name}, du bist ${user.age}`)
}

// Mit Destructuring
function greet({ name, age }) {
  console.log(`Hallo ${name}, du bist ${age}`)
}

greet({ name: 'Max', age: 25 })
```

**Im MindPanel Code:**
```typescript
// src/views/DashboardView.vue
const { element } = item  // Extrahiert element aus item

// src/stores/auth.ts
const { username, password } = credentials  // Extrahiert beide Werte
```

### Spread Operator (...)

**Array Spread:**
```javascript
const arr1 = [1, 2, 3]
const arr2 = [4, 5, 6]

// Arrays zusammenführen
const combined = [...arr1, ...arr2]
// [1, 2, 3, 4, 5, 6]

// Array kopieren
const copy = [...arr1]

// Elemente hinzufügen
const withMore = [...arr1, 4, 5]
// [1, 2, 3, 4, 5]
```

**Object Spread:**
```javascript
const user = { name: 'Max', age: 25 }

// Objekt kopieren
const userCopy = { ...user }

// Eigenschaften überschreiben
const updatedUser = { ...user, age: 26 }
// { name: 'Max', age: 26 }

// Objekte zusammenführen
const address = { city: 'Berlin', country: 'Deutschland' }
const fullUser = { ...user, ...address }
// { name: 'Max', age: 25, city: 'Berlin', country: 'Deutschland' }
```

**Im MindPanel:**
```typescript
// src/stores/auth.ts
user.value = { ...mockUser.user }  // Kopiert User-Objekt

// src/stores/auth.ts
user.value = { ...user.value, ...updates }  // Merged alte Daten mit Updates
```

### Optional Chaining (?.)

Sicherer Zugriff auf verschachtelte Eigenschaften:

```javascript
// ❌ Ohne Optional Chaining
const user = { name: 'Max' }
const street = user.address.street  // ❌ Fehler! address ist undefined

// Mit Null-Check
const street = user && user.address && user.address.street

// ✅ Mit Optional Chaining
const street = user?.address?.street  // undefined (kein Fehler)
```

**Im MindPanel:**
```typescript
// src/components/layout/AppShell.vue
const first = authStore.user?.firstName?.charAt(0) || ''
// Wenn user oder firstName undefined → kein Fehler, sondern ''
```

### Nullish Coalescing (??)

```javascript
// || gibt den rechten Wert, wenn links "falsy" ist (0, '', false, null, undefined)
const value1 = 0 || 'default'  // 'default' (0 ist falsy)
const value2 = '' || 'default'  // 'default' ('' ist falsy)

// ?? gibt den rechten Wert NUR wenn links null oder undefined ist
const value3 = 0 ?? 'default'  // 0 (0 ist nicht null/undefined)
const value4 = '' ?? 'default'  // '' ('' ist nicht null/undefined)
const value5 = null ?? 'default'  // 'default'
```

**Praktisches Beispiel:**
```javascript
const config = {
  port: 0,  // 0 ist ein valider Port
  host: ''  // '' ist ein valider Host
}

// ❌ Falsch mit ||
const port = config.port || 3000  // 3000 (0 wird als falsy behandelt!)

// ✅ Richtig mit ??
const port = config.port ?? 3000  // 0 (nur null/undefined werden ersetzt)
```

---

## Views vs Components

### Was ist der Unterschied?

**Components (Komponenten):**
- Wiederverwendbare UI-Bausteine
- Können mehrfach verwendet werden
- Haben keinen direkten Bezug zu einer Route
- Beispiele: Button, Card, Input, Widget

**Views (Ansichten):**
- Seiten-Level Komponenten
- Werden vom Router geladen
- Repräsentieren eine komplette Seite
- Nutzen meist mehrere Components

### Verzeichnisstruktur

```
src/
├── components/           # Wiederverwendbare Komponenten
│   ├── MindPanelLogo.vue
│   ├── layout/
│   │   ├── AppShell.vue     # Layout-Wrapper
│   │   └── AppSidebar.vue   # Sidebar-Navigation
│   └── widgets/
│       ├── TodosWidget.vue
│       ├── NotesWidget.vue
│       ├── WeatherWidget.vue
│       ├── CalendarWidget.vue
│       └── PomodoroWidget.vue
│
└── views/                # Seiten-Level Komponenten
    ├── DashboardView.vue      # Haupt-Dashboard (Route: /)
    ├── LoginView.vue          # Login-Seite (Route: /login)
    ├── RegisterView.vue       # Registrierung (Route: /register)
    ├── SettingsView.vue       # Einstellungen (Route: /settings)
    └── ProfileSettingsView.vue # Profil (Route: /profile-settings)
```

### Component-Beispiel: WeatherWidget.vue

```vue
<!-- Kann überall verwendet werden -->
<template>
  <Card>
    <template #content>
      <div>{{ temperature }}°C</div>
    </template>
  </Card>
</template>
```

**Verwendung:**
```vue
<!-- In DashboardView.vue -->
<WeatherWidget />

<!-- Könnte auch hier verwendet werden -->
<!-- In SettingsView.vue -->
<WeatherWidget />
```

### View-Beispiel: DashboardView.vue

```vue
<template>
  <AppShell>  <!-- Layout -->
    <div class="dashboard">
      <h2>Dashboard</h2>

      <!-- Nutzt mehrere Components -->
      <TodosWidget />
      <NotesWidget />
      <WeatherWidget />
    </div>
  </AppShell>
</template>
```

**Wird vom Router geladen:**
```typescript
// router/index.ts
{
  path: '/',
  component: () => import('@/views/DashboardView.vue')
}
```

### Layout-Komponenten

**AppShell.vue** ist eine spezielle Layout-Komponente:
- Umhüllt alle Views
- Stellt Header, Sidebar und Footer bereit
- Nutzt `<slot />` für den Inhaltsbereich

```vue
<!-- AppShell.vue -->
<template>
  <div class="app-shell">
    <header>...</header>
    <aside>...</aside>

    <main>
      <slot />  <!-- Hier wird der View-Inhalt eingefügt -->
    </main>
  </div>
</template>
```

**Verwendung in Views:**
```vue
<!-- DashboardView.vue -->
<template>
  <AppShell>
    <!-- Dieser Inhalt wird im <slot /> von AppShell angezeigt -->
    <div class="dashboard">
      <h2>Dashboard</h2>
    </div>
  </AppShell>
</template>
```

**Ergebnis:**
```html
<div class="app-shell">
  <header>...</header>
  <aside>...</aside>

  <main>
    <!-- Inhalt von DashboardView -->
    <div class="dashboard">
      <h2>Dashboard</h2>
    </div>
  </main>
</div>
```

---

## Vue Router

### Was ist Vue Router?

Vue Router ist das offizielle Routing-System für Vue.js. Es ermöglicht:
- Navigation zwischen verschiedenen Seiten ohne Page Reload (SPA - Single Page Application)
- URL-basierte Navigation
- Programmgesteuerte Navigation
- Route Guards (Zugriffsschutz)
- Route-Parameter und Queries

### Router-Konfiguration

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  // History-Modus: Nutzt HTML5 History API (clean URLs ohne #)
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/login',              // URL-Pfad
      name: 'login',               // Name für Navigation
      component: () => import('@/views/LoginView.vue'),  // Lazy Loading
      meta: { requiresGuest: true },  // Metadaten für Guards
    },
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },  // Nur für eingeloggte User
    },
    // ... weitere Routes
  ],
})
```

### Lazy Loading von Routes

```typescript
// ❌ Eager Loading - alles wird sofort geladen
import DashboardView from '@/views/DashboardView.vue'
component: DashboardView

// ✅ Lazy Loading - wird erst geladen wenn benötigt
component: () => import('@/views/DashboardView.vue')
```

**Vorteile:**
- Kleinere initiale Bundle-Größe
- Schnellere Ladezeit beim App-Start
- Components werden nur bei Bedarf geladen

### Navigation

**1. Deklarative Navigation (im Template):**

```vue
<template>
  <!-- Mit Route-Name -->
  <router-link to="/dashboard">Dashboard</router-link>
  <router-link :to="{ name: 'dashboard' }">Dashboard</router-link>

  <!-- Mit Parametern -->
  <router-link :to="{ name: 'user', params: { id: 123 } }">User 123</router-link>

  <!-- Mit Query-Parametern -->
  <router-link :to="{ path: '/search', query: { q: 'vue' } }">Suche</router-link>
</template>
```

**2. Programmgesteuerte Navigation (im Code):**

```vue
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// Navigation zu einer Route
function goToDashboard() {
  router.push('/')
  // oder
  router.push({ name: 'dashboard' })
}

// Navigation mit Parametern
function goToUser(userId) {
  router.push({ name: 'user', params: { id: userId } })
}

// Zurück navigieren
function goBack() {
  router.back()
}

// Ersetzen (ohne History-Eintrag)
function replaceRoute() {
  router.replace({ name: 'dashboard' })
}
</script>
```

**Im MindPanel:**
```typescript
// src/components/layout/AppShell.vue
const router = useRouter()

// Navigation zu Profil-Einstellungen
command: () => router.push('/profile-settings')

// Navigation zu Login nach Logout
command: () => {
  authStore.logout()
  router.push('/login')
}
```

### Route Meta Fields

Meta-Felder speichern zusätzliche Informationen über eine Route:

```typescript
{
  path: '/dashboard',
  component: DashboardView,
  meta: {
    requiresAuth: true,    // Eigene Felder
    title: 'Dashboard',
    roles: ['admin', 'user']
  }
}
```

**Zugriff auf Meta-Felder:**
```typescript
const route = useRoute()
console.log(route.meta.requiresAuth)  // true
```

### Navigation Guards (Wächter)

Guards schützen Routes und kontrollieren den Zugriff.

**Global beforeEach Guard:**

```typescript
// router/index.ts
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // Prüfe ob Route Authentifizierung benötigt
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Nicht eingeloggt → Weiterleitung zu Login
    next('/login')
  }
  // Prüfe ob Route nur für Gäste ist (Login/Register)
  else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    // Bereits eingeloggt → Weiterleitung zu Dashboard
    next('/')
  }
  // Alles OK → Route erlauben
  else {
    next()
  }
})
```

**Parameter erklärt:**
- `to`: Ziel-Route (wohin will der User)
- `from`: Aktuelle Route (wo kommt der User her)
- `next()`: Callback zum Fortfahren
  - `next()` - Route erlauben
  - `next('/login')` - Zu anderer Route umleiten
  - `next(false)` - Navigation abbrechen

**Beispiel-Flow:**

```
User ist nicht eingeloggt und versucht / zu öffnen:
→ to.path = '/'
→ to.meta.requiresAuth = true
→ authStore.isAuthenticated = false
→ next('/login')
→ User wird zu /login umgeleitet

User ist eingeloggt und versucht /login zu öffnen:
→ to.path = '/login'
→ to.meta.requiresGuest = true
→ authStore.isAuthenticated = true
→ next('/')
→ User wird zu / umgeleitet
```

### Route-Parameter und Query

**Route mit Parametern:**
```typescript
{
  path: '/user/:id',  // :id ist ein Parameter
  component: UserView
}
```

**Zugriff auf Parameter:**
```vue
<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const userId = route.params.id  // URL: /user/123 → userId = '123'
</script>
```

**Query-Parameter:**
```
URL: /search?q=vue&page=2
```

```vue
<script setup>
const route = useRoute()
const query = route.query.q      // 'vue'
const page = route.query.page    // '2'
</script>
```

### Router View

`<RouterView />` ist der Platzhalter, wo die aktuelle Route gerendert wird:

```vue
<!-- App.vue -->
<template>
  <div>
    <RouterView />  <!-- Hier wird die aktuelle View eingefügt -->
  </div>
</template>
```

**Beispiel:**
```
URL: /dashboard
→ RouterView rendert DashboardView.vue

URL: /login
→ RouterView rendert LoginView.vue
```

---

## Authentifizierung

### Authentifizierungs-System Übersicht

MindPanel nutzt ein **Mock-basiertes Authentifizierungs-System** mit LocalStorage-Persistierung. In einer echten Anwendung würde dies durch eine Backend-API ersetzt.

### Auth Store Struktur

```typescript
// src/stores/auth.ts
export interface User {
  id: string
  username: string
  email: string
  firstName: string
  lastName: string
  street: string
  zipCode: string
  city: string
  country: string
  avatar: string | null
  createdAt: string
}
```

### Mock Users

```typescript
const MOCK_USERS = [
  {
    username: 'admin',
    password: 'admin123',
    user: {
      id: 'user_001',
      username: 'admin',
      email: 'admin@mindpanel.de',
      firstName: 'Max',
      lastName: 'Mustermann',
      // ... weitere Daten
    }
  },
  {
    username: 'demo',
    password: 'demo123',
    user: { /* ... */ }
  }
]
```

**Verwendung:**
- Testaccounts für Entwicklung
- In Production würden diese durch echte API-Calls ersetzt

### Login-Prozess

```typescript
function login(username: string, password: string): boolean {
  // 1. Suche Mock User mit matching credentials
  const mockUser = MOCK_USERS.find(
    u => u.username === username && u.password === password
  )

  if (!mockUser) {
    return false  // Login fehlgeschlagen
  }

  // 2. Lade gespeicherte User-Daten aus LocalStorage
  const usersDataRaw = window.localStorage.getItem(USERS_DATA_KEY)
  let savedUser: User | null = null

  if (usersDataRaw) {
    const usersData: Record<string, User> = JSON.parse(usersDataRaw)
    savedUser = usersData[username] || null
  }

  // 3. Nutze gespeicherte Daten falls vorhanden, sonst Mock-Daten
  if (savedUser) {
    user.value = savedUser  // Vorherige Session
  } else {
    user.value = { ...mockUser.user }  // Erste Login
  }

  // 4. Speichere in LocalStorage
  saveUser()
  return true
}
```

**Schritt-für-Schritt:**

1. **Credentials prüfen**: Username und Passwort gegen MOCK_USERS
2. **Gespeicherte Daten laden**: Schaut ob User vorher schon mal eingeloggt war
3. **User-Daten setzen**: Nutzt gespeicherte Daten (mit Profiländerungen) oder Standard-Daten
4. **Persistieren**: Speichert aktuelle Session in LocalStorage

### Multi-User Session Management

MindPanel unterstützt **Daten-Persistierung pro User**:

```typescript
// LocalStorage Struktur
{
  "mindpanel_user": {
    // Aktuell eingeloggter User
    "id": "user_001",
    "username": "admin",
    // ...
  },

  "mindpanel_users_data": {
    // Alle User-Daten (für Session-Switching)
    "admin": { /* User-Daten von admin */ },
    "demo": { /* User-Daten von demo */ }
  }
}
```

**Vorteil:**
- Jeder User hat seine eigenen Profil-Daten
- Beim erneuten Login werden die gespeicherten Daten geladen
- Profile bleiben erhalten auch nach Logout

### saveUser() Funktion

```typescript
function saveUser() {
  if (user.value) {
    // 1. Speichere aktuellen User
    window.localStorage.setItem(USER_KEY, JSON.stringify(user.value))

    // 2. Lade globale User-Datenbank
    const usersDataRaw = window.localStorage.getItem(USERS_DATA_KEY)
    let usersData: Record<string, User> = {}

    if (usersDataRaw) {
      usersData = JSON.parse(usersDataRaw)
    }

    // 3. Update User-Daten für diesen Username
    usersData[user.value.username] = user.value

    // 4. Speichere globale Datenbank zurück
    window.localStorage.setItem(USERS_DATA_KEY, JSON.stringify(usersData))
  } else {
    // Logout - entferne User
    window.localStorage.removeItem(USER_KEY)
  }
}
```

### Registrierung

```typescript
function register(username: string, email: string, password: string): boolean {
  // 1. Prüfe ob Username bereits existiert
  if (MOCK_USERS.find(u => u.username === username)) {
    return false  // Username vergeben
  }

  // 2. Erstelle neuen User
  const newUser: User = {
    id: Math.random().toString(36).substring(7),  // Zufällige ID
    username,
    email,
    firstName: '',
    lastName: '',
    street: '',
    zipCode: '',
    city: '',
    country: '',
    avatar: null,
    createdAt: new Date().toISOString(),
  }

  // 3. Setze als aktuellen User und speichere
  user.value = newUser
  saveUser()
  return true
}
```

**In Production:**
```typescript
async function register(username, email, password) {
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    })

    if (!response.ok) {
      throw new Error('Registrierung fehlgeschlagen')
    }

    const { user, token } = await response.json()
    // Token speichern, User setzen, etc.
  } catch (error) {
    console.error(error)
  }
}
```

### isAuthenticated Computed

```typescript
const isAuthenticated = computed(() => user.value !== null)
```

**Verwendung:**
```vue
<template>
  <div v-if="authStore.isAuthenticated">
    Willkommen, {{ authStore.user.username }}!
  </div>
  <div v-else>
    Bitte einloggen
  </div>
</template>
```

**Im Router Guard:**
```typescript
if (to.meta.requiresAuth && !authStore.isAuthenticated) {
  next('/login')  // Nicht eingeloggt → Login
}
```

### Profil-Update

```typescript
function updateProfile(updates: Partial<Omit<User, 'id' | 'createdAt'>>) {
  if (user.value) {
    // Merge aktuelle Daten mit Updates
    user.value = { ...user.value, ...updates }
    saveUser()  // Persistieren
  }
}
```

**`Partial<T>`** - TypeScript Utility Type:
```typescript
interface User {
  id: string
  name: string
  email: string
}

// Partial macht alle Properties optional
type PartialUser = Partial<User>
// { id?: string, name?: string, email?: string }
```

**`Omit<T, K>`** - Entfernt Properties:
```typescript
type UserWithoutId = Omit<User, 'id'>
// { name: string, email: string }

type UserUpdate = Partial<Omit<User, 'id' | 'createdAt'>>
// { name?: string, email?: string } (id und createdAt entfernt)
```

**Verwendung:**
```vue
<script setup>
const authStore = useAuthStore()

function saveProfile() {
  authStore.updateProfile({
    firstName: 'Max',
    lastName: 'Mustermann',
    city: 'Berlin'
  })
  // id und createdAt können nicht geändert werden
}
</script>
```

### Logout

```typescript
function logout() {
  user.value = null
  saveUser()  // Entfernt User aus LocalStorage
}
```

**Mit Navigation:**
```typescript
// In Component
function handleLogout() {
  authStore.logout()
  router.push('/login')
}
```

### Initialisierung

```typescript
function initAuth() {
  loadUser()  // Lädt User aus LocalStorage beim App-Start
}

// In main.ts
const authStore = useAuthStore()
authStore.initAuth()
```

**Flow beim App-Start:**
```
1. App startet
2. main.ts läuft
3. authStore.initAuth() wird aufgerufen
4. loadUser() lädt User aus LocalStorage
5. Wenn User vorhanden → isAuthenticated = true
6. Router Guard prüft isAuthenticated
7. User wird zur richtigen Seite weitergeleitet
```

---

## Services

### Was sind Services?

Services sind Module, die externe APIs oder komplexe Business-Logik kapseln. Sie:
- Trennen API-Logik von Components
- Sind wiederverwendbar
- Zentralisieren API-Calls
- Sind einfacher zu testen

### WeatherService Architektur

```typescript
// src/services/weatherService.ts
import axios from 'axios'

const GEOCODING_API = 'https://geocoding-api.open-meteo.com/v1/search'
const FORECAST_API = 'https://api.open-meteo.com/v1/forecast'
```

### Axios HTTP Client

**Was ist Axios?**
- HTTP-Client für Browser und Node.js
- Promise-basiert
- Automatische JSON-Transformation
- Interceptors für globale Request/Response-Behandlung

**Grundlegende Nutzung:**

```typescript
// GET Request
const response = await axios.get('https://api.example.com/data')
console.log(response.data)

// GET mit Query-Parametern
const response = await axios.get('https://api.example.com/search', {
  params: {
    q: 'vue',
    limit: 10
  }
})
// Request URL: https://api.example.com/search?q=vue&limit=10

// POST Request
const response = await axios.post('https://api.example.com/users', {
  name: 'Max',
  email: 'max@example.com'
})

// Mit Headers
const response = await axios.get('https://api.example.com/data', {
  headers: {
    'Authorization': 'Bearer token123',
    'Content-Type': 'application/json'
  }
})
```

### TypeScript Interfaces

```typescript
export interface GeocodingResult {
  id: number
  name: string
  latitude: number
  longitude: number
  country: string
  admin1?: string  // Optional (?)
}

export interface WeatherData {
  temperature: number
  weatherCode: number
  windSpeed: number
  humidity: number
}
```

**Warum Interfaces?**
- Type Safety - TypeScript warnt bei falscher Verwendung
- Autocomplete in IDE
- Dokumentation der Datenstruktur
- Compile-Time Checks

### searchCity() Funktion

```typescript
export async function searchCity(query: string): Promise<GeocodingResult[]> {
  try {
    const response = await axios.get(GEOCODING_API, {
      params: {
        name: query,        // Suchbegriff
        count: 5,          // Max 5 Ergebnisse
        language: 'de',    // Deutsche Namen
        format: 'json',    // JSON Response
      },
    })

    // API gibt results Array zurück, oder leeres Array falls keine Ergebnisse
    return response.data.results || []
  } catch (error) {
    console.error('Geocoding error:', error)
    throw new Error('Stadt konnte nicht gefunden werden')
  }
}
```

**API Response Beispiel:**
```json
{
  "results": [
    {
      "id": 2950159,
      "name": "Berlin",
      "latitude": 52.52437,
      "longitude": 13.41053,
      "country": "Deutschland",
      "admin1": "Berlin"
    },
    {
      "id": 2950157,
      "name": "Berlin",
      "latitude": 42.44306,
      "longitude": -71.19089,
      "country": "USA",
      "admin1": "Massachusetts"
    }
  ]
}
```

**Verwendung in Component:**
```vue
<script setup>
import { ref } from 'vue'
import { searchCity } from '@/services/weatherService'

const cities = ref([])
const searchQuery = ref('')
const loading = ref(false)
const error = ref(null)

async function handleSearch() {
  loading.value = true
  error.value = null

  try {
    cities.value = await searchCity(searchQuery.value)
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <input v-model="searchQuery" @keyup.enter="handleSearch" />
    <button @click="handleSearch" :disabled="loading">
      {{ loading ? 'Suche...' : 'Suchen' }}
    </button>

    <div v-if="error" class="error">{{ error }}</div>

    <ul v-if="cities.length">
      <li v-for="city in cities" :key="city.id">
        {{ city.name }}, {{ city.country }}
      </li>
    </ul>
  </div>
</template>
```

### getWeather() Funktion

```typescript
export async function getWeather(
  latitude: number,
  longitude: number
): Promise<WeatherData> {
  try {
    const response = await axios.get(FORECAST_API, {
      params: {
        latitude,
        longitude,
        current_weather: true,  // Nur aktuelle Daten
        timezone: 'auto',       // Automatische Zeitzone
      },
    })

    const current = response.data.current_weather

    return {
      temperature: Math.round(current.temperature),
      weatherCode: current.weathercode,
      windSpeed: Math.round(current.windspeed),
      humidity: 0,  // Nicht im current_weather Endpoint
    }
  } catch (error) {
    console.error('Weather fetch error:', error)
    throw new Error('Wetter konnte nicht abgerufen werden')
  }
}
```

**API Response Beispiel:**
```json
{
  "current_weather": {
    "temperature": 15.3,
    "windspeed": 12.7,
    "weathercode": 2,
    "time": "2024-03-15T14:00"
  }
}
```

**Math.round() erklärt:**
```javascript
Math.round(15.3)  // 15
Math.round(15.7)  // 16
Math.round(15.5)  // 16
```

### Weather Codes Mapping

```typescript
const weatherCodeDescriptions: Record<number, string> = {
  0: 'Klar',
  1: 'Hauptsächlich klar',
  2: 'Teilweise bewölkt',
  3: 'Bewölkt',
  45: 'Nebelig',
  // ... weitere Codes
  95: 'Gewitter',
  96: 'Gewitter mit Hagel',
}

export function getWeatherDescription(code: number): string {
  return weatherCodeDescriptions[code] || 'Unbekannt'
}
```

**`Record<K, V>` TypeScript Type:**
```typescript
// Record<KeyType, ValueType> ist ein Objekt mit typed Keys und Values
const ages: Record<string, number> = {
  'Max': 25,
  'Anna': 30,
  'Tom': 28
}

ages.Max  // 25 (type: number)
ages.NewPerson = 'hello'  // ❌ TypeScript Error - muss number sein
```

**Verwendung:**
```vue
<script setup>
import { getWeatherDescription } from '@/services/weatherService'

const weatherCode = 2
const description = getWeatherDescription(weatherCode)  // 'Teilweise bewölkt'
</script>
```

### Error Handling

**Try-Catch Pattern:**
```typescript
try {
  // Code der fehlschlagen könnte
  const response = await axios.get(API_URL)
  return response.data
} catch (error) {
  // Fehlerbehandlung
  console.error('Error:', error)

  // Neuen Fehler werfen mit besserer Message
  throw new Error('Benutzerfreundliche Fehlermeldung')
}
```

**In Component mit Error State:**
```vue
<script setup>
const error = ref(null)

async function loadData() {
  try {
    const data = await fetchData()
  } catch (err) {
    error.value = err.message  // Zeige Fehler in UI
  }
}
</script>

<template>
  <div v-if="error" class="error">
    {{ error }}
  </div>
</template>
```

### Vollständiger Weather-Workflow

```vue
<script setup>
import { ref } from 'vue'
import { searchCity, getWeather, getWeatherDescription } from '@/services/weatherService'

const selectedCity = ref(null)
const weatherData = ref(null)
const loading = ref(false)

async function selectCity(city) {
  selectedCity.value = city
  loading.value = true

  try {
    // 1. Hole Wetter-Daten mit Koordinaten
    weatherData.value = await getWeather(city.latitude, city.longitude)
  } catch (error) {
    console.error('Fehler beim Laden:', error)
  } finally {
    loading.value = false
  }
}

// Computed Property für Beschreibung
const weatherDescription = computed(() => {
  if (!weatherData.value) return ''
  return getWeatherDescription(weatherData.value.weatherCode)
})
</script>

<template>
  <div v-if="weatherData">
    <h3>{{ selectedCity.name }}</h3>
    <p>{{ weatherData.temperature }}°C</p>
    <p>{{ weatherDescription }}</p>
    <p>Wind: {{ weatherData.windSpeed }} km/h</p>
  </div>
</template>
```

---

## State Management

### Was ist State Management?

State Management organisiert den **globalen Zustand** einer Anwendung. Ohne State Management:

```vue
<!-- ❌ Probleme ohne State Management -->

<!-- ComponentA.vue -->
<script setup>
const user = ref({ name: 'Max' })
</script>

<!-- ComponentB.vue -->
<script setup>
const user = ref({ name: 'Max' })  // Duplikat!
// Wenn sich user in A ändert, weiß B nichts davon
</script>
```

**Mit State Management (Pinia):**

```vue
<!-- ✅ Mit Pinia Store -->

<!-- store/user.ts -->
export const useUserStore = defineStore('user', () => {
  const user = ref({ name: 'Max' })
  return { user }
})

<!-- ComponentA.vue -->
<script setup>
const userStore = useUserStore()
userStore.user.name = 'Anna'  // Änderung
</script>

<!-- ComponentB.vue -->
<script setup>
const userStore = useUserStore()
console.log(userStore.user.name)  // 'Anna' - automatisch aktualisiert!
</script>
```

### Pinia Store Struktur

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCounterStore = defineStore('counter', () => {
  // State (wie data in Components)
  const count = ref(0)

  // Getters (wie computed)
  const doubleCount = computed(() => count.value * 2)

  // Actions (wie methods)
  function increment() {
    count.value++
  }

  // Muss alles returnen was exported werden soll
  return {
    count,
    doubleCount,
    increment
  }
})
```

**Verwendung:**
```vue
<script setup>
import { useCounterStore } from '@/stores/counter'

const counter = useCounterStore()

// State lesen
console.log(counter.count)

// Getter nutzen
console.log(counter.doubleCount)

// Action aufrufen
counter.increment()
</script>
```

### Dashboard Store im Detail

Der Dashboard Store verwaltet:
- Widget Layout und Reihenfolge
- Notes (Notizen)
- Todos (Aufgaben)
- Archivierte Notizen und Todos

**State:**
```typescript
const widgets = ref<Widget[]>(getDefaultWidgets())
const notes = ref<Note[]>([])
const todos = ref<Todo[]>([])
const archivedNotes = ref<Note[]>([])
const archivedTodos = ref<Todo[]>([])
```

**Counters für Auto-Increment IDs:**
```typescript
let todoIdCounter = 1
let noteIdCounter = 1

// Wenn neue Note erstellt wird:
function addNote(title, content) {
  notes.value.push({
    id: noteIdCounter++,  // 1, dann 2, dann 3, ...
    title,
    content,
    createdAt: new Date().toISOString()
  })
}
```

**Warum Counter?**
- Jede Note/Todo braucht eindeutige ID
- Counter garantiert keine Duplikate
- In echter App würde Backend IDs generieren

### Widget Management

**Default Widgets laden:**
```typescript
const getDefaultWidgets = (): Widget[] => [
  { id: 'todos', type: 'todos', enabled: true, title: 'Aufgaben' },
  { id: 'notes', type: 'notes', enabled: true, title: 'Notizen' },
  { id: 'calendar', type: 'calendar', enabled: true, title: 'Kalender' },
  { id: 'weather', type: 'weather', enabled: true, title: 'Wetter' },
  { id: 'pomodoro', type: 'pomodoro', enabled: true, title: 'Pomodoro' },
]
```

**Widget Toggle (Ein/Ausschalten):**
```typescript
function toggleWidget(widgetId: string) {
  const widget = widgets.value.find(w => w.id === widgetId)
  if (widget) {
    widget.enabled = !widget.enabled
    saveWidgets()  // Persistieren
  }
}
```

**Update Widget Order (Drag & Drop):**
```typescript
function updateWidgetOrder(newOrder: Widget[]) {
  widgets.value = newOrder  // Neue Reihenfolge übernehmen
  saveWidgets()             // In LocalStorage speichern
}
```

### Enabled Widgets (Computed via Watcher)

```typescript
const enabledWidgets = ref<Widget[]>([])

watch(widgets, () => {
  enabledWidgets.value = widgets.value.filter(w => w.enabled)
}, { deep: true, immediate: true })
```

**`deep: true`** - Beobachtet verschachtelte Änderungen:
```typescript
widgets.value[0].enabled = false  // Wird getriggert wegen deep
```

**`immediate: true`** - Führt Watcher sofort aus (nicht erst bei erster Änderung):
```typescript
// Ohne immediate: enabledWidgets ist initial leer
// Mit immediate: enabledWidgets wird sofort gefiltert
```

### Notes Management

**Add Note:**
```typescript
function addNote(title: string, content: string) {
  notes.value.push({
    id: noteIdCounter++,
    title,
    content,
    createdAt: new Date().toISOString(),  // ISO-Format: "2024-03-15T14:30:00.000Z"
  })
  // Auto-save durch Watcher (siehe unten)
}
```

**Update Note:**
```typescript
function updateNote(id: number, title: string, content: string) {
  const note = notes.value.find(n => n.id === id)
  if (note) {
    note.title = title
    note.content = content
    // Auto-save durch Watcher
  }
}
```

**Delete Note:**
```typescript
function deleteNote(id: number) {
  notes.value = notes.value.filter(n => n.id !== id)
  // Auto-save durch Watcher
}
```

**Archive Note:**
```typescript
function archiveNote(id: number) {
  const note = notes.value.find(n => n.id === id)
  if (note) {
    note.archived = true
    archivedNotes.value.push({ ...note })  // Kopie zu Archiv
    notes.value = notes.value.filter(n => n.id !== id)  // Aus aktiven entfernen
    saveArchivedNotes()
  }
}
```

**Spread Operator `{ ...note }`:**
```typescript
const original = { id: 1, name: 'Max' }
const copy = { ...original }

copy.name = 'Anna'
console.log(original.name)  // 'Max' (unverändert)
console.log(copy.name)      // 'Anna'
```

### Todos Management

**Add Todo:**
```typescript
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
```

**Toggle Completed:**
```typescript
function toggleTodo(id: number) {
  const todo = todos.value.find(t => t.id === id)
  if (todo) {
    todo.completed = !todo.completed
  }
}
```

**Toggle Priority (mit Limit):**
```typescript
function togglePriority(id: number): boolean {
  const todo = todos.value.find(t => t.id === id)
  if (!todo) return false

  // Zähle aktuelle Priority Todos (außer diesem)
  const priorityCount = todos.value.filter(t => t.isPriority && t.id !== id).length

  // Wenn Todo noch keine Priority hat und bereits 3 vorhanden
  if (!todo.isPriority && priorityCount >= 3) {
    return false  // Limit erreicht
  }

  // Ansonsten toggle erlaubt
  todo.isPriority = !todo.isPriority
  return true
}
```

**Update Todo (Partial Update):**
```typescript
function updateTodo(id: number, updates: Partial<Omit<Todo, 'id' | 'createdAt'>>) {
  const todo = todos.value.find(t => t.id === id)
  if (todo) {
    Object.assign(todo, updates)  // Merge updates in todo
  }
}
```

**`Object.assign()` erklärt:**
```typescript
const target = { a: 1, b: 2 }
const updates = { b: 3, c: 4 }

Object.assign(target, updates)
// target ist jetzt: { a: 1, b: 3, c: 4 }
```

**Verwendung:**
```typescript
updateTodo(123, {
  title: 'Neuer Titel',
  completed: true
})
// Nur title und completed werden geändert, rest bleibt gleich
```

### Auto-Save mit Watchers

```typescript
// Watch for changes and auto-save
watch(notes, saveNotes, { deep: true })
watch(todos, saveTodos, { deep: true })
watch(archivedNotes, saveArchivedNotes, { deep: true })
watch(archivedTodos, saveArchivedTodos, { deep: true })
```

**Wie es funktioniert:**
1. User fügt Note hinzu: `addNote('Titel', 'Inhalt')`
2. `notes.value.push(...)` ändert das Array
3. Watcher merkt Änderung (wegen `deep: true`)
4. `saveNotes()` wird automatisch aufgerufen
5. Daten werden in LocalStorage gespeichert

**Ohne Watcher müsste man manuell speichern:**
```typescript
// ❌ Mühsam
function addNote(title, content) {
  notes.value.push({ ... })
  saveNotes()  // Manuell aufrufen
}

function updateNote(id, title, content) {
  const note = notes.value.find(...)
  note.title = title
  saveNotes()  // Wieder manuell
}

// ✅ Mit Watcher - automatisch
function addNote(title, content) {
  notes.value.push({ ... })
  // saveNotes() wird automatisch aufgerufen
}
```

### Store Initialisierung

```typescript
function initializeStore() {
  loadWidgets()
  loadNotes()
  loadTodos()
  loadArchivedNotes()
  loadArchivedTodos()
}
```

**Load-Funktionen Beispiel:**
```typescript
function loadNotes() {
  if (typeof window === 'undefined') return  // SSR Check

  const raw = window.localStorage.getItem(NOTES_KEY)
  if (!raw) return  // Keine gespeicherten Daten

  try {
    const parsed = JSON.parse(raw) as Note[]

    if (Array.isArray(parsed)) {
      notes.value = parsed

      // Counter auf höchste ID + 1 setzen
      if (parsed.length > 0) {
        noteIdCounter = Math.max(...parsed.map(n => n.id)) + 1
      }
    }
  } catch (e) {
    console.warn('Konnte Notizen nicht laden:', e)
  }
}
```

**`Math.max(...array)` erklärt:**
```typescript
const ids = [5, 2, 8, 1, 9]
const maxId = Math.max(...ids)  // 9

// Spread Operator expandiert Array
Math.max(...[5, 2, 8, 1, 9])
// ist das gleiche wie
Math.max(5, 2, 8, 1, 9)

// Für Notes:
const notes = [
  { id: 1, title: 'A' },
  { id: 5, title: 'B' },
  { id: 3, title: 'C' }
]

const maxId = Math.max(...notes.map(n => n.id))  // 5
noteIdCounter = maxId + 1  // 6
```

**Warum Counter anpassen?**
```
Gespeicherte Notes: [{ id: 1 }, { id: 5 }, { id: 3 }]
Höchste ID: 5
noteIdCounter = 6

Neue Note wird erstellt:
{ id: 6, ... }  ✅ Keine Duplikate!

Ohne Anpassung:
noteIdCounter = 1 (Standardwert)
Neue Note: { id: 1, ... }  ❌ Duplikat!
```

---

## Anwendungs-Initialisierung

### main.ts - Der Einstiegspunkt

```typescript
// src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'

import App from './App.vue'
import { router } from './router'
```

**Import-Reihenfolge:**
1. Framework (Vue, Pinia, PrimeVue)
2. App-Komponente
3. Router
4. Stores
5. CSS

### Schritt-für-Schritt Initialisierung

**1. App-Instanz erstellen:**
```typescript
const app = createApp(App)
```

**2. Pinia installieren:**
```typescript
const pinia = createPinia()
app.use(pinia)
```

**Was ist `app.use()`?**
- Installiert Vue Plugins
- Plugin kann globale Funktionalität hinzufügen
- Muss VOR `app.mount()` aufgerufen werden

**3. Router installieren:**
```typescript
app.use(router)
```

**4. PrimeVue konfigurieren:**
```typescript
app.use(PrimeVue, {
  theme: {
    preset: Aura,                    // UI Theme
    options: {
      darkModeSelector: '.app-dark', // CSS-Klasse für Dark Mode
      cssLayer: {
        name: 'primevue',
        order: 'tailwind-base, primevue, tailwind-utilities',
      },
    },
  },
})
```

**Dark Mode Selector erklärt:**
```css
/* Wenn <html class="app-dark"> */
.app-dark {
  /* PrimeVue wechselt automatisch zu Dark Theme */
}
```

**5. Services registrieren:**
```typescript
app.use(ToastService)         // Benachrichtigungen
app.use(ConfirmationService)  // Bestätigungs-Dialoge
```

**6. Directives registrieren:**
```typescript
app.directive('tooltip', Tooltip)
```

**Verwendung:**
```vue
<button v-tooltip.top="'Hilfe-Text'">Hover me</button>
```

**7. Stores initialisieren:**
```typescript
const uiStore = useUiStore()
uiStore.initTheme()

const dashboardStore = useDashboardStore()
dashboardStore.initializeStore()

const authStore = useAuthStore()
authStore.initAuth()
```

**Warum vor mount()?**
- Stores müssen Daten aus LocalStorage laden
- Theme muss gesetzt sein bevor UI rendert
- Auth-Status muss bekannt sein für Router Guards

**8. App mounten:**
```typescript
app.mount('#app')
```

**Was passiert beim mount?**
1. Vue erstellt den Component Tree
2. App.vue wird gerendert
3. Router bestimmt aktuelle Route
4. Entsprechende View wird geladen
5. DOM wird aktualisiert

### App.vue - Root Component

```vue
<!-- src/App.vue -->
<template>
  <RouterView />
  <Toast />
  <ConfirmDialog />
</template>
```

**RouterView:**
- Platzhalter für aktuelle Route
- Rendert DashboardView, LoginView, etc.

**Toast & ConfirmDialog:**
- Globale Overlay-Komponenten
- Werden von Services gesteuert
- Sind immer im DOM, aber initial versteckt

### Initialisierungs-Reihenfolge

```
1. Browser lädt index.html
   ↓
2. index.html lädt main.ts
   ↓
3. main.ts läuft:
   - createApp(App)
   - Plugins installieren (Pinia, Router, PrimeVue)
   - Stores initialisieren
   ↓
4. app.mount('#app')
   ↓
5. App.vue rendert
   ↓
6. Router startet
   - beforeEach Guard prüft Auth
   - Lädt entsprechende View
   ↓
7. View rendert
   - Nutzt Stores
   - Zeigt UI
   ↓
8. App ist bereit
```

### Environment Variables

```typescript
import.meta.env.BASE_URL
```

**Vite Environment Variables:**
- `import.meta.env.MODE` - 'development' oder 'production'
- `import.meta.env.BASE_URL` - Base URL der App
- `import.meta.env.VITE_*` - Custom Variables

**.env Datei:**
```
VITE_API_URL=https://api.example.com
VITE_APP_TITLE=MindPanel
```

**Verwendung:**
```typescript
const apiUrl = import.meta.env.VITE_API_URL
console.log(apiUrl)  // "https://api.example.com"
```

---

## Layouts

### AppShell Layout-Komponente

**Struktur:**
```
┌─────────────────────────────────┐
│          Header                 │  ← Sticky, immer sichtbar
├──────────┬──────────────────────┤
│          │                      │
│ Sidebar  │   Main Content       │  ← Scrollbar
│          │   (RouterView)       │
│          │                      │
└──────────┴──────────────────────┘
```

**AppShell.vue Template:**
```vue
<template>
  <div class="app-shell">
    <!-- Header -->
    <header class="app-header">
      <div class="flex justify-content-between">
        <!-- Logo & Mobile Menu -->
        <div class="flex align-items-center gap-3">
          <Button v-if="isMobile" icon="pi pi-bars" @click="toggleSidebar" />
          <MindPanelLogo />
        </div>

        <!-- Actions -->
        <div class="flex gap-2">
          <Button :icon="darkModeIcon" @click="toggleDarkMode" />
          <Button icon="pi pi-cog" @click="goToSettings" />
          <Avatar />
        </div>
      </div>
    </header>

    <div class="app-body">
      <!-- Desktop Sidebar (permanent) -->
      <aside v-if="!isMobile" class="desktop-sidebar">
        <AppSidebar :visible="true" :is-desktop="true" />
      </aside>

      <!-- Mobile Sidebar (drawer) -->
      <AppSidebar v-if="isMobile" v-model:visible="sidebarVisible" />

      <!-- Main Content -->
      <main class="app-content">
        <slot />  <!-- View-Inhalt kommt hier rein -->
      </main>
    </div>
  </div>
</template>
```

### Responsive Design

**Mobile Detection:**
```typescript
const isMobile = ref(false)

function checkMobile() {
  isMobile.value = window.innerWidth < 1024

  if (!isMobile.value) {
    sidebarVisible.value = false  // Schließe Mobile Sidebar
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
```

**Desktop vs Mobile Sidebar:**
```vue
<!-- Desktop: Permanent Sidebar -->
<aside v-if="!isMobile" class="desktop-sidebar">
  <AppSidebar :visible="true" :is-desktop="true" />
</aside>

<!-- Mobile: Drawer (Overlay) -->
<AppSidebar v-if="isMobile" v-model:visible="sidebarVisible" :is-desktop="false" />
```

**CSS Media Queries:**
```css
/* Desktop */
.desktop-sidebar {
  width: 260px;
  position: sticky;
  top: 65px;
}

/* Mobile */
@media (max-width: 1024px) {
  .app-content {
    padding: 1rem;  /* Weniger Padding */
  }
}
```

### Event Listeners Lifecycle

**Problem ohne Cleanup:**
```typescript
// ❌ Memory Leak
onMounted(() => {
  window.addEventListener('resize', checkMobile)
  // Wenn Component unmounted wird, läuft Listener weiter!
})
```

**Lösung mit Cleanup:**
```typescript
// ✅ Korrekt
onMounted(() => {
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)  // Cleanup
})
```

### User Initials Computed

```typescript
const userInitials = computed(() => {
  const first = authStore.user?.firstName?.charAt(0) || ''
  const last = authStore.user?.lastName?.charAt(0) || ''

  if (first && last) return (first + last).toUpperCase()
  if (first) return first.toUpperCase()
  if (last) return last.toUpperCase()

  return authStore.user?.username?.substring(0, 2).toUpperCase() || 'U'
})
```

**Fallback-Logik:**
```
1. Versuche: FirstName + LastName (MM)
2. Sonst: Nur FirstName (M)
3. Sonst: Nur LastName (M)
4. Sonst: Username ersten 2 Buchstaben (ma)
5. Sonst: 'U'
```

**Optional Chaining `?.` verhindert Fehler:**
```typescript
// Ohne Optional Chaining
const first = authStore.user.firstName.charAt(0)  // ❌ Crash wenn user null

// Mit Optional Chaining
const first = authStore.user?.firstName?.charAt(0)  // ✅ undefined wenn user null
```

### PrimeVue Menu Component

```typescript
const menuItems = computed(() => [
  {
    label: authStore.user?.username || 'Benutzer',
    items: [
      {
        label: 'Profil-Einstellungen',
        icon: 'pi pi-user',
        command: () => router.push('/profile-settings'),
      },
      {
        separator: true,  // Trennlinie
      },
      {
        label: 'Abmelden',
        icon: 'pi pi-sign-out',
        command: () => {
          authStore.logout()
          router.push('/login')
        },
      },
    ],
  },
])
```

**Menu Template:**
```vue
<Menu ref="userMenu" :model="menuItems" popup />

<Avatar @click="userMenu.toggle($event)" />
```

**`command` Callback:**
- Wird ausgeführt wenn Menu-Item geklickt wird
- Kann beliebiger Code sein
- Typisch: Navigation oder Actions

---

## Drag and Drop

### vuedraggable Library

```vue
<script setup>
import draggable from 'vuedraggable'

const items = ref(['Item 1', 'Item 2', 'Item 3'])
</script>

<template>
  <draggable
    v-model="items"
    :animation="200"
    item-key="id"
  >
    <template #item="{ element }">
      <div>{{ element }}</div>
    </template>
  </draggable>
</template>
```

### MindPanel Dashboard Drag & Drop

```vue
<draggable
  v-model="enabledWidgets"
  :animation="200"
  handle=".drag-handle"
  class="widgets-grid"
  item-key="id"
>
  <template #item="{ element }">
    <div class="widget-wrapper">
      <component :is="widgetComponents[element.type]" />
    </div>
  </template>
</draggable>
```

**Props erklärt:**

**`v-model="enabledWidgets"`:**
- Two-Way Binding
- Wenn User Widgets verschiebt, wird `enabledWidgets` automatisch aktualisiert

**`animation="200"`:**
- Animations-Dauer in Millisekunden
- Smooth Transition beim Verschieben

**`handle=".drag-handle"`:**
- Nur Elemente mit Klasse `drag-handle` können gezogen werden
- Verhindert versehentliches Dragging

**`item-key="id"`:**
- Eindeutiger Identifier für jedes Item
- Vue nutzt dies für effizientes Re-Rendering

### Computed Getter/Setter für v-model

```typescript
const enabledWidgets = computed({
  get: () => dashboardStore.enabledWidgets,

  set: (value: Widget[]) => {
    dashboardStore.updateWidgetOrder(value)
  }
})
```

**Wie es funktioniert:**

```
1. User zieht Widget von Position 2 zu Position 0
   ↓
2. vuedraggable aktualisiert v-model
   ↓
3. Setter wird aufgerufen: set([Widget3, Widget1, Widget2])
   ↓
4. dashboardStore.updateWidgetOrder() speichert neue Reihenfolge
   ↓
5. LocalStorage wird aktualisiert
```

### Dynamic Components

```vue
<component :is="widgetComponents[element.type]" />
```

**`widgetComponents` Mapping:**
```typescript
const widgetComponents = {
  todos: TodosWidget,
  notes: NotesWidget,
  weather: WeatherWidget,
  calendar: CalendarWidget,
  pomodoro: PomodoroWidget,
}
```

**Wie es funktioniert:**
```typescript
element.type = 'todos'
→ widgetComponents['todos']
→ TodosWidget
→ <TodosWidget /> wird gerendert

element.type = 'weather'
→ widgetComponents['weather']
→ WeatherWidget
→ <WeatherWidget /> wird gerendert
```

**Ohne Dynamic Component:**
```vue
<!-- ❌ Müsste jeden Typ manuell behandeln -->
<TodosWidget v-if="element.type === 'todos'" />
<NotesWidget v-if="element.type === 'notes'" />
<WeatherWidget v-if="element.type === 'weather'" />
<!-- ... -->

<!-- ✅ Mit Dynamic Component -->
<component :is="widgetComponents[element.type]" />
```

### Drag Handle

```vue
<!-- Im Widget -->
<template>
  <Card>
    <template #header>
      <div class="widget-header drag-handle">  <!-- Drag Handle -->
        <h3>Widget Titel</h3>
      </div>
    </template>
  </Card>
</template>

<style>
.drag-handle {
  cursor: move;  /* Zeigt dass Element draggable ist */
}
</style>
```

**Vorteile:**
- User kann nur am Header ziehen
- Content (Buttons, Inputs) bleibt interaktiv
- Verhindert versehentliches Drag & Drop

---

## LocalStorage

### Was ist LocalStorage?

LocalStorage ist ein Browser-Feature zum Speichern von Daten:
- Speichert Daten als Key-Value Paare
- Daten bleiben nach Browser-Schließung erhalten
- Nur Strings können gespeichert werden
- Limit: ca. 5-10 MB pro Domain
- Synchrone API (blocking)

### Basis-API

```javascript
// Speichern
localStorage.setItem('name', 'Max')

// Laden
const name = localStorage.getItem('name')  // 'Max'

// Löschen
localStorage.removeItem('name')

// Alles löschen
localStorage.clear()

// Anzahl Items
const count = localStorage.length

// Key by Index
const firstKey = localStorage.key(0)
```

### JSON Serialisierung

**Problem:** LocalStorage speichert nur Strings

```javascript
// ❌ Funktioniert nicht richtig
const user = { name: 'Max', age: 25 }
localStorage.setItem('user', user)  // Speichert "[object Object]"

// ✅ JSON.stringify für Objekte
localStorage.setItem('user', JSON.stringify(user))

// Laden mit JSON.parse
const savedUser = JSON.parse(localStorage.getItem('user'))
console.log(savedUser.name)  // 'Max'
```

### MindPanel LocalStorage Pattern

**Keys definieren:**
```typescript
const WIDGET_LAYOUT_KEY = 'mindpanel_widgets'
const NOTES_KEY = 'mindpanel_notes'
const TODOS_KEY = 'mindpanel_todos'
```

**Speichern:**
```typescript
function saveWidgets() {
  if (typeof window === 'undefined') return  // SSR Check
  window.localStorage.setItem(
    WIDGET_LAYOUT_KEY,
    JSON.stringify(widgets.value)
  )
}
```

**Laden:**
```typescript
function loadWidgets() {
  if (typeof window === 'undefined') return

  const raw = window.localStorage.getItem(WIDGET_LAYOUT_KEY)
  if (!raw) return  // Keine Daten vorhanden

  try {
    const parsed = JSON.parse(raw)
    widgets.value = parsed
  } catch (e) {
    console.warn('Konnte Widgets nicht laden:', e)
    // Fallback zu Defaults
    widgets.value = getDefaultWidgets()
  }
}
```

**Warum try/catch?**
- JSON.parse kann fehlschlagen (ungültige JSON)
- User könnte LocalStorage manuell editiert haben
- Corrupted Data durch Browser-Fehler

### SSR Check

```typescript
if (typeof window === 'undefined') return
```

**Warum nötig?**
- Bei Server-Side Rendering (SSR) gibt es kein `window`
- `window.localStorage` würde zu Fehler führen
- Check verhindert Crashes bei SSR

**Alternative für SSR-Safe Code:**
```typescript
const storage = typeof window !== 'undefined' ? window.localStorage : null

if (storage) {
  storage.setItem('key', 'value')
}
```

### Auto-Save mit Watchers

```typescript
watch(widgets, saveWidgets, { deep: true })
```

**Flow:**
1. User verschiebt Widget
2. `widgets.value` Array ändert sich
3. Watcher triggert
4. `saveWidgets()` wird aufgerufen
5. Neue Reihenfolge wird in LocalStorage gespeichert

**Vorteil:**
- Keine manuellen `save()` Aufrufe nötig
- Daten werden automatisch persistiert
- User verliert keine Änderungen

### Multi-User Data Isolation

```typescript
// Globale User-Datenbank
const USERS_DATA_KEY = 'mindpanel_users_data'

// Struktur:
{
  "mindpanel_users_data": {
    "admin": {
      // Alle Daten von User "admin"
      id: "user_001",
      username: "admin",
      // ...
    },
    "demo": {
      // Alle Daten von User "demo"
      id: "user_002",
      username: "demo",
      // ...
    }
  }
}
```

**Vorteil:**
- Jeder User hat eigene Daten
- Beim Logout/Login bleiben Daten erhalten
- User können zwischen Accounts wechseln

### LocalStorage Best Practices

**1. Fehlerbehandlung:**
```typescript
try {
  localStorage.setItem('key', value)
} catch (e) {
  // QuotaExceededError wenn voll
  console.error('LocalStorage voll:', e)
}
```

**2. Namespace verwenden:**
```typescript
// ❌ Kollisionsgefahr
localStorage.setItem('user', data)

// ✅ App-Prefix
localStorage.setItem('mindpanel_user', data)
```

**3. TypeScript Type Safety:**
```typescript
function loadSettings<T>(key: string, defaultValue: T): T {
  const raw = localStorage.getItem(key)
  if (!raw) return defaultValue

  try {
    return JSON.parse(raw) as T
  } catch {
    return defaultValue
  }
}

// Verwendung
interface Settings {
  theme: string
  language: string
}

const settings = loadSettings<Settings>('settings', {
  theme: 'light',
  language: 'de'
})
```

**4. Daten-Migration:**
```typescript
function loadSettings() {
  const raw = localStorage.getItem(SETTINGS_KEY)
  if (!raw) return

  try {
    const data = JSON.parse(raw)

    // Migration: Alte Version → Neue Version
    if (!data.version) {
      data.version = 2
      data.newField = 'default'
      localStorage.setItem(SETTINGS_KEY, JSON.stringify(data))
    }

    return data
  } catch (e) {
    console.warn('Migration failed:', e)
  }
}
```

---

## Zusammenfassung - Prüfungsrelevante Konzepte

### JavaScript Fundamentals

1. **Promises & Async/Await**
   - Asynchrone Operationen
   - Error Handling mit try/catch
   - Promise.all für parallele Requests

2. **ES6+ Features**
   - Arrow Functions
   - Destructuring (Array & Object)
   - Spread Operator
   - Optional Chaining (?.)
   - Nullish Coalescing (??)

### Vue.js Architektur

1. **Components vs Views**
   - Views = Seiten (Routes)
   - Components = Wiederverwendbare UI
   - Layout Components (AppShell)

2. **Composition API**
   - ref vs computed
   - Lifecycle Hooks (onMounted, onUnmounted)
   - Watchers

### Router

1. **Navigation**
   - Deklarativ vs Programmatisch
   - Route Guards (beforeEach)
   - Meta Fields
   - Lazy Loading

### State Management (Pinia)

1. **Store Structure**
   - State (ref)
   - Getters (computed)
   - Actions (functions)

2. **Persistence**
   - LocalStorage Integration
   - Auto-Save mit Watchers

### Services & APIs

1. **HTTP Requests**
   - Axios für API Calls
   - Error Handling
   - TypeScript Interfaces

2. **WeatherService**
   - Geocoding API
   - Forecast API
   - Code-Mapping

### Authentifizierung

1. **Auth Flow**
   - Login/Register/Logout
   - Multi-User Sessions
   - Route Protection

### Advanced Features

1. **Drag & Drop**
   - vuedraggable
   - Dynamic Components
   - Computed Getter/Setter

2. **Responsive Design**
   - Mobile Detection
   - Conditional Rendering
   - Media Queries

### Best Practices

1. **Type Safety** mit TypeScript
2. **Error Handling** überall
3. **Code Organization** (Services, Stores, Components)
4. **Performance** (Lazy Loading, Computed Caching)
5. **User Experience** (Loading States, Error Messages)

---

Diese Dokumentation deckt alle wichtigen Konzepte ab, die in einer Prüfung zu MindPanel relevant sein könnten. Jedes Konzept wird mit praktischen Beispielen und im Kontext der Anwendung erklärt.
