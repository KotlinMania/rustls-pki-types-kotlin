package io.github.kotlinmania.rustlspkitypes

import kotlin.time.Clock

actual fun currentTimeMillis(): Long = Clock.System.now().epochSeconds * 1000L