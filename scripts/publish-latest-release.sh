#!/usr/bin/env bash
#
# publish-latest-release.sh
#
# Publish a Geoweaver release so this URL always serves the newest jar:
#   https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
#
# Typical use after merging to main:
#   ./scripts/publish-latest-release.sh              # version from pom.xml
#   ./scripts/publish-latest-release.sh 2.2.0        # explicit version
#   ./scripts/publish-latest-release.sh 2.2.0 --bump # also update pom.xml + gw.js and commit
#   ./scripts/publish-latest-release.sh --dry-run
#
# Requirements: git, gh (authenticated), python3
# Permissions: repo scope that can create releases, delete/create tags, and push.
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

STABLE_JAR_URL="https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar"
WORKFLOW_FILE="release_workflow.yml"
DRY_RUN=0
DO_BUMP=0
SKIP_WAIT=0
TARGET_BRANCH="main"
VERSION_ARG=""

usage() {
  cat <<'EOF'
Usage: ./scripts/publish-latest-release.sh [VERSION] [options]

  VERSION           e.g. 2.2.0 (default: pom.xml version with -SNAPSHOT stripped)

Options:
  --bump            Update pom.xml + gw.js to VERSION, commit, and push to main
  --branch NAME     Release target branch (default: main)
  --skip-wait       Do not wait for GitHub Actions (you must retag manually later)
  --dry-run         Print actions only
  -h, --help        Show help

Stable download URL after success:
  https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
EOF
}

log() { printf '==> %s\n' "$*" >&2; }
warn() { printf 'WARNING: %s\n' "$*" >&2; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

run() {
  if [[ "$DRY_RUN" -eq 1 ]]; then
    printf '[dry-run] %s\n' "$*" >&2
  else
    "$@"
  fi
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bump) DO_BUMP=1; shift ;;
    --branch) TARGET_BRANCH="${2:?}"; shift 2 ;;
    --skip-wait) SKIP_WAIT=1; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    -h|--help) usage; exit 0 ;;
    -*)
      die "Unknown option: $1"
      ;;
    *)
      [[ -z "$VERSION_ARG" ]] || die "Unexpected extra argument: $1"
      VERSION_ARG="$1"
      shift
      ;;
  esac
done

command -v gh >/dev/null || die "gh CLI is required (https://cli.github.com/)"
command -v git >/dev/null || die "git is required"
command -v python3 >/dev/null || die "python3 is required"
if [[ "$DRY_RUN" -eq 0 ]]; then
  gh auth status >/dev/null 2>&1 || die "Run: gh auth login"
fi

REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
if [[ -z "$REPO" ]]; then
  REPO="$(git remote get-url origin 2>/dev/null | sed -E 's#.*github.com[:/](.+)(\.git)?#\1#' | sed 's#\.git$##')"
fi
[[ -n "$REPO" ]] || die "Not inside a GitHub repo / gh cannot resolve remote"

read_pom_version() {
  python3 - <<'PY'
import re, pathlib
text = pathlib.Path("pom.xml").read_text()
# first non-parent <version> under project
m = re.search(r"<artifactId>geoweaver</artifactId>\s*<version>([^<]+)</version>", text)
if not m:
    # fallback: project version after parent block
    parts = re.findall(r"<version>([^<]+)</version>", text)
    if len(parts) < 2:
        raise SystemExit("Could not parse version from pom.xml")
    print(parts[1])
else:
    print(m.group(1).strip())
PY
}

normalize_version() {
  local v="$1"
  v="${v#v}"
  v="${v%-SNAPSHOT}"
  v="${v%-snapshot}"
  [[ "$v" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][A-Za-z0-9]+)?$ ]] || die "Invalid version: $1"
  printf '%s' "$v"
}

bump_version_files() {
  local ver="$1"
  python3 - "$ver" <<'PY'
import pathlib, re, sys
ver = sys.argv[1]
pom = pathlib.Path("pom.xml")
text = pom.read_text()
# Replace geoweaver module version (the one after artifactId geoweaver)
new, n = re.subn(
    r"(<artifactId>geoweaver</artifactId>\s*<version>)[^<]+(</version>)",
    rf"\g<1>{ver}\2",
    text,
    count=1,
)
if n != 1:
    # fallback: second <version> in file (after parent)
    versions = list(re.finditer(r"<version>[^<]+</version>", text))
    if len(versions) < 2:
        raise SystemExit("Failed to update pom.xml version")
    start, end = versions[1].span()
    text = text[:start] + f"<version>{ver}</version>" + text[end:]
else:
    text = new
pom.write_text(text)

js = pathlib.Path("src/main/resources/static/js/gw.js")
js_text = js.read_text()
js_new, jn = re.subn(
    r'(version:\s*")[^"]+(")',
    rf"\g<1>{ver}\2",
    js_text,
    count=1,
)
if jn != 1:
    raise SystemExit("Failed to update gw.js version")
js.write_text(js_new)
print(f"Updated pom.xml and gw.js to {ver}")
PY
}

