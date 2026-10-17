package io.github.kotlinmania.rustlspkitypes

/**
 * A TLS-encoded Encrypted Client Hello (ECH) configuration list (`ECHConfigList`);
 * as specified in draft-ietf-tls-esni-18 section 4.
 */
class EchConfigListBytes(
    val bytes: ByteArray,
) {
    /**
     * Converts this config into its owned variant, copying borrowed content (if any).
     */
    fun intoOwned(): EchConfigListBytes = EchConfigListBytes(bytes.copyOf())

    fun asRef(): ByteArray = bytes

    fun deref(): ByteArray = asRef()

    override fun equals(other: Any?): Boolean = other is EchConfigListBytes && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = hex(bytes)

    companion object {
        fun from(slice: ByteArray): EchConfigListBytes = EchConfigListBytes(slice)

        fun fromPem(kind: SectionKind, value: ByteArray): EchConfigListBytes? =
            if (kind == SectionKind.EchConfigList) from(value) else null

        /**
         * Convert an iterator over PEM items into an [EchConfigListBytes] and private key.
         */
        fun configAndKeyFromIter(
            iter: Iterator<Result<Pair<SectionKind, ByteArray>>>,
        ): Result<Pair<EchConfigListBytes, PrivatePkcs8KeyDer>> {
            var key: PrivatePkcs8KeyDer? = null
            var config: EchConfigListBytes? = null

            for (item in iter) {
                if (item.isFailure) return Result.failure(item.exceptionOrNull()!!)
                val (kind, data) = item.getOrThrow()
                when (kind) {
                    SectionKind.PrivateKey -> {
                        key = PrivatePkcs8KeyDer.fromPem(kind, data)
                    }

                    SectionKind.EchConfigList -> {
                        config = fromPem(kind, data)
                    }

                    else -> {
                        continue
                    }
                }

                if (key != null && config != null) {
                    return Result.success(Pair(config, key))
                }
            }

            return Result.failure(PemError.NoItemsFound)
        }
    }
}
