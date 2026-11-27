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
internal class CardTypeTest {

  @Test
  fun declaresTheValuesOfTheSpecification() {
    assertEquals(
        listOf(
            "ISO_7816_3",
            "ISO_14443_4",
            "ISO_14443_3A_MIFARE_CLASSIC_1K",
            "ISO_14443_3A_MIFARE_CLASSIC_4K",
            "ISO_14443_3A_MIFARE_ULTRALIGHT",
            "ISO_14443_3B_ST25_SRT512",
            "INNOVATRON_B_PRIME",
            "FELICA",
            "ISO_15693",
            "UNKNOWN"),
        CardType.entries.map { it.name })
  }
}
