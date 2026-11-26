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
 * [Normative definition -- Terminal Reader Definitions API 1.0, Reader Definitions API
 * Properties](https://docs.terminal-api.calypsonet.org/reader-definitions/latest-1.0/#type_ReaderDefinitionsApiProperties)
 *
 * @since 1.0.0
 */
public object ReaderDefinitionsApiProperties {

  /**
   * Version of the API implemented by the conforming binding, as a `MAJOR.MINOR` dotted decimal.
   *
   * @since 1.0.0
   */
  public const val VERSION: String = "1.0"
}
