# diktat – Design-Übergabe

Stand: 18.10.2026. Entstanden in einer Design-Session mit Claude (claude.ai). Dieses Dokument
enthält alle Entscheidungen, damit die Umsetzung im Code ohne den ursprünglichen Chat weitergehen kann.

Das ursprüngliche Design-Canvas (privat): https://claude.ai/artifact/Rs7wSSxyuCEvwaud7Puj2j

## Inhalt dieses Ordners

| Pfad | Was |
|---|---|
| `entwurf-3/*.html` | Die freigegebenen Bildschirme von Entwurf 3 als HTML-Referenz (im Browser öffnen). Alle Maße, Farben und Abstände stehen inline im Markup. |
| `entwurf-3/SchwebenderButton.html` | Der schwebende Button („Wolke“) mit allen Zuständen und Farbpaletten. |
| `entwurf-3/textures/` | Rausch-Texturen für die Wolke (Rauch und Körnung), als SVG mit `feTurbulence`. |
| `icon/diktat_icon.py` | Generator für das App-Icon und die Animationen. Erzeugt SVGs und Android-VectorDrawables (`python3 diktat_icon.py`). |
| `icon/export/svg/` | Fertige SVGs: Icon weiß und schwarz, Animation A und D als animiertes SVG (im Browser ansehen). |
| `icon/export/android/` | Fertige VectorDrawables für das adaptive Icon (Vordergrund weiß, schwarz, einfarbig). |

---

## 1. Ziel der App

Luxx1993s eigene, anbieterunabhängige Diktier-App für Android, ein Fork von Dictate Keyboard
(Branch `diktat-edition`). Kernablauf: sprechen → KI räumt den Text auf → Text wird eingefügt.
Bedient wird sie über den schwebenden Button („Wolke“) neben SwiftKey. Eigene API-Keys,
aktuell ElevenLabs Scribe (Transkription) und Claude Haiku 5.5 (Aufräumen).

## 2. Designsystem (Unisono-Look, warm und ruhig)

Schrift: **Manrope** (300 für große Überschriften, 400–700 für UI), **IBM Plex Mono** für Zeitangaben und Zahlen in Listen.
Name/Wortmarke: „diktat“ in Manrope 400, klein geschrieben.

### Farben hell

| Token | Wert |
|---|---|
| surface-canvas | `#F4F1EC` (warmes Papier) |
| surface-raised | `#FFFFFF` |
| surface-sunken | `#ECE7E0` |
| text-primary | `#1F1C1A` (Tinte) |
| text-secondary | `#5E5852` |
| text-tertiary | `#6F6962` |
| border-subtle | `rgba(31,28,26,.08)` |
| border-strong | `rgba(31,28,26,.16)` |
| action-primary / on-primary | `#1F1C1A` / `#F4F1EC` |
| provider-elevenlabs | `#D69A4E` (Bernstein) |
| provider-free (für Claude) | `#C07A7A` (Rosenholz) |
| status-success | `#357A5B` |

### Farben dunkel

| Token | Wert |
|---|---|
| surface-canvas | `#121110` |
| surface-raised | `#1B1A18` |
| surface-sunken | `#2A2825` |
| text-primary | `#F2EEE8` |
| text-secondary | `#A9A29A` |
| text-tertiary | `#857E77` |
| border-subtle / strong | `rgba(242,238,232,.08)` / `.16` |
| action-primary / on-primary | `#F2EEE8` / `#121110` |

Radien: 10 (md), 16 (lg), Pille 999. Schatten: nur sehr zart (`elev-1`: 0,5 px Kontur plus 1 px Schatten), keine Glows.
Aktionen sind tintenfarben, keine Akzentfarbe in Buttons.

## 3. App-Aufbau (Entwurf 3)

Untere Navigation mit 4 Einträgen: **Start, Wörterbuch, Stile, Einstellungen**. Keine Pille um den aktiven Eintrag;
aktiv = text-primary und Schrift 700, inaktiv = text-tertiary und 600. Icon-Feld 64×22, Abstand Icon–Text 4 px,
Leiste `padding: 12px 12px 24px`, Hintergrund surface-raised, oben 1 px border-subtle.
Alle Seitenüberschriften stehen auf derselben Kante: `padding-top: 40px`, Überschrift 32/38 px, Manrope 300, −0,02 em.
Keine Wortmarken-Zeile über der Überschrift.

