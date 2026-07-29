// port-lint: source lib.rs (hex function only)
package io.github.kotlinmania.rustlspkitypes

/**
 * Format a byte array as a lowercase hex string prefixed with `0x`.
 */
internal fun hex(payload: ByteArray): String {
    val sb = StringBuilder(payload.size * 2 + 2)
    sb.append("0x")
    for (b in payload) {
        sb.append(String.format("%02x", b.toInt() and 0xff))
    }
    return sb.toString()
}