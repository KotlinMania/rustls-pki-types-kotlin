// port-lint: source base64.rs
package io.github.kotlinmania.rustlspkitypes

/**
 * Decode base64 [input], writing the result into [output].
 *
 * [input] is treated as secret, so efforts are made to avoid leaking its value
 * through side channels, such as timing, memory accesses, and execution trace.
 *
 * The following is deemed non-secret information:
 *
 * - appearance of whitespace in [input]
 * - erroneous characters in [input], including the first illegal character
 * - the length of [input]
 * - the length of [output]
 *
 * Returns the number of bytes written to [output].
 */
internal fun decodeSecret(input: ByteArray, output: ByteArray): Base64DecodeResult =
    decode(input, output, CodePoint::decodeSecret)

/**
 * Decode base64 [input], writing the result into [output].
 *
 * [input] is treated as public information, so its value may be leaked through
 * side channels.
 *
 * Returns the number of bytes written to [output].
 */
internal fun decodePublic(input: ByteArray, output: ByteArray): Base64DecodeResult =
    decode(input, output, CodePoint::decodePublic)

/** Provide an upper limit on how much space could be required to decode a base64 encoding of [base64Len]. */
internal fun decodedLength(base64Len: Int): Int = ((base64Len + 3) / 4) * 3

private fun decode(
    input: ByteArray,
    output: ByteArray,
    decodeByte: (Int) -> CodePoint,
): Base64DecodeResult {
    var buffer = 0UL
    var used = 0
    var shift = SHIFT_INITIAL
    var padMask = 0
    var outputOffset = 0

    for (rawByte in input) {
        val byte = rawByte.toInt() and 0xff
        val codePoint = decodeByte(byte)
        if (codePoint == CodePoint.WHITESPACE) {
            continue
        }
        if (codePoint == CodePoint.INVALID) {
            return Base64DecodeResult.Err(Error.InvalidCharacter(byte))
        }
        val item: Int
        val pad: Int
        if (codePoint == CodePoint.PAD) {
            item = 0
            pad = 1
        } else {
            item = codePoint.value
            pad = 0
        }

        // We collect 8 code points, therefore 6 output bytes, into buffer. This
        // keeps this loop as tight as possible.
        if (used == 8) {
            if (padMask != 0b0000_0000) {
                return Base64DecodeResult.Err(Error.PrematurePadding)
            }
            if (outputOffset + 6 > output.size) {
                return Base64DecodeResult.Err(Error.InsufficientOutputSpace)
            }
            output[outputOffset] = (buffer shr 40).toByte()
            output[outputOffset + 1] = (buffer shr 32).toByte()
            output[outputOffset + 2] = (buffer shr 24).toByte()
            output[outputOffset + 3] = (buffer shr 16).toByte()
            output[outputOffset + 4] = (buffer shr 8).toByte()
            output[outputOffset + 5] = buffer.toByte()

            outputOffset += 6
            buffer = 0UL
            used = 0
            padMask = 0
            shift = SHIFT_INITIAL
        }

        buffer = buffer or (item.toULong() shl shift)
        shift -= 6
        padMask = padMask or (pad shl used)
        used += 1
    }

    // Reduce to the final block.
    if (used > 4) {
        if ((padMask and 0b0000_1111) != 0) {
            return Base64DecodeResult.Err(Error.PrematurePadding)
        }
        if (outputOffset + 3 > output.size) {
            return Base64DecodeResult.Err(Error.InsufficientOutputSpace)
        }
        output[outputOffset] = (buffer shr 40).toByte()
        output[outputOffset + 1] = (buffer shr 32).toByte()
        output[outputOffset + 2] = (buffer shr 24).toByte()

        buffer = buffer shl 24
        padMask = padMask ushr 4
        used -= 4
        outputOffset += 3
    }

    when {
        // No trailing bytes.
        used == 0 && padMask == 0b0000 -> {}

        // 4 trailing bytes, no padding.
        used == 4 && padMask == 0b0000 -> {
            if (outputOffset + 3 > output.size) {
                return Base64DecodeResult.Err(Error.InsufficientOutputSpace)
            }
            output[outputOffset] = (buffer shr 40).toByte()
            output[outputOffset + 1] = (buffer shr 32).toByte()
            output[outputOffset + 2] = (buffer shr 24).toByte()
            outputOffset += 3
        }

        // 4 trailing bytes with one padding character, or 3 trailing bytes.
        (used == 4 && padMask == 0b1000) || (used == 3 && padMask == 0b0000) -> {
            if (outputOffset + 2 > output.size) {
                return Base64DecodeResult.Err(Error.InsufficientOutputSpace)
            }
            output[outputOffset] = (buffer shr 40).toByte()
            output[outputOffset + 1] = (buffer shr 32).toByte()
            outputOffset += 2
        }

        // 4 trailing bytes with two padding characters, or 2 trailing bytes.
        (used == 4 && padMask == 0b1100) || (used == 2 && padMask == 0b0000) -> {
            if (outputOffset + 1 > output.size) {
                return Base64DecodeResult.Err(Error.InsufficientOutputSpace)
            }
            output[outputOffset] = (buffer shr 40).toByte()
            outputOffset += 1
        }

        // Everything else is illegal.
        else -> return Base64DecodeResult.Err(Error.InvalidTrailingPadding)
    }

    return Base64DecodeResult.Ok(outputOffset)
}

