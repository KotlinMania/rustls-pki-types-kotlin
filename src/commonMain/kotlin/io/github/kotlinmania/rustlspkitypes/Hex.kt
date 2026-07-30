// port-lint: source lib.rs
package io.github.kotlinmania.rustlspkitypes

private val HEX_DIGITS = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
)

/**
 * Format a byte array as a lowercase hex string prefixed with `0x`.
 */
internal fun hex(payload: ByteArray): String {
    val sb = StringBuilder(payload.size * 2 + 2)
    sb.append("0x")
    for (b in payload) {
        val v = b.toInt() and 0xff
        sb.append(HEX_DIGITS[v ushr 4])
        sb.append(HEX_DIGITS[v and 0x0f])
    }
    return sb.toString()
}