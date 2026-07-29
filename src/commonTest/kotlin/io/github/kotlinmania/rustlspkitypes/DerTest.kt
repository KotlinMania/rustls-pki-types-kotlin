// port-lint: tests lib.rs
package io.github.kotlinmania.rustlspkitypes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DerTest {
    @Test
    fun derDebug() {
        val der = Der.fromSlice(byteArrayOf(0x01, 0x02, 0x03))
        assertEquals("0x010203", der.toString())
    }

    @Test
    fun algIdDebug() {
        val algId = AlgorithmIdentifier.fromSlice(byteArrayOf(0x01, 0x02, 0x03))
        assertEquals("0x010203", algId.toString())
    }

    @Test
    fun bytesInnerEquality() {
        val ownedA = Der(byteArrayOf(1, 2, 3))
        val ownedB = Der(byteArrayOf(4, 5))
        val borrowedA = Der(byteArrayOf(1, 2, 3))
        val borrowedB = Der(byteArrayOf(99))

        // Self-equality.
        assertEquals(ownedA, ownedA)
        assertEquals(ownedB, ownedB)
        assertEquals(borrowedA, borrowedA)
        assertEquals(borrowedB, borrowedB)

        // Borrowed vs Owned equality
        assertEquals(ownedA, borrowedA)
        assertEquals(borrowedA, ownedA)

        // Owned inequality
        assertNotEquals(ownedA, ownedB)
        assertNotEquals(ownedB, ownedA)

        // Borrowed inequality
        assertNotEquals(borrowedA, borrowedB)
        assertNotEquals(borrowedB, borrowedA)

        // Borrowed vs Owned inequality
        assertNotEquals(ownedA, borrowedB)
        assertNotEquals(borrowedB, ownedA)
    }
}