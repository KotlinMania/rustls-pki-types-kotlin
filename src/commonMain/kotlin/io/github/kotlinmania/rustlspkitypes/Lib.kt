// port-lint: source lib.rs
package io.github.kotlinmania.rustlspkitypes

/**
 * DER-encoded data.
 *
 * This wrapper type is used to represent DER-encoded data. Since Kotlin
 * does not have Rust's lifetime/borrow semantics, this is an owned
 * [ByteArray] wrapper.
 */
class Der(
    bytes: ByteArray,
) {
    val bytes: ByteArray = bytes

    companion object {
        fun fromSlice(der: ByteArray): Der = Der(der)
    }

    fun asRef(): ByteArray = bytes

    fun intoOwned(): Der = Der(bytes.copyOf())

    override fun equals(other: Any?): Boolean = other is Der && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = hex(bytes)
}

/**
 * A DER-encoded X.509 private key, in one of several formats.
 *
 * See variant inner types for more detailed information.
 */
sealed class PrivateKeyDer {
    /** An RSA private key */
    data class Pkcs1(val key: PrivatePkcs1KeyDer) : PrivateKeyDer()

    /** A Sec1 private key */
    data class Sec1(val key: PrivateSec1KeyDer) : PrivateKeyDer()

    /** A PKCS#8 private key */
    data class Pkcs8(val key: PrivatePkcs8KeyDer) : PrivateKeyDer()

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
            val skipLen: Int = if ((key[1].toInt() and 0xff) < SHORT_FORM_LEN_MAX) {
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
    }
}

/**
 * A DER-encoded plaintext RSA private key; as specified in PKCS#1/RFC 3447.
 *
 * RSA private keys are identified in PEM context as `RSA PRIVATE KEY` and when stored in a
 * file usually use a `.pem` or `.key` extension.
 */
class PrivatePkcs1KeyDer private constructor(val der: Der) {
    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivatePkcs1KeyDer = PrivatePkcs1KeyDer.from(der.asRef().copyOf())

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretPkcs1Der(): ByteArray = der.asRef()

    override fun equals(other: Any?): Boolean = other is PrivatePkcs1KeyDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = "PrivatePkcs1KeyDer([secret key elided])"

    companion object {
        fun from(slice: ByteArray): PrivatePkcs1KeyDer = PrivatePkcs1KeyDer(Der(slice))

        fun from(slice: Der): PrivatePkcs1KeyDer = PrivatePkcs1KeyDer(slice)
    }
}

/**
 * A Sec1-encoded plaintext private key; as specified in RFC 5915.
 *
 * Sec1 private keys are identified in PEM context as `EC PRIVATE KEY` and when stored in a
 * file usually use a `.pem` or `.key` extension.
 */
class PrivateSec1KeyDer private constructor(val der: Der) {
    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivateSec1KeyDer = PrivateSec1KeyDer.from(der.asRef().copyOf())

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretSec1Der(): ByteArray = der.asRef()

    override fun equals(other: Any?): Boolean = other is PrivateSec1KeyDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = "PrivateSec1KeyDer([secret key elided])"

    companion object {
        fun from(slice: ByteArray): PrivateSec1KeyDer = PrivateSec1KeyDer(Der(slice))

        fun from(slice: Der): PrivateSec1KeyDer = PrivateSec1KeyDer(slice)
    }
}

/**
 * A DER-encoded plaintext private key; as specified in PKCS#8/RFC 5958.
 *
 * PKCS#8 private keys are identified in PEM context as `PRIVATE KEY` and when stored in a
 * file usually use a `.pem` or `.key` extension.
 */
class PrivatePkcs8KeyDer private constructor(val der: Der) {
    /**
     * Clone the private key to an owned value.
     */
    fun cloneKey(): PrivatePkcs8KeyDer = PrivatePkcs8KeyDer.from(der.asRef().copyOf())

    /**
     * Yield the DER-encoded bytes of the private key.
     */
    fun secretPkcs8Der(): ByteArray = der.asRef()

    override fun equals(other: Any?): Boolean = other is PrivatePkcs8KeyDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = "PrivatePkcs8KeyDer([secret key elided])"

