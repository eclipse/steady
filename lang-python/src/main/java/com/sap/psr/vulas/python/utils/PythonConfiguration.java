/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.python.utils;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Wraps {@link VulasConfiguration} for accessing core-specific configuration settings.
 */
public class PythonConfiguration {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    /** Constant <code>PY_BOM_IGNORE_PACKS="vulas.core.bom.python.ignorePacks"</code> */
    public static final String PY_BOM_IGNORE_PACKS = "vulas.core.bom.python.ignorePacks";

    /** Constant <code>PY_PIP_PATH="vulas.core.bom.python.pip"</code> */
    public static final String PY_PIP_PATH = "vulas.core.bom.python.pip";

    /** Constant <code>PY_PY_PATH="vulas.core.bom.python.python"</code> */
    public static final String PY_PY_PATH = "vulas.core.bom.python.python";
}