archive_current_latest() {
  local has_latest_release=0
  if gh release view latest >/dev/null 2>&1; then
    has_latest_release=1
  fi

  if [[ "$has_latest_release" -eq 0 ]]; then
    if git ls-remote --tags origin "refs/tags/latest" | grep -q .; then
      warn "Git tag 'latest' exists but no release is attached. Deleting orphan tag."
      run git push --delete origin latest
    else
      log "No existing GitHub release tagged 'latest' — nothing to archive"
    fi
    return 0
  fi
  local old_id old_name old_tag archive_tag
  old_id="$(gh release view latest --json databaseId -q .databaseId)"
  old_name="$(gh release view latest --json name -q .name)"
  old_tag="$(gh release view latest --json tagName -q .tagName)"
  log "Current latest release: id=$old_id name='$old_name' tag='$old_tag'"

  local base
  base="$(normalize_version "${old_name:-$old_tag}")"
  archive_tag="v${base}-pre"
  if gh release view "$archive_tag" >/dev/null 2>&1 || git ls-remote --tags origin "refs/tags/${archive_tag}" | grep -q .; then
    archive_tag="v${base}-pre-$(date +%Y%m%d%H%M%S)"
  fi

  log "Archiving previous latest as tag '$archive_tag'"
  # Point the existing release at an archival tag BEFORE deleting 'latest',
  # so assets remain downloadable under a versioned URL.
  run gh api -X PATCH "repos/${REPO}/releases/${old_id}" \
    -f tag_name="$archive_tag" \
    -f name="$base" \
    -F draft=false \
    -F make_latest=false \
    >/dev/null

  # Remove leftover 'latest' git tag if it still exists
  if git ls-remote --tags origin "refs/tags/latest" | grep -q .; then
    run git push --delete origin latest
  fi
}

