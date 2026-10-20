#!/usr/bin/env bash
# Legt den Upload-Schlüssel für Google Play an und schreibt keystore.properties.
#
# Aufruf im Hauptordner:  scripts/make-upload-key.sh [Zielordner]
#   Zielordner: wohin die .jks-Datei kommt (Standard: ~/diktat-keys, außerhalb des Repositorys).
#
# Der Schlüssel ist der *Upload*-Schlüssel. Bei Play App Signing (Pflicht für neue Apps) signiert Google die
# ausgelieferte App selbst mit einem eigenen Schlüssel; mit diesem hier beweist du nur, dass ein Upload von dir
# kommt. Geht er verloren, lässt er sich in der Play Console über den Support zurücksetzen – die App bleibt.
#
# Sichere die .jks-Datei UND die Passwörter an einem zweiten Ort (Passwortmanager). Beides gehört nie in Git
# (.gitignore schließt *.jks und keystore.properties aus).
set -euo pipefail
cd "$(dirname "$0")/.."

dir="${1:-$HOME/diktat-keys}"
mkdir -p "$dir"
jks="$dir/diktat-upload-key.jks"
[ ! -e "$jks" ] || { echo "Es gibt schon einen Schlüssel: $jks" >&2; exit 1; }
[ ! -e keystore.properties ] || { echo "keystore.properties gibt es schon, bitte erst wegräumen." >&2; exit 1; }

pass=$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)
keytool -genkeypair -v -keystore "$jks" -alias diktat-upload -keyalg RSA -keysize 4096 -validity 10000 \
    -storepass "$pass" -keypass "$pass" -dname "CN=diktat, O=Luxx1993, C=DE" >/dev/null

# Pfad relativ zum Hauptordner, wie ihn app/build.gradle.kts erwartet.
rel=$(python3 -c 'import os,sys; print(os.path.relpath(sys.argv[1], sys.argv[2]))' "$jks" "$PWD")
cat > keystore.properties <<PROPS
storeFile=$rel
storePassword=$pass
keyAlias=diktat-upload
keyPassword=$pass
PROPS
chmod 600 keystore.properties "$jks"

echo "Upload-Schlüssel: $jks"
echo "Passwort (für beide Felder): $pass"
echo "Fingerabdruck:"
keytool -list -v -keystore "$jks" -storepass "$pass" 2>/dev/null | sed -n 's/^\s*SHA256: /  SHA-256 /p'
echo "keystore.properties ist geschrieben. Datei und Passwort jetzt zusätzlich an einem sicheren zweiten Ort ablegen."
