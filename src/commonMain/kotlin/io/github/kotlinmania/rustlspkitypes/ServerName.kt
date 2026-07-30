// port-lint: source server_name.rs
package io.github.kotlinmania.rustlspkitypes

/**
 * Encodes ways a client can know the expected name of the server.
 *
 * This currently covers knowing the DNS name of the server, but
 * will be extended in the future to supporting privacy-preserving names
 * for the server ("ECH").
 */
sealed class ServerName {
    /** The server is identified by a DNS name. The name is sent in the TLS Server Name Indication (SNI) extension. */
    data class DnsNameValue(val dnsName: DnsName) : ServerName()

    /** The server is identified by an IP address. SNI is not done. */
    data class IpAddressValue(val ipAddress: IpAddr) : ServerName()

    /**
     * Produce an owned [ServerName] from this [ServerName].
     */
    fun toOwned(): ServerName =
        when (this) {
            is DnsNameValue -> DnsNameValue(dnsName.toOwned())
            is IpAddressValue -> this
        }

    /**
     * Return the string representation of this [ServerName].
     */
    fun toStr(): String =
        when (this) {
            is DnsNameValue -> dnsName.asRef()
            is IpAddressValue -> ipAddress.toString()
        }

    companion object {
        /**
         * Attempt to make a [ServerName] from a string by parsing as a DNS name or IP address.
         */
        fun tryFrom(value: String): Result<ServerName> {
            val dnsResult = DnsName.tryFrom(value)
            if (dnsResult.isSuccess) {
                return Result.success(DnsNameValue(dnsResult.getOrThrow()))
            }
            val ipResult = IpAddr.tryFrom(value)
            if (ipResult.isSuccess) {
                return Result.success(IpAddressValue(ipResult.getOrThrow()))
            }
            return Result.failure(InvalidDnsNameError)
        }

        fun from(addr: IpAddr): ServerName = IpAddressValue(addr)

        fun from(v4: Ipv4Addr): ServerName = IpAddressValue(IpAddr.V4(v4))

        fun from(v6: Ipv6Addr): ServerName = IpAddressValue(IpAddr.V6(v6))

        fun from(dnsName: DnsName): ServerName = DnsNameValue(dnsName)
    }
}

/**
 * A type which encapsulates a string that is a syntactically valid DNS name.
 */
class DnsName private constructor(val value: String) {
    /**
     * Produce an owned [DnsName] from this [DnsName].
     */
    fun toOwned(): DnsName = DnsName(value)

    /**
     * Copy this object to produce an owned [DnsName], smashing the case to lowercase
     * in one operation.
     */
    fun toLowercaseOwned(): DnsName = DnsName(value.lowercase())

    fun asRef(): String = value

    override fun equals(other: Any?): Boolean =
        other is DnsName && value.equals(other.value, ignoreCase = true)

    override fun hashCode(): Int = value.lowercase().hashCode()

    override fun toString(): String = "DnsName(\"$value\")"

    companion object {
        fun tryFromString(s: String): Result<DnsName> =
            if (validate(s.encodeToByteArray())) {
                Result.success(DnsName(s))
            } else {
                Result.failure(InvalidDnsNameError)
            }

        fun tryFrom(s: String): Result<DnsName> = tryFromString(s)

        fun tryFrom(value: ByteArray): Result<DnsName> {
            val s = value.decodeToString()
            return if (validate(value)) {
                Result.success(DnsName(s))
            } else {
                Result.failure(InvalidDnsNameError)
            }
        }

        fun tryFromStr(s: String): Result<DnsName> = tryFromString(s)
    }
}

/**
 * The provided input could not be parsed because it is not a syntactically-valid DNS Name.
 */
object InvalidDnsNameError : Throwable("invalid dns name")

// ---- Validation state machine ----

private enum class DnsValidationState {
    Start,
    Next,
    NumericOnly,
    NextAfterNumericOnly,
    Subsequent,
    Hyphen,
}

private data class DnsValidationContext(val state: DnsValidationState, val len: Int)

private const val MAX_LABEL_LENGTH: Int = 63
private const val MAX_NAME_LENGTH: Int = 253