    companion object {
        fun from(slice: ByteArray): PrivatePkcs8KeyDer = PrivatePkcs8KeyDer(Der(slice))

        fun from(slice: Der): PrivatePkcs8KeyDer = PrivatePkcs8KeyDer(slice)
    }
}

/**
 * A trust anchor (a.k.a. root CA).
 *
 * Traditionally, certificate verification libraries have represented trust anchors as full X.509
 * root certificates. However, those certificates contain a lot more data than is needed for
 * verifying certificates. This representation allows an application to store
 * just the essential elements of trust anchors.
 */
data class TrustAnchor(
    /** Value of the `subject` field of the trust anchor */
    val subject: Der,
    /** Value of the `subjectPublicKeyInfo` field of the trust anchor */
    val subjectPublicKeyInfo: Der,
    /** Value of DER-encoded `NameConstraints`, containing name constraints to the trust anchor, if any */
    val nameConstraints: Der? = null,
) {
    /**
     * Yield an owned copy of this [TrustAnchor].
     */
    fun toOwned(): TrustAnchor = TrustAnchor(
        subject = Der(subject.asRef().copyOf()),
        subjectPublicKeyInfo = Der(subjectPublicKeyInfo.asRef().copyOf()),
        nameConstraints = nameConstraints?.let { Der(it.asRef().copyOf()) },
    )
}

/**
 * A Certificate Revocation List; as specified in RFC 5280.
 *
 * Certificate revocation lists are identified in PEM context as `X509 CRL` and when stored in a
 * file usually use a `.crl` extension.
 */
class CertificateRevocationListDer(val der: Der) {
    fun asRef(): ByteArray = der.asRef()

    override fun equals(other: Any?): Boolean = other is CertificateRevocationListDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    companion object {
        fun from(slice: ByteArray): CertificateRevocationListDer = CertificateRevocationListDer(Der(slice))
    }
}

/**
 * A Certificate Signing Request; as specified in RFC 2986.
 *
 * Certificate signing requests are identified in PEM context as `CERTIFICATE REQUEST` and when stored in a
 * file usually use a `.csr` extension.
 */
class CertificateSigningRequestDer(val der: Der) {
    fun asRef(): ByteArray = der.asRef()

    override fun equals(other: Any?): Boolean = other is CertificateSigningRequestDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    companion object {
        fun from(slice: ByteArray): CertificateSigningRequestDer = CertificateSigningRequestDer(Der(slice))
    }
}

/**
 * A DER-encoded X.509 certificate; as specified in RFC 5280.
 *
 * Certificates are identified in PEM context as `CERTIFICATE` and when stored in a
 * file usually use a `.pem`, `.cer` or `.crt` extension.
 */
class CertificateDer(val der: Der) {
    fun asRef(): ByteArray = der.asRef()

    /**
     * Converts this certificate into an owned variant, copying borrowed content (if any).
     */
    fun intoOwned(): CertificateDer = CertificateDer(Der(der.asRef().copyOf()))

    override fun equals(other: Any?): Boolean = other is CertificateDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    companion object {
        fun fromSlice(bytes: ByteArray): CertificateDer = CertificateDer(Der(bytes))

        fun from(slice: ByteArray): CertificateDer = CertificateDer(Der(slice))
    }
}

/**
 * A DER-encoded SubjectPublicKeyInfo (SPKI), as specified in RFC 5280.
 *
 * Public keys are identified in PEM context as a `PUBLIC KEY`.
 */
@Deprecated("Prefer SubjectPublicKeyInfoDer instead", ReplaceWith("SubjectPublicKeyInfoDer"))
typealias SubjectPublicKeyInfo = SubjectPublicKeyInfoDer

/**
 * A DER-encoded SubjectPublicKeyInfo (SPKI), as specified in RFC 5280.
 *
 * Public keys are identified in PEM context as a `PUBLIC KEY`.
 */
class SubjectPublicKeyInfoDer(val der: Der) {
    fun asRef(): ByteArray = der.asRef()

    /**
     * Converts this SubjectPublicKeyInfo into an owned variant, copying borrowed content (if any).
     */
    fun intoOwned(): SubjectPublicKeyInfoDer = SubjectPublicKeyInfoDer(Der(der.asRef().copyOf()))

