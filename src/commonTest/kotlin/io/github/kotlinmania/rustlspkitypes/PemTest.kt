// port-lint: tests pem.rs
package io.github.kotlinmania.rustlspkitypes

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PemTest {
    @Test
    fun parseCertificatePem() {
        val pem = """
            -----BEGIN CERTIFICATE-----
            MIIB
            -----END CERTIFICATE-----
        """.trimIndent().encodeToByteArray()

        val iter = SliceIter.new(pem) { kind, der -> Pair(kind, der) }
        assertTrue(iter.hasNext())
        val result = iter.next()
        assertTrue(result.isSuccess)
        val (kind, der) = result.getOrThrow()
        assertEquals(SectionKind.Certificate, kind)
        assertEquals(3, der.size)
        assertFalse(iter.hasNext())
    }

    @Test
    fun parseMultipleSections() {
        val pem = """
            -----BEGIN CERTIFICATE-----
            MIIB
            -----END CERTIFICATE-----
            -----BEGIN PUBLIC KEY-----
            MIIB
            -----END PUBLIC KEY-----
        """.trimIndent().encodeToByteArray()

        val iter = SliceIter.new(pem) { kind, der -> Pair(kind, der) }
        assertTrue(iter.hasNext())
        val res1 = iter.next().getOrThrow()
        assertEquals(SectionKind.Certificate, res1.first)

        assertTrue(iter.hasNext())
        val res2 = iter.next().getOrThrow()
        assertEquals(SectionKind.PublicKey, res2.first)

        assertFalse(iter.hasNext())
    }

    @Test
    fun pemObjectFromPemSlice() {
        val pem = """
            -----BEGIN CERTIFICATE-----
            MIIB
            -----END CERTIFICATE-----
        """.trimIndent().encodeToByteArray()

        val certResult = PemObject.fromPemSlice(pem) { kind, der ->
            if (kind == SectionKind.Certificate) CertificateDer.from(der) else null
        }
        assertTrue(certResult.isSuccess)
        val cert = certResult.getOrThrow()
        assertEquals(3, cert.asRef().size)
    }

    @Test
    fun missingSectionEnd() {
        val pem = """
            -----BEGIN CERTIFICATE-----
            MIIB
        """.trimIndent().encodeToByteArray()

        val iter = SliceIter.new(pem) { kind, der -> Pair(kind, der) }
        assertTrue(iter.hasNext())
        val result = iter.next()
        assertTrue(result.isFailure)
        val err = result.exceptionOrNull()
        assertTrue(err is PemError.MissingSectionEnd)
    }

    @Test
    fun illegalSectionStart() {
        val pem = """
            -----BEGIN CERTIFICATE---
            MIIB
            -----END CERTIFICATE-----
        """.trimIndent().encodeToByteArray()

        val iter = SliceIter.new(pem) { kind, der -> Pair(kind, der) }
        assertTrue(iter.hasNext())
        val result = iter.next()
        assertTrue(result.isFailure)
        val err = result.exceptionOrNull()
        assertTrue(err is PemError.IllegalSectionStart)
    }
}