private fun validate(input: ByteArray): Boolean {
    var state = DnsValidationState.Start
    var len = 0

    if (input.size > MAX_NAME_LENGTH) {
        return false
    }

    var idx = 0
    while (idx < input.size) {
        val ch = input[idx].toInt() and 0xff
        state = when (state) {
            DnsValidationState.Start, DnsValidationState.Next, DnsValidationState.NextAfterNumericOnly -> {
                when (ch) {
                    '.'.code -> return false
                    in '0'.code..'9'.code -> { len = 1; DnsValidationState.NumericOnly }
                    in 'a'.code..'z'.code, in 'A'.code..'Z'.code, '_'.code -> { len = 1; DnsValidationState.Subsequent }
                    else -> return false
                }
            }
            DnsValidationState.Subsequent -> {
                when (ch) {
                    '.'.code -> DnsValidationState.Next
                    '-'.code -> { len += 1; DnsValidationState.Hyphen }
                    in 'a'.code..'z'.code, in 'A'.code..'Z'.code, '_'.code, in '0'.code..'9'.code -> { len += 1; DnsValidationState.Subsequent }
                    else -> return false
                }
            }
            DnsValidationState.NumericOnly -> {
                when (ch) {
                    '.'.code -> DnsValidationState.NextAfterNumericOnly
                    '-'.code -> { len += 1; DnsValidationState.Hyphen }
                    in 'a'.code..'z'.code, in 'A'.code..'Z'.code, '_'.code, in '0'.code..'9'.code -> { len += 1; DnsValidationState.Subsequent }
                    else -> return false
                }
            }
            DnsValidationState.Hyphen -> {
                when {
                    ch == '.'.code -> return false
                    len >= MAX_LABEL_LENGTH -> return false
                    '-'.code == ch -> { len += 1; DnsValidationState.Hyphen }
                    (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch == '_'.code || ch in '0'.code..'9'.code) -> { len += 1; DnsValidationState.Subsequent }
                    else -> return false
                }
            }
        }

        // Check label length for Subsequent/NumericOnly/Hyphen states.
        // In the upstream Rust code, the guard `if len >= MAX_LABEL_LENGTH` runs
        // before processing the next character, so a 63-char label is valid.
        // Here the check runs after incrementing len, so we use strict greater-than.
        when (state) {
            DnsValidationState.Subsequent, DnsValidationState.NumericOnly, DnsValidationState.Hyphen -> {
                if (len > MAX_LABEL_LENGTH) return false
            }
            else -> {}
        }

        idx++
    }

    return when (state) {
        DnsValidationState.Start, DnsValidationState.Hyphen,
        DnsValidationState.NumericOnly, DnsValidationState.NextAfterNumericOnly -> false
        else -> true
    }
}

// ---- IP addresses ----

/**
 * An IP address, either IPv4 or IPv6.
 */
sealed class IpAddr {
    /** An IPv4 address. */
    class V4(val addr: Ipv4Addr) : IpAddr() {
        override fun equals(other: Any?): Boolean = other is V4 && addr == other.addr
        override fun hashCode(): Int = addr.hashCode()
        override fun toString(): String = addr.toString()
    }

    /** An IPv6 address. */
    class V6(val addr: Ipv6Addr) : IpAddr() {
        override fun equals(other: Any?): Boolean = other is V6 && addr == other.addr
        override fun hashCode(): Int = addr.hashCode()
        override fun toString(): String = addr.toString()
    }

    companion object {
        fun tryFrom(value: String): Result<IpAddr> {
            val v4Result = Ipv4Addr.tryFrom(value)
            if (v4Result.isSuccess) {
                return Result.success(V4(v4Result.getOrThrow()))
            }
            return Ipv6Addr.tryFrom(value).map { V6(it) }
        }

        fun from(v4: Ipv4Addr): IpAddr = V4(v4)

        fun from(v6: Ipv6Addr): IpAddr = V6(v6)
    }

    override fun toString(): String =
        when (this) {
            is V4 -> addr.toString()
            is V6 -> addr.toString()
        }
}

/**
 * An IPv4 address.
 */
data class Ipv4Addr(val octets: ByteArray) {
    init {
        require(octets.size == 4) { "IPv4 address must have 4 octets" }
    }

    companion object {
        fun from(value: ByteArray): Ipv4Addr = Ipv4Addr(value)

        fun tryFrom(value: String): Result<Ipv4Addr> {
            if (value.length > 15) {
                return Result.failure(AddrParseError(AddrKind.Ipv4))
            }
            return Parser(value.encodeToByteArray()).parseWithIpv4({ p -> p.readIpv4Addr() }, AddrKind.Ipv4)
        }
    }

    override fun equals(other: Any?): Boolean = other is Ipv4Addr && octets.contentEquals(other.octets)

    override fun hashCode(): Int = octets.contentHashCode()

    override fun toString(): String = octets.joinToString(".") { (it.toInt() and 0xff).toString() }
}

/**
 * An IPv6 address.
 */
data class Ipv6Addr(val octets: ByteArray) {
    init {
        require(octets.size == 16) { "IPv6 address must have 16 octets" }
    }

