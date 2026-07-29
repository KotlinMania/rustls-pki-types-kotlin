package io.github.kotlinmania.rustlspkitypes

import kotlin.experimental.ExperimentalNativeApi
import kotlin.system.getTimeMillis

@OptIn(ExperimentalNativeApi::class)
actual fun currentTimeMillis(): Long = getTimeMillis()