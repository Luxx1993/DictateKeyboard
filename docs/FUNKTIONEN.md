# diktat – Funktionen

Stand: aus dem Code des Branches `diktat-edition` gelesen (Oktober 2026). Beschreibt, was die App **heute kann**,
wo es im Code liegt und was Lukas damit vorhat. Design und Optik stehen in `docs/design/diktat-design.md`.

Status-Legende: **vorhanden** (läuft heute) · **versteckt** (Code da, in der Diktat-Edition ausgeblendet) ·
**geplant** (von Lukas gewünscht, noch nicht gebaut) · **offen** (Entscheidung von Lukas steht aus).

Pfade beginnen, wenn nicht anders genannt, bei `app/src/main/kotlin/dev/patrickgold/florisboard/`.

---

## 0. Soll-Zustand (maßgeblich)

Die Screens in `docs/design/entwurf-3/` zeigen, was die App können soll. Alles, was dort nicht vorkommt, ist kein Ziel.
Die Abschnitte 1–12 beschreiben, was der Code heute kann; wo sie mehr enthalten als dieser Abschnitt, ist das Mehrangebot
des Originals, das nur dann bleibt, wenn es unten steht.

**Aus den Screens:**
- **Start:** Begrüßung, Statistik (Wörter gesamt, Diesen Monat, Tage in Folge), die zwei aktiven Anbieter (Transkription, Aufräumen), „Letzte Diktate“ mit Kopieren-Taste und „Alle“.
- **Wörterbuch:** Suche/Hinzufügen, **Eigene Wörter**, **Ersetzungen** (gesprochen → geschrieben, läuft nach dem Diktat).
- **Stile:** Aufräum-Stile, genau einer aktiv und läuft nach jedem Diktat; Standard „Aufräumen“, dazu Wörtlich, E-Mail, Chat, Aktenvermerk; „Neuer Stil“.
- **Einstellungen:** Schwebender Button (Stil Wolke, Farbe, Schalter), **Diktat** (Sprachen, KI-Anbieter, Aufnahme: Gedrückt halten und Stille kürzen), **Mehr** (Datei transkribieren, Spracheingabe = Mikrofon-Taste in SwiftKey, Sichern und Wiederherstellen).
- Dazu aus Lukas' Wünschen: Icon mit Animationen und Umschalter weiß/schwarz; Verlauf und Statistik behalten; Offline-Spracherkennung behalten.

**Nicht im Soll:** Tastatur in jeder Form, Wear OS, GIFs, Sticker, Scan, Übersetzung, Rechtschreibprüfung, Dictate Cloud (Guthaben).
**Im Code vorhanden, aber nicht in den Screens (Verbleib noch zu klären):** Echtzeit, Langdiktat, Bluetooth-Mikro, Audioquelle, Audio beschleunigen, Sofortaufnahme, Auto-Enter, Tippgeschwindigkeit, Prompt-Bibliothek, Tipp-Kürzel, Proxy, Verlauf-Details. Vorschlag: unter „Mehr“ oder „Aufnahme“ in der neuen Oberfläche bündeln, nichts davon neu bauen.

---

## 1. Kernablauf

sprechen → (Transkription durch Anbieter) → optional KI-Aufräumen / Stil → Ersetzungen (Wörterbuch) → Text einfügen.

- Zentrale Steuerung: `dictate/DictateController.kt` (ein Prozess-Singleton mit einem Zustand und einem Ausgabeziel).
- Aufnahme: `dictate/audio/RecordingController.kt` (Mikrofonquelle, Bluetooth-Mikro, Pegel, Stille-Erkennung).
- Eigene Wörter werden dem Transkriptions-Prompt angehängt (`DictatePromptDefaults.appendCustomWords`, Aufruf in `DictateController`).
- Ersetzungen (Wörterbuch) laufen **danach**, rein lokal und ohne Tokens (`dictate/data/mappings/DictateMappings.kt`).
- Status: **vorhanden**.

## 2. Wo man diktiert (Eingabewege)

| Weg | Was es ist | Code | Status |
|---|---|---|---|
| **Schwebender Button („Wolke“)** | Bubble über anderen Apps (Bedienungshilfe-Dienst), schreibt in das fokussierte Feld. Lukas' Hauptweg neben SwiftKey. | `dictate/overlay/` | vorhanden |
| **Spracheingabe-Methode** | Dictate als Spracheingabe-Methode, zusammen mit einer fremden Tastatur (SwiftKey) nutzbar. | `dictate/recognition/DictateVoiceInputMethodService.kt` | vorhanden |
| **System-Erkennungsdienst** | Dictate als systemweiter Spracherkenner (Standard-Android-`RecognitionService`). Manche Tastaturen (z. B. Gboard) nutzen ihn nicht. | `dictate/recognition/DictateRecognitionService.kt`, `RecognitionActivity.kt` | vorhanden |
| **Datei transkribieren** | Audiodatei wählen oder aus anderer App teilen, Ergebnis in eigener Ansicht. Auch im Menü des schwebenden Buttons. | `dictate/importer/`, `FileTranscriptionActivity.kt` | vorhanden |
| **Volle Tastatur (IME)** | FlorisBoard-Tastatur mit Diktat-Leiste, Prompts, GIFs, Sticker, Scan, Übersetzung | `dictate/ui/`, `dictate/gif/`, `dictate/sticker/`, `dictate/scan/`, `dictate/translate/` | **versteckt** (`dictationOnly`) |
| **Wear OS** | Diktat von der Uhr | `dictate/wear/` | **versteckt** |

