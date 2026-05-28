# SecurityConfig

**Datei:** `src/main/kotlin/com/mindpanel/api/security/SecurityConfig.kt`

## Was macht diese Klasse?

Konfiguriert Spring Security für die MindPanel API. Schützt alle Endpoints und validiert JWT-Tokens, die von Keycloak ausgestellt werden.

## Konfiguration

### `.csrf { it.disable() }`
CSRF-Schutz deaktiviert — bei REST APIs nicht nötig, da kein session-basiertes Auth über Browser-Cookies verwendet wird.

### `.anyRequest().authenticated()`
Alle Endpoints erfordern eine gültige Authentifizierung. Öffentliche Routen (z.B. Health-Check) können hier später per `requestMatchers(...).permitAll()` freigeschaltet werden.

### `.oauth2ResourceServer { oauth2 -> oauth2.jwt { } }`
Aktiviert die JWT-Validierung. Spring prüft bei jedem Request den `Authorization: Bearer <token>`-Header und validiert das Token gegen den Keycloak Issuer.

## Keycloak-Konfiguration

In `application.properties`:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/mindpanel
```

Spring lädt automatisch die öffentlichen Schlüssel von Keycloak unter `<issuer-uri>/.well-known/openid-configuration` und verifiziert damit die JWT-Signatur.

## JWT in Controllern

Die User-ID wird aus dem JWT `sub`-Claim extrahiert:
```kotlin
@GetMapping
fun getNotes(@AuthenticationPrincipal jwt: Jwt): List<Note> =
    noteService.getNotes(jwt.subject)
```

## Keycloak Setup (einmalig)

1. Keycloak starten: `docker compose up keycloak`
2. Admin-UI öffnen: http://localhost:8180
3. Neuen Realm anlegen: `mindpanel`
4. Neuen Client anlegen: `mindpanel-api` (Client Authentication: off, Standard Flow: on)
5. User anlegen und Credentials setzen

## Änderungshistorie

- Basic Auth → Keycloak JWT (OAuth2 Resource Server)
