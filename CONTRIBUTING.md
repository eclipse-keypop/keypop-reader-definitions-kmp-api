# Contributing to 'Eclipse Keypop'

**Welcome to the Eclipse Keypop Community!**

We are thrilled to have you onboard and look forward to your contributions. Whether you're a coder, designer,
documenter, or enthusiast, your involvement is invaluable to us. Here's how you can start contributing to Eclipse
Keypop:

## Project Description

Eclipse Keypop provides transcriptions of the Ticketing Terminal APIs defined by the Calypso Networks Association,
in Java/JVM, C++, **Kotlin Multiplatform** and **Rust**.

The Kotlin Multiplatform and Rust transcriptions are the ones being introduced, and they are intended to **replace
the historical Java/JVM and C++ ones**: Kotlin Multiplatform addresses the JVM and iOS platforms, Rust the native
ones. New work therefore goes to those two, the Java/JVM and C++ repositories being maintained for the
applications already built on them.

This repository holds the **Kotlin Multiplatform** transcription of the *Terminal Reader Definitions API*. It is a
transcription, not a design: the specification is the reference, and this code follows it.

* Project home: https://projects.eclipse.org/projects/iot.keypop
* Developer resources: https://projects.eclipse.org/projects/iot.keypop/developer

## Quick Start

