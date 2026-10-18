# CLAUDE.md – diktat (Diktat-Edition von Dictate Keyboard)

Kontext für Claude Code. Projektinhaber: Lukas. Kommunikation auf Deutsch, Lukas mit Namen ansprechen.

## Was das ist

Lukas' eigene, anbieterunabhängige Diktier-App für Android, ein Fork von **Dictate Keyboard**
(DevEmperor, Apache 2.0, basiert auf FlorisBoard). Arbeitsbranch: **`diktat-edition`**.

Kernablauf: sprechen → KI räumt den Text auf → Text wird eingefügt. Lukas nutzt die App über den
**schwebenden Button („Wolke“)** neben SwiftKey, mit eigenen API-Keys: aktuell **ElevenLabs Scribe**
(Transkription) und **Claude Haiku 5.5** (Aufräumen). Gerät: Samsung Galaxy S24 (arm64).

## Stand der Diktat-Edition

- Schalter `dictationOnly` (Standard `true`, Gradle-Property): Tastatur (IME), Rechtschreibprüfung, Wear OS
  und Offline-Übersetzung sind im Manifest abgeschaltet (`manifestPlaceholders["fullKeyboard"]`). Spracheingabe-Methode,
  Erkennungsdienst und schwebender Button bleiben. Mit `-PdictationOnly=false` entsteht wieder die vollständige App.
- Tastatur-Routen in den Einstellungen werden über `app/src/main/kotlin/dev/patrickgold/florisboard/app/DictationOnly.kt` ausgeblendet.
- **Lukas' Vorgabe: Tastatur und alles Zugehörige soll so weit wie möglich raus.** Bisher nur ausgeblendet; das Entfernen
  ist geplant (`docs/FUNKTIONEN.md` Abschnitt 13). Updates vom Original lassen sich danach kaum noch übernehmen, das ist gewollt.
  Schrittweise vorgehen, nach jedem Schritt bauen.
  Ausnahme: Hängt ein gewünschter Teil im Code an einer Tastatur-Funktion und liefe sonst nicht, bleibt nur dieser Teil drin;
  im Zweifel entfernen, bauen, prüfen und Behaltenes mit Grund in FUNKTIONEN.md Abschnitt 13 notieren.

## Bauen

```bash
./gradlew :app:assembleDebug          # Debug-APK, Paket net.devemperor.dictate.debug (läuft neben der Play-Version)
```
APK unter `app/build/outputs/apk/debug/`. Debug-Builds sind mit einem Debug-Schlüssel signiert.

## Wichtige Orte im Code

- `app/src/main/kotlin/dev/patrickgold/florisboard/dictate/` – Diktat-Logik (Anbieter, Aufnahme, Overlay)
- `dictate/overlay/` – schwebender Button (Accessibility-Service, Bubble-Controller)
- `dictate/ui/AudioReactiveCloudOrbView.kt` – AGSL-Shader der Wolke
- `app/settings/` – Compose-Einstellungen (`HomeScreen.kt`, `dictate/DictateProvidersScreen.kt` …)
- `app/src/main/AndroidManifest.xml` – Launcher-Alias `dev.patrickgold.florisboard.SettingsLauncherAlias`, Splash-Theme `FlorisAppTheme.Splash`
- `app/src/main/res/mipmap-anydpi-v26/app_icon.xml` – adaptives Icon (noch das alte Dictate-Icon)

## Design

Was die App können soll und heute kann: **`docs/FUNKTIONEN.md`** (Abschnitt 0 = Soll laut Screens).
Alle Design-Entscheidungen stehen in **`docs/design/diktat-design.md`**. Das ist die maßgebliche Quelle. Kurz:

- **Oberfläche „Entwurf 3“**: warmer Unisono-Look (Papier `#F4F1EC`, Tinte `#1F1C1A`, Manrope und IBM Plex Mono),
  4 Tabs (Start, Wörterbuch, Stile, Einstellungen), hell und dunkel. HTML-Referenzen in `docs/design/entwurf-3/`.
- **Wolke**: körnige, farbige Glaskugel mit ruhiger Rauch-Animation, 5 Paletten, Standard Lagune.
- **App-Icon „Welle um die Welt“**: Ring plus durchgehende Welle, nur Weiß oder Schwarz, transparenter Hintergrund,
  Umschalter weiß/schwarz; Animation A (Drehen) beim Öffnen, Animation D (Drehen plus gegenläufige Amplitude) in der App.
  Generator und fertige Dateien: `docs/design/icon/` (`python3 diktat_icon.py`).

## Nächste Aufgaben

0. Tastatur und Zubehör entfernen (`docs/FUNKTIONEN.md` Abschnitt 13).
1. App-Icon einbauen (adaptives Icon, Themen-Icon, Umschalter weiß/schwarz), dann Splash-Animation A, dann In-App-Animation D.
2. Entwurf 3 als neue Oberfläche umsetzen.
3. Wolken-Shader an den Prototyp angleichen.
4. Später: Anbieter Gladia, Preset Cerebras, AssemblyAI Sync API.

Details und Reihenfolge: Abschnitt 5 und 7 in `docs/design/diktat-design.md`.
