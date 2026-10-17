package io.github.kotlinmania.rustlspkitypes

/**
 * An abstract signature verification algorithm.
 *
 * One of these is needed per supported pair of public key type (identified
 * with [publicKeyAlgId]) and `signatureAlgorithm` (identified with
 * [signatureAlgId]). Note that both of these [AlgorithmIdentifier]s include
 * the parameters encoding, so separate [SignatureVerificationAlgorithm]s are needed
 * for each possible public key or signature parameters.
 */
interface SignatureVerificationAlgorithm {
    /**
     * Verify a signature.
     *
     * `publicKey` is the `subjectPublicKey` value from a `SubjectPublicKeyInfo` encoding
     * and is untrusted. The key's `subjectPublicKeyInfo` matches the [AlgorithmIdentifier]
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
 * FIPS validation status of an algorithm or implementation.
 */
sealed class FipsStatus {
    /** Not FIPS tested, or unapproved algorithm. */
    data object Unvalidated : FipsStatus()

    /** In queue for FIPS validation. */
    data object Pending : FipsStatus()

    /** FIPS certified, with named certificate. */
    data class Certified(
        val certificate: String,
    ) : FipsStatus()
}
