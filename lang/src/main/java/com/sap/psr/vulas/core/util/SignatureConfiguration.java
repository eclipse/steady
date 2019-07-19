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
    public final static String SAVE_EDIT_SCRIPTS = "vulas.core.sign.saveEditScripts";
    /** Constant <code>SAVE_EDIT_SCRIPT_INTERSECTION="vulas.core.sign.saveEditScriptIntersect"{trunked}</code> */
    public final static String SAVE_EDIT_SCRIPT_INTERSECTION = "vulas.core.sign.saveEditScriptIntersection";
    /** Constant <code>SAVE_DECOMPILED_ARCHIVE="saveDecompiledArchive"</code> */
    public final static String SAVE_DECOMPILED_ARCHIVE = "saveDecompiledArchive";
    /** Constant <code>RELAX_DECOMPILER="vulas.core.sign.relaxDecompiler"</code> */
    public final static String RELAX_DECOMPILER = "vulas.core.sign.relaxDecompiler";
    /** Constant <code>RELAX_EDIT_SCRIPT="vulas.core.sign.relaxEditScript"</code> */
    public final static String RELAX_EDIT_SCRIPT = "vulas.core.sign.relaxEditScript";
    /** Constant <code>RELAX_EQUAL_IGNORE_PARENT_ROOT="vulas.core.sign.relaxEqualIgnoreParentR"{trunked}</code> */
    public final static String RELAX_EQUAL_IGNORE_PARENT_ROOT = "vulas.core.sign.relaxEqualIgnoreParentRoot";
    /** Constant <code>SHOW_DECOMPILED_CONSTRUCT="vulas.core.sign.showDecompiledConstruct"</code> */
    public final static String SHOW_DECOMPILED_CONSTRUCT = "vulas.core.sign.showDecompiledConstruct";
    /** Constant <code>RELAX_STRIP_FINALS="vulas.core.sign.relaxStripFinals"</code> */
    public final static String RELAX_STRIP_FINALS = "vulas.core.sign.relaxStripFinals";
    /** Constant <code>RELAXED_BY_DEFAULT="vulas.core.sign.relaxRelaxedByDefault"</code> */
    public final static String RELAXED_BY_DEFAULT = "vulas.core.sign.relaxRelaxedByDefault";
}
