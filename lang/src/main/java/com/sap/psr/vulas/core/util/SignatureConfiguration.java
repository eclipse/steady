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
    public final static String SAVE_EDIT_SCRIPTS = "vulas.core.sign.saveEditScripts";
    public final static String SAVE_EDIT_SCRIPT_INTERSECTION = "vulas.core.sign.saveEditScriptIntersection";
    public final static String SAVE_DECOMPILED_ARCHIVE = "saveDecompiledArchive";
    public final static String RELAX_DECOMPILER = "vulas.core.sign.relaxDecompiler";
    public final static String RELAX_EDIT_SCRIPT = "vulas.core.sign.relaxEditScript";
    public final static String RELAX_EQUAL_IGNORE_PARENT_ROOT = "vulas.core.sign.relaxEqualIgnoreParentRoot";
    public final static String SHOW_DECOMPILED_CONSTRUCT = "vulas.core.sign.showDecompiledConstruct";
    public final static String RELAX_STRIP_FINALS = "vulas.core.sign.relaxStripFinals";
    public final static String RELAXED_BY_DEFAULT = "vulas.core.sign.relaxRelaxedByDefault";
}