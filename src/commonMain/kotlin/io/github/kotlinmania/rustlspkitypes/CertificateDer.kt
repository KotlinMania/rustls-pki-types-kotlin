package io.github.kotlinmania.rustlspkitypes

/**
 * A DER-encoded X.509 certificate; as specified in RFC 5280.
 *
 * Certificates are identified in PEM context as `CERTIFICATE` and when stored in a
 * file usually use a `.pem`, `.cer` or `.crt` extension.
 */
class CertificateDer(
    val der: Der,
) {
    fun asRef(): ByteArray = der.asRef()

    fun deref(): ByteArray = asRef()

    /**
     * Converts this certificate into an owned variant, copying borrowed content (if any).
     */
    fun intoOwned(): CertificateDer = CertificateDer(Der(der.asRef().copyOf()))

    override fun equals(other: Any?): Boolean = other is CertificateDer && der == other.der

    override fun hashCode(): Int = der.hashCode()

    override fun toString(): String = der.toString()

    companion object {
        fun fromSlice(bytes: ByteArray): CertificateDer = CertificateDer(Der(bytes))

        fun from(slice: ByteArray): CertificateDer = CertificateDer(Der(slice))

        fun fromPem(kind: SectionKind, value: ByteArray): CertificateDer? =
            if (kind == SectionKind.Certificate) from(value) else null
    }
}
