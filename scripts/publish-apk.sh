#!/usr/bin/env bash
# Baut die Debug-APK und legt sie auf den Zweig `apk`, damit der feste Download-Link aus der README
# immer die neueste Version liefert:
#   https://github.com/Luxx1993/unisono-diktat/raw/apk/Dictate-Diktat-arm64.apk
#
# Aufruf im Hauptordner:  scripts/publish-apk.sh
# Voraussetzung: Android-SDK, wie fürs Bauen, und Schreibrecht auf origin.
# Der Zweig `apk` behält immer nur die neueste APK (ein einziger Commit, per Force-Push ersetzt), damit das
# Repository nicht mit jeder Version um etwa 75 MB wächst. Ältere APKs sind dort bewusst nicht aufgehoben.
# Wichtig: Dieselbe Debug-Signatur wie bei der vorherigen APK verwenden (~/.android/debug.keystore dieses
# Rechners), sonst lässt sich die neue Version nicht über die alte installieren.
set -euo pipefail
cd "$(dirname "$0")/.."

./gradlew :app:assembleDebug

src=$(ls app/build/outputs/apk/debug/*arm64*.apk 2>/dev/null | head -n1 || true)
[ -n "$src" ] || src=$(ls app/build/outputs/apk/debug/*.apk | head -n1)
commit=$(git rev-parse --short HEAD)
branch=$(git rev-parse --abbrev-ref HEAD)
sha=$(sha256sum "$src" | cut -d' ' -f1)

tmp=$(mktemp -d)
trap 'git worktree remove --force "$tmp" >/dev/null 2>&1 || true' EXIT
git fetch -q origin apk
git worktree add -q --detach "$tmp" origin/apk

cp "$src" "$tmp/Dictate-Diktat-arm64.apk"
sed -i -E "s/gebaut aus dem Zweig \`[^\`]+\` \(Commit [0-9a-f]+[^)]*\)/gebaut aus dem Zweig \`$branch\` (Commit $commit)/" "$tmp/README.md"
sed -i -E "s/^- SHA-256: .*/- SHA-256: \`$sha\`/" "$tmp/README.md"

git -C "$tmp" checkout -q --orphan apk-neu
git -C "$tmp" add Dictate-Diktat-arm64.apk README.md
git -C "$tmp" -c user.name="${GIT_AUTHOR_NAME:-$(git config user.name)}" -c user.email="${GIT_AUTHOR_EMAIL:-$(git config user.email)}" \
  commit -q -m "diktat als arm64-Debug-APK (Commit $commit)"
git -C "$tmp" push -q --force origin apk-neu:refs/heads/apk
echo "Fertig: https://github.com/Luxx1993/unisono-diktat/raw/apk/Dictate-Diktat-arm64.apk (SHA-256 $sha)"
