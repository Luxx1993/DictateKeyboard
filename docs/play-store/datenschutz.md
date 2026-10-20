# Datenschutzerklärung für die App „diktat“

Stand: 20. Oktober 2026

Diese Erklärung gilt für die Android-App **diktat** (Paket `app.diktat`). Sie beschreibt, welche Daten die App verarbeitet, wohin sie gehen und was du dagegen tun kannst. Kurz gesagt: **diktat hat keinen eigenen Server.** Ich bekomme weder deine Aufnahmen noch deine Texte noch deine Schlüssel zu sehen. Was du diktierst, geht direkt von deinem Handy an die KI-Anbieter, die du selbst einrichtest.

## 1. Wer verantwortlich ist

Lukas Klenk (Luxx1993)
[Anschrift: Straße, PLZ Ort, Land — vor der Veröffentlichung eintragen]
E-Mail: [Kontakt-E-Mail — vor der Veröffentlichung eintragen]

Fragen zum Datenschutz oder Löschwünsche kannst du an diese Adresse schicken oder als Meldung auf https://github.com/Luxx1993/unisono-diktat/issues stellen (dort nichts Persönliches schreiben).

## 2. Was die App tut

diktat nimmt auf, was du sprichst, lässt es von einem Spracherkennungs-Anbieter in Text umwandeln, lässt den Text auf Wunsch von einer KI aufräumen und fügt ihn in das Textfeld ein, in dem du gerade schreibst.

## 3. Welche Daten verarbeitet werden und wohin sie gehen

| Daten | Wann | Wohin |
|---|---|---|
| **Sprachaufnahmen** (Mikrofon) | Nur während du aufnimmst. | An den **Spracherkennungs-Anbieter**, den du gewählt hast. Bei einem Offline-Modell bleibt die Aufnahme auf dem Gerät. |
| **Diktierter Text** | Wenn „Aufräumen“ oder „Umformulieren“ an ist. | An den **KI-Anbieter**, den du gewählt hast, zusammen mit deinen eigenen Wörtern und der gewählten Stil-Anweisung. |
| **Fachbegriffe und Ersetzungen** | Wenn du welche angelegt hast. | Die Fachbegriffe gehen als Hinweis an die Spracherkennung und die KI. Die Ersetzungen laufen nur auf dem Gerät. |
| **API-Schlüssel** | Wenn du einen Anbieter einrichtest. | Bleiben auf dem Gerät und gehen nur an den jeweiligen Anbieter, um dich dort anzumelden. |
| **Verlauf, Statistik, Einstellungen** | Immer. | Bleiben auf dem Gerät. |

Du wählst die Anbieter selbst, mit deinem eigenen Konto und deinem eigenen Schlüssel. Zur Auswahl stehen unter anderem: ElevenLabs, AssemblyAI, Gladia, Mistral, OpenAI, Groq, Deepgram, Soniox, Google (Gemini), Anthropic, xAI, DeepSeek, SiliconFlow, Cerebras, Together AI, DeepInfra, OVHcloud, Scaleway, OpenRouter, Microsoft Azure sowie ein eigener Server (z. B. Ollama in deinem Netzwerk). **Für die Verarbeitung dort gelten die Datenschutzbestimmungen des jeweiligen Anbieters.** Einige sitzen außerhalb der EU (z. B. in den USA oder China). Wer das vermeiden will, wählt einen europäischen Anbieter (Mistral, Scaleway, OVHcloud), einen mit EU-Region oder ein Offline-Modell.

Es gibt **keine Werbung, keine Analyse- oder Absturz-Dienste und keine Weitergabe an mich oder Dritte** außer den von dir gewählten Anbietern.

## 4. Verbindungen, die die App sonst aufbaut

- **Offline-Modelle herunterladen:** Wenn du ein Modell für die Erkennung auf dem Gerät lädst, ruft die App die Datei von github.com ab. GitHub sieht dabei deine IP-Adresse.
- **Stil-Bibliothek:** Öffnest du die Bibliothek mit fertigen Stil-Anweisungen, lädt die App die Liste von raw.githubusercontent.com (GitHub). Dabei werden keine Inhalte von dir übertragen, GitHub sieht deine IP-Adresse. Ohne Verbindung zeigt die App die mitgelieferte Fassung. Mitmachen mit einer eigenen Anweisung öffnet GitHub im Browser; was du dort einreichst, ist öffentlich.
- **Eigener Server:** Trägst du eine eigene Adresse ein (auch `http://` im lokalen Netz), gehen Audio und Text dorthin. Über unverschlüsselte Adressen läuft alles ohne Verschlüsselung; die App weist darauf hin.