    companion object {
        fun from(value: ByteArray): Ipv6Addr = Ipv6Addr(value)

        fun from(value: ShortArray): Ipv6Addr {
            val bytes = ByteArray(16)
            for (i in 0..7) {
                val v = (value[i].toInt() and 0xffff)
                bytes[i * 2] = ((v ushr 8) and 0xff).toByte()
                bytes[i * 2 + 1] = (v and 0xff).toByte()
            }
            return Ipv6Addr(bytes)
        }

        fun tryFrom(value: String): Result<Ipv6Addr> =
            Parser(value.encodeToByteArray()).parseWithIpv6({ p -> p.readIpv6Addr() }, AddrKind.Ipv6)
    }

    override fun equals(other: Any?): Boolean = other is Ipv6Addr && octets.contentEquals(other.octets)

    override fun hashCode(): Int = octets.contentHashCode()

    override fun toString(): String {
        val groups = IntArray(8)
        for (i in 0..7) {
            groups[i] = ((octets[i * 2].toInt() and 0xff) shl 8) or (octets[i * 2 + 1].toInt() and 0xff)
        }

        // Find the longest run of zeros for :: compression
        var bestStart = -1
        var bestLen = 0
        var curStart = -1
        var curLen = 0
        for (i in 0..7) {
            if (groups[i] == 0) {
                if (curStart < 0) curStart = i
                curLen++
                if (curLen > bestLen) {
                    bestStart = curStart
                    bestLen = curLen
                }
            } else {
                curStart = -1
                curLen = 0
            }
        }

        val sb = StringBuilder()
        var i = 0
        while (i < 8) {
            if (bestLen >= 2 && i == bestStart) {
                if (i == 0) sb.append("::") else sb.append(":")
                i += bestLen
                if (i < 8) {
                    // nothing — next group will be appended after ::
                }
            } else {
                if (i > 0 && (bestLen < 2 || i != bestStart + bestLen)) {
                    sb.append(":")
                } else if (i == 0 && bestLen >= 2 && bestStart == 0) {
                    sb.append(":")
                }
                sb.append(groups[i].toString(16))
                i++
            }
        }
        // Handle trailing :: case
        if (bestLen >= 2 && bestStart + bestLen == 8) {
            sb.append(":")
        }
        return sb.toString()
    }
}

enum class AddrKind { Ipv4, Ipv6 }

/**
 * Failure to parse an IP address.
 */
data class AddrParseError(val kind: AddrKind) : Throwable(
    when (kind) {
        AddrKind.Ipv4 -> "invalid IPv4 address syntax"
        AddrKind.Ipv6 -> "invalid IPv6 address syntax"
    },
)

// ---- Parser (adapted from Rust core library) ----

private class Parser(val input: ByteArray) {
    private var state: ByteArray = input

    fun parseWithIpv4(inner: (Parser) -> Ipv4Addr?, kind: AddrKind): Result<Ipv4Addr> {
        val result = inner(this)
        return if (result != null && state.isEmpty()) {
            Result.success(result)
        } else {
            Result.failure(AddrParseError(kind))
        }
    }

    fun parseWithIpv6(inner: (Parser) -> Ipv6Addr?, kind: AddrKind): Result<Ipv6Addr> {
        val result = inner(this)
        return if (result != null && state.isEmpty()) {
            Result.success(result)
        } else {
            Result.failure(AddrParseError(kind))
        }
    }

    private inline fun <T> readAtomically(inner: (Parser) -> T?): T? {
        val savedState = state
        val result = inner(this)
        if (result == null) {
            state = savedState
        }
        return result
    }

    private fun peekChar(): Int? = if (state.isNotEmpty()) state[0].toInt() and 0xff else null

    private fun readChar(): Int? {
        if (state.isEmpty()) return null
        val ch = state[0].toInt() and 0xff
        state = state.copyOfRange(1, state.size)
        return ch
    }

    private fun readGivenChar(target: Int): Boolean =
        readAtomically<Boolean> { p ->
            val ch = p.readChar()
            if (ch != null && ch == target) true else null
        } != null

    private inline fun <T> readSeparator(sep: Int, index: Int, inner: (Parser) -> T?): T? =
        readAtomically { p ->
            if (index > 0) {
                if (!p.readGivenChar(sep)) return@readAtomically null
            }
            inner(p)
        }

    private fun readNumber(radix: Int, maxDigits: Int?, allowZeroPrefix: Boolean): Int? =
        readAtomically { p ->
            var result = 0
            var digitCount = 0
            val hasLeadingZero = p.peekChar() == '0'.code

            while (true) {
                val digit: Int? = p.readAtomically { pp ->
                    val ch = pp.readChar()
                    if (ch != null) {
                        val d = charToDigit(ch, radix)
                        if (d >= 0) d else null
                    } else {
                        null
                    }
                }
                if (digit == null) break

                result = result * radix + digit
                digitCount++
                if (maxDigits != null && digitCount > maxDigits) {
                    return@readAtomically null
                }
            }

            if (digitCount == 0 || (!allowZeroPrefix && hasLeadingZero && digitCount > 1)) {
                null
            } else {
                result
            }
        }

