/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.utils;

import java.nio.file.Path;

import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Provides configuration Strings for Patch Evaluator.
 */
public class PEConfiguration extends VulasConfiguration {
    // Configuration Settings
    /** Constant <code>BUGID="vulas.patchEval.bugId"</code> */
    public final static String BUGID = "vulas.patchEval.bugId";
    /** Constant <code>BASEFOLDER="vulas.patchEval.basefolder"</code> */
    public final static String BASEFOLDER = "vulas.patchEval.basefolder";
    /** Constant <code>LANG="vulas.patchEval.lang"</code> */
    public final static String LANG = "vulas.patchEval.lang";
    /** Constant <code>UPLOAD_RESULTS="vulas.patchEval.uploadResults"</code> */
    public final static String UPLOAD_RESULTS = "vulas.patchEval.uploadResults";
    /** Constant <code>ADD_RESULTS="vulas.patchEval.onlyAddNewResults"</code> */
    public final static String ADD_RESULTS = "vulas.patchEval.onlyAddNewResults";
   
    //Comma separated list of gavs to be analysed
    /** Constant <code>GAV="vulas.patchEval.gav"</code> */
    public final static String GAV = "vulas.patchEval.gav"; 
  //Comma separated list of ga to be analysed
    /** Constant <code>GA="vulas.patchEval.ga"</code> */
    public final static String GA = "vulas.patchEval.ga"; 
  //Comma separated list of group to be analysed
    /** Constant <code>GROUP="vulas.patchEval.g"</code> */
    public final static String GROUP = "vulas.patchEval.g"; 
    
    //Comma separated list of gavs to be ignored
    /** Constant <code>GAV_EXCLUDED="vulas.patchEval.excludedGav"</code> */
    public final static String GAV_EXCLUDED = "vulas.patchEval.excludedGav"; 
  //Comma separated list of ga to be ignored
    /** Constant <code>GA_EXCLUDED="vulas.patchEval.excludedGa"</code> */
    public final static String GA_EXCLUDED = "vulas.patchEval.excludedGa"; 
  //Comma separated list of group to be ignored
    /** Constant <code>GROUP_EXCLUDED="vulas.patchEval.excludedG"</code> */
    public final static String GROUP_EXCLUDED = "vulas.patchEval.excludedG"; 
    
    
	/**
	 * <p>getBaseFolder.</p>
	 *
	 * @return a {@link java.nio.file.Path} object.
	 */
	public final static Path getBaseFolder() {
		return VulasConfiguration.getGlobal().getDir(BASEFOLDER);
	}
}