- **Start** (`entwurf-3/Main.html`, dunkel `StartDunkel.html`, Galaxy-S24-Mockup `StartS24.html`)
  - „Hallo“ (mit eingetragenem Namen „Hallo, Name“) und Untertitel „Diktiere in jeder App über die Wolke oder die Mikrofon-Taste deiner Tastatur.“
  - Statistik in drei Spalten: Wörter gesamt, Diesen Monat, Tage in Folge (Zahl 34 px, Manrope 300)
  - Karte „Aktive Anbieter“: Transkription (ElevenLabs Scribe, Punkt Bernstein) und Aufräumen (Claude Haiku 5.5, Punkt Rosenholz), jeweils mit Pfeil zu den Einstellungen
  - „Letzte Diktate“: 3 Einträge, Text zweizeilig gekürzt, Zeit in IBM Plex Mono, Kopieren-Button 44×44
  - Bewusst **kein** „Diktieren / Datei transkribieren“ auf der Startseite.
- **Stile** (`Stile.html`): aktiver Stil als Karte mit Tinten-Rahmen und Schalter „Nach jedem Diktat anwenden“, weitere Stile im 2er-Raster (Wörtlich, E-Mail, Chat, Aktenvermerk), Button „Neuer Stil“ gestrichelt.
- **Wörterbuch** (`Woerterbuch.html`): Suchfeld (Pille, surface-sunken), „Eigene Wörter“ als Chips, „Ersetzungen“ als Liste (gesprochen → geschrieben).
- **Einstellungen** (`Einstellungen.html`): oben Karte „Schwebender Button“ mit Vorschau-Wolke, Schalter und Farbwahl (5 Paletten); darunter Gruppen „Diktat“ und „Mehr“. Untermenüs **nicht** eingerückt.

## 4. Schwebender Button („Wolke“)

Referenz: `entwurf-3/SchwebenderButton.html` (CSS-Prototyp). Im Code existiert bereits ein AGSL-Shader in
`app/src/main/kotlin/dev/patrickgold/florisboard/dictate/ui/AudioReactiveCloudOrbView.kt`
(Modi IDLE, LISTENING, THINKING, SUCCESS, ERROR; im Zuhören folgen Größe, Fließgeschwindigkeit und Aktivität dem Mikrofonpegel).
Ziel: diesen Shader optisch an den Prototyp angleichen.

- Look: körnige, farbige Kugel, ruhige Animation „wie Rauch in einer Glaskugel“ (ElevenLabs/ChatGPT-Voice-Stil).
- Aufbau des Prototyps: 4 weichgezeichnete Farbflecken (o1–o4) driften langsam (18–32 s, alternierend), darüber Rauch-Textur (Overlay, gegenläufig rotierend, 90 s),
  Körnung (Overlay), Glas-Licht oben links, Schatten unten rechts, heller Rand. Gesamtrotation 140 s. Ruhezustand atmet mit 2 % Skalierung in 9 s.
- Zuhören: Stimme-Keyframes (Skalierung 1,03–1,065 über 7 s), Flecken schneller (12 s). Verarbeiten: dünner Kreis-Spinner in der Mitte (1,8 s). Erfolg: grüne Tönung und Häkchen.
- `prefers-reduced-motion` bzw. „Animationen entfernen“ → alles statisch.
- Paletten (o0 Grund, o1–o4 Flecken). Standard ist **Lagune**:

| Palette | o0 | o1 | o2 | o3 | o4 |
|---|---|---|---|---|---|
| Lagune (Standard) | `#9AD7E6` | `#3FA9C8` | `#1E6F8E` | `#DDF4EF` | `#86D3B7` |
| Periwinkle | `#B8C7F9` | `#7A8FFB` | `#5C63FB` | `#E3EBFE` | `#C9B8F9` |
| Salbei | `#B5D9A8` | `#6BA86F` | `#2F6A52` | `#EDF3CF` | `#D2E48F` |
| Abendrot | `#EDBFCF` | `#C96F9D` | `#7B3F80` | `#F8E1D2` | `#EBA57E` |
| Tinte | `#CFC8BF` | `#6F6962` | `#1F1C1A` | `#F4F1EC` | `#A59E96` |

