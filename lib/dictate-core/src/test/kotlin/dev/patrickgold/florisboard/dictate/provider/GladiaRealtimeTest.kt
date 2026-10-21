/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.provider

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.ByteString
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Gladia live: the session call answers with a socket address, the socket takes binary PCM, `transcript`
 * messages come back and `stop_recording` ends it (docs.gladia.io, live quickstart and API reference).
 */
class GladiaRealtimeTest : FunSpec({

    class Recorder : RealtimeCallbacks {
        val partials = mutableListOf<String>()
        val finals = mutableListOf<String>()
        val errors = mutableListOf<Throwable>()
        val closed = CountDownLatch(1)
        override fun onPartial(text: String) { synchronized(this) { partials += text } }
        override fun onFinalSegment(text: String) { synchronized(this) { finals += text } }
        override fun onError(t: Throwable) { synchronized(this) { errors += t } }
        override fun onClosed() = closed.countDown()
    }

    test("the session call carries the key and the format, the socket takes PCM and ends with stop_recording") {
        val received = LinkedBlockingQueue<Any>()
        val serverClosed = CountDownLatch(1)
        val server = MockWebServer()
        server.start()
        server.enqueue(
            MockResponse().setResponseCode(201).setHeader("Content-Type", "application/json")
                .setBody("""{"id":"sid","created_at":"x","url":"ws://${server.hostName}:${server.port}/v2/live?token=sid"}"""),
        )
        server.enqueue(
            MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    received += bytes
                    webSocket.send("""{"type":"transcript","data":{"id":0,"is_final":false,"utterance":{"text":"Hallo"}}}""")
                    webSocket.send("""{"type":"transcript","data":{"id":0,"is_final":true,"utterance":{"text":"Hallo Welt."}}}""")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    received += text
                    if (text.contains("stop_recording")) {
                        webSocket.send("""{"type":"transcript","data":{"id":1,"is_final":true,"utterance":{"text":"Und Ende."}}}""")
                        webSocket.close(1000, null)
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = serverClosed.countDown()
            }),
        )
        try {
            val out = Recorder()
            val session = GladiaRealtimeSession(
                client = OkHttpClient(), apiKey = "gladia-test", model = "solaria-1", language = "de-DE", callbacks = out,
                initUrl = "http://${server.hostName}:${server.port}/v2/live?region=eu-west",
            )
            session.connect()
            // Spoken before the session call has come back: held, then sent.
            session.sendAudio(byteArrayOf(1, 2, 3, 4), 4)

            received.poll(5, TimeUnit.SECONDS) shouldBe ByteString.of(1, 2, 3, 4)
            session.finish()
            received.poll(5, TimeUnit.SECONDS) shouldBe """{"type":"stop_recording"}"""
            out.closed.await(5, TimeUnit.SECONDS) shouldBe true

            out.partials shouldContainExactly listOf("Hallo")
            out.finals shouldContainExactly listOf("Hallo Welt.", "Und Ende.")
            out.errors.shouldBeEmpty()

            val init = server.takeRequest()
            init.method shouldBe "POST"
            init.path.orEmpty() shouldBe "/v2/live?region=eu-west"
            init.getHeader("x-gladia-key") shouldBe "gladia-test"
            val body = init.body.readUtf8()
            body shouldContain "\"encoding\":\"wav/pcm\""
            body shouldContain "\"sample_rate\":16000"
            body shouldContain "\"languages\":[\"de\"]"
            body shouldContain "\"receive_partial_transcripts\":true"
            server.takeRequest().path.orEmpty() shouldContain "/v2/live?token=sid"
            serverClosed.await(5, TimeUnit.SECONDS) shouldBe true
        } finally {
            server.shutdown()
        }
    }

    test("without a pinned language the languages the user dictates in are offered") {
        val session = GladiaRealtimeSession(
            client = OkHttpClient(), apiKey = "k", model = "solaria-1", language = null, callbacks = Recorder(),
            expectedLanguages = listOf("de-DE", "en-US", "de-AT"),
        )
        session.initBody() shouldContain "\"languages\":[\"de\",\"en\"]"
    }

    test("a refused session call is an error with its status") {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"gladia user not found"}"""))
        server.start()
        try {
            val out = Recorder()
            GladiaRealtimeSession(
                client = OkHttpClient(), apiKey = "bad", model = "solaria-1", language = null, callbacks = out,
                initUrl = "http://${server.hostName}:${server.port}/v2/live?region=eu-west",
            ).connect()
            out.closed.await(5, TimeUnit.SECONDS) shouldBe true
            out.errors.size shouldBe 1
            out.errors[0].message.orEmpty() shouldContain "HTTP 401"
        } finally {
            server.shutdown()
        }
    }
})
