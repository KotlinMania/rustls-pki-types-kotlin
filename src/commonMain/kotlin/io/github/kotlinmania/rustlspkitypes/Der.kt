package io.github.kotlinmania.rustlspkitypes

import io.github.kotlinmania.zeroize.Zeroize
import io.github.kotlinmania.zeroize.zeroize

/**
 * DER-encoded data, either owned or borrowed.
 *
 * This wrapper type is used to represent DER-encoded data as a [ByteArray] wrapper.
 */
class Der(
    val bytes: ByteArray,
) : Zeroize {
    fun asRef(): ByteArray = bytes

    fun deref(): ByteArray = asRef()

    fun intoOwned(): Der = Der(bytes.copyOf())

    override fun zeroize() {
        bytes.zeroize()
    }

    override fun equals(other: Any?): Boolean = other is Der && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = hex(bytes)

    companion object {
        fun fromSlice(der: ByteArray): Der = Der(der)

        fun from(slice: ByteArray): Der = Der(slice)
    }
}