    fun readIpv4Addr(): Ipv4Addr? =
        readAtomically { p ->
            val groups = IntArray(4)
            for (i in 0..3) {
                val num: Int? = p.readSeparator('.'.code, i) { pp ->
                    pp.readNumber(10, 3, false)
                }
                if (num == null || num > 255) return@readAtomically null
                groups[i] = num
            }
            Ipv4Addr(ByteArray(4) { groups[it].toByte() })
        }

    fun readIpv6Addr(): Ipv6Addr? =
        readAtomically { p ->
            val head = IntArray(8)
            val (headSize, headIpv4) = readGroups(head, p)

            if (headSize == 8) {
                return@readAtomically ipv6FromGroups(head)
            }

            // IPv4 part is not allowed before ::
            if (headIpv4) {
                return@readAtomically null
            }

            // Read `::` if previous code parsed less than 8 groups.
            if (!p.readGivenChar(':'.code)) return@readAtomically null
            if (!p.readGivenChar(':'.code)) return@readAtomically null

            // Read the back part of the address. The :: must contain at least one
            // set of zeroes, so our max length is 7.
            val tail = IntArray(7)
            val limit = 8 - (headSize + 1)
            val (tailSize, _) = readGroupsLimit(tail, p, limit)

            // Concat the head and tail of the IP address
            for (i in 0 until tailSize) {
                head[8 - tailSize + i] = tail[i]
            }
            ipv6FromGroups(head)
        }

    private fun readGroups(groups: IntArray, p: Parser): Pair<Int, Boolean> {
        val limit = groups.size
        for (i in 0 until limit) {
            // Try to read a trailing embedded IPv4 address. There must be
            // at least two groups left.
            if (i < limit - 1) {
                val ipv4: Ipv4Addr? = p.readSeparator(':'.code, i) { pp -> pp.readIpv4Addr() }
                if (ipv4 != null) {
                    val one = ipv4.octets[0].toInt() and 0xff
                    val two = ipv4.octets[1].toInt() and 0xff
                    val three = ipv4.octets[2].toInt() and 0xff
                    val four = ipv4.octets[3].toInt() and 0xff
                    groups[i] = (one shl 8) or two
                    groups[i + 1] = (three shl 8) or four
                    return Pair(i + 2, true)
                }
            }

            val group: Int? = p.readSeparator(':'.code, i) { pp -> pp.readNumber(16, 4, true) }
            if (group != null) {
                groups[i] = group
            } else {
                return Pair(i, false)
            }
        }
        return Pair(groups.size, false)
    }

    private fun readGroupsLimit(groups: IntArray, p: Parser, limit: Int): Pair<Int, Boolean> {
        for (i in 0 until limit) {
            if (i < limit - 1) {
                val ipv4: Ipv4Addr? = p.readSeparator(':'.code, i) { pp -> pp.readIpv4Addr() }
                if (ipv4 != null) {
                    val one = ipv4.octets[0].toInt() and 0xff
                    val two = ipv4.octets[1].toInt() and 0xff
                    val three = ipv4.octets[2].toInt() and 0xff
                    val four = ipv4.octets[3].toInt() and 0xff
                    groups[i] = (one shl 8) or two
                    groups[i + 1] = (three shl 8) or four
                    return Pair(i + 2, true)
                }
            }

            val group: Int? = p.readSeparator(':'.code, i) { pp -> pp.readNumber(16, 4, true) }
            if (group != null) {
                groups[i] = group
            } else {
                return Pair(i, false)
            }
        }
        return Pair(limit, false)
    }

    private fun ipv6FromGroups(groups: IntArray): Ipv6Addr {
        val bytes = ByteArray(16)
        for (i in 0..7) {
            bytes[i * 2] = ((groups[i] ushr 8) and 0xff).toByte()
            bytes[i * 2 + 1] = (groups[i] and 0xff).toByte()
        }
        return Ipv6Addr(bytes)
    }

    companion object {
        private fun charToDigit(ch: Int, radix: Int): Int {
            val digit = when (ch) {
                in '0'.code..'9'.code -> ch - '0'.code
                in 'a'.code..'f'.code -> ch - 'a'.code + 10
                in 'A'.code..'F'.code -> ch - 'A'.code + 10
                else -> return -1
            }
            return if (digit < radix) digit else -1
        }
    }
}