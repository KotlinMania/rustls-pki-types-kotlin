package io.github.kotlinmania.rustlspkitypes

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLOCK_REALTIME
import platform.posix.clock_gettime
import platform.posix.timespec

@OptIn(ExperimentalForeignApi::class)
actual fun currentTimeMillis(): Long {
    memScoped {
        val ts = alloc<timespec>()
        clock_gettime(CLOCK_REALTIME.convert(), ts.ptr)
        return ts.tv_sec.toLong() * 1000L + ts.tv_nsec.toLong() / 1_000_000L
    }
}
