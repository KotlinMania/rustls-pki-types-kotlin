// port-lint: source alg_id.rs
package io.github.kotlinmania.rustlspkitypes

/**
 * AlgorithmIdentifier for `id-ml-dsa-44`.
 *
 * This is:
 *
 * ```text
 * OBJECT_IDENTIFIER { 2.16.840.1.101.3.4.3.17 }
 * ```
 *
 * <https://www.ietf.org/archive/id/draft-ietf-lamps-dilithium-certificates-07.html#name-identifiers>
 */
val ML_DSA_44: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01, 0x65, 0x03, 0x04, 0x03, 0x11,
    ),
)

/**
 * AlgorithmIdentifier for `id-ml-dsa-65`.
 *
 * This is:
 *
 * ```text
 * OBJECT_IDENTIFIER { 2.16.840.1.101.3.4.3.18 }
 * ```
 *
 * <https://www.ietf.org/archive/id/draft-ietf-lamps-dilithium-certificates-07.html#name-identifiers>
 */
val ML_DSA_65: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01, 0x65, 0x03, 0x04, 0x03, 0x12,
    ),
)

/**
 * AlgorithmIdentifier for `id-ml-dsa-87`.
 *
 * This is:
 *
 * ```text
 * OBJECT_IDENTIFIER { 2.16.840.1.101.3.4.3.19 }
 * ```
 *
 * <https://www.ietf.org/archive/id/draft-ietf-lamps-dilithium-certificates-07.html#name-identifiers>
 */
val ML_DSA_87: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01, 0x65, 0x03, 0x04, 0x03, 0x13,
    ),
)

/**
 * AlgorithmIdentifier for `id-ecPublicKey` with named curve `secp256k1`.
 *
 * This is:
 *
 * ```text
 * # ecPublicKey
 * OBJECT_IDENTIFIER { 1.2.840.10045.2.1 }
 * # secp256k1
 * OBJECT_IDENTIFIER { 1.3.132.0.10 }
 * ```
 */
val ECDSA_P256K1: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x07, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x02, 0x01,
        0x06, 0x05, 0x2b, 0x81.toByte(), 0x04, 0x00, 0x0a,
    ),
)

/**
 * AlgorithmIdentifier for `id-ecPublicKey` with named curve `secp256r1`.
 *
 * This is:
 *
 * ```text
 * # ecPublicKey
 * OBJECT_IDENTIFIER { 1.2.840.10045.2.1 }
 * # secp256r1
 * OBJECT_IDENTIFIER { 1.2.840.10045.3.1.7 }
 * ```
 */
val ECDSA_P256: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x07, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x02, 0x01,
        0x06, 0x08, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x03, 0x01, 0x07,
    ),
)

/**
 * AlgorithmIdentifier for `id-ecPublicKey` with named curve `secp384r1`.
 *
 * This is:
 *
 * ```text
 * # ecPublicKey
 * OBJECT_IDENTIFIER { 1.2.840.10045.2.1 }
 * # secp384r1
 * OBJECT_IDENTIFIER { 1.3.132.0.34 }
 * ```
 */
val ECDSA_P384: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x07, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x02, 0x01,
        0x06, 0x05, 0x2b, 0x81.toByte(), 0x04, 0x00, 0x22,
    ),
)

/**
 * AlgorithmIdentifier for `id-ecPublicKey` with named curve `secp521r1`.
 *
 * This is:
 *
 * ```text
 * # ecPublicKey
 * OBJECT_IDENTIFIER { 1.2.840.10045.2.1 }
 * # secp521r1
 * OBJECT_IDENTIFIER { 1.3.132.0.35 }
 * ```
 */
val ECDSA_P521: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x07, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x02, 0x01,
        0x06, 0x05, 0x2b, 0x81.toByte(), 0x04, 0x00, 0x23,
    ),
)

/**
 * AlgorithmIdentifier for `ecdsa-with-SHA256`.
 *
 * This is:
 *
 * ```text
 * # ecdsa-with-SHA256
 * OBJECT_IDENTIFIER { 1.2.840.10045.4.3.2 }
 * ```
 */
val ECDSA_SHA256: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x08, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x04, 0x03, 0x02,
    ),
)

/**
 * AlgorithmIdentifier for `ecdsa-with-SHA384`.
 *
 * This is:
 *
 * ```text
 * # ecdsa-with-SHA384
 * OBJECT_IDENTIFIER { 1.2.840.10045.4.3.3 }
 * ```
 */
val ECDSA_SHA384: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x08, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x04, 0x03, 0x03,
    ),
)

/**
 * AlgorithmIdentifier for `ecdsa-with-SHA512`.
 *
 * This is:
 *
 * ```text
 * # ecdsa-with-SHA512
 * OBJECT_IDENTIFIER { 1.2.840.10045.4.3.4 }
 * ```
 */
val ECDSA_SHA512: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x08, 0x2a, 0x86.toByte(), 0x48.toByte(), 0xce.toByte(), 0x3d, 0x04, 0x03, 0x04,
    ),
)

/**
 * AlgorithmIdentifier for `rsaEncryption`.
 *
 * This is:
 *
 * ```text
 * # rsaEncryption
 * OBJECT_IDENTIFIER { 1.2.840.113549.1.1.1 }
 * NULL {}
 * ```
 */
val RSA_ENCRYPTION: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x01,
        0x05, 0x00,
    ),
)

/**
 * AlgorithmIdentifier for `sha256WithRSAEncryption`.
 *
 * This is:
 *
 * ```text
 * # sha256WithRSAEncryption
 * OBJECT_IDENTIFIER { 1.2.840.113549.1.1.11 }
 * NULL {}
 * ```
 */
