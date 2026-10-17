package io.github.kotlinmania.rustlspkitypes

import kotlin.time.Duration

/**
 * A timestamp, tracking the number of non-leap seconds since the Unix epoch.
 *
 * The Unix epoch is defined January 1, 1970 00:00:00 UTC.
 */
data class UnixTime(
    val seconds: ULong,
) {
    /**
     * Convert a number of seconds since the start of 1970 to a [UnixTime].
     */
    companion object {
        fun sinceUnixEpoch(seconds: ULong): UnixTime = UnixTime(seconds)

        fun sinceUnixEpoch(duration: Duration): UnixTime =
            UnixTime(duration.inWholeSeconds.toULong())

        /**
         * The current time, as a [UnixTime].
         */
        fun now(): UnixTime {
            val durationMs = currentTimeMillis()
            return UnixTime((durationMs / 1000).toULong())
        }
    }

    /**
     * Number of seconds since the Unix epoch.
     */
    fun asSecs(): ULong = seconds

    /**
     * Number of seconds since the Unix epoch.
     */
    fun asSeconds(): ULong = seconds
}
