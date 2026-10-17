// port-lint: source pem.rs
package io.github.kotlinmania.rustlspkitypes

/**
 * A single recognised section in a PEM file.
 */
enum class SectionKind(
    val label: String,
) {
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
sealed class PemError(
    message: String? = null,
) : Throwable(message) {
    /** A section is missing its "END marker" line. */
    data class MissingSectionEnd(
        val endMarker: ByteArray,
    ) : PemError("missing section end marker: ${endMarker.decodeToString()}") {
        override fun equals(other: Any?): Boolean = other is MissingSectionEnd && endMarker.contentEquals(other.endMarker)

        override fun hashCode(): Int = endMarker.contentHashCode()

        override fun toString(): String = "missing section end marker: ${endMarker.decodeToString()}"
    }

    /** Syntax error found in the line that starts a new section. */
    data class IllegalSectionStart(
        val line: ByteArray,
    ) : PemError("illegal section start: ${line.decodeToString()}") {
        override fun equals(other: Any?): Boolean = other is IllegalSectionStart && line.contentEquals(other.line)

        override fun hashCode(): Int = line.contentHashCode()

        override fun toString(): String = "illegal section start: ${line.decodeToString()}"
    }

    /** Base64 decode error. */
    data class Base64Decode(
        val base64Message: String,
    ) : PemError("base64 decode error: $base64Message") {
        override fun toString(): String = "base64 decode error: $base64Message"
    }

    /** I/O errors, from APIs that accept streaming readers. */
    data class Io(
        val ioMessage: String,
    ) : PemError("I/O error: $ioMessage") {
        override fun toString(): String = "I/O error: $ioMessage"
    }

    /** No items found of desired type. */
    data object NoItemsFound : PemError("no items found") {
        override fun toString(): String = "no items found"
    }

    fun fmt(): String = message ?: toString()
}

typealias Error = PemError
typealias Item<T> = Result<T>

/**
 * Conversion from a PEM [SectionKind] and body data for tuple (SectionKind, ByteArray).
 */
fun fromPem(kind: SectionKind, der: ByteArray): Pair<SectionKind, ByteArray>? =
    Pair(kind, der)

/**
 * Filter trait for types that can be created from a specific PEM section.
 */
interface PemObjectFilter<T : Any> : PemObject {
    val kind: SectionKind

    fun from(der: ByteArray): T
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

    companion object {
        fun <T : Any> fromPemSlice(pem: ByteArray, fromPem: (SectionKind, ByteArray) -> T?): Result<T> {
            val iter = SliceIter(pem, fromPem)
            return if (iter.hasNext()) {
                iter.next()
            } else {
                Result.failure(PemError.NoItemsFound)
            }
        }

        fun <T : Any> pemSliceIter(pem: ByteArray, fromPem: (SectionKind, ByteArray) -> T?): SliceIter<T> =
            SliceIter(pem, fromPem)

        fun <T : Any> fromPemFile(fileName: String, fromPem: (SectionKind, ByteArray) -> T?): Result<T> =
            Result.failure(PemError.Io("reading PEM files is not supported directly in common code"))

        fun <T : Any> pemFileIter(fileName: String, fromPem: (SectionKind, ByteArray) -> T?): Result<ReadIter<BufRead, T>> =
            Result.failure(PemError.Io("reading PEM files is not supported directly in common code"))

        fun <R : BufRead, T : Any> fromPemReader(rd: R, fromPem: (SectionKind, ByteArray) -> T?): Result<T> {
            val iter = ReadIter(rd, fromPem)
            return if (iter.hasNext()) {
                iter.next()
            } else {
                Result.failure(PemError.NoItemsFound)
            }
        }

        fun <R : BufRead, T : Any> pemReaderIter(rd: R, fromPem: (SectionKind, ByteArray) -> T?): ReadIter<R, T> =
            ReadIter(rd, fromPem)
    }
}

/**
 * Trait for buffered reading of bytes.
 */
interface BufRead {
    fun fillBuf(): ByteArray

    fun consume(amount: Int)
}

/**
 * Extract and return all PEM sections by reading [rd].
 */
class ReadIter<R : BufRead, T : Any>(
    private val rd: R,
    private val fromPem: (SectionKind, ByteArray) -> T?,
) : Iterator<Result<T>> {
    private val line = mutableListOf<Byte>()
    private val b64Buf = mutableListOf<Byte>()
    private var done: Boolean = false

    companion object {
        fun <R : BufRead, T : Any> new(rd: R, fromPem: (SectionKind, ByteArray) -> T?): ReadIter<R, T> =
            ReadIter(rd, fromPem)
    }

    override fun hasNext(): Boolean = !done

    override fun next(): Result<T> {
        if (done) throw NoSuchElementException()
        while (true) {
            b64Buf.clear()
            val result = fromBufInner(rd, line, b64Buf)
            if (result.isFailure) {
                done = true
                return Result.failure(result.exceptionOrNull()!!)
            }
            val pair = result.getOrThrow()
            if (pair == null) {
                done = true
                throw NoSuchElementException()
            }
            val (sec, item) = pair
            val converted = fromPem(sec, item)
            if (converted != null) {
                return Result.success(converted)
            }
        }
    }
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
class SliceIter<T : Any>(
    private var current: ByteArray,
    private val fromPem: (SectionKind, ByteArray) -> T?,
) : Iterator<Result<T>> {
    private var offset: Int = 0
    private var b64Buf: ByteArray = ByteArray(0)
    private var nextItem: Result<T>? = null
    private var reachedEnd: Boolean = false

    companion object {
        fun <T : Any> new(input: ByteArray, fromPem: (SectionKind, ByteArray) -> T?): SliceIter<T> =
            SliceIter(input, fromPem)
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
        var i = offset
        while (i < current.size && current[i] != '\n'.code.toByte() && current[i] != '\r'.code.toByte()) {
            i++
        }
        val line = current.copyOfRange(offset, i)
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

    override fun hasNext(): Boolean {
        if (nextItem != null) return true
        if (reachedEnd) return false
        while (true) {
            val sectionResult = readSection()
            if (sectionResult.isFailure) {
                nextItem = Result.failure(sectionResult.exceptionOrNull()!!)
                reachedEnd = true
                return true
            }
            val pair = sectionResult.getOrThrow()
            if (pair == null) {
                reachedEnd = true
                return false
            }
            val (kind, der) = pair
            val converted = fromPem(kind, der)
            if (converted != null) {
                nextItem = Result.success(converted)
                return true
            }
        }
    }

    override fun next(): Result<T> {
        if (!hasNext()) throw NoSuchElementException()
        val item = nextItem!!
        nextItem = null
        return item
    }
}

typealias PemSliceIter = SliceIter<Pair<SectionKind, ByteArray>>

internal sealed class PemFlow {
    data class Continue(
        val section: SectionLabel?,
        val b64buf: ByteArray,
    ) : PemFlow()

    data class Break(
        val item: Pair<SectionKind, ByteArray>?,
    ) : PemFlow()
}

internal sealed class SectionLabel {
    data class Known(
        val kind: SectionKind,
    ) : SectionLabel()

    data class Unknown(
        val ty: ByteArray,
    ) : SectionLabel() {
        override fun equals(other: Any?): Boolean = other is Unknown && ty.contentEquals(other.ty)

        override fun hashCode(): Int = ty.contentHashCode()
    }

    fun asSlice(): ByteArray =
        when (this) {
            is Known -> kind.asSlice()
            is Unknown -> ty
        }

    fun asRef(): ByteArray = asSlice()

    fun isEnd(line: ByteArray): Boolean = isEndCheck(line)

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

internal fun read(
    nextLine: ByteArray?,
    section: SectionLabel?,
    b64buf: ByteArray,
): Result<PemFlow> = pemRead(nextLine, section, b64buf)

internal fun pemRead(
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

                '\n'.code.toByte(), '\r'.code.toByte(), ' '.code.toByte() -> {
                    continue
                }

                else -> {
                    break
                }
            }
        }

        if (trailer != 5) {
            return Result.failure(PemError.IllegalSectionStart(line))
        }

        val ty = line.copyOfRange(11, pos)
        return Result.success(PemFlow.Continue(SectionLabel.from(ty), b64buf))
    }

    if (section != null && section.isEnd(line)) {
        val kind =
            when (section) {
                is SectionLabel.Known -> {
                    section.kind
                }

                is SectionLabel.Unknown -> {
                    return Result.success(PemFlow.Continue(null, ByteArray(0)))
                }
            }

        var der = ByteArray(decodedLength(b64buf.size))
        val derLen =
            when (kind.secret()) {
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

    val newB64 =
        if (section != null) {
            byteArrayConcat(b64buf, line)
        } else {
            b64buf
        }

    return Result.success(PemFlow.Continue(section, newB64))
}

/**
 * Extract and decode the next supported PEM section from [rd].
 */
fun <R : BufRead> fromBuf(rd: R): Result<Pair<SectionKind, ByteArray>?> {
    val b64Buf = mutableListOf<Byte>()
    val line = mutableListOf<Byte>()
    return fromBufInner(rd, line, b64Buf)
}

fun <R : BufRead> fromBufInner(
    rd: R,
    line: MutableList<Byte>,
    b64Buf: MutableList<Byte>,
): Result<Pair<SectionKind, ByteArray>?> {
    var section: SectionLabel? = null
    while (true) {
        line.clear()
        val len = readUntilNewline(rd, line)
        val nextLine = if (len == 0) null else line.toByteArray()

        val flowResult = pemRead(nextLine, section, b64Buf.toByteArray())
        if (flowResult.isFailure) {
            return Result.failure(flowResult.exceptionOrNull()!!)
        }
        when (val f = flowResult.getOrThrow()) {
            is PemFlow.Break -> {
                return Result.success(f.item)
            }

            is PemFlow.Continue -> {
                section = f.section
                b64Buf.clear()
                for (b in f.b64buf) b64Buf.add(b)
            }
        }
    }
}

fun <R : BufRead> readUntilNewline(r: R, buf: MutableList<Byte>): Int {
    var totalRead = 0
    while (true) {
        val available = r.fillBuf()
        if (available.isEmpty()) break
        var foundIndex = -1
        for (i in available.indices) {
            val b = available[i]
            if (b == '\n'.code.toByte() || b == '\r'.code.toByte()) {
                foundIndex = i
                break
            }
        }
        if (foundIndex >= 0) {
            for (i in 0..foundIndex) {
                buf.add(available[i])
            }
            val used = foundIndex + 1
            r.consume(used)
            totalRead += used
            break
        } else {
            for (b in available) {
                buf.add(b)
            }
            val used = available.size
            r.consume(used)
            totalRead += used
        }
    }
    return totalRead
}