- Wolke und App-Icon sind **getrennte Dinge**: das Icon zeigt keine Wolke.
- **Stil „Welle“** (zusätzlicher Button-Stil, `DictateWaveOrbView`): das App-Icon als Strichzeichnung, schwarz oder weiß je nach
  Hintergrund. Ruhe: steht still wie das Icon. Aufnahme: Welle dreht sich (Animation D) und die Kugel atmet (2,6 s, etwas tiefer mit
  der Stimme). Transkribieren/Umformulieren: nur Drehen. Erfolg/Fehler: kurz grün/rot. Die Farbe kommt aus einem Standbild des
  Bildschirms (nur wenige Bildpunkte um den Button, nichts gespeichert; braucht `canTakeScreenshot`, Android 11+), sonst aus hell/dunkel
  des Systems.

## 5. App-Icon „Welle um die Welt“ (entschieden)

Eine Kugel als Ring, um die einmal eine Welle läuft. Alles flache Linien; die räumliche Wirkung entsteht nur über die
Strichstärke: vorne 2,8, nach hinten weich auf 1,3 abnehmend. Die Welle ist **durchgehend** und berührt den Ring
(keine Lücke!). Nur Weiß oder Schwarz, **kein Hintergrund**.

Geometrie (alles in `icon/diktat_icon.py`, Konstanten oben in der Datei):
48er-Raster, Mitte (24, 24), Kugelradius 20,6, Welle `lat = 19° · sin(3·lon + 180°)`, Kugel um 22° geneigt,
orthografische Projektion, Strichstärke abhängig von der Tiefe z (smoothstep), in 9 Stufen gebündelt.
Im adaptiven Icon (108 dp) wird das 48er-Raster auf die sichtbaren 72 dp gelegt (Skalierung 1,5, Versatz 18),
die Kugel ist damit ca. 62 dp groß und liegt in der 66-dp-Schutzzone.

### Umsetzung in Android (alle Punkte erledigt)

1. **Adaptives Icon** – *erledigt.* `mipmap-anydpi-v26/app_icon.xml` (weiß) und `app_icon_black.xml` (schwarz):
   `background` = `@android:color/transparent`, `foreground` = `ic_diktat_globe_white` bzw. `_black`,
   `monochrome` = `ic_diktat_globe_monochrome`. Die alten WebP-Vordergründe, `app_icon_round` und die Farbe
   `ic_app_icon_background` sind raus. Die Dateien in `app/src/main/res/drawable/ic_diktat_globe_*.xml` sind Kopien aus
   `icon/export/android/`; nach einer Änderung am Generator neu kopieren.
   Zusätzlich erzeugt der Generator `ic_diktat_globe_notification` (24 dp) für Benachrichtigungen, die Einstellungszeile
   und die Info-Seite (dort in der Textfarbe getönt, weil Weiß auf hellem Grund verschwände), und `ic_diktat_globe_themed`
   (statisch, Linienfarbe `@color/diktat_icon_ink`; derzeit nicht in der App).
2. **Umschalter „Icon weiß / Icon schwarz“** – *erledigt:* Einstellungen › Weitere › App-Icon, Code in `app/AppIcon.kt`,
   Einstellung `other__app_icon_color`. Ursprünglicher Plan: Zweiter Alias neben dem bestehenden
   `dev.patrickgold.florisboard.SettingsLauncherAlias` (Manifest) mit schwarzem Icon (`app_icon_black.xml`), standardmäßig deaktiviert;
   beim Umschalten per `PackageManager.setComponentEnabledSetting` den einen Alias an-, den anderen ausschalten.
   Achtung: Die vorhandene Funktion „App-Icon ausblenden“ (`lib/android/.../PackageManager.kt`, `SETTINGS_ACTIVITY_NAME`, nur API ≤ 28) muss den jeweils aktiven Alias berücksichtigen.
   Automatisch je nach Hintergrundbild wechseln kann Android nicht; Weiß ist Standard.
