/*
 * Copyright (C) 2026 Luxx1993
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.provider

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AssemblySyncMergeTest {

    private val sync = ProviderAccount(providerId = LEGACY_ASSEMBLY_SYNC_ID, apiKey = "sync-key", transcriptionModel = "universal-3-5-pro")

    @Test
    fun `without a sync entry nothing changes`() {
        val keyring = ProviderAccounts().put(ProviderAccount(providerId = "assemblyai", apiKey = "k"))
        assertSame(keyring, keyring.foldAssemblySync(syncWasActive = false))
    }

    @Test
    fun `the key moves over and the sync entry disappears`() {
        val merged = ProviderAccounts().put(sync).foldAssemblySync(syncWasActive = false)
        assertNull(merged[LEGACY_ASSEMBLY_SYNC_ID])
        assertEquals("sync-key", merged.getOrEmpty("assemblyai").apiKey)
        assertEquals("", merged.getOrEmpty("assemblyai").transcriptionModel)
    }

    @Test
    fun `an existing AssemblyAI key stays`() {
        val keyring = ProviderAccounts().put(sync).put(ProviderAccount(providerId = "assemblyai", apiKey = "own-key"))
        assertEquals("own-key", keyring.foldAssemblySync(syncWasActive = false).getOrEmpty("assemblyai").apiKey)
    }

    @Test
    fun `an active sync entry becomes the sync model of AssemblyAI`() {
        val merged = ProviderAccounts().put(sync.copy(customBaseUrl = "https://sync.eu.assemblyai.com/")).foldAssemblySync(syncWasActive = true)
        val account = merged.getOrEmpty("assemblyai")
        assertEquals("universal-3-5-pro-sync", account.transcriptionModel)
        assertEquals("https://api.eu.assemblyai.com/", account.customBaseUrl)
    }
}
