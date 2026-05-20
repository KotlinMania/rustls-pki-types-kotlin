// port-lint: source base64.rs
package io.github.kotlinmania.rustlspkitypes

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Base64Test {
    @Test
    fun decodeTest() {
        assertContentEquals(
            byteArrayOf(
                0x00, 0x10, 0x83.toByte(), 0x10, 0x51, 0x87.toByte(), 0x20, 0x92.toByte(),
                0x8b.toByte(), 0x30, 0xd3.toByte(), 0x8f.toByte(), 0x41, 0x14, 0x93.toByte(),
                0x51, 0x55, 0x97.toByte(), 0x61, 0x96.toByte(), 0x9b.toByte(), 0x71,
                0xd7.toByte(), 0x9f.toByte(), 0x82.toByte(), 0x18, 0xa3.toByte(), 0x92.toByte(),
                0x59, 0xa7.toByte(), 0xa2.toByte(), 0x9a.toByte(), 0xab.toByte(), 0xb2.toByte(),
                0xdb.toByte(), 0xaf.toByte(), 0xc3.toByte(), 0x1c, 0xb3.toByte(), 0xd3.toByte(),
                0x5d, 0xb7.toByte(), 0xe3.toByte(), 0x9e.toByte(), 0xbb.toByte(), 0xf3.toByte(),
                0xdf.toByte(), 0xbf.toByte(),
            ),
            decode("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"),
        )
        assertContentEquals("hello".encodeToByteArray(), decode("aGVsbG8="))
        assertContentEquals("hello world".encodeToByteArray(), decode("aGVsbG8gd29ybGQ="))
        assertContentEquals("hello world!".encodeToByteArray(), decode("aGVsbG8gd29ybGQh"))
        assertContentEquals(byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xff.toByte()), decode("////"))
        assertContentEquals(byteArrayOf(0xfb.toByte(), 0xef.toByte(), 0xbe.toByte()), decode("++++"))
        assertContentEquals(byteArrayOf(0x00, 0x00, 0x00), decode("AAAA"))
        assertContentEquals(byteArrayOf(0x00, 0x00), decode("AAA="))
        assertContentEquals(byteArrayOf(0x00), decode("AA=="))

        // Like the previous use of rust-base64, padding is not required if the
        // encoding is otherwise valid given the length.
        assertContentEquals(byteArrayOf(0x00, 0x00), decode("AAA"))
        assertContentEquals(byteArrayOf(0x00), decode("AA"))

        assertContentEquals(byteArrayOf(), decode(""))
    }

    @Test
    fun decodeErrors() {
        val buffer = ByteArray(6)

        // Illegal trailing padding.
        assertEquals(Base64DecodeResult.Err(Error.InvalidTrailingPadding), decodeBoth("A===", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidTrailingPadding), decodeBoth("====", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidTrailingPadding), decodeBoth("A==", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidTrailingPadding), decodeBoth("AA=", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidTrailingPadding), decodeBoth("A", buffer))

        // Padding before final block.
        assertEquals(Base64DecodeResult.Err(Error.PrematurePadding), decodeBoth("=AAAAA==", buffer))
        assertEquals(Base64DecodeResult.Err(Error.PrematurePadding), decodeBoth("A=AAAA==", buffer))
        assertEquals(Base64DecodeResult.Err(Error.PrematurePadding), decodeBoth("AA=AAA==", buffer))
        assertEquals(Base64DecodeResult.Err(Error.PrematurePadding), decodeBoth("AAA=AA==", buffer))

        // Illegal inputs.
        assertEquals(Base64DecodeResult.Err(Error.InvalidCharacter('%'.code)), decodeBoth("%AAA", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidCharacter('%'.code)), decodeBoth("A%AA", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidCharacter('%'.code)), decodeBoth("AA%A", buffer))
        assertEquals(Base64DecodeResult.Err(Error.InvalidCharacter('%'.code)), decodeBoth("AAA%", buffer))

        // Output sizing.
        assertEquals(Base64DecodeResult.Ok(6), decodeBoth("am9lIGJw", ByteArray(7)))
        assertEquals(Base64DecodeResult.Ok(6), decodeBoth("am9lIGJw", ByteArray(6)))
        assertEquals(Base64DecodeResult.Err(Error.InsufficientOutputSpace), decodeBoth("am9lIGJw", ByteArray(5)))
        assertEquals(Base64DecodeResult.Err(Error.InsufficientOutputSpace), decodeBoth("am9lIGJw", ByteArray(4)))
        assertEquals(Base64DecodeResult.Err(Error.InsufficientOutputSpace), decodeBoth("am9lIGJw", ByteArray(3)))

        // Output sizing is not pessimistic when padding is valid.
        assertEquals(Base64DecodeResult.Ok(2), decodeBoth("am9=", ByteArray(2)))
        assertEquals(Base64DecodeResult.Ok(1), decodeBoth("am==", ByteArray(1)))
        assertEquals(Base64DecodeResult.Ok(2), decodeBoth("am9", ByteArray(2)))
        assertEquals(Base64DecodeResult.Ok(1), decodeBoth("am", ByteArray(1)))
    }

    @Test
    fun publicAndSecretDecodersAgree() {
        for (byte in 0..255) {
            assertEquals(
                decodePublic(byteArrayOf(byte.toByte()), ByteArray(decodedLength(1))),
                decodeSecret(byteArrayOf(byte.toByte()), ByteArray(decodedLength(1))),
            )
        }
    }

    private fun decode(input: String): ByteArray {
        val bytes = input.encodeToByteArray()
        val output = ByteArray(decodedLength(bytes.size))
        val result = decodeBoth(input, output)
        require(result is Base64DecodeResult.Ok)
        return output.copyOf(result.bytesWritten)
    }

    private fun decodeBoth(input: String, output: ByteArray): Base64DecodeResult {
        val bytes = input.encodeToByteArray()
        val outputCopy = output.copyOf()
        val publicResult = decodePublic(bytes, outputCopy)
        val secretResult = decodeSecret(bytes, output)
        assertEquals(publicResult, secretResult)
        return secretResult
    }
}