3. **Animation A beim Öffnen** – *erledigt:* `res/drawable/avd_diktat_globe_splash.xml` (vom Generator,
   `android_avd_splash`), im Theme `FlorisAppTheme.Splash` mit 1000 ms. Die Welle dreht sich in 1 s um 60° mit ease-out
   (kubisch) und **endet genau auf dem Launcher-Icon** (Start bei −60°), damit der Übergang in die App ohne Sprung ist.
   60 Wellenstücke à 6°, je 21 Keyframes für Pfad und Strichstärke; Ring statisch; Linienfarbe `@color/diktat_icon_ink`
   (Tinte hell, Papier dunkel), kein Icon-Hintergrund (`Theme.SplashScreen`). Unter Android 12 zeigt die Compat-Bibliothek
   die Kugel statisch. Ursprünglicher Plan: `windowSplashScreenAnimatedIcon` als AnimatedVectorDrawable,
   höchstens ~1000 ms. Bewegung: die Welle dreht sich um die Kugel (in `lat_anim(..., mode='A')` wächst nur der Längenversatz).
   Für 1 s etwa 60° Drehung mit ease-out. Linienfarbe hell/dunkel je nach Splash-Hintergrund (Farbressource mit `values-night`).
   Umsetzungsidee: Welle als z. B. 60 kurze Segmente (`M x y L x y`), je Segment eine Folge von pathData-Morphs plus strokeWidth-Animation; Ring statisch.
   `windowSplashScreenIconBackgroundColor` transparent bzw. Theme ohne Icon-Hintergrund.
4. **Animation D in der App** – *erledigt:* Compose-Komponente `DiktatGlobe` (`dictate/ui/DiktatGlobe.kt`), Geometrie als
   Kotlin-Port des Generators in `DiktatGlobeGeometry` (Unit-Test vergleicht mit Werten aus `diktat_icon.py`). Zeichnet pro
   Frame 240 Stücke mit tiefenabhängiger Strichstärke, Farbe = Textfarbe des Themes; bei „Animationen entfernen“ statisch.
   Eingesetzt in „Datei transkribieren“: groß (96 dp) während der Transkription, klein (20 dp) statt des Kreisels während
   des Umformulierens. Weitere Stellen (z. B. Start in Entwurf 3) mit `DiktatGlobe(animate = …)`.
   Ursprüngliche Vorgabe: solange diktiert oder verarbeitet wird: Welle dreht sich, gleichzeitig atmet die Amplitude,
   vorne und hinten **gegenläufig** (vorne höher = hinten flacher), Ausschlag nur bis 65 %.
   Formel in `lat_anim(..., mode='D')`: Schleife 6 s (eine Schleife = 120° Drehung, volle Umdrehung 18 s), zwei Atemzüge pro Schleife.
   Am besten als Compose-`Canvas`, der pro Frame projiziert und die Segmente mit tiefenabhängiger Strichstärke zeichnet (kein Video, keine Bitmap).
   Bei „Animationen entfernen“ statisch zeigen.
5. **Themen-Icon** (Android 13+): `monochrome`-Ebene wie oben – *erledigt.*

Vorschau: `icon/export/svg/diktat-anim-A-drehen.svg` und `diktat-anim-D-drehen-amplitude-gegenlaeufig.svg` im Browser öffnen.

## 6. Abgelehnt (nicht wieder vorschlagen)

Lila/violette Akzente; Wolke/Orb im App-Icon; allgemeine Sprechblasen; Buchstaben und Anführungszeichen als Icon;
farbige oder zweifarbige Logos; Pille um den aktiven Navigationseintrag; eingerückte Untermenüs; überladene Startseite
(Diktieren/Datei transkribieren/Wolke auf Start); Wortmarken-Zeile über den Seitenüberschriften; Welle mit Lücke zum Ring.

## 7. Nächste Schritte

