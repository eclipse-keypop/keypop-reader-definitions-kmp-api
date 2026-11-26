/* **************************************************************************************
 * Copyright (c) 2026 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * MIT License which is available at https://opensource.org/licenses/MIT
 *
 * SPDX-License-Identifier: MIT
 ************************************************************************************** */
package org.eclipse.keypop.reader.definitions

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * That [ReaderDefinitionsApiProperties.VERSION] states `spec.apiVersion` is verified by the
 * `checkSpecConsistency` Gradle task, which compares the two statements of the same datum. What the
 * build cannot see, and this test covers, is the shape of the value.
 */
internal class ReaderDefinitionsApiPropertiesTest {

  @Test
  fun version_isADottedDecimalMajorMinor() {
    assertTrue(
        Regex("""^\d+\.\d+$""").matches(ReaderDefinitionsApiProperties.VERSION),
        "VERSION must be a dotted decimal of the form MAJOR.MINOR, " +
            "was \"${ReaderDefinitionsApiProperties.VERSION}\"")
  }
}
