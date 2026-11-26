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
import kotlin.test.assertEquals

/**
 * The values of the enumeration, and their order, are part of the contract: a consumer may switch
 * on them, and the specification lists them in this order.
 */
internal class RfTechnologyTest {

  @Test
  fun declaresTheValuesOfTheSpecification() {
    assertEquals(
        listOf("ISO_14443_AB", "INNOVATRON_B_PRIME", "FELICA", "ISO_15693"),
        RfTechnology.entries.map { it.name })
  }
}
