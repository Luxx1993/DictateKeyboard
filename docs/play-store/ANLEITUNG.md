# diktat im Google Play Store veröffentlichen

Der Weg vom heutigen Stand zur Veröffentlichung. Vorbereitet ist: Release-Build, Skripte für Schlüssel und Bundle, der Zustimmungsdialog für die Bedienungshilfen, Datenschutzerklärung und Store-Texte. Offen sind die Schritte, die nur du in deinem Konto machen kannst.

| Datei | Inhalt |
|---|---|
| `datenschutz.md` | Datenschutzerklärung (zwei Stellen mit deinen Angaben ausfüllen) |
| `store-eintrag.md` | Store-Texte, Antworten für alle Formulare, Text für die Erklärung zu den Bedienungshilfen |
| `../../scripts/make-upload-key.sh` | legt den Upload-Schlüssel an |
| `../../scripts/build-play-bundle.sh` | baut und prüft das signierte Bundle |

## Schon erledigt
- Paket-ID `app.diktat`, `targetSdk` 36, 16-KB-Seitengröße der nativen Bibliotheken geprüft.
- Release-Build mit Verkleinerung (R8) und Signatur aus `keystore.properties` ist eingerichtet.
- Der Dialog vor den Bedienungshilfen nennt jetzt genau, was der Dienst liest, und hat „Zustimmen und Einstellungen öffnen“ und „Ablehnen“.

## Was du tun musst

1. **Entwicklerkonto** anlegen: https://play.google.com/console (einmalig 25 US-Dollar, Ausweis-Prüfung). Wähle „Privatperson“ oder „Organisation“; bei Privatpersonen gilt der geschlossene Test (siehe unten).
2. **Datenschutzerklärung veröffentlichen.** Trag in `datenschutz.md` deine Anschrift und E-Mail ein (Abschnitt 1). Play braucht eine öffentlich erreichbare Webadresse. Am einfachsten über GitHub Pages: Repository › Settings › Pages › Branch `diktat-edition`, Ordner `/docs` – dann liegt sie unter `https://luxx1993.github.io/unisono-diktat/play-store/datenschutz`. Das Repository muss dafür öffentlich sein. Alternativ geht jede eigene Webseite.
3. **Upload-Schlüssel anlegen:** `scripts/make-upload-key.sh`. Das Skript schreibt `keystore.properties` und nennt das Passwort. **Sichere die `.jks`-Datei und das Passwort an einem zweiten Ort.** Geht der Schlüssel verloren, lässt er sich über den Play-Support zurücksetzen; die App bleibt.
4. **Bundle bauen:** `scripts/build-play-bundle.sh`. Es erzeugt `app/build/outputs/bundle/release/app-release.aab` und prüft die Signatur.
5. **App in der Play Console anlegen** (Name, Standardsprache Deutsch, App, kostenlos). Bei „Play App Signing“ zustimmen.
6. **Formulare ausfüllen** mit `store-eintrag.md`: Store-Eintrag, Datensicherheit, Inhaltseinstufung, Zielgruppe, Werbung, Bedienungshilfen, Vordergrunddienste.
7. **Grafiken und Screenshots** hochladen (Maße in `store-eintrag.md`). Das Video zu den Bedienungshilfen aufnehmen (Ablauf steht dort).
8. **Interner Test:** Bundle in „Testen › Interner Test“ hochladen, dich als Tester eintragen, über den Link installieren und alles durchspielen.
9. **Geschlossener Test:** Bei einem privaten Konto vorgeschrieben: mindestens 12 Tester, die 14 Tage lang angemeldet bleiben. Das Konto bekommt danach den Zugang zur **Produktion**.
10. **Produktion beantragen** und auf die Prüfung warten (meist ein paar Tage, bei Bedienungshilfen eher länger).

## Was sich durch Play ändert
- **Neue Signatur:** Die Play-Version trägt Googles Signatur, deine jetzige Debug-APK einen anderen. Sie lassen sich nicht überinstallieren. Wechsle einmal: Einstellungen sichern (in der App), alte App deinstallieren, Play-Version installieren, Sicherung zurückspielen. Dein S24 kann also entweder die Play-Version oder die APK vom Zweig `apk` haben, nicht beides.
- **Updates:** Bei jeder neuen Version `projectVersionCode` in `gradle.properties` erhöhen und ein neues Bundle hochladen.
- **Name:** Der Paketname `app.diktat` bleibt für immer. Der Anzeigename lässt sich später ändern.

## Worauf bei der Prüfung geachtet wird
- **Bedienungshilfen:** Google prüft Dialog, Beschreibung und Video. Bleib bei dem, was die App wirklich tut. Das größte Risiko ist die Helligkeitsmessung für den Stil „Welle“ (`canTakeScreenshot`); fällt sie auf, kann man sie streichen.
- **Datenfluss:** Gib die Übertragung von Audio und Text an die Anbieter offen an (siehe Datensicherheit).
- **Markenrechte:** Der Name „Dictate“ gehört DevEmperor. diktat nutzt ihn nicht. Prüfe, ob „diktat“ als App-Name frei ist, und nenne die Herkunft weiter (Über-Seite, `NOTICE`, README), wie die Apache-Lizenz es verlangt.
- **Ein Fehler in der Prüfung** ist normal und kein Verbot: Google nennt den Punkt, du besserst nach und reichst neu ein.
