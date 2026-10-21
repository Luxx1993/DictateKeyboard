# Preise der Spracherkennung (€ pro Stunde Audio)

Die App zeigt den Preis neben jedem Modell in der Modellauswahl (Datei) und in der Auswahl der Echtzeit-Modelle. Die Daten stehen in `lib/dictate-core/.../provider/ModelPrices.kt`, die Browser-Version in `js/prices.js` von Unisono.

Quelle: die Preisseiten der Anbieter, abgerufen am 21.10.2026, normale Pay-as-you-go-Preise, umgerechnet mit 1 € = 1,1215 $ (Gladia, Scaleway und OVHcloud listen in Euro). Aufpreise für Sprechertrennung oder Fachbegriffe sind nicht enthalten. „ca.“ steht bei gerechneten oder nur aus Drittquellen belegten Werten. Wo kein Preis gefunden wurde, zeigt die App keinen.

## Datei

| Anbieter | Modell | €/h |
|---|---|---|
| OpenRouter | whisper-large-v3-turbo / whisper-large-v3 / parakeet / MAI-Transcribe-2 | 0,01 / 0,03 / 0,08 / 0,09 (+5,5 % beim Aufladen) |
| Groq | whisper-large-v3-turbo / whisper-large-v3 | 0,04 / 0,10 (mindestens 10 s je Anfrage) |
| OVHcloud | whisper-large-v3-turbo / whisper-large-v3 | 0,05 / 0,15 |
| Scaleway | whisper-large-v3 | 0,18 (erste 60 Minuten frei) |
| Together AI | whisper-large-v3 / parakeet | 0,08 |
| xAI | grok-voice-transcribe-2.0 | ca. 0,09 |
| Soniox | stt-async-v5 | 0,09 (Sprecher inklusive) |
| Azure | MAI-Transcribe-2 / 1.5 | 0,09 (Einführungspreis bis 31.12.2026) / 0,32 |
| AssemblyAI | Universal-2 / Universal-3.5 Pro / 3.5 Pro Sync | 0,13 / 0,19 / ca. 0,40 |
| Gladia | Solaria-3 / Solaria-1 | 0,16 / 0,27 (Sprecher inklusive) |
| OpenAI | gpt-4o-mini-transcribe / gpt-transcribe / gpt-4o-transcribe | 0,16 / ca. 0,24 / 0,32 |
| Mistral | Voxtral Mini 3 / Small 3 | 0,19 / 0,21 |
| ElevenLabs | Scribe v2 | 0,20 |
| Deepgram | Nova-3 | 0,23 |
| Gemini | 3.5-transcribe / Flash / Flash-Lite | ca. 0,27 / ca. 0,23 (bis 31.12.2026 ca. 0,12) / ca. 0,06 |

## Echtzeit

| Anbieter | Modell | €/h |
|---|---|---|
| Soniox | stt-rt-v5 | 0,11 |
| AssemblyAI | Universal-Streaming / Universal-3.6 Pro | 0,13 / 0,40 (nach Sitzungsdauer abgerechnet) |
| xAI | Streaming | 0,18 |
| Deepgram | Nova-2 / Flux / Nova-3 | 0,31 / ca. 0,35 / ab ca. 0,41 (Nova-3 derzeit Aktion ab 0,26) |
| ElevenLabs | Scribe v2 Realtime | 0,35 |
| Mistral | Voxtral Mini Realtime | 0,43 |
| Gladia | Live | 0,68 |

## Nicht gefunden

AssemblyAI Universal-3 Pro, SiliconFlow (SenseVoice wird nach Zeichen abgerechnet), Deepgram Nova-2 und Flux für Dateien, die Preise des Aufräumens mit KI-Modellen (Token-Preise, nicht recherchiert).
