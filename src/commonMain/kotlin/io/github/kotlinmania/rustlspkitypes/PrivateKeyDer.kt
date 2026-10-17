package io.github.kotlinmania.rustlspkitypes

import io.github.kotlinmania.zeroize.Zeroize

/**
 * A DER-encoded X.509 private key, in one of several formats.
 *
 * See variant inner types for more detailed information.
 */
sealed class PrivateKeyDer : Zeroize {
    /** An RSA private key */
    data class Pkcs1(
        val key: PrivatePkcs1KeyDer,
    ) : PrivateKeyDer() {
        override fun zeroize() {
            key.zeroize()
        }
    }

    /** A Sec1 private key */
    data class Sec1(
        val key: PrivateSec1KeyDer,
    ) : PrivateKeyDer() {
        override fun zeroize() {
            key.zeroize()
        }
    }

    /** A PKCS#8 private key */
    data class Pkcs8(
        val key: PrivatePkcs8KeyDer,
    ) : PrivateKeyDer() {
        override fun zeroize() {
            key.zeroize()
        }
    }

    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivateKeyDer =
        when (this) {
            is Pkcs1 -> Pkcs1(key.cloneKey())
            is Sec1 -> Sec1(key.cloneKey())
            is Pkcs8 -> Pkcs8(key.cloneKey())
        }

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretDer(): ByteArray =
        when (this) {
            is Pkcs1 -> key.secretPkcs1Der()
            is Sec1 -> key.secretSec1Der()
            is Pkcs8 -> key.secretPkcs8Der()
        }

    companion object {
        private const val INVALID_KEY_DER_ERR: String = "unknown or invalid key format"

        private const val SHORT_FORM_LEN_MAX: Int = 128
        private const val TAG_SEQUENCE: Int = 0x30
        private const val TAG_INTEGER: Int = 0x02

        /**
         * Attempt to identify the format of a DER-encoded private key from
         * its leading bytes.
         *
         * Returns the [PrivateKeyDer] on success, or an error string on failure.
         */
        fun tryFrom(key: ByteArray): Result<PrivateKeyDer> {
            // We expect all key formats to begin with a SEQUENCE, which requires at least 2 bytes
            // in the short length encoding.
            if (key.isEmpty() || (key[0].toInt() and 0xff) != TAG_SEQUENCE || key.size < 2) {
                return Result.failure(IllegalArgumentException(INVALID_KEY_DER_ERR))
            }

            // The length of the SEQUENCE is encoded in the second byte. We must skip this many bytes.
            val skipLen: Int =
                if ((key[1].toInt() and 0xff) < SHORT_FORM_LEN_MAX) {
                    // 1 byte for SEQUENCE tag, 1 byte for short-form len
                    2
                } else {
                    // 1 byte for SEQUENCE tag, 1 byte for start of len, remaining bytes encoded in key[1].
                    2 + ((key[1].toInt() and 0xff) - SHORT_FORM_LEN_MAX)
                }

            if (skipLen >= key.size) {
                return Result.failure(IllegalArgumentException(INVALID_KEY_DER_ERR))
            }

            val keyBytes = key.copyOfRange(skipLen, key.size)

            // PKCS#8: outer SEQUENCE, version 0, then AlgorithmIdentifier SEQUENCE
            if (keyBytes.size >= 4 &&
                (keyBytes[0].toInt() and 0xff) == TAG_INTEGER &&
                (keyBytes[1].toInt() and 0xff) == 0x01 &&
                (keyBytes[3].toInt() and 0xff) == TAG_SEQUENCE
            ) {
                return Result.success(Pkcs8(PrivatePkcs8KeyDer.from(key)))
            }

            // PKCS#1: outer SEQUENCE, version 0
            if (keyBytes.size >= 3 &&
                (keyBytes[0].toInt() and 0xff) == TAG_INTEGER &&
                (keyBytes[1].toInt() and 0xff) == 0x01 &&
                (keyBytes[2].toInt() and 0xff) == 0x00
            ) {
                return Result.success(Pkcs1(PrivatePkcs1KeyDer.from(key)))
            }

            // SEC1: outer SEQUENCE, version 1
            if (keyBytes.size >= 3 &&
                (keyBytes[0].toInt() and 0xff) == TAG_INTEGER &&
                (keyBytes[1].toInt() and 0xff) == 0x01 &&
                (keyBytes[2].toInt() and 0xff) == 0x01
            ) {
                return Result.success(Sec1(PrivateSec1KeyDer.from(key)))
            }

            return Result.failure(IllegalArgumentException(INVALID_KEY_DER_ERR))
        }

        fun from(key: PrivatePkcs1KeyDer): PrivateKeyDer = Pkcs1(key)

        fun from(key: PrivateSec1KeyDer): PrivateKeyDer = Sec1(key)

        fun from(key: PrivatePkcs8KeyDer): PrivateKeyDer = Pkcs8(key)

        fun fromPem(kind: SectionKind, value: ByteArray): PrivateKeyDer? =
            when (kind) {
                SectionKind.RsaPrivateKey -> Pkcs1(PrivatePkcs1KeyDer.from(value))
                SectionKind.EcPrivateKey -> Sec1(PrivateSec1KeyDer.from(value))
                SectionKind.PrivateKey -> Pkcs8(PrivatePkcs8KeyDer.from(value))
                else -> null
            }
    }
}

