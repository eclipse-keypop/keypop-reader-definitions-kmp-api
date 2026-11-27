# Keypop Reader Definitions KMP API

## Overview

This repository contains a **Kotlin Multiplatform** implementation aligned with the **Terminal Reader Definitions**
specification proposed by the [Calypso Networks Association](https://www.calypsonet.org). This foundation API defines
the RF technologies a contactless reader can activate during polling, and the card types a reader reports after
detection. It is used by the other Keypop reader APIs and by extension modules.

It is a transcription, not a design: the specification is the reference, and the documentation of each type links to
the definition that governs it rather than restating it.

## Documentation & Contribution Guide

The full documentation, including the **user guide**, **download information** and **contribution guide**, is available
on the Keypop website [keypop.org](https://keypop.org/).

The conventions of this repository are described in [CONTRIBUTING.md](CONTRIBUTING.md).

## API documentation

API KDoc is available [here](https://docs.keypop.org/keypop-reader-definitions-kmp-api).

The UML class diagram of this implementation is embedded in the home page of that documentation.

The specification this API transcribes is available
[here](https://docs.terminal-api.calypsonet.org/reader-definitions/latest-1.0/).

## About the source code

The code is built with **Gradle** and lives entirely in the `commonMain` source set: this API carries no
platform-specific behaviour.

| Platform | Artifact | Notes |
|---|---|---|
| JVM | `.jar` | compliant with **Java 1.8**, in order to address a wide range of applications, Android included |
| iOS | `.klib` | `iosArm64`, `iosSimulatorArm64`, `iosX64` |

No Android archive is published: the API carries no resource, manifest nor platform code, so an Android consumer
resolves the JVM artifact. Native desktop platforms are out of scope, being served by dedicated Rust libraries.

This library and the specification are synchronised on the **MAJOR.MINOR** version only, each side advancing its own
patch level.

## Continuous Integration

This project uses **GitHub Actions** for continuous integration. Every push and pull request triggers automated builds
and checks to ensure code quality and maintain compatibility with the defined specifications.