## 3. Anbieter (Transkription und Aufräumen)

Zwei getrennte Aufgaben, je ein aktiver Anbieter: **Transkription** und **Aufräumen/Umformulieren** (Rewording).
Eigene Keys pro Anbieter. Liste und Standardmodelle: `lib/dictate-core/.../provider/ProviderRegistry.kt`.
Die Einstellungsseite zeigt nur eingerichtete Anbieter, weitere hinter „Anbieter hinzufügen“ (Filter: Transkription, Umformulieren, Echtzeit, EU).

| Anbieter | Transkription | Aufräumen (Chat) | Echtzeit |
|---|---|---|---|
| ElevenLabs Scribe (Standard `scribe_v2`) | ja | – | ja |
| Anthropic (Claude) (Standard im Code `claude-haiku-4-5-20251001`) | – | ja | – |
| OpenAI | ja | ja | ja |
| Groq | ja | ja | – |
| Google Gemini | ja | ja | ja |
| Mistral AI (Voxtral) | ja | ja | – |
| Deepgram (Nova-3) | ja | – | ja |
| AssemblyAI (Universal-3 Pro, asynchron) | ja | – | ja |
| Soniox | ja | – | ja |
| Azure Speech | ja | – | – |
| xAI (Grok) | ja | ja | ja |
| Scaleway, OVHcloud (EU-Server) | ja | ja | – |
| OpenRouter, Together AI, DeepInfra, DeepSeek, SiliconFlow | je nach Anbieter | ja | – |
| Ollama (lokal im Netz, ohne Key) | – | ja | – |
| **Auf dem Gerät (offline)** | ja (Whisper-Modelle) | – | ja |
| **Eigener Server** (OpenAI-kompatibel, benutzerdefiniert) | ja | ja | – |
| Dictate Cloud | ja | ja | – |

Hinweise:
- Lukas nutzt heute **ElevenLabs Scribe** + **Claude Haiku 5.5**. Das Modell wird im Konto des Anbieters eingetragen (nicht der Code-Standard).
- **Dictate Cloud** ist das Guthaben-Angebot des Originals (kostenpflichtig, Credit-Pakete). Lukas will nichts bezahlen und nutzt eigene Keys. Sie ist nicht im Soll und kommt mit der Tastatur raus (Abschnitt 13).
- Weitere Wünsche: **Gladia**, **Cerebras** (als Voreinstellung zum Aufräumen), **AssemblyAI Sync API** – **geplant**, noch nicht im Code.
- Proxy-Einstellung (HTTP/SOCKS mit Login) vorhanden: `app/settings/dictate/DictateProxyScreen.kt`.
- Offline-Modelle werden heruntergeladen und verwaltet: `dictate/provider/LocalModelManager.kt`, `ModelDownloadService.kt`.

## 4. Sprachen

- Mehrere Diktiersprachen wählbar, zwischen denen durchgeschaltet wird („Transkriptionssprachen“): `app/settings/dictate/DictateLanguagesScreen.kt`, `dictate/DictateLanguages.kt`.
- Status: **vorhanden**.

## 5. Aufnahme-Einstellungen (Gruppe „Aufnahme“)

Alle in `app/settings/dictate/DictateScreen.kt`:

- **Halten zum Aufnehmen** (Push-to-Talk) statt zweimal tippen
- **Echtzeit-Transkription** (Streaming, wenn der Anbieter es kann)
- **Langdiktat**: aus / manuell („Weiter“-Taste schneidet Abschnitte) / automatisch (Sprachpausen-Erkennung mit Silero-VAD und Smart Turn v3)
- Andere Audio-Wiedergabe pausieren; Bluetooth-Mikrofon; Audioquelle wählen; Bildschirm anlassen
- **Aufnahme-Anzeige**: statisch, Puls, Pegel, Wellenform
- **Stille Aufnahmen überspringen**; **lange Pausen herausschneiden**; **Audio beschleunigen** (Tonhöhe bleibt)
- **Sofortaufnahme** (Aufnahme startet, sobald die Eingabe öffnet)
- Status: **vorhanden**.

