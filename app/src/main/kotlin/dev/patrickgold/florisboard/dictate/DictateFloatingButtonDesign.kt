/*
 * Copyright (C) 2026 DevEmperor (Dictate)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate

/**
 * Visual style of the floating dictation button (issue #88), selectable in the floating-button settings.
 *
 * - [RING]: a compact circular bubble with a thin animated ring that encodes the state (pulse while
 *   recording, spinner while transcribing/rewording).
 * - [PILL]: a circular bubble that expands into a pill while active, showing an elapsed timer and a live
 *   waveform during recording and a labelled spinner while transcribing/rewording.
 * - [ORB]: a solid orb that glows and pulses with the voice amplitude while recording and breathes
 *   gently while transcribing/rewording.
 * - [CLOUD]: diktat's cloud, a glass sphere with coloured smoke in the chosen palette that rises with the
 *   voice; transcribing/rewording shows a thin spinner in it (see AudioReactiveCloudOrbView).
 */
enum class DictateFloatingButtonDesign {
    RING,
    PILL,
    ORB,
    CLOUD,

    /** A thinking orb: light moving inside a sphere, its temperament set by what the app is doing (#253). */
    AURORA,

    /** A sphere of dots that wires itself while idle, ripples with the voice, and twists while working (#253). */
    LATTICE;
}
