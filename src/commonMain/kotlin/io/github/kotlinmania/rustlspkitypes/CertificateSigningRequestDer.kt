package io.github.kotlinmania.rustlspkitypes

/**
 * A Certificate Signing Request; as specified in RFC 2986.
 *
 * Certificate signing requests are identified in PEM context as `CERTIFICATE REQUEST` and when stored in a
 * file usually use a `.csr` extension.
 */
class CertificateSigningRequestDer(
    val der: Der,
) {
    fun asRef(): ByteArray = der.asRef()

    fun deref(): ByteArray = asRef()

    override fun equals(other: Any?): Boolean = other is CertificateSigningRequestDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = der.toString()

    companion object {
        fun from(slice: ByteArray): CertificateSigningRequestDer = CertificateSigningRequestDer(Der(slice))

        fun fromPem(kind: SectionKind, value: ByteArray): CertificateSigningRequestDer? =
            if (kind == SectionKind.Csr) from(value) else null
    }
}