## 6. Ausgabe-Einstellungen (Gruppe „Ausgabe“)

- **Auto-Enter** nach dem Diktat
- **Sofort einfügen** statt Tippanimation; **Tippgeschwindigkeit**
- **In Absätze teilen** (lange Transkripte an Satzgrenzen)
- **Erneut-senden-Taste** bei Fehler; **haptisches Feedback**
- **Letztes Diktat merken** (erneut einfügbar)
- Status: **vorhanden**.

## 7. Aufräumen / Umformulieren (KI)

- Schalter „Umformulieren aktivieren“, eigener Anbieter, Key (kann den der Transkription mitnutzen) und Modell: `app/settings/dictate/DictateRewordingScreen.kt`.
- **Denkaufwand** (Reasoning effort): aus, minimal, niedrig, mittel, hoch, benutzerdefiniert – global und pro Prompt überschreibbar.
- Status: **vorhanden**.

## 8. Stile (im Code: „Prompts“)

Im Design heißen sie **Stile**, im Code **Prompts** (`dictate/data/prompts/PromptModel.kt`, UI `DictatePromptsScreen.kt`).

Ein Prompt hat:
- Name und Anweisungstext an die KI. Text in `[eckigen Klammern]` wird **unverändert eingefügt** (Textbaustein/Snippet).
- **Auswahl erforderlich**: wirkt auf den markierten Text und ersetzt ihn.
- **Automatisch anwenden**: läuft nach jedem Diktat von selbst (das ist der „Aktive Stil“ / „Nach jedem Diktat anwenden“ im Entwurf).
- **Tipp-Kürzel** (z. B. `;sig`), das einen Textbaustein beim Tippen auslöst (`dictate/snippet/SnippetTriggers.kt`).
- **Denkaufwand** pro Prompt.
- Reihenfolge per langem Drücken und Ziehen; **Export/Import** als `dictate_prompts.json`.
- **Prompt-Bibliothek** der Community zum Hinzufügen (`DictatePromptLibraryScreen.kt`, `dictate/data/prompts/PromptLibrary*.kt`).
- Außerdem „Zeichensetzung & Stil“ für die Transkription selbst (keiner / Standard / eigener Text).
- Mitgelieferte Stile aus dem Entwurf (Wörtlich, E-Mail, Chat, Aktenvermerk): im Entwurf gezeigt, **geplant** als Voreinstellungen – ob sie im Code schon als Standard-Prompts existieren, ist nicht geprüft.

## 9. Wörterbuch

Zwei getrennte Dinge (im Entwurf in einem Tab „Wörterbuch“):

1. **Eigene Wörter** (`DictateCustomWordsSection.kt`): Namen und Fachbegriffe, die dem Sprachmodell mitgegeben werden, damit es sie richtig schreibt. Zählt Tokens (das Sprachmodell liest nur begrenzt viel), Import/Export als Textdatei.
2. **Ersetzungen / Mappings** (`DictateMappingsScreen.kt`, `DictateMappings.kt`): Suchen-und-Ersetzen auf dem fertigen Text, exakt und lokal. Pro Regel: Groß-/Kleinschreibung beachten, nur ganze Wörter.

Status: **vorhanden**. Die Persönliche-Wörterbuch-Seiten der Tastatur sind **versteckt**.

## 10. Schwebender Button („Wolke“)

Einstellungen in `app/settings/dictate/DictateFloatingButtonScreen.kt`:

- Ein/Aus, Bedienungshilfe-Dienst und Mikrofonfreigabe einrichten
- **Wann zeigen**: sobald ein Feld Fokus hat · nur bei offener Tastatur · auch mit Dictate-Tastatur (`DictateFloatingButtonShowWhen.kt`)
- **App-Auswahl**: überall · nur in bestimmten Apps · überall außer in bestimmten Apps (`DictateFloatingButtonAppsScreen.kt`)
- **Stil**: Ring, Pille, Kugel, **Wolke**, Aurora, Gitterkugel (`DictateFloatingButtonDesign.kt`) – Lukas' Wahl: Wolke
- Größe, an Rand einrasten, Farbe, ausblenden wenn untätig, **Position pro App merken**, Vibration beim Tippen
- **Rückgängig-Taste**; **jedes Diktat in die Zwischenablage kopieren**
- **Menü am Button**: Datei transkribieren, Verlauf, Einstellungen (`overlay/BubbleMenuAction.kt`) – einzeln abschaltbar
- Zustände der Wolke: Ruhe, Zuhören (folgt dem Mikrofonpegel), Denken, Erfolg, Fehler (`ui/AudioReactiveCloudOrbView.kt`)
- Optik der Wolke und Paletten: siehe Design-Dokument Abschnitt 4. Angleichen an den Prototyp: **geplant**.

