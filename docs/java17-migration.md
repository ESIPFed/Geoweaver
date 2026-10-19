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

## Cluster / no-sudo: PyGeoWeaver `~/jdk` upgrade fails with Permission denied

This happens when PyGeoWeaver detects an old **managed** JDK under `~/jdk` (often Java 11), tries to install Temurin 17 into the same folder, and extraction fails, for example:

```text
/home/YOU/jdk.tar.gz already exists.
Extracting /home/YOU/jdk.tar.gz...
Error upgrading managed JDK: [Errno 13] Permission denied: '.../jdk-11.../thaidict.md'
```

**What usually went wrong:** a leftover `~/jdk.tar.gz` from the **old Java 11** install is still on disk. PyGeoWeaver skips re-download when that file exists, then extracts it again on top of `~/jdk/jdk-11*`. Some files in that tree are not writable by your account, so extract stops. You do **not** need sudo if you can rename files under your home directory.

### Fix (all under your account)

Run these on the cluster login/compute node where you start Geoweaver:

```bash
# 1) See what you have
ls -la ~/jdk.tar.gz ~/jdk 2>/dev/null
java -version 2>&1 | head -3
which java || true

# 2) Move the stale archive aside (forces a fresh Temurin 17 download)
mv -f ~/jdk.tar.gz ~/jdk.tar.gz.jdk11.bak 2>/dev/null || true

# 3) Move the old managed JDK tree aside (prefer rename over rm)
if [ -d "$HOME/jdk" ]; then
  mv "$HOME/jdk" "$HOME/jdk.jdk11.bak.$(date +%Y%m%d%H%M%S)"
fi

# 4) Install JDK 17 under ~/jdk again and start
pip install --user pygeoweaver --upgrade   # or your usual env
gw installjdk
# or simply:
gw start
```

Confirm:

```bash
"$HOME"/jdk/jdk-17*/bin/java -version
# Expect: openjdk version "17...."
```

Put this in `~/.bashrc` (or the shell rc your job uses) so later sessions keep JDK 17 first:

```bash
# PyGeoWeaver-managed Temurin 17 (adjust the folder name after ls ~/jdk)
export JAVA_HOME="$HOME/jdk/$(ls -1 "$HOME/jdk" | grep '^jdk-17' | head -1)"
export PATH="$JAVA_HOME/bin:$PATH"
```

Then `source ~/.bashrc` and run `gw start` again.

### If you cannot rename or delete `~/jdk`

Some sites leave files owned by another user inside your tree. Install Temurin 17 into a **new** directory you own, and put it first on `PATH` so Geoweaver does not need the broken tree:

```bash
mkdir -p "$HOME/.local/jdk"
cd /tmp
# Linux x64 Temurin 17 (Adoptium API resolves the current GA build)
curl -fsSL -o openjdk17.tar.gz \
  "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
tar -xzf openjdk17.tar.gz -C "$HOME/.local/jdk"
NEW_HOME=$(echo "$HOME"/.local/jdk/jdk-17*)
export JAVA_HOME="$NEW_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
java -version

# Keep PyGeoWeaver from using the broken managed tree:
# rename ~/jdk if you can; otherwise ensure PATH's java is 17+ *and*
# that you are not forcing JAVA_HOME to ~/jdk/jdk-11*
unset JAVA_HOME  # then set to NEW_HOME as above
export JAVA_HOME="$NEW_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
```

If PyGeoWeaver still insists on bumping `~/jdk`, rename that directory when possible (`mv ~/jdk ~/jdk.broken`). A **system** Java 11 that is *not* under `~/jdk` will make recent PyGeoWeaver fall back to Geoweaver **2.1.x** instead of installing 17—so for Geoweaver **3.0.0 / 2.2+** you want a working Java **17+** on `PATH` (managed under a clean `~/jdk`, or under `~/.local/jdk` as above).

### Do not use sudo for `gw start`

Running install/start with sudo can create root-owned files under `~/jdk` and make the next user-level upgrade fail the same way.

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
