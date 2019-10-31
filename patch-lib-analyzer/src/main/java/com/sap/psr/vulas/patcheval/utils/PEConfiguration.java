/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.utils;

import com.sap.psr.vulas.shared.util.VulasConfiguration;
import java.nio.file.Path;

/** Provides configuration Strings for Patch Evaluator. */
public class PEConfiguration extends VulasConfiguration {
  // Configuration Settings
  /** Constant <code>BUGID="vulas.patchEval.bugId"</code> */
  public static final String BUGID = "vulas.patchEval.bugId";
  /** Constant <code>BASEFOLDER="vulas.patchEval.basefolder"</code> */
  public static final String BASEFOLDER = "vulas.patchEval.basefolder";
  /** Constant <code>LANG="vulas.patchEval.lang"</code> */
  public static final String LANG = "vulas.patchEval.lang";
  /** Constant <code>UPLOAD_RESULTS="vulas.patchEval.uploadResults"</code> */
  public static final String UPLOAD_RESULTS = "vulas.patchEval.uploadResults";
  /** Constant <code>ADD_RESULTS="vulas.patchEval.onlyAddNewResults"</code> */
  public static final String ADD_RESULTS = "vulas.patchEval.onlyAddNewResults";

  // Comma separated list of gavs to be analysed
  /** Constant <code>GAV="vulas.patchEval.gav"</code> */
  public static final String GAV = "vulas.patchEval.gav";
  // Comma separated list of ga to be analysed
  /** Constant <code>GA="vulas.patchEval.ga"</code> */
  public static final String GA = "vulas.patchEval.ga";
  // Comma separated list of group to be analysed
  /** Constant <code>GROUP="vulas.patchEval.g"</code> */
  public static final String GROUP = "vulas.patchEval.g";

  // Comma separated list of gavs to be ignored
  /** Constant <code>GAV_EXCLUDED="vulas.patchEval.excludedGav"</code> */
  public static final String GAV_EXCLUDED = "vulas.patchEval.excludedGav";
  // Comma separated list of ga to be ignored
  /** Constant <code>GA_EXCLUDED="vulas.patchEval.excludedGa"</code> */
  public static final String GA_EXCLUDED = "vulas.patchEval.excludedGa";
  // Comma separated list of group to be ignored
  /** Constant <code>GROUP_EXCLUDED="vulas.patchEval.excludedG"</code> */
  public static final String GROUP_EXCLUDED = "vulas.patchEval.excludedG";

  /**
   * getBaseFolder.
   *
   * @return a {@link java.nio.file.Path} object.
   */
  public static final Path getBaseFolder() {
    return VulasConfiguration.getGlobal().getDir(BASEFOLDER);
  }
}
