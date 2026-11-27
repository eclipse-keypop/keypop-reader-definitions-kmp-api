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

/**
 * [Normative definition -- Terminal Reader Definitions API 1.0, Card
 * Type](https://docs.terminal-api.calypsonet.org/reader-definitions/latest-1.0/#type_CardType)
 *
 * @since 1.0.0
 */
public enum class CardType {
  /** @since 1.0.0 */
  ISO_7816_3,
  /** @since 1.0.0 */
  ISO_14443_4,
  /** @since 1.0.0 */
  ISO_14443_3A_MIFARE_CLASSIC_1K,
  /** @since 1.0.0 */
  ISO_14443_3A_MIFARE_CLASSIC_4K,
  /** @since 1.0.0 */
  ISO_14443_3A_MIFARE_ULTRALIGHT,
  /** @since 1.0.0 */
  ISO_14443_3B_ST25_SRT512,
  /** @since 1.0.0 */
  INNOVATRON_B_PRIME,
  /** @since 1.0.0 */
  FELICA,
  /** @since 1.0.0 */
  ISO_15693,
  /** @since 1.0.0 */
  UNKNOWN
}
