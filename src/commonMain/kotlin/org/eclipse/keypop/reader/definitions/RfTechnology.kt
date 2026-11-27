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
 * [Normative definition -- Terminal Reader Definitions API 1.0, RF
 * Technology](https://docs.terminal-api.calypsonet.org/reader-definitions/latest-1.0/#type_RfTechnology)
 *
 * @since 1.0.0
 */
public enum class RfTechnology {
  /** @since 1.0.0 */
  ISO_14443_AB,
  /** @since 1.0.0 */
  INNOVATRON_B_PRIME,
  /** @since 1.0.0 */
  FELICA,
  /** @since 1.0.0 */
  ISO_15693
}
