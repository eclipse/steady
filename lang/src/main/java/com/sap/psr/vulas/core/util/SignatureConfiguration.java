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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.core.util;

import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Wraps {@link VulasConfiguration} for accessing signature-specific configuration settings.
 */
public class SignatureConfiguration {
    /** Constant <code>SAVE_EDIT_SCRIPTS="vulas.core.sign.saveEditScripts"</code> */
    public static final String SAVE_EDIT_SCRIPTS = "vulas.core.sign.saveEditScripts";
    /** Constant <code>SAVE_EDIT_SCRIPT_INTERSECTION="vulas.core.sign.saveEditScriptIntersect"{trunked}</code> */
    public static final String SAVE_EDIT_SCRIPT_INTERSECTION =
            "vulas.core.sign.saveEditScriptIntersection";
    /** Constant <code>SAVE_DECOMPILED_ARCHIVE="saveDecompiledArchive"</code> */
    public static final String SAVE_DECOMPILED_ARCHIVE = "saveDecompiledArchive";
    /** Constant <code>RELAX_DECOMPILER="vulas.core.sign.relaxDecompiler"</code> */
    public static final String RELAX_DECOMPILER = "vulas.core.sign.relaxDecompiler";
    /** Constant <code>RELAX_EDIT_SCRIPT="vulas.core.sign.relaxEditScript"</code> */
    public static final String RELAX_EDIT_SCRIPT = "vulas.core.sign.relaxEditScript";
    /** Constant <code>RELAX_EQUAL_IGNORE_PARENT_ROOT="vulas.core.sign.relaxEqualIgnoreParentR"{trunked}</code> */
    public static final String RELAX_EQUAL_IGNORE_PARENT_ROOT =
            "vulas.core.sign.relaxEqualIgnoreParentRoot";
    /** Constant <code>SHOW_DECOMPILED_CONSTRUCT="vulas.core.sign.showDecompiledConstruct"</code> */
    public static final String SHOW_DECOMPILED_CONSTRUCT =
            "vulas.core.sign.showDecompiledConstruct";
    /** Constant <code>RELAX_STRIP_FINALS="vulas.core.sign.relaxStripFinals"</code> */
    public static final String RELAX_STRIP_FINALS = "vulas.core.sign.relaxStripFinals";
    /** Constant <code>RELAXED_BY_DEFAULT="vulas.core.sign.relaxRelaxedByDefault"</code> */
    public static final String RELAXED_BY_DEFAULT = "vulas.core.sign.relaxRelaxedByDefault";
}