/**
 * A DER-encoded plaintext RSA private key; as specified in PKCS#1/RFC 3447.
 *
 * RSA private keys are identified in PEM context as `RSA PRIVATE KEY` and when stored in a
 * file usually use a `.pem` or `.key` extension.
 */
class PrivatePkcs1KeyDer private constructor(
    val der: Der,
) : Zeroize {
    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivatePkcs1KeyDer = PrivatePkcs1KeyDer.from(der.asRef().copyOf())

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretPkcs1Der(): ByteArray = der.asRef()

    override fun zeroize() {
        der.zeroize()
    }

    override fun equals(other: Any?): Boolean = other is PrivatePkcs1KeyDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = "PrivatePkcs1KeyDer([secret key elided])"

    companion object {
        fun from(slice: ByteArray): PrivatePkcs1KeyDer = PrivatePkcs1KeyDer(Der(slice))

        fun from(slice: Der): PrivatePkcs1KeyDer = PrivatePkcs1KeyDer(slice)

        fun fromPem(kind: SectionKind, value: ByteArray): PrivatePkcs1KeyDer? =
            if (kind == SectionKind.RsaPrivateKey) from(value) else null
    }
}

/**
 * A Sec1-encoded plaintext private key; as specified in RFC 5915.
 *
 * Sec1 private keys are identified in PEM context as `EC PRIVATE KEY` and when stored in a
 * file usually use a `.pem` or `.key` extension.
 */
class PrivateSec1KeyDer private constructor(
    val der: Der,
) : Zeroize {
    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivateSec1KeyDer = PrivateSec1KeyDer.from(der.asRef().copyOf())

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretSec1Der(): ByteArray = der.asRef()

    override fun zeroize() {
        der.zeroize()
    }

    override fun equals(other: Any?): Boolean = other is PrivateSec1KeyDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = "PrivateSec1KeyDer([secret key elided])"

    companion object {
        fun from(slice: ByteArray): PrivateSec1KeyDer = PrivateSec1KeyDer(Der(slice))

        fun from(slice: Der): PrivateSec1KeyDer = PrivateSec1KeyDer(slice)

        fun fromPem(kind: SectionKind, value: ByteArray): PrivateSec1KeyDer? =
            if (kind == SectionKind.EcPrivateKey) from(value) else null
    }
}

/**
 * A DER-encoded plaintext private key; as specified in PKCS#8/RFC 5958.
 *
 * PKCS#8 private keys are identified in PEM context as `PRIVATE KEY` and when stored in a
 * file usually use a `.pem` or `.key` extension.
 */
class PrivatePkcs8KeyDer private constructor(
    val der: Der,
) : Zeroize {
    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivatePkcs8KeyDer = PrivatePkcs8KeyDer.from(der.asRef().copyOf())

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretPkcs8Der(): ByteArray = der.asRef()

    override fun zeroize() {
        der.zeroize()
    }

    override fun equals(other: Any?): Boolean = other is PrivatePkcs8KeyDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = "PrivatePkcs8KeyDer([secret key elided])"

    companion object {
        fun from(slice: ByteArray): PrivatePkcs8KeyDer = PrivatePkcs8KeyDer(Der(slice))

        fun from(slice: Der): PrivatePkcs8KeyDer = PrivatePkcs8KeyDer(slice)

        fun fromPem(kind: SectionKind, value: ByteArray): PrivatePkcs8KeyDer? =
            if (kind == SectionKind.PrivateKey) from(value) else null
    }
}
