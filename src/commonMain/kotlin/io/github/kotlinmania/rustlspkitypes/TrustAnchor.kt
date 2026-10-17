package io.github.kotlinmania.rustlspkitypes

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
    fun toOwned(): TrustAnchor =
        TrustAnchor(
            subject = Der(subject.asRef().copyOf()),
            subjectPublicKeyInfo = Der(subjectPublicKeyInfo.asRef().copyOf()),
            nameConstraints = nameConstraints?.let { Der(it.asRef().copyOf()) },
        )
}
