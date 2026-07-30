// port-lint: source pem.rs
package io.github.kotlinmania.rustlspkitypes

/**
 * A single recognised section in a PEM file.
 */
enum class SectionKind(val label: String) {
    /** A DER-encoded x509 certificate. Appears as "CERTIFICATE" in PEM files. */
    Certificate("CERTIFICATE"),

    /** A DER-encoded Subject Public Key Info; as specified in RFC 7468. Appears as "PUBLIC KEY" in PEM files. */
    PublicKey("PUBLIC KEY"),

    /** A DER-encoded plaintext RSA private key; as specified in PKCS #1/RFC 3447. Appears as "RSA PRIVATE KEY". */
    RsaPrivateKey("RSA PRIVATE KEY"),

    /** A DER-encoded plaintext private key; as specified in PKCS #8/RFC 5958. Appears as "PRIVATE KEY". */
    PrivateKey("PRIVATE KEY"),

    /** A Sec1-encoded plaintext private key; as specified in RFC 5915. Appears as "EC PRIVATE KEY". */
    EcPrivateKey("EC PRIVATE KEY"),

    /** A Certificate Revocation List; as specified in RFC 5280. Appears as "X509 CRL". */
    Crl("X509 CRL"),

    /** A Certificate Signing Request; as specified in RFC 2986. Appears as "CERTIFICATE REQUEST". */
    Csr("CERTIFICATE REQUEST"),

    /** An EchConfigList structure. Appears as "ECHCONFIG" in PEM files. */
    EchConfigList("ECHCONFIG"),
    ;

    fun secret(): Boolean = this in SECRET_KINDS

    fun asSlice(): ByteArray = label.encodeToByteArray()

    companion object {
        private val SECRET_KINDS = setOf(RsaPrivateKey, PrivateKey, EcPrivateKey)

        fun tryFrom(value: ByteArray): SectionKind? =
            when (value.decodeToString()) {
                "CERTIFICATE" -> Certificate
                "PUBLIC KEY" -> PublicKey
                "RSA PRIVATE KEY" -> RsaPrivateKey
                "PRIVATE KEY" -> PrivateKey
                "EC PRIVATE KEY" -> EcPrivateKey
                "X509 CRL" -> Crl
                "CERTIFICATE REQUEST" -> Csr
                "ECHCONFIG" -> EchConfigList
                else -> null
            }
    }
}

/**
 * Errors that may arise when parsing the contents of a PEM file.
 */
sealed class PemError : Throwable() {
    /** A section is missing its "END marker" line. */
    class MissingSectionEnd(val endMarker: ByteArray) : PemError() {
        override fun toString(): String = "missing section end marker: ${endMarker.decodeToString()}"
    }

    /** Syntax error found in the line that starts a new section. */
    class IllegalSectionStart(val line: ByteArray) : PemError() {
        override fun toString(): String = "illegal section start: ${line.decodeToString()}"
    }

    /** Base64 decode error. */
    class Base64Decode(val base64Message: String) : PemError() {
        override fun toString(): String = "base64 decode error: $base64Message"
    }

    /** No items found of desired type. */
    object NoItemsFound : PemError() {
        override fun toString(): String = "no items found"
    }
}

/**
 * Items that can be decoded from PEM data.
 */
interface PemObject {
    /**
     * Conversion from a PEM [SectionKind] and body data.
     *
     * This inspects `kind`, and if it matches this type's PEM section kind,
     * converts `der` into this type.
     */
    fun fromPem(kind: SectionKind, der: ByteArray): Any?
}

private fun byteArrayStartsWith(arr: ByteArray, prefix: ByteArray): Boolean {
    if (arr.size < prefix.size) return false
    for (i in prefix.indices) {
        if (arr[i] != prefix[i]) return false
    }
    return true
}

private fun byteArrayConcat(a: ByteArray, b: ByteArray): ByteArray {
    val result = ByteArray(a.size + b.size)
    a.copyInto(result, 0)
    b.copyInto(result, a.size)
    return result
}

/**
 * Iterator over all PEM sections in a byte slice.
 */
