# How to Create a New Release in Geoweaver

Geoweaver keeps a **stable jar URL** for installers and PyGeoweaver:

```text
https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
```

That URL works because the published release is tagged **`latest`** (a git tag name), not merely marked with GitHub’s “Latest” badge.

## Recommended: one-command publish (after merge to `main`)

From a clean checkout with [`gh`](https://cli.github.com/) authenticated:

```bash
# After merging your release PR into main — bump version, push, build, promote to latest
./scripts/publish-latest-release.sh 2.2.0 --bump
```

Or if `pom.xml` / `gw.js` are already set to the release version on `main`:

```bash
git checkout main && git pull
./scripts/publish-latest-release.sh
```

What the script does:

1. Archives the previous `latest` release under a versioned tag (e.g. `v2.1.7-pre`) so old jars remain downloadable.
2. Creates a temporary build tag (e.g. `v2.2.0-pre`) on `main` → triggers `release_workflow.yml`.
3. Waits until Actions attaches `geoweaver.jar` (and other installers).
4. Retags that release to **`latest`** so the stable URL above always points at the new jar.
5. Verifies HTTP access to the stable URL.

Dry run:

```bash
./scripts/publish-latest-release.sh 2.2.0 --dry-run
```

## Manual process (same outcome)

1. **Update version** in `pom.xml` and `src/main/resources/static/js/gw.js` (e.g. `2.2.0`). Commit and push to `main`.

2. **Archive the current `latest` release**  
   Edit the release currently tagged `latest` and change its tag to a historical name such as `v2.1.7-pre`.  
   Or delete the remote tag only after re-pointing the release:
   ```bash
   git push --delete origin latest   # only after the old release has another tag
   ```

3. **Publish a temporary release** tagged e.g. `v2.2.0-pre` targeting `main`.  
   Wait for **Build and Publish Geoweaver App** to finish and attach `geoweaver.jar`.

4. **Promote to stable URL**  
   Edit that release and set the tag to **`latest`** (create tag `latest` if prompted).  
   Title can remain `2.2.0`.

5. Confirm:
   ```bash
   curl -sI -L https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar | head
   ```

## Notes

- Geoweaver **2.2+** requires **Java 17+**. Keep **2.1.x** jars under versioned tags for Java 11 users.
- The GitHub UI badge “Latest” is not the same as the git tag named `latest`; the download URL requires the **tag** `latest`.
- Docker tags `geoweaver/geoweaver:latest` and `geoweaver/geoweaver:<version>` are pushed by the same release workflow.
