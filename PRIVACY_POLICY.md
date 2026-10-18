# Datenschutz bei diktat

**Stand:** 18. Oktober 2026

diktat ist eine Diktier-App für Android und ein privates Open-Source-Projekt
([github.com/Luxx1993/unisono-diktat](https://github.com/Luxx1993/unisono-diktat)). Diese Seite beschreibt, was
mit deinen Daten passiert. Kurz gesagt: **diktat betreibt keinen Server und erhält keine Daten von dir.**

## Kurzfassung

- Aufnahmen gehen **direkt vom Gerät zum Anbieter**, den du selbst eingetragen hast (z. B. ElevenLabs, OpenAI,
  Anthropic oder ein eigener Server). Bei Offline-Erkennung verlassen sie das Gerät gar nicht.
- API-Keys, Stile, Wörterbuch, Verlauf und Einstellungen liegen **nur auf dem Gerät**.
- **Keine Werbung, kein Tracking, keine Telemetrie, kein Absturzbericht-Dienst.**
- diktat hat **keine Tastatur** und liest nicht mit, was du tippst.

## Was die App verarbeitet

**Aufnahmen.** Beim Diktieren nimmt die App über das Mikrofon auf. Die Aufnahme liegt in einer privaten
Zwischendatei und geht zur Transkription an deinen Anbieter, oder wird auf dem Gerät erkannt. Danach wird sie
gelöscht. Ausnahmen gibt es nur, wenn du sie zum erneuten Senden aufhebst oder im Verlauf mitspeicherst.

**Text.** Der erkannte Text und, wenn ein Stil aktiv ist, das Ergebnis des Aufräumens gehen an den Anbieter, den du
dafür gewählt hast. Eigene Wörter aus dem Wörterbuch werden der Transkription als Hinweis mitgegeben.

**Verlauf und Statistik.** Auf Wunsch speichert die App deine Diktate, optional mit Aufnahme, auf dem Gerät. Du
kannst den Verlauf jederzeit leeren oder ausschalten. Die Statistik zählt nur Diktate, Wörter und Tage.

**Schlüssel und Einstellungen.** API-Keys, Stile, Wörterbuch und Einstellungen liegen im privaten Speicher der App.

**Zwischenablage.** Nimmt eine App den Text nicht an, legt diktat ihn in die Zwischenablage, damit er nicht verloren
geht. Auf Wunsch kopiert diktat jedes Diktat dorthin.

## Der schwebende Button (Bedienungshilfe)

Der Button ist aus, bis du ihn einschaltest. Er arbeitet über eine Android-Bedienungshilfe und macht damit nur
Folgendes:

- Er erkennt das Textfeld, in dem du gerade bist, und ob es ein Passwortfeld ist.
- Er fügt dein Diktat in dieses Feld ein.
- Er liest den Text dieses Feldes, wenn du einen Stil auf markierten Text anwendest.
- Er merkt sich, welche App vorne ist, damit er an der richtigen Stelle erscheint.

Er liest nichts anderes auf dem Bildschirm und zeichnet kein Tippen auf. Nichts davon wird gespeichert oder
verschickt, außer dem Text, den du aufräumen lässt. Das App-Protokoll nennt beim Einfügen die App, nie den Text. Du
kannst den Dienst jederzeit in den Android-Einstellungen unter Bedienungshilfen abschalten.

## Dienste, die du selbst wählst

**KI-Anbieter.** Für Transkription und Aufräumen gelten die Bedingungen und Datenschutzregeln des Anbieters, den du
einträgst. Was der Anbieter mit Aufnahme und Text macht, entscheidet er, nicht diktat.

**Downloads.** Offline-Modelle und das Pausen-Modell für das Langdiktat werden auf Knopfdruck von GitHub
heruntergeladen, derzeit aus den Releases von Dictate Keyboard. Die Prompt-Bibliothek lädt ihre Liste aus diesem
Repository (Zweig `prompt-library`). Dabei sieht GitHub deine IP-Adresse und welche Datei angefragt wurde, sonst
nichts.

## Sicherungen

**Androids eigene Sicherung** (Google-Konto, Umzug aufs neue Gerät) enthält die Einstellungen der App einschließlich
der Keys. Ab Android 12 nimmt die App nur an einer Ende-zu-Ende-verschlüsselten Sicherung teil. Verlauf, Aufnahmen
und Zwischenablage gehören nie dazu.

**Sichern und Wiederherstellen** in der App schreibt eine Datei, die du selbst ablegst. Ob die Keys darin stehen,
wählst du dabei aus.

## Berechtigungen

| Berechtigung | Wofür |
| --- | --- |
| `RECORD_AUDIO` | Aufnahme beim Diktieren, nur während du aufnimmst |
| `INTERNET` | Verbindung zu deinem Anbieter, Downloads von Modellen |
| `MODIFY_AUDIO_SETTINGS`, `BLUETOOTH` | Aufnahme über Bluetooth-Headsets |
| `VIBRATE` | Optionale Vibration |
| `POST_NOTIFICATIONS` | Statusmeldungen, z. B. beim Modell-Download |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` | Aufnahme über dem schwebenden Button, während eine andere App vorne ist |
| `FOREGROUND_SERVICE_DATA_SYNC` | Modell-Download weiterlaufen lassen, wenn du die App verlässt |
| Bedienungshilfe | Der optionale schwebende Button, siehe oben |

## Nachprüfbar

Der gesamte Quellcode ist offen: [github.com/Luxx1993/unisono-diktat](https://github.com/Luxx1993/unisono-diktat).
Fragen und Hinweise bitte über die Issues dort.