val RSA_PKCS1_SHA256: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0b,
        0x05, 0x00,
    ),
)

/**
 * AlgorithmIdentifier for `sha384WithRSAEncryption`.
 *
 * This is:
 *
 * ```text
 * # sha384WithRSAEncryption
 * OBJECT_IDENTIFIER { 1.2.840.113549.1.1.12 }
 * NULL {}
 * ```
 */
val RSA_PKCS1_SHA384: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0c,
        0x05, 0x00,
    ),
)

/**
 * AlgorithmIdentifier for `sha512WithRSAEncryption`.
 *
 * This is:
 *
 * ```text
 * # sha512WithRSAEncryption
 * OBJECT_IDENTIFIER { 1.2.840.113549.1.1.13 }
 * NULL {}
 * ```
 */
val RSA_PKCS1_SHA512: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0d,
        0x05, 0x00,
    ),
)

/**
 * AlgorithmIdentifier for `rsassaPss` with:
 *
 * - hashAlgorithm: sha256
 * - maskGenAlgorithm: mgf1 with sha256
 * - saltLength: 32
 *
 * See <https://datatracker.ietf.org/doc/html/rfc4055#section-3.1> for
 * the meaning of the context-specific tags.
 */
val RSA_PSS_SHA256: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0a,
        0x30, 0x34,
        0xa0.toByte(), 0x0f, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48.toByte(), 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00,
        0xa1.toByte(), 0x1c, 0x30, 0x1a, 0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x08,
        0x30, 0x0d, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48.toByte(), 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00,
        0xa2.toByte(), 0x03, 0x02, 0x01, 0x20,
    ),
)

/**
 * AlgorithmIdentifier for `rsassaPss` with:
 *
 * - hashAlgorithm: sha384
 * - maskGenAlgorithm: mgf1 with sha384
 * - saltLength: 48
 *
 * See <https://datatracker.ietf.org/doc/html/rfc4055#section-3.1> for
 * the meaning of the context-specific tags.
 */
val RSA_PSS_SHA384: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0a,
        0x30, 0x34,
        0xa0.toByte(), 0x0f, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48.toByte(), 0x01, 0x65, 0x03, 0x04, 0x02, 0x02, 0x05, 0x00,
        0xa1.toByte(), 0x1c, 0x30, 0x1a, 0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x08,
        0x30, 0x0d, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48.toByte(), 0x01, 0x65, 0x03, 0x04, 0x02, 0x02, 0x05, 0x00,
        0xa2.toByte(), 0x03, 0x02, 0x01, 0x30,
    ),
)

/**
 * AlgorithmIdentifier for `rsassaPss` with:
 *
 * - hashAlgorithm: sha512
 * - maskGenAlgorithm: mgf1 with sha512
 * - saltLength: 64
 *
 * See <https://datatracker.ietf.org/doc/html/rfc4055#section-3.1> for
 * the meaning of the context-specific tags.
 */
val RSA_PSS_SHA512: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(
        0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0a,
        0x30, 0x34,
        0xa0.toByte(), 0x0f, 0x30, 0x0d, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48.toByte(), 0x01, 0x65, 0x03, 0x04, 0x02, 0x03, 0x05, 0x00,
        0xa1.toByte(), 0x1c, 0x30, 0x1a, 0x06, 0x09, 0x2a, 0x86.toByte(), 0x48.toByte(), 0x86.toByte(), 0xf7.toByte(), 0x0d, 0x01, 0x01, 0x08,
        0x30, 0x0d, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48.toByte(), 0x01, 0x65, 0x03, 0x04, 0x02, 0x03, 0x05, 0x00,
        0xa2.toByte(), 0x03, 0x02, 0x01, 0x40,
    ),
)

/**
 * AlgorithmIdentifier for `ED25519`.
 *
 * This is:
 *
 * ```text
 * # ed25519
 * OBJECT_IDENTIFIER { 1.3.101.112 }
 * ```
 */
val ED25519: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(0x06, 0x03, 0x2b, 0x65, 0x70),
)

/**
 * AlgorithmIdentifier for `ED448`.
 *
 * This is:
 *
 * ```text
 * # ed448
 * OBJECT_IDENTIFIER { 1.3.101.113 }
 * ```
 */
val ED448: AlgorithmIdentifier = AlgorithmIdentifier.fromSlice(
    byteArrayOf(0x06, 0x03, 0x2b, 0x65, 0x71),
)

/**
 * A DER encoding of the PKIX AlgorithmIdentifier type:
 *
 * ```ASN.1
 * AlgorithmIdentifier  ::=  SEQUENCE  {
 *     algorithm               OBJECT IDENTIFIER,
 *     parameters              ANY DEFINED BY algorithm OPTIONAL  }
 *                                -- contains a value of the type
 *                                -- registered for use with the
 *                                -- algorithm object identifier value
 * ```
 * (from <https://www.rfc-editor.org/rfc/rfc5280#section-4.1.1.2>)
 *
 * The outer sequence encoding is *not included*, so this is the DER encoding
 * of an OID for `algorithm` plus the `parameters` value.
 *
 * Common values for this type are provided in this file.
 */
class AlgorithmIdentifier private constructor(val bytes: ByteArray) {

    /**
     * Makes a new [AlgorithmIdentifier] from a static octet slice.
     *
     * This does not validate the contents of the slice.
     */
    companion object {
        fun fromSlice(bytes: ByteArray): AlgorithmIdentifier = AlgorithmIdentifier(bytes)
    }

    fun asRef(): ByteArray = bytes

    override fun equals(other: Any?): Boolean =
        other is AlgorithmIdentifier && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = hex(bytes)
}