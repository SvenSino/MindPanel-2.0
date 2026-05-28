# TestController

**Datei:** `src/main/kotlin/TestController.kt`

## Was macht diese Klasse?

Temporärer Controller zum Testen ob die API erreichbar ist und die Security-Konfiguration korrekt funktioniert.

## Endpunkt

`GET /api` → gibt `"MindPanel API is running"` zurück

## Wichtig

`@GetMapping` (oder `@PostMapping` etc.) ist zwingend nötig, damit Spring die Methode als HTTP-Endpunkt registriert. Eine Methode ohne diese Annotation wird vom Framework ignoriert.

## Nächste Schritte

Dieser Controller wird entfernt, sobald echte Controller (z.B. `AuthController`, `DashboardController`) vorhanden sind.
