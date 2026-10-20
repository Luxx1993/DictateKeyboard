# Store-Eintrag und Antworten für die Play Console

Alles, was du in der Play Console eintippst oder ankreuzt, zum Kopieren. Die Antworten beruhen auf dem Stand der App am 20. Oktober 2026. Prüfe jede Angabe noch einmal gegen die Fragen, die Google dir tatsächlich stellt; die Formulare ändern sich.

## 1. Texte (Deutsch, Standardsprache)

**App-Name** (höchstens 30 Zeichen)
```
diktat – KI-Diktier-App
```
Der kurze Name „diktat“ ist vermutlich vergeben oder zu allgemein. Die Play Console sagt dir beim Anlegen, ob der Name frei ist. Alternativen: „diktat: Sprache zu Text“, „Diktat mit KI“.

**Kurzbeschreibung** (höchstens 80 Zeichen)
```
Sprechen, KI räumt auf, Text landet in jeder App. Mit deinen eigenen Keys.
```

**Vollständige Beschreibung** (höchstens 4000 Zeichen)
```
diktat ist eine Diktier-App, die nicht an einen Anbieter gebunden ist. Du sprichst, eine KI räumt den Text auf (Füllwörter weg, Satzzeichen, Stil), und er wird direkt in das Textfeld eingefügt, in dem du gerade schreibst.

So funktioniert es
• Tippe auf den schwebenden Button oder nutze die Spracheingabe deiner Tastatur und sprich einfach los.
• Die Spracherkennung wandelt deine Stimme in Text um.
• Eine KI deiner Wahl bringt den Text in Form: aufgeräumt, wörtlich, als E-Mail, als Chat-Nachricht oder als Aktenvermerk. Eigene Stile legst du selbst an.
• Der Text erscheint an der Cursorposition, ohne Umweg über die Zwischenablage.

Deine Wahl, nicht meine
• Du trägst deine eigenen Zugänge (API-Keys) ein. Es gibt kein Abo und keinen Server von diktat dazwischen.
• Spracherkennung zum Beispiel mit ElevenLabs, AssemblyAI, Gladia, Mistral, OpenAI, Groq, Deepgram, Soniox, Google Gemini, xAI, Azure oder einem eigenen Server.
• Aufräumen mit Anthropic Claude, OpenAI, Google Gemini, Mistral, Groq, Cerebras, DeepSeek, Ollama und vielen weiteren.
• Auf Wunsch europäische Anbieter oder eine EU-Region, oder Erkennung ganz ohne Internet mit Offline-Modellen auf dem Gerät.

Weitere Funktionen
• Eigene Wörter und Namen für bessere Erkennung, dazu feste Ersetzungsregeln.
• Langdiktat: lange Aufnahmen werden an Pausen geteilt und schon beim Sprechen verarbeitet.
• Umformulieren: markierten Text per Sprachbefehl ändern („kürzer und freundlicher“).
• Datei transkribieren: Audio- und Videodateien in Text verwandeln.
• Verlauf, Statistik und eine Sicherung deiner Einstellungen.
• Hell und dunkel, mit zwei App-Symbolen zur Wahl.

Datenschutz
diktat hat keinen eigenen Server, keine Werbung und keine Analyse-Dienste. Aufnahmen und Texte gehen direkt von deinem Gerät an die Anbieter, die du selbst eingerichtet hast. Deine Schlüssel bleiben auf dem Gerät.

Schwebender Button (freiwillig)
Der schwebende Button nutzt Androids Bedienungshilfen, damit er in jeder App erscheinen und den Text einfügen kann. Er liest nur das ausgewählte Textfeld und sammelt keine Bildschirminhalte. Du schaltest ihn selbst ein und kannst ihn jederzeit ausschalten. diktat ist kein Hilfsmittel für Menschen mit Behinderung.

Hinweis
diktat braucht Zugänge zu den Diensten der Anbieter. Diese können Kosten verursachen; die Abrechnung läuft direkt mit dem jeweiligen Anbieter. diktat ist freie Software (Apache 2.0) und basiert auf Dictate Keyboard von DevEmperor und FlorisBoard.
```

