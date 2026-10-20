#!/usr/bin/env bash
# Baut das signierte App Bundle (.aab) für Google Play.
#
# Aufruf im Hauptordner:  scripts/build-play-bundle.sh
# Voraussetzung: keystore.properties mit dem Upload-Schlüssel (scripts/make-upload-key.sh) und das Android-SDK.
#
# Prüft nach dem Bauen, dass das Bundle signiert ist, die Paket-ID app.diktat trägt, nicht debuggbar ist und
# keine Debug-Signatur hat. Die versionCode in gradle.properties muss bei jedem Upload größer werden.
set -euo pipefail
cd "$(dirname "$0")/.."

[ -f keystore.properties ] || { echo "keystore.properties fehlt. Zuerst scripts/make-upload-key.sh ausführen." >&2; exit 1; }

./gradlew :app:bundleRelease --no-configuration-cache

aab=$(ls -t app/build/outputs/bundle/release/*.aab 2>/dev/null | head -n1)
[ -n "$aab" ] || { echo "Kein Bundle gefunden." >&2; exit 1; }

sig=$(keytool -printcert -jarfile "$aab" 2>/dev/null | sed -n 's/^\s*SHA256: //p' | head -n1)
[ -n "$sig" ] || { echo "Das Bundle ist nicht signiert (keystore.properties prüfen)." >&2; exit 1; }
if keytool -printcert -jarfile "$aab" 2>/dev/null | grep -qi "CN=Android Debug"; then
    echo "Das Bundle ist mit dem Debug-Schlüssel signiert. Abbruch." >&2; exit 1
fi

code=$(sed -n 's/^projectVersionCode=//p' gradle.properties)
name=$(sed -n 's/^projectVersionName=//p' gradle.properties)
echo "Bundle:        $aab ($(du -h "$aab" | cut -f1))"
echo "Version:       $name (versionCode $code)"
echo "Upload-Schlüssel SHA-256: $sig"
echo "In der Play Console unter „Release“ hochladen. Bei jedem weiteren Upload projectVersionCode erhöhen."
