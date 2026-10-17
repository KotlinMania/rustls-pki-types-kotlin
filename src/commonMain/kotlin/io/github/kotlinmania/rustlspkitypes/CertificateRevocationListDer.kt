package io.github.kotlinmania.rustlspkitypes

/**
 * A Certificate Revocation List; as specified in RFC 5280.
 *
 * Certificate revocation lists are identified in PEM context as `X509 CRL` and when stored in a
 * file usually use a `.crl` extension.
 */
class CertificateRevocationListDer(
    val der: Der,
) {
    fun asRef(): ByteArray = der.asRef()

    fun deref(): ByteArray = asRef()

    override fun equals(other: Any?): Boolean = other is CertificateRevocationListDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = der.toString()

    companion object {
        fun from(slice: ByteArray): CertificateRevocationListDer = CertificateRevocationListDer(Der(slice))

        fun fromPem(kind: SectionKind, value: ByteArray): CertificateRevocationListDer? =
            if (kind == SectionKind.Crl) from(value) else null
    }
}