**Grafiken**
- App-Symbol: 512 × 512 PNG. Aus `docs/design/icon/` (die Welle um die Welt, weiß oder schwarz auf passendem Grund; Play verlangt einen vollflächigen Hintergrund).
- Funktionsgrafik: 1024 × 500.
- Screenshots: mindestens zwei, besser vier bis acht (Handy, hoch). Vorschläge: Start-Tab, Diktat mit schwebendem Button in einer Chat-App, Stile, Anbieter-Auswahl.

**Kategorie:** Produktivität. **Tags:** Diktieren, Sprache zu Text, Notizen. **Kontakt:** E-Mail (Pflicht), Webseite: https://github.com/Luxx1993/unisono-diktat, Datenschutzerklärung: öffentliche Adresse von `datenschutz.md` (siehe Anleitung).

## 2. Inhalte der App (Fragebögen)

| Frage | Antwort |
|---|---|
| App oder Spiel | App |
| Kostenlos oder kostenpflichtig | Kostenlos (kein In-App-Kauf, keine Werbung) |
| Enthält Werbung | Nein |
| Zugriff auf alle Funktionen ohne Einschränkung | Ja (kein Konto nötig; Anbieter-Konten sind Sache der Nutzer) |
| Zielgruppe | 18+ (oder 16+). Nicht an Kinder gerichtet. |
| Inhaltseinstufung (IARC) | Alle Fragen mit „Nein“ beantworten, soweit sie auf die App zutreffen; die App erzeugt Text nur im Auftrag der Nutzer. Hinweis: Eine KI erzeugt auf Wunsch Text (Frage zu generativer KI, falls gestellt: ja, der Nutzer steuert ihn). |
| Nachrichten-App, Gesundheits-App, Finanz-App, Regierungs-App | Nein |
| Nutzergenerierte Inhalte | Nein (die Stil-Bibliothek läuft über GitHub und ist kuratiert) |

## 3. Datensicherheit

Die Fragen in der Play Console nennen „erfasst“ jede Übertragung vom Gerät weg. Die Aufnahmen verlassen das Gerät Richtung Anbieter, den die Nutzer selbst gewählt haben. Ich empfehle, das offen als „erfasst“ anzugeben statt es zu verschweigen.

| Frage | Antwort |
|---|---|
| Erhebt oder teilt die App Nutzerdaten | Ja |
| Alle Daten bei der Übertragung verschlüsselt | Ja (https; nur beim selbst eingetragenen Server im lokalen Netz kann es http sein). Wenn du das genau nehmen willst: „Nein“ mit dieser Begründung. |
| Können Nutzer die Löschung verlangen | Ja, Daten liegen auf dem Gerät; über die App oder Deinstallation. |

Datentypen:

| Datentyp | Erfasst | Geteilt | Zweck | Optional |
|---|---|---|---|---|
| Audio: Sprach- oder Tonaufnahmen | Ja | Ja, an den vom Nutzer gewählten Anbieter | App-Funktionen | Ja (ohne Aufnahme keine Funktion, aber auf Wunsch offline) |
| Nachrichten: sonstige Nachrichten (diktierter Text) | Ja | Ja, an den gewählten KI-Anbieter | App-Funktionen | Ja |
| Persönliche Daten, Standort, Kontakte, Fotos, Finanzdaten, Gerätekennungen | Nein | – | – | – |

Hinweis zu „geteilt“: Google zählt die Weitergabe an Dienstleister, die Daten nur im Auftrag verarbeiten, nicht als Teilen. Weil die Nutzer hier ihre eigenen Konten bei den Anbietern nutzen, ist „geteilt“ die vorsichtigere Angabe.