## 5. Gespeichert auf deinem Gerät

Einstellungen, API-Schlüssel, eigene Wörter, Stile, der Diktat-Verlauf und die Statistik liegen im privaten Speicher der App. Andere Apps kommen nicht heran. Löschen kannst du alles in der App oder durch Entfernen der App.

**Android-Sicherung:** Die Einstellungen **einschließlich der API-Schlüssel** können in die Android-Sicherung (Google-Konto) und beim Gerätewechsel mitgehen. Die Cloud-Sicherung läuft nur, wenn Android sie Ende-zu-Ende mit deiner Bildschirmsperre verschlüsseln kann. Der Verlauf und Audio gehören nicht dazu. Du kannst die Sicherung in den Android-Einstellungen ausschalten. Die App hat außerdem eine eigene Sicherung und Wiederherstellung in Dateiform.

## 6. Berechtigungen

- **Mikrofon:** zum Aufnehmen deiner Sprache. Aufgenommen wird nur nach deinem Antippen des Buttons.
- **Internet:** um Audio und Text an die gewählten Anbieter zu schicken.
- **Benachrichtigungen und Vordergrunddienst:** zeigt dir, dass die Aufnahme läuft oder ein Modell lädt.
- **Bedienungshilfen (nur wenn du den schwebenden Button einschaltest):** siehe Abschnitt 7.
- **Bluetooth/Audio-Einstellungen:** um ein Headset als Mikrofon zu nutzen.

## 7. Bedienungshilfen für den schwebenden Button

Der schwebende Button ist **freiwillig**. Er nutzt den Bedienungshilfe-Dienst von Android, damit er
- erkennt, wann in einer beliebigen App ein Textfeld ausgewählt ist, und dann erscheint,
- den diktierten Text an der Cursorposition dieses Feldes einfügt,
- nur beim Stil „Welle“ die Helligkeit einiger Bildpunkte neben dem Button liest, um Schwarz oder Weiß zu wählen. Das Bild wird sofort verworfen.

diktat ist **kein Hilfsmittel für Menschen mit Behinderung**; die Schnittstelle dient ausschließlich dem Diktieren. Der Dienst liest keine Inhalte außerhalb des ausgewählten Textfeldes mit, speichert keine Bildschirminhalte oder Eingaben und gibt nichts weiter. Vor dem Einschalten fragt die App dich mit einem eigenen Hinweis um Zustimmung. Du kannst den Dienst jederzeit in den Bedienungshilfen von Android ausschalten.

## 8. Rechtsgrundlage

Die Verarbeitung der Aufnahmen und Texte beruht auf deiner Anfrage: Du startest die Aufnahme und wählst die Anbieter selbst (Art. 6 Abs. 1 lit. b DSGVO, Vertragserfüllung bzw. vorvertragliche Maßnahmen; für die freiwilligen Bedienungshilfen Art. 6 Abs. 1 lit. a, Einwilligung). Auf die Verarbeitung durch die Anbieter habe ich keinen Einfluss.

## 9. Deine Rechte

Du hast das Recht auf Auskunft, Berichtigung, Löschung, Einschränkung, Widerspruch und Datenübertragbarkeit sowie auf Beschwerde bei einer Aufsichtsbehörde. Da ich keine Daten von dir speichere, gehen Auskunft und Löschung vor allem an die Anbieter, die du nutzt. Auf deinem Gerät löschst du alles in der App.

## 10. Kinder

Die App richtet sich nicht an Kinder unter 16 Jahren.

## 11. Änderungen

Ändert sich die App, passe ich diese Erklärung an. Das Datum oben zeigt den Stand.

## 12. Herkunft der App

diktat basiert auf der Open-Source-App Dictate Keyboard von DevEmperor (Apache 2.0), die auf FlorisBoard aufbaut. Der Quelltext steht unter https://github.com/Luxx1993/unisono-diktat.
