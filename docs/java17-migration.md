# Java 17 / Spring Boot 3 — What You Should Do

Geoweaver **2.2+** runs on **Spring Boot 3** and requires **Java 17 or newer**.  
Geoweaver **2.1.x** remains the last line that supports **Java 11**.

| Line | Runtime | Stack | Use when |
|------|---------|-------|----------|
| **2.2+ (current)** | Java **17+** | Spring Boot **3.3** | New installs, and upgrades when you can use JDK 17 |
| **2.1.x (legacy)** | Java **11+** | Spring Boot **2.x** | You cannot upgrade the system JDK yet |

---

## New users (first-time install)

1. **Install Java 17+** (Temurin / OpenJDK recommended).
   ```bash
   java -version
   # Expect: 17, 21, or higher
   ```
2. Prefer **PyGeoweaver** (it can help install a JDK when needed):
   ```bash
   pip install pygeoweaver --upgrade
   gw start
   ```
3. Or download the latest JAR and run:
   ```bash
   java -jar geoweaver.jar
   ```
4. Open **http://localhost:8070/Geoweaver/**

Details: [Installation](install.md)

---

## Existing users (already running Geoweaver 2.1.x)

### Option A — Upgrade to 2.2+ (recommended when you can)

1. **Upgrade the JDK to 17+** on every machine that runs the Geoweaver server (not required on remote SSH hosts that only execute jobs).
2. **Back up** your data before upgrading:
   - Default H2 DB folder: `~/h2/`
   - Workspace: `~/gw-workspace/`
   - Logs: `~/geoweaver/logs/` (if present)
3. Upgrade the app:
   - **Python:** `pip install pygeoweaver --upgrade` then `gw start --force`
   - **JAR:** download [latest geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar) and restart with Java 17+
4. If the H2 engine changed across versions and the DB will not start, follow the migration steps in [Upgrading](upgrade.md).
5. After start, confirm the UI loads and re-test a simple localhost process.

### Option B — Stay on 2.1.x (cannot use Java 17 yet)

1. **Do not** run a 2.2+ JAR with Java 11 — the process will exit with an unsupported-Java warning.
2. Keep using **Geoweaver 2.1.x** from [Releases](https://github.com/ESIPFed/Geoweaver/releases).
3. Example legacy JAR: [v2.1.7 geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/v2.1.7/geoweaver.jar)
   ```bash
   java -jar geoweaver.jar   # with Java 11
   ```
4. **PyGeoweaver:** pin an older package / jar workflow that still targets 2.1.x, or upgrade the JDK before using the newest `gw start` that expects Java 17.

---

## Developers / contributors

- Build and CI use **JDK 17**.
- From source:
  ```bash
  mvn clean install
  java -jar target/geoweaver.jar
  ```
- Cypress and unit tests in this branch assume Java 17 and Spring Boot 3 APIs.

---

## Quick decision guide

```text
Can you install Java 17+ on the Geoweaver server machine?
  ├─ YES → Use Geoweaver 2.2+ (latest). Upgrade pygeoweaver / jar; back up ~/h2 first.
  └─ NO  → Stay on Geoweaver 2.1.x + Java 11. Do not download “latest” until you can upgrade JDK.
```

Questions or upgrade issues: [GitHub Issues](https://github.com/ESIPFed/Geoweaver/issues).
