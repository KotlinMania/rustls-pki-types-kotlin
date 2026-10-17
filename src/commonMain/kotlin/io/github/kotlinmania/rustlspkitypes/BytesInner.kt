package io.github.kotlinmania.rustlspkitypes

import io.github.kotlinmania.zeroize.Zeroize
import io.github.kotlinmania.zeroize.zeroize

sealed class BytesInner : Zeroize {
    class Owned(
        val bytes: ByteArray,
    ) : BytesInner() {
        override fun asRef(): ByteArray = bytes

        override fun intoOwned(): Owned = Owned(bytes.copyOf())

        override fun zeroize() {
            bytes.zeroize()
        }
    }

    class Borrowed(
        val bytes: ByteArray,
    ) : BytesInner() {
        override fun asRef(): ByteArray = bytes

        override fun intoOwned(): Owned = Owned(bytes.copyOf())

        override fun zeroize() {
            // Borrowed bytes do not zeroize in upstream Rust
        }
    }

    abstract fun asRef(): ByteArray

    abstract fun intoOwned(): Owned

    override fun equals(other: Any?): Boolean =
        other is BytesInner && asRef().contentEquals(other.asRef())

    override fun hashCode(): Int = asRef().contentHashCode()

    override fun toString(): String = hex(asRef())
}
