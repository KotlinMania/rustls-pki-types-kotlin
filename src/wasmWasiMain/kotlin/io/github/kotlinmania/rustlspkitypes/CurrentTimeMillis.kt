package io.github.kotlinmania.rustlspkitypes

actual fun currentTimeMillis(): Long = js("Date.now()")