## 4. Berechtigungen und besondere Erklärungen

### Bedienungshilfen (Permissions Declaration Form)
Die Play Console fragt nach dem Zweck und verlangt ein Video. Vorschlag für den Text:

> **Kernfunktion:** diktat ist eine Diktier-App. Der freiwillige schwebende Button erscheint über anderen Apps, wenn ein Textfeld ausgewählt ist, und fügt den vom Nutzer diktierten Text an der Cursorposition ein. Dafür braucht die App die Bedienungshilfen-API (Fokus auf ein bearbeitbares Feld erkennen, Text einfügen).
> **diktat ist kein Hilfsmittel für Menschen mit Behinderung** (`isAccessibilityTool` ist nicht gesetzt).
> **Daten:** Es wird nur das ausgewählte Textfeld gelesen, um den Text an der Cursorposition einzufügen. Nichts wird gesammelt, gespeichert oder weitergegeben. Nur beim Button-Stil „Welle“ liest die App kurz die Helligkeit einiger Bildpunkte neben dem Button (`canTakeScreenshot`), um Schwarz oder Weiß zu wählen; das Bild wird sofort verworfen.
> **Offenlegung und Einwilligung:** Vor dem Öffnen der Systemeinstellungen zeigt die App einen eigenen Dialog mit Zustimmen und Ablehnen (Einstellungen › Schwebender Button). Der Dienst ist standardmäßig aus.

Das **Video** (bis etwa 2 Minuten, über YouTube „nicht gelistet“) zeigt: Einstellungen › Schwebender Button öffnen → Offenlegung lesen → „Zustimmen und Einstellungen öffnen“ → Dienst in den Bedienungshilfen einschalten → in einer Chat-App ein Textfeld antippen → Button erscheint → diktieren → Text steht im Feld. Danach den Dienst wieder ausschalten.

Das größte Risiko beim Prüfen ist `canTakeScreenshot`. Wenn Google das beanstandet, bleibt als Ausweg, die Helligkeitsmessung zu entfernen und beim Stil „Welle“ eine feste Farbe zu nehmen. Das ist schnell gemacht; sag Bescheid.

### Vordergrunddienste
Die Play Console fragt je Typ nach einer Erklärung und einem Video:
- **microphone** (`DictateAccessibilityService`, Aufnahmedienst): „Aufnahme der Sprache läuft nur, solange der Nutzer sie gestartet hat; eine Benachrichtigung zeigt es an.“
- **dataSync** (`ModelDownloadService`): „Der Download von Offline-Spracherkennungs-Modellen (hunderte MB) läuft weiter, wenn der Nutzer die App verlässt; eine Fortschritts-Benachrichtigung zeigt es an.“

### Mikrofon (RECORD_AUDIO)
Wird erst beim ersten Aufnehmen angefragt. Zweck: Sprache aufnehmen, die der Nutzer diktiert.

### Klartext-Verkehr
Die App erlaubt `http://` nur, damit Nutzer einen eigenen Server im lokalen Netz (z. B. Ollama) eintragen können. Standard-Anbieter laufen alle über https, und beim Eintrag einer http-Adresse zeigt die App eine Warnung.

### Zielversion und Seitengröße
`targetSdk` ist 36. Die mitgelieferten nativen Bibliotheken (sherpa-onnx, onnxruntime) sind für Seiten von 16 KB ausgerichtet (geprüft für arm64-v8a und x86_64).

## 5. Veröffentlichung

1. **Interner Test** mit dir selbst (Bundle hochladen, Link über die Testerliste, sofort verfügbar).
2. **Geschlossener Test** mit mindestens 12 Testern über 14 Tage, wenn dein Konto ein privates Konto ist (die Zahl prüfst du in der Play Console).
3. **Produktion** beantragen, wenn Google sie freigibt.
4. Danach jede neue Version mit höherer `projectVersionCode` (in `gradle.properties`) hochladen.