## 11. Verlauf

- Verlauf der Diktate an/aus, **Audio mit aufbewahren**, Speichergrenzen (maximale Einträge, automatisch löschen nach X Tagen, Audio-Speichergrenze)
- Einträge ansehen, kopieren, Audio abspielen, Verlauf leeren, **in einen Ordner exportieren** (optional mit Aufnahme)
- Code: `dictate/data/history/`, `app/settings/dictate/DictateHistoryScreen.kt`
- Status: **vorhanden**. Lukas will Verlauf und Statistik **behalten**.

## 12. Statistik

- Diktate, Wörter, Zeichen, gesprochene Zeit, Wörter pro Diktat, Sprechtempo (WPM), Umformulierungen, **gesparte Zeit gegenüber Tippen**, **Tage in Folge** (mit Bestwert), Aktivität der letzten 7 Tage, Zurücksetzen, Teilen
- Code: `dictate/data/stats/DictateStats.kt`, `app/settings/dictate/DictateStatsScreen.kt`
- Der Entwurf zeigt auf der Startseite: Wörter gesamt, Diesen Monat, Tage in Folge. „Diesen Monat“ ist als eigene Zahl im Code nicht geprüft.
- Status: **vorhanden**.

## 13. Tastatur und Zubehör entfernen

Lukas' Vorgabe: **die Tastatur und alles, was damit zusammenhängt, soll so weit wie möglich raus.** Heute ist sie nur
ausgeblendet (`app/DictationOnly.kt`, `manifestPlaceholders["fullKeyboard"]`, Gradle-Property `dictationOnly`);
der Code steckt noch in der App. Ausgeblendet sind: Sprachen/Layouts, Design, Tastatur, Eingabe-Feedback, eigene Symbole,
Smartbar, Tippen/Korrektur, Wörterbuch der Tastatur, Gesten, Zwischenablage, Medien (GIF/Sticker), physische Tastatur,
Übersetzung, Diktat-Layout, Wear. Rechtschreibprüfung und Offline-Übersetzung sind im Manifest abgeschaltet.

Zu entfernen (Status: **geplant**, in Schritten, nach jedem Schritt Build prüfen):
1. Tastatur-IME, Rechtschreibprüfung, Wear (`dictate/wear/`), Übersetzung (`dictate/translate/`), GIFs (`dictate/gif/`), Sticker (`dictate/sticker/`), Scan (`dictate/scan/`), Dictate-Tastaturoberfläche (`dictate/ui/` soweit nur Tastatur).
2. Die dazugehörigen Einstellungsseiten, Ressourcen, Sprachdateien und Abhängigkeiten (nativer Übersetzer, Wear-Module).
3. Dictate Cloud (Guthaben-Seiten, Abrechnung) als nicht im Soll.

**Bleibt wegen Abhängigkeit:** (noch leer, Claude Code trägt hier ein, was er aus diesem Grund behalten musste)
**Regel (Lukas):** Tastatur-Funktionen werden entfernt, außer ein gewünschter Teil hängt im Code daran und liefe sonst nicht.
Dann bleibt der Teil drin, der dafür gebraucht wird (nicht mehr). Im Zweifel: entfernen, bauen, prüfen; geht etwas Gewünschtes kaputt, den Teil
zurückholen und hier unter „Bleibt wegen Abhängigkeit“ mit Grund notieren.

Bleiben muss alles, was der Kernablauf braucht (Abschnitt 2: Button, Spracheingabe-Methode, Erkennungsdienst, Datei transkribieren).

Preis: Das **Übernehmen von Updates des Originals** wird dadurch schwer. Das nimmt Lukas' Vorgabe in Kauf.
`dictationOnly=false` geht dann nicht mehr. Wenn das nicht gewollt ist, vorher auf einem Branch sichern.

## 14. Geplant (von Lukas)

Reihenfolge und Details: `CLAUDE.md` und `docs/design/diktat-design.md` Abschnitt 7.

1. App-Icon „Welle um die Welt“ einbauen, Splash-Animation A, In-App-Animation D, Umschalter weiß/schwarz
2. Neue Oberfläche „Entwurf 3“ (Start, Wörterbuch, Stile, Einstellungen)
3. Wolken-Shader an den Prototyp angleichen
4. Anbieter Gladia, Preset Cerebras, AssemblyAI Sync API

## 15. Noch offen (Entscheidung von Lukas)

- Mitgelieferte Stile (Wörtlich, E-Mail, Chat, Aktenvermerk): Wortlaut der Prompts festlegen.
- Welche der vorhandenen Einstellungen bleiben in der neuen Oberfläche sichtbar, welche wandern unter „Mehr“?
- Offline-Spracherkennung: laut Lukas **behalten**; Modellverwaltung muss in die neue Oberfläche passen.