class PemSliceIter private constructor(
    private var current: ByteArray,
    private var offset: Int,
) {
    private var b64Buf: ByteArray = ByteArray(0)

    companion object {
        fun new(input: ByteArray): PemSliceIter = PemSliceIter(input, 0)
    }

    /**
     * Extract and decode the next PEM section.
     *
     * Returns null if there are no more sections, or a [Result] containing
     * a [Pair] of [SectionKind] and decoded bytes.
     */
    fun readSection(): Result<Pair<SectionKind, ByteArray>?> {
        b64Buf = ByteArray(0)
        var section: SectionLabel? = null

        while (true) {
            val nextLine: ByteArray? = readNextLine()

            val result = pemRead(nextLine, section, b64Buf)
            if (result.isFailure) return result.map { null }

            val flow = result.getOrThrow()
            when (flow) {
                is PemFlow.Continue -> {
                    section = flow.section
                    b64Buf = flow.b64buf
                    continue
                }
                is PemFlow.Break -> {
                    return Result.success(flow.item)
                }
            }
        }
    }

    private fun readNextLine(): ByteArray? {
        if (offset >= current.size) return null
        // Find next newline or CR
        var i = offset
        while (i < current.size && current[i] != '\n'.code.toByte() && current[i] != '\r'.code.toByte()) {
            i++
        }
        val line = current.copyOfRange(offset, i)
        // Skip the delimiter
        if (i < current.size) {
            offset = i + 1
        } else {
            offset = i
        }
        return line
    }

    /**
     * Returns the rest of the unparsed data.
     */
    fun remainder(): ByteArray = current.copyOfRange(offset, current.size)
}

private sealed class PemFlow {
    data class Continue(val section: SectionLabel?, val b64buf: ByteArray) : PemFlow()
    data class Break(val item: Pair<SectionKind, ByteArray>?) : PemFlow()
}

private sealed class SectionLabel {
    data class Known(val kind: SectionKind) : SectionLabel()
    data class Unknown(val ty: ByteArray) : SectionLabel()

    fun asSlice(): ByteArray =
        when (this) {
            is Known -> kind.asSlice()
            is Unknown -> ty
        }

    companion object {
        fun from(value: ByteArray): SectionLabel {
            val kind = SectionKind.tryFrom(value)
            return if (kind != null) Known(kind) else Unknown(value)
        }
    }
}

private fun SectionLabel.isEndCheck(line: ByteArray): Boolean {
    val prefix = "-----END ".encodeToByteArray()
    if (!byteArrayStartsWith(line, prefix)) return false
    val rest = line.copyOfRange(prefix.size, line.size)

    val ty = this.asSlice()
    if (!byteArrayStartsWith(rest, ty)) return false

    val afterTy = rest.copyOfRange(ty.size, rest.size)
    return byteArrayStartsWith(afterTy, "-----".encodeToByteArray())
}

private fun pemRead(
    nextLine: ByteArray?,
    section: SectionLabel?,
    b64buf: ByteArray,
): Result<PemFlow> {
    val line = nextLine
    if (line == null) {
        // EOF
        return if (section != null) {
            Result.failure(PemError.MissingSectionEnd(section.asSlice()))
        } else {
            Result.success(PemFlow.Break(null))
        }
    }

    if (byteArrayStartsWith(line, "-----BEGIN ".encodeToByteArray())) {
        var trailer = 0
        var pos = line.size
        for (i in line.size - 1 downTo 0) {
            val b = line[i]
            when (b) {
                '-'.code.toByte() -> {
                    trailer++
                    pos = i
                }
                '\n'.code.toByte(), '\r'.code.toByte(), ' '.code.toByte() -> continue
                else -> break
            }
        }

        if (trailer != 5) {
            return Result.failure(PemError.IllegalSectionStart(line))
        }

        val ty = line.copyOfRange(11, pos)
        return Result.success(PemFlow.Continue(SectionLabel.from(ty), b64buf))
    }

    if (section != null && section.isEndCheck(line)) {
        val kind = when (section) {
            is SectionLabel.Known -> section.kind
            is SectionLabel.Unknown -> {
                return Result.success(PemFlow.Continue(null, ByteArray(0)))
            }
        }

        var der = ByteArray(decodedLength(b64buf.size))
        val derLen = when (kind.secret()) {
            true -> decodeSecret(b64buf, der)
            false -> decodePublic(b64buf, der)
        }

        if (derLen is Base64DecodeResult.Err) {
            return Result.failure(PemError.Base64Decode(derLen.toString()))
        }

        derLen as Base64DecodeResult.Ok
        der = der.copyOfRange(0, derLen.bytesWritten)

        return Result.success(PemFlow.Break(Pair(kind, der)))
    }

    val newB64 = if (section != null) {
        byteArrayConcat(b64buf, line)
    } else {
        b64buf
    }

    return Result.success(PemFlow.Continue(section, newB64))
}