# Notices for 'Eclipse Keypop' Kotlin Multiplatform implementation

This content is produced and maintained by the Eclipse Keypop project.

* Project home: https://projects.eclipse.org/projects/iot.keypop

## Supported platforms

* JVM, with Java 1.8 bytecode
* Android, through the JVM artifact: no Android archive is published
* iOS: iosArm64, iosSimulatorArm64, iosX64

Native desktop platforms are out of scope and are served by dedicated Rust libraries.

## Trademarks

* Eclipse Keypop and the Eclipse Keypop project are Trademarks of the Eclipse Foundation, Inc.
* Eclipse® is a Trademark of the Eclipse Foundation, Inc.
* Eclipse Foundation is a Trademark of the Eclipse Foundation, Inc.

The `docs/assets/logo-icon.svg` file, embedded in the generated documentation, is the Eclipse Keypop
logo and is subject to the trademarks above.

## Copyright

All content is the property of the respective authors or their employers.
For more information regarding authorship of content, please consult the
listed source code repository logs.

## Declared Project Licenses

This program and the accompanying materials are made available under the terms
of the MIT License which is available at
https://opensource.org/license/mit/

SPDX-License-Identifier: MIT

## Third-party Content

Kotlin Standard Library (2.4.21)

* License: Apache License, 2.0
* Project: https://kotlinlang.org/
* Source: https://github.com/JetBrains/kotlin

Declared as a `compile` dependency of the JVM module and a `runtime` dependency of the root module;
it is the only artifact this API carries to its consumers. The build also uses PlantUML to render
the class diagram, and Dokka to generate the documentation, but neither is published nor
redistributed.
