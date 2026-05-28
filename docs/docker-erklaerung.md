# Docker — Wie funktioniert das?

## Was ist Docker?

Docker ist eine Software, die Programme in sogenannten **Containern** ausführt.
Ein Container ist wie eine kleine, abgeschlossene Box mit allem, was ein Programm braucht:
- das Programm selbst (z.B. MongoDB)
- alle Abhängigkeiten
- die Konfiguration

Der Vorteil: Du musst MongoDB, Keycloak etc. **nicht auf deinem Mac installieren**.
Die laufen isoliert in ihrer Box — und wenn du sie nicht mehr brauchst, einfach stoppen.

---

## Was ist Docker Desktop?

Docker Desktop ist die App, die auf deinem Mac die "Docker-Maschine" betreibt.
Im Hintergrund läuft eine kleine Linux-VM (da Container Linux brauchen).

Wenn Docker Desktop läuft, registriert es automatisch den `docker`-Befehl im Terminal.
Das heißt: Docker Desktop läuft → Terminal erkennt `docker` und `docker compose`.

**Kurzversion:**
> Docker Desktop = Motor. Terminal = Lenkrad. Du steuerst Docker über das Terminal.

---

## Was ist ein Image?

Ein **Image** ist eine Vorlage — sozusagen der Bauplan für einen Container.
Beispiel: `mongo:8` ist das offizielle MongoDB-Image in Version 8.

Images werden von [hub.docker.com](https://hub.docker.com) heruntergeladen,
beim ersten Start automatisch — du musst nichts manuell herunterladen.

```
Image (Vorlage)  →  Container (laufende Instanz)
mongo:8          →  mindpanel-mongodb
```

---

## Was ist docker-compose.yml?

Die `docker-compose.yml` ist eine Konfigurationsdatei, in der du beschreibst,
**welche Container** du brauchst und **wie sie konfiguriert** sind.

Unsere Datei startet zwei Container:

```yaml
services:
  mongodb:           # Name des Service
    image: mongo:8   # welches Image
    ports:
      - "27017:27017"  # PORT_AUF_DEINEM_MAC:PORT_IM_CONTAINER
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin   # Umgebungsvariablen = Konfiguration
      MONGO_INITDB_ROOT_PASSWORD: adminnimda

  keycloak:
    image: quay.io/keycloak/keycloak:26.1
    ports:
      - "8080:8080"
```

---

## Die wichtigsten Befehle

```bash
# Alle Container aus docker-compose.yml starten (im Hintergrund)
docker compose up -d

# Status der laufenden Container anzeigen
docker compose ps

# Logs eines Containers anschauen
docker compose logs mongodb

# Alle Container stoppen
docker compose down

# Container stoppen UND Daten löschen (Vorsicht!)
docker compose down -v
```

---

## Port-Mapping erklärt

```
"27017:27017"
   ^      ^
   |      Port im Container (fix, so läuft MongoDB intern)
   Port auf deinem Mac (den du im Browser/Code verwendest)
```

Deshalb steht in der `application.properties`:
```
mongodb://...@localhost:27017/mindpanel
              ^         ^
              dein Mac  Port auf deinem Mac
```

---

## Ablauf beim ersten Start

1. Du startest Docker Desktop → Motor läuft
2. Du führst `docker compose up -d` aus
3. Docker lädt `mongo:8` und `keycloak:26.1` von Docker Hub herunter
4. Docker startet beide Container im Hintergrund
5. MongoDB läuft auf `localhost:27017`
6. Keycloak läuft auf `localhost:8080`
7. Die Spring Boot API kann sich jetzt mit MongoDB verbinden