    override fun equals(other: Any?): Boolean = other is SubjectPublicKeyInfoDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    companion object {
        fun from(slice: ByteArray): SubjectPublicKeyInfoDer = SubjectPublicKeyInfoDer(Der(slice))
    }
}

/**
 * A TLS-encoded Encrypted Client Hello (ECH) configuration list (`ECHConfigList`);
 * as specified in draft-ietf-tls-esni-18 section 4.
 */
class EchConfigListBytes(val bytes: ByteArray) {
    /**
     * Converts this config into its owned variant, copying borrowed content (if any).
     */
    fun intoOwned(): EchConfigListBytes = EchConfigListBytes(bytes.copyOf())

    fun asRef(): ByteArray = bytes

    override fun equals(other: Any?): Boolean = other is EchConfigListBytes && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = hex(bytes)

    companion object {
        fun from(slice: ByteArray): EchConfigListBytes = EchConfigListBytes(slice)
    }
}

/**
 * An abstract signature verification algorithm.
 *
 * One of these is needed per supported pair of public key type (identified
 * with [publicKeyAlgId]) and `signatureAlgorithm` (identified with
 * [signatureAlgId]).  Note that both of these [AlgorithmIdentifier]s include
 * the parameters encoding, so separate [SignatureVerificationAlgorithm]s are needed
 * for each possible public key or signature parameters.
 */
interface SignatureVerificationAlgorithm {
    /**
     * Verify a signature.
     *
     * `publicKey` is the `subjectPublicKey` value from a `SubjectPublicKeyInfo` encoding
     * and is untrusted.  The key's `subjectPublicKeyInfo` matches the [AlgorithmIdentifier]
     * returned by [publicKeyAlgId].
     *
     * `message` is the data over which the signature was allegedly computed.
     * It is not hashed; implementations of this function must do hashing
     * if that is required by the algorithm they implement.
     *
     * `signature` is the signature allegedly over `message`.
     *
     * @return null on success, or [InvalidSignature] if the signature is invalid.
     */
    fun verifySignature(
        publicKey: ByteArray,
        message: ByteArray,
        signature: ByteArray,
    ): InvalidSignature?

    /**
     * Return the [AlgorithmIdentifier] that must equal a public key's
     * `subjectPublicKeyInfo` value for this [SignatureVerificationAlgorithm]
     * to be used for signature verification.
     */
    fun publicKeyAlgId(): AlgorithmIdentifier

    /**
     * Return the [AlgorithmIdentifier] that must equal the `signatureAlgorithm` value
     * on the data to be verified for this [SignatureVerificationAlgorithm] to be used
     * for signature verification.
     */
    fun signatureAlgId(): AlgorithmIdentifier

    /**
     * Return the FIPS status of this algorithm or implementation.
     */
    fun fipsStatus(): FipsStatus =
        if (fips()) FipsStatus.Pending else FipsStatus.Unvalidated

    /**
     * Return true if this is backed by a FIPS-approved implementation.
     */
    fun fips(): Boolean = false
}

/**
 * A detail-less error when a signature is not valid.
 */
object InvalidSignature

/**
 * A timestamp, tracking the number of non-leap seconds since the Unix epoch.
 *
 * The Unix epoch is defined January 1, 1970 00:00:00 UTC.
 */
data class UnixTime(val seconds: ULong) {
    /**
     * Convert a number of seconds since the start of 1970 to a [UnixTime].
     */
    companion object {
        fun sinceUnixEpoch(seconds: ULong): UnixTime = UnixTime(seconds)

        /**
         * The current time, as a [UnixTime].
         */
        fun now(): UnixTime {
            val durationMs = currentTimeMillis()
            return UnixTime((durationMs / 1000).toULong())
        }
    }

    /**
     * Number of seconds since the Unix epoch.
     */
    fun asSeconds(): ULong = seconds
}

/**
 * FIPS validation status of an algorithm or implementation.
 */
sealed class FipsStatus {
    /** Not FIPS tested, or unapproved algorithm. */
    data object Unvalidated : FipsStatus()

    /** In queue for FIPS validation. */
    data object Pending : FipsStatus()

    /** FIPS certified, with named certificate. */
    data class Certified(val certificate: String) : FipsStatus()
}