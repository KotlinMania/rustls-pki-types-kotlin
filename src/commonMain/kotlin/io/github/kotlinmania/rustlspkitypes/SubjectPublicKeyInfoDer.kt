package io.github.kotlinmania.rustlspkitypes

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
class SubjectPublicKeyInfoDer(
    val der: Der,
) {
    fun asRef(): ByteArray = der.asRef()

    fun deref(): ByteArray = asRef()

    /**
     * Converts this SubjectPublicKeyInfo into an owned variant, copying borrowed content (if any).
     */
    fun intoOwned(): SubjectPublicKeyInfoDer = SubjectPublicKeyInfoDer(Der(der.asRef().copyOf()))

    override fun equals(other: Any?): Boolean = other is SubjectPublicKeyInfoDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = der.toString()

    companion object {
        fun from(slice: ByteArray): SubjectPublicKeyInfoDer = SubjectPublicKeyInfoDer(Der(slice))

        fun fromPem(kind: SectionKind, value: ByteArray): SubjectPublicKeyInfoDer? =
            if (kind == SectionKind.PublicKey) from(value) else null
    }
}
