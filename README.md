# rustls-pki-types-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Frustls--pki--types--kotlin-blue.svg)](https://github.com/KotlinMania/rustls-pki-types-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/rustls-pki-types-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/rustls-pki-types-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/rustls-pki-types-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/rustls-pki-types-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`rustls/pki-types`](https://github.com/rustls/pki-types).

**Original Project:** This port is based on [`rustls/pki-types`](https://github.com/rustls/pki-types). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `rustls/pki-types`

> The text below is reproduced and lightly edited from [`https://github.com/rustls/pki-types`](https://github.com/rustls/pki-types). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## rustls-pki-types

[![Build Status](https://github.com/rustls/pki-types/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/rustls/pki-types/actions/workflows/ci.yml?query=branch%3Amain)
[![Documentation](https://docs.rs/rustls-pki-types/badge.svg)](https://docs.rs/rustls-pki-types/)
[![Chat](https://img.shields.io/discord/976380008299917365?logo=discord)](https://discord.gg/MCSB76RU96)

This crate provides types for representing X.509 certificates, keys and other types as commonly
used in the rustls ecosystem. It is intended to be used by crates that need to work with such X.509
types, such as [rustls](https://crates.io/crates/rustls),
[rustls-webpki](https://crates.io/crates/rustls-webpki),
and others.

Some of these crates used to define their own trivial wrappers around DER-encoded bytes.
However, in order to avoid inconvenient dependency edges, these were all disconnected. By
using a common low-level crate of types with long-term stable API, we hope to avoid the
downsides of unnecessary dependency edges while providing interoperability between crates.

## Features

- Interoperability between different crates in the rustls ecosystem
- Long-term stable API
- No dependencies
- Support for `no_std` contexts, with optional support for `alloc`

## DER and PEM

Many of the types defined in this crate represent DER-encoded data. DER is a binary encoding of
the ASN.1 format commonly used in web PKI specifications. It is a binary encoding, so it is
relatively compact when stored in memory. However, as a binary format, it is not very easy to
work with for humans and in contexts where binary data is inconvenient. For this reason,
many tools and protocols use a ASCII-based encoding of DER, called PEM. In addition to the
base64-encoded DER, PEM objects are delimited by header and footer lines which indicate the type
of object contained in the PEM blob.

This crate's types can be created from both DER and PEM encodings.

## Creating new certificates and keys

This crate does not provide any functionality for creating new certificates or keys. However,
the [rcgen](https://docs.rs/rcgen) crate can be used to create new certificates and keys.

## Cloning private keys

This crate intentionally **does not** implement `Clone` on private key types in
order to minimize the exposure of private key data in memory.

If you want to extend the lifetime of a `PrivateKeyDer<'_>`, consider [`PrivateKeyDer::clone_key()`]. 
Alternatively  since these types are immutable, consider wrapping the `PrivateKeyDer<'_>` in a [`Rc`]
or an [`Arc`].

[`Rc`]: https://doc.rust-lang.org/std/rc/struct.Rc.html
[`Arc`]: https://doc.rust-lang.org/std/sync/struct.Arc.html
[`PrivateKeyDer::clone_key()`]: https://docs.rs/rustls-pki-types/latest/rustls_pki_types/enum.PrivateKeyDer.html#method.clone_key

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:rustls-pki-types-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`rustls/pki-types`](https://github.com/rustls/pki-types). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the pki-types authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`rustls/pki-types`](https://github.com/rustls/pki-types) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