internal sealed class Base64DecodeResult {
    data class Ok(val bytesWritten: Int) : Base64DecodeResult()
    data class Err(val error: Error) : Base64DecodeResult()
}

internal sealed class Error {
    /** Given character is not valid in the base64 alphabet. */
    data class InvalidCharacter(val byte: Int) : Error()

    /** A padding character appeared outside the final block of 4 characters. */
    data object PrematurePadding : Error()

    /** The padding characters at the end of the input were invalid. */
    data object InvalidTrailingPadding : Error()

    /**
     * Not enough space in the output buffer.
     *
     * Use [decodedLength] to get an upper bound.
     */
    data object InsufficientOutputSpace : Error()
}

private data class CodePoint(val value: Int) {
    companion object {
        val WHITESPACE: CodePoint = CodePoint(0xf0)
        val PAD: CodePoint = CodePoint(0xf1)
        val INVALID: CodePoint = CodePoint(0xf2)

        /**
         * Side-channel rules:
         *
         * - code paths that produce [CodePoint] with a decoded value must not make
         *   that value observable through a side channel.
         * - other code paths, whitespace, padding or invalid, need not avoid this;
         *   these are not considered secret conditions.
         */
        fun decodeSecret(byte: Int): CodePoint {
            val isUpper = u8InRange(byte, 'A'.code, 'Z'.code)
            val isLower = u8InRange(byte, 'a'.code, 'z'.code)
            val isDigit = u8InRange(byte, '0'.code, '9'.code)
            val isPlus = u8Equals(byte, '+'.code)
            val isSlash = u8Equals(byte, '/'.code)
            val isPad = u8Equals(byte, '='.code)
            val isSpace = u8InRange(byte, '\t'.code, '\r'.code) or u8Equals(byte, ' '.code)
            val isInvalid = (isLower or isUpper or isDigit or isPlus or isSlash or isPad or isSpace) xor 0xff

            return CodePoint(
                (isUpper and u8WrappingSub(byte, 'A'.code)) or
                    (isLower and u8WrappingAdd(u8WrappingSub(byte, 'a'.code), 26)) or
                    (isDigit and u8WrappingAdd(u8WrappingSub(byte, '0'.code), 52)) or
                    (isPlus and 62) or
                    (isSlash and 63) or
                    (isSpace and WHITESPACE.value) or
                    (isPad and PAD.value) or
                    (isInvalid and INVALID.value),
            )
        }

        fun decodePublic(byte: Int): CodePoint =
            when (byte) {
                in '\t'.code..'\r'.code, ' '.code -> WHITESPACE
                '='.code -> PAD
                '+'.code -> CodePoint(62)
                '/'.code -> CodePoint(63)
                in '0'.code..'9'.code -> CodePoint(byte - '0'.code + 52)
                in 'A'.code..'Z'.code -> CodePoint(byte - 'A'.code)
                in 'a'.code..'z'.code -> CodePoint(byte - 'a'.code + 26)
                else -> INVALID
            }
    }
}

/** Returns 0xff if [a] is in [lo] through [hi]. */
private fun u8InRange(a: Int, lo: Int, hi: Int): Int {
    check(lo <= hi)
    check(hi - lo != 255)
    val shifted = u8WrappingSub(a, lo)
    return u8LessThan(shifted, u8WrappingAdd(hi - lo, 1))
}

/** Returns 0xff if [a] is less than [b], 0 otherwise. */
private fun u8LessThan(a: Int, b: Int): Int {
    val diff = (a - b) and 0xffff
    return u8Broadcast16(diff)
}

/** Returns 0xff if [a] equals [b], 0 otherwise. */
private fun u8Equals(a: Int, b: Int): Int {
    val diff = (a xor b) and 0xff
    return u8Nonzero(diff)
}

/** Returns 0xff if [x] is zero, 0 otherwise. */
private fun u8Nonzero(x: Int): Int =
    u8Broadcast8((x.inv() and u8WrappingSub(x, 1)) and 0xff)

/**
 * Broadcasts the top bit of [x].
 *
 * In other words, if the top bit of [x] is set, returns 0xff else 0x00.
 */
private fun u8Broadcast8(x: Int): Int {
    val msb = (x and 0xff) ushr 7
    return u8WrappingSub(0, msb)
}

/**
 * Broadcasts the top bit of [x].
 *
 * In other words, if the top bit of [x] is set, returns 0xff else 0x00.
 */
private fun u8Broadcast16(x: Int): Int {
    val msb = (x and 0xffff) ushr 15
    return u8WrappingSub(0, msb)
}

private fun u8WrappingSub(a: Int, b: Int): Int = (a - b) and 0xff

private fun u8WrappingAdd(a: Int, b: Int): Int = (a + b) and 0xff

private const val SHIFT_INITIAL: Int = (8 - 1) * 6