1. **Read the Contribution Guidelines**: Before starting, please visit our detailed contributing guide at the
[Eclipse Keypop Contribution Guide](https://keypop.org/community/contributing/).
This guide covers all the necessary information about contributing to the project.

2. **Sign the Eclipse Contributor Agreement**: See the [Eclipse Contributor Agreement](#eclipse-contributor-agreement)
section below.

3. **Join the mailing list**: Connect with other contributors on our
[mailing list](https://accounts.eclipse.org/mailing-list/keypop-dev/).
It's a great place to ask questions, share ideas, and collaborate.

4. **Explore Open Issues**: Check out the issues tab of this GitHub repository.

## How to Contribute

- **Code**: Submit pull requests with bug fixes, new features, and improvements. The conventions of this repository
are described in [Developing this repository](#developing-this-repository), and the build enforces them.

- **Documentation**: Help us improve our documentation by fixing errors, adding examples, or writing tutorials.

- **Feedback**: Share your experience using Eclipse Keypop. Feedback on usability, features, and your overall experience
is incredibly valuable.

- **Community Support**: Answer questions on the mailing list, help with user support, and participate in
discussions.

**Security issues must not be reported through public issues or pull requests.** Please follow the process described
in the [Security Policy](SECURITY.md).

## Developing this repository

### Building

```
./gradlew build
```

A Java 17 or later JDK is required to *run* the build; the JVM bytecode it emits targets Java 8, so that the library
can be integrated on legacy platforms. The Apple targets are cross-compiled from any host, so no macOS machine is
needed to build or publish them.

### What the build enforces

| Task | Checks |
|---|---|
| `spotlessCheck` | formatting (ktfmt) and the licence header of every source file |
| `checkJvmBytecode` | the JVM class files really target Java 8 |
| `checkSpecConsistency` | the specification version is stated consistently, and every type links to its own normative definition (see below) |
| `allTests` | the test suite of the common source set |

Run `./gradlew spotlessApply` to fix formatting rather than editing by hand.

On a host other than macOS, `allTests` **compiles** the common tests for every target but only **runs** `jvmTest`:
linking and executing an Apple test binary requires macOS, so `iosSimulatorArm64Test` and `iosX64Test` report as
`SKIPPED`. A compilation error in a test is therefore caught everywhere; a failing assertion specific to an Apple
target would not be, which is one more reason to keep this module free of platform-specific code.

### Writing documentation

**No description is duplicated from the specification.** The KDoc of a type carries a link to its normative
definition and nothing else; the specification is the reference, and it is one click away:

```kotlin
/**
 * [Normative definition -- Terminal Reader Definitions API 1.0, Card Type](https://docs.terminal-api.calypsonet.org/reader-definitions/latest-1.0/#type_CardType)
 *
 * @since 1.0.0
 */
```

Enumeration values carry only `@since`: their semantics belong to the table of the type in the specification, which
the link above reaches. Do not paraphrase a `Purpose` or a `Description` here -- a copy ages silently, since an
editorial fix to the specification ships in a patch, which changes nothing in this repository and triggers no check.

Those links target the `latest-<MAJOR>.<MINOR>` alias of the published specification, which always resolves to its
latest patch. They are never written by hand with another shape: `checkSpecConsistency` rejects any mention of a
version other than `spec.apiVersion`.

**Every public type must carry the link to its own definition**, and the check verifies it. The anchor is not a
matter of taste: the specification anchors a type as `[[type_<Name>]]`, so a type named `CardType` must link to
`#type_CardType`. Two mistakes are caught, and they are the ones that transcribing by copy produces:

- a type declared without any link -- *declares `CardType` without a link to …#type_CardType*;
- an anchor copied from a sibling -- *declares `RfTechnology` but links to the definition of `CardType`*.

This cannot be a unit test: KDoc is erased at compile time, so no `kotlin.test` can read it. The verification
therefore lives in the build, which reads the source text.

**External links open in a new tab.** Since the documentation of a type *is* a link to the specification, following
one must not replace the page the reader came for. Dokka offers no setting for this, so `openExternalLinksInNewTab`
adds `target="_blank" rel="noopener noreferrer"` to the absolute links of the generated HTML once Dokka has run.
Relative links are left alone, and the rewrite is idempotent.

### Versioning

This library and the specification are synchronised on the **MAJOR.MINOR** version only, each side advancing its own
patch level, and the library follows [Semantic Versioning](https://semver.org/).

`spec.apiVersion` in `gradle.properties` is the single source of truth. Raising it is a two-step operation:

```
# 1. edit spec.apiVersion in gradle.properties
./gradlew updateSpecVersion   # 2. rewrite every derived mention, and the version of the library
./gradlew check               # 3. prove it
```

`updateSpecVersion` rewrites the documentation links, the human-readable mentions of the API version, the `VERSION`
constant and the version of the library. `checkSpecConsistency`, which `check` depends on, is the gate: whatever the
rewrite could not reach, it names with a file and a line. `CHANGELOG.md` is deliberately left alone, its entries
describing what a past release did.

What neither task can do is follow the **content** of the specification. A new revision may reword a `Purpose`, and a
patch may do so without any version changing at all. Re-read the elements of the new revision; nothing in the build
will tell you to.

### Class diagram

One rule: **the visual theme of the specification, the notation of the implementation language.**

The theme -- colours, stereotypes, legend, layout -- is copied from `uml/class-diagram.puml` of the
`calypsonet-terminal-*-api-spec` repository, so that the two diagrams are read as one family. Do not restyle it;
when the specification changes its theme, copy it over again. The notation is the only intended difference: Kotlin
here, language-independent there.

The logo is referenced as the local `../assets/logo-icon.svg`, never as a remote URL: PlantUML inlines it, which is
what makes the SVG self-contained -- and it has to be, since it is copied into the Dokka output.

```
./gradlew renderUml
```

Edit `docs/uml/class-diagram.puml`, run that task, and **commit the resulting SVG**: reading the documentation must
never require a PlantUML installation.

### API surface

The module is compiled with `explicitApi()`: every public declaration states its visibility and its return type.
Keep all code in `commonMain` -- this API carries no platform-specific behaviour, and a platform source set would
be a design change, not an implementation detail.

## Eclipse Contributor Agreement

In order to be able to contribute to Eclipse Foundation projects you must electronically sign the Eclipse Contributor
Agreement (ECA).

* https://www.eclipse.org/legal/ECA.php

The ECA provides the Eclipse Foundation with a permanent record that you agree that each of your contributions will
comply with the commitments documented in the Developer Certificate of Origin (DCO). Having an ECA on file associated
with the email address matching the "Author" field of your contribution's Git commits fulfils the DCO's requirement
that you sign-off on your contributions.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

## Project License

This project is licensed under the MIT License (see the [LICENSE](LICENSE) file). Contributions are received under
the terms of the project license.

## Eclipse Foundation Development Process

This Eclipse Foundation open project is governed by the Eclipse Foundation Development Process and operates under the
terms of the Eclipse IP Policy.

* https://www.eclipse.org/projects/dev_process
* https://www.eclipse.org/org/documents/Eclipse_IP_Policy.pdf

This repository is subject to the Terms of Use of the Eclipse Foundation.

* https://www.eclipse.org/legal/termsofuse.php

## Code of Conduct

Please adhere to the [Eclipse Foundation Community Code of Conduct](CODE_OF_CONDUCT.md) when participating in this
project.

## Need Help?

If you have any questions or need assistance, please reach out on the
[mailing list](https://accounts.eclipse.org/mailing-list/keypop-dev/).

---

_Your contribution is the key to the success of Eclipse Keypop. Let's build something amazing together!_
