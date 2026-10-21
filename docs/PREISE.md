# Preise der Spracherkennung (€ pro Stunde Audio)

Die App zeigt den Preis neben jedem Modell in der Modellauswahl (Datei) und in der Auswahl der Echtzeit-Modelle, mit 2 bis 4 Nachkommastellen und ohne überflüssige Nullen. Die Daten stehen in `lib/dictate-core/.../provider/ModelPrices.kt`, die Browser-Version in `js/prices.js` von Unisono.

Quelle: die Preisseiten der Anbieter, abgerufen am 21.10.2026, normale Pay-as-you-go-Preise, Dollarpreise umgerechnet mit 1 € = 1,1215 $ (Gladia, Scaleway und OVHcloud listen in Euro). Aufpreise für Sprechertrennung oder Fachbegriffe sind nicht enthalten. „ca.“ steht bei gerechneten, geschätzten oder nur aus Drittquellen belegten Werten (Token-Preise, Aktionspreise, „ab“-Preise). Wo kein Preis gefunden wurde, zeigt die App keinen.

## Datei

| Anbieter | Modell | €/h |
|---|---|---|
| OpenRouter | openai/whisper-large-v3-turbo | 0,0096 |
| OpenRouter | openai/whisper-large-v3 | 0,0257 |
| OpenRouter | nvidia/parakeet-tdt-0.6b-v3 | 0,0802 |
| OpenRouter | microsoft/mai-transcribe-2 | 0,0892 |
| OpenRouter | openai/gpt-transcribe | 0,2407 |
| OpenRouter | elevenlabs/scribe-v2 | 0,1958 |
| OpenRouter | x-ai/grok-stt-1.0 | 0,0899 |
| OpenRouter | mistralai/voxtral-mini-transcribe | 0,1605 |
| OpenRouter | google/gemini-3.5-transcribe | ca. 0,2675 |
| OpenRouter | deepgram/nova-3 | ca. 0,2311 |
| OpenRouter | assemblyai/universal-3-6-pro | ca. 0,2664 |
| Groq | whisper-large-v3-turbo | 0,0357 |
| Groq | whisper-large-v3 | 0,099 |
| OVHcloud | whisper-large-v3-turbo | 0,046 |
| OVHcloud | whisper-large-v3 | 0,147 |
| Scaleway | whisper-large-v3 | 0,18 |
| Together AI | openai/whisper-large-v3 | 0,0802 |
| Together AI | nvidia/parakeet-tdt-0.6b-v3 | 0,0802 |
| xAI | grok-voice-transcribe-2.0 | ca. 0,0892 |
| Soniox | stt-async-v5 | ca. 0,087 |
| Azure | mai-transcribe-2 | 0,0892 |
| Azure | mai-transcribe-1.5 | 0,321 |
| AssemblyAI | universal-2 | 0,1337 |
| AssemblyAI | universal-3-5-pro | 0,1872 |
| AssemblyAI | universal-3-5-pro-sync | ca. 0,4012 |
| Gladia | solaria-3 | 0,16 |
| Gladia | solaria-1 | 0,27 |
| OpenAI | gpt-4o-mini-transcribe | 0,1605 |
| OpenAI | gpt-transcribe | ca. 0,2407 |
| OpenAI | gpt-4o-transcribe | 0,321 |
| OpenAI | gpt-4o-transcribe-diarize | 0,321 |
| Mistral | voxtral-mini-latest | ca. 0,1872 |
| Mistral | voxtral-mini-transcribe-3-0 | 0,1872 |
| Mistral | voxtral-small-transcribe-3-0 | 0,214 |
| ElevenLabs | scribe_v2 | 0,1962 |
| Deepgram | nova-3 | 0,23 |
| Gemini | gemini-3.5-transcribe | ca. 0,2675 |
| Gemini | gemini-3.6-flash | ca. 0,2318 |
| Gemini | gemini-3.8-flash | ca. 0,2318 |
| Gemini | gemini-3.5-flash-lite | ca. 0,058 |

## Echtzeit

| Anbieter | Modell | €/h |
|---|---|---|
| Soniox | stt-rt-v5 | 0,107 |
| AssemblyAI | universal-streaming-english | 0,1337 |
| AssemblyAI | universal-streaming-multilingual | 0,1337 |
| AssemblyAI | universal-3-6-pro | 0,4012 |
| xAI | grok-voice-transcribe-2.0 | ca. 0,1783 |
| Deepgram | nova-2 | 0,3121 |
| Deepgram | nova-3 | ca. 0,2568 |
| Deepgram | flux-general-en | ca. 0,3477 |
| Deepgram | flux-general-multi | 0,4173 |
| ElevenLabs | scribe_v2_realtime | 0,3477 |
| Mistral | voxtral-mini-transcribe-realtime-3-0 | 0,428 |
| Mistral | voxtral-mini-transcribe-realtime-2602 | ca. 0,428 |
| Gladia | solaria-1 | 0,68 |

## Nicht gefunden

AssemblyAI Universal-3 Pro, SiliconFlow (SenseVoice wird nach Zeichen abgerechnet), Deepgram Nova-2 und Flux für Dateien, die Preise des Aufräumens mit KI-Modellen (Token-Preise, nicht recherchiert). Groq rechnet mindestens 10 s je Anfrage ab, Scaleway gibt 60 Minuten frei, OpenRouter erhebt 5,5 % beim Aufladen; Deepgram Nova-3 und Flux Streaming sind derzeit Aktionspreise.