create_build_release() {
  local ver="$1"
  local temp_tag="v${ver}-pre"
  if gh release view "$temp_tag" >/dev/null 2>&1 || git ls-remote --tags origin "refs/tags/${temp_tag}" | grep -q .; then
    temp_tag="v${ver}-pre-$(date +%Y%m%d%H%M%S)"
  fi

  log "Creating build release tag '$temp_tag' on branch '$TARGET_BRANCH' (triggers $WORKFLOW_FILE)"
  run gh release create "$temp_tag" \
    --target "$TARGET_BRANCH" \
    --title "$ver" \
    --notes "$(cat <<EOF
Geoweaver **${ver}**

Requires **Java 17+** (Spring Boot 3). Users on Java 11 should stay on Geoweaver 2.1.x.

Stable jar URL (after this release is promoted to \`latest\`):
\`${STABLE_JAR_URL}\`
EOF
)" \
    --latest=false

  # dry-run still needs a tag string for later steps
  printf '%s\n' "$temp_tag"
}

wait_for_workflow() {
  local temp_tag="$1"
  log "Waiting for workflow '$WORKFLOW_FILE' after publishing $temp_tag …"
  # Give GitHub a moment to enqueue the workflow
  if [[ "$DRY_RUN" -eq 1 ]]; then
    log "[dry-run] would wait for Actions run"
    return 0
  fi
  sleep 8

  local run_id=""
  local i
  for i in $(seq 1 30); do
    run_id="$(
      gh run list --workflow "$WORKFLOW_FILE" --limit 10 \
        --json databaseId,displayTitle,headBranch,status,event,createdAt \
        --jq "map(select(.displayTitle | test(\"${temp_tag}|${VERSION}\"; \"i\") or .headBranch == \"${temp_tag}\")) | .[0].databaseId // empty"
    )"
    if [[ -n "$run_id" ]]; then
      break
    fi
    # Fallback: most recent workflow_run from a release event
    run_id="$(
      gh run list --workflow "$WORKFLOW_FILE" --limit 1 \
        --json databaseId,event,status \
        --jq '.[0] | select(.event=="release") | .databaseId // empty'
    )"
    [[ -n "$run_id" ]] && break
    sleep 5
  done

  [[ -n "$run_id" ]] || die "Could not find Actions run for $WORKFLOW_FILE — check https://github.com/${REPO}/actions"

  log "Watching run $run_id …"
  gh run watch "$run_id" --exit-status
  log "Workflow finished successfully"
}

wait_for_jar_asset() {
  local temp_tag="$1"
  local i
  log "Waiting for geoweaver.jar on release $temp_tag …"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    return 0
  fi
  for i in $(seq 1 60); do
    if gh release view "$temp_tag" --json assets --jq '.assets[].name' | grep -qx 'geoweaver.jar'; then
      log "Found geoweaver.jar on $temp_tag"
      return 0
    fi
    sleep 10
  done
  die "geoweaver.jar was not uploaded to $temp_tag in time"
}

promote_to_latest() {
  local temp_tag="$1"
  local ver="$2"
  local release_id
  release_id="$(gh release view "$temp_tag" --json databaseId -q .databaseId)"

  # Ensure no conflicting latest tag/release
  if git ls-remote --tags origin "refs/tags/latest" | grep -q .; then
    warn "Deleting existing origin/latest tag before promotion"
    run git push --delete origin latest
  fi

  log "Promoting release $temp_tag → tag 'latest' (stable download URL)"
  run gh api -X PATCH "repos/${REPO}/releases/${release_id}" \
    -f tag_name="latest" \
    -f name="$ver" \
    -F draft=false \
    -F prerelease=false \
    -F make_latest=true \
    >/dev/null
}

verify_stable_url() {
  log "Verifying $STABLE_JAR_URL"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    return 0
  fi
  local code
  code="$(curl -sI -o /dev/null -w '%{http_code}' -L --max-redirs 5 "$STABLE_JAR_URL" || true)"
  [[ "$code" == "200" ]] || die "Stable URL check failed (HTTP $code). Inspect https://github.com/${REPO}/releases/tag/latest"
  log "OK — geoweaver.jar is reachable at the stable URL"
}

# ----- main -----

POM_VERSION="$(read_pom_version)"
if [[ -n "$VERSION_ARG" ]]; then
  VERSION="$(normalize_version "$VERSION_ARG")"
else
  VERSION="$(normalize_version "$POM_VERSION")"
fi

log "Repository: $REPO"
log "Release version: $VERSION (pom currently: $POM_VERSION)"
log "Target branch: $TARGET_BRANCH"

if [[ "$DO_BUMP" -eq 1 ]]; then
  log "Bumping version files to $VERSION"
  if [[ "$DRY_RUN" -eq 0 ]]; then
    bump_version_files "$VERSION"
    git add pom.xml src/main/resources/static/js/gw.js
    if git diff --cached --quiet; then
      log "Version files already at $VERSION"
    else
      git commit -m "Release Geoweaver ${VERSION}"
      git push origin "HEAD:${TARGET_BRANCH}"
    fi
  else
    log "[dry-run] would bump pom.xml / gw.js and push to $TARGET_BRANCH"
  fi
elif [[ "$POM_VERSION" == *SNAPSHOT* ]]; then
  warn "pom.xml is still '$POM_VERSION'."
  warn "Pass an explicit version and --bump, e.g.: ./scripts/publish-latest-release.sh 2.2.0 --bump"
fi

git fetch origin "$TARGET_BRANCH" --tags >/dev/null 2>&1 || true

archive_current_latest
TEMP_TAG="$(create_build_release "$VERSION" | tail -n1)"
log "Build release tag: $TEMP_TAG"

if [[ "$DRY_RUN" -eq 1 ]]; then
  cat <<EOF >&2

[dry-run] Planned next steps:
  1. Wait for workflow ${WORKFLOW_FILE}
  2. Confirm geoweaver.jar on ${TEMP_TAG}
  3. Retag that release to 'latest'
  4. Verify ${STABLE_JAR_URL}
EOF
  exit 0
fi

if [[ "$SKIP_WAIT" -eq 1 ]]; then
  warn "--skip-wait set. After Actions finishes and geoweaver.jar is attached, run:"
  warn "  gh api -X PATCH repos/${REPO}/releases/\$(gh release view ${TEMP_TAG} --json databaseId -q .databaseId) -f tag_name=latest -f name=${VERSION} -F make_latest=true"
  exit 0
fi

wait_for_workflow "$TEMP_TAG"
wait_for_jar_asset "$TEMP_TAG"
promote_to_latest "$TEMP_TAG" "$VERSION"
verify_stable_url

cat <<EOF

Release ${VERSION} published.

Stable jar URL (unchanged for clients / pygeoweaver):
  ${STABLE_JAR_URL}

Release page:
  https://github.com/${REPO}/releases/tag/latest
EOF
