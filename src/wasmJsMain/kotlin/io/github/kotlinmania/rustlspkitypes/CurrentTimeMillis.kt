package io.github.kotlinmania.rustlspkitypes

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
actual fun currentTimeMillis(): Long = js("Date.now()")