- [x] App-Icon (Abschnitt 5, Punkte 1, 2, 5) eingebaut
- [ ] Auf dem Galaxy S24 prüfen, ob der transparente Hintergrund wirklich ohne Fläche erscheint (One UI legt bei manchen Launcher-Einstellungen eine eigene Fläche unter Icons)
- [x] Splash-Animation A (auf dem S24 ansehen)
- [x] In-App-Animation D (Compose) in „Datei transkribieren“; in Entwurf 3 weiterverwenden
- [x] Entwurf 3 als neue Oberfläche umsetzen (Start, Wörterbuch, Stile, Einstellungen, hell und dunkel): `app/main/` mit `MainScreen`, Tabs, `UnisonoComponents`, `UnisonoIcons` (Pfade aus den HTML-Referenzen); Theme in `app/apptheme/Unisono.kt`
- [ ] Entwurf 3 auf dem S24 ansehen (hell, dunkel) und Abstände nachziehen
- [x] Unterseiten im Unisono-Look, Teil 1: Schwebender Button und Aufnahme (`UnisonoPage`, Karten, `UChoiceDialog` statt Material-Dialogen, Stil-Auswahl mit Vorschaubildern `res/drawable-xhdpi/preview_bubble_*.png`; Bausteine in `app/main/UnisonoSettings.kt`)
- [x] Unterseiten Teil 2a: KI-Anbieter (Im Einsatz, Deine Anbieter, Netzwerk; Auswahl als `UDialog`) und Sprachen (aktive Sprache, Chips, Hinzufügen mit Suche). Der Anbieter-Editor-Dialog (`ProviderEditorDialog`) ist noch im alten Look
- [x] Unterseiten Teil 2b: Statistik, Ersetzungen und Verlauf im Unisono-Look. Im Verlauf bleiben die Dialoge (Export, Detail, Limits) noch alt
- [x] Rest der Unterseiten im Unisono-Look: Prompts, Prompt-Bibliothek, Anbieter-Editor, Verlauf-Dialoge, Proxy, Umformulieren, Anbieter hinzufügen, App-Auswahl, Dictate-Hub mit Formatierung und Ausgabe, Alle Einstellungen, Suche; Modellauswahl und Dialoge der lokalen Modelle. Neue Bausteine: `UPrefSwitch`, `UPrefSlider`, `UPrefText`, `UPrefChoice` (`app/main/UnisonoPrefs.kt`), `UnisonoLazyPage`, `UChip`, `UCheckMark`
- [x] Einstellungen-Tab ohne Zwischenseiten: Diktat-Bereiche, Mehr und App stehen direkt im Tab, die Suche als Pille oben. „Alle Einstellungen“ und der Diktat-Hub sind entfernt; die Suchtreffer führen direkt auf die Seiten. Keine freie Farbwahl mehr (weder für den Button noch als Akzent), nur Hell, Dunkel und Systemstandard
- [ ] Noch Material statt Unisono: die Schritte der Einrichtung (`SetupScreen`), die Zeilen der lokalen Modelle (`LocalModelSection`, `LocalModelRow`) und kleine Bestätigungsdialoge in den Tabs (`AlertDialog`); sie nutzen das Material-Theme, das auf Unisono abgebildet ist
- [x] Unterseiten Teil 3: Sonstige, Sichern, Wiederherstellen, Über und die drei Lizenzseiten im Unisono-Look (neue Bausteine `UButton`, `UNotice`, `UTextField`). Die Einrichtung (`SetupScreen`) bekommt den Look über das Material-Theme (Farben, Schrift) und die neuen Rundungen; ihre Schritte sind nicht neu gebaut
- [ ] Mitgelieferte Stile (Wörtlich, E-Mail, Chat, Aktenvermerk) als Standard-Prompts
- [x] Wolken-Shader an den Prototyp angeglichen (`dictate/ui/AudioReactiveCloudOrbView.kt`, Bewegung in `CloudMotion.kt`, Paletten in `CloudPalette.kt`):
  dieselben Ebenen, Keyframes und Dauern wie `SchwebenderButton.html`. Gegen Screenshots des Prototyps (Chromium) mit einem
  Python-Nachbau des Shaders verglichen, in Ruhe und bei 13 s, Lagune und Abendrot: Farben im Mittel auf wenige Stufen gleich.
  Die Texturen des Prototyps sind kein neutrales Grau (Mittel 0,73 bei halber Deckkraft) und hellen die Kugel auf; das ist übernommen.
  Ruhe ohne Mikro-Symbol, mit 20 Bildern/s. Zuhören folgt dem echten Mikrofonpegel statt der festen Stimm-Keyframes.
  Die Farbwahl speichert den Hauptton (o1) der Palette; Dictates altes Hellblau gilt als Lagune.
- [ ] Wolke auf dem S24 ansehen (Ruhe, Zuhören, Verarbeiten, Fertig, Fehler; Akkuverbrauch in Ruhe)
- [x] Anbieter Gladia, Preset Cerebras, AssemblyAI Sync API als Modell von AssemblyAI (Oktober 2026; mit echten Keys noch ungetestet)
