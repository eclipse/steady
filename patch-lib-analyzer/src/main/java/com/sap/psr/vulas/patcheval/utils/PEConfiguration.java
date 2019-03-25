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
    public final static String BUGID = "vulas.patchEval.bugId";
    public final static String BASEFOLDER = "vulas.patchEval.basefolder";
    public final static String LANG = "vulas.patchEval.lang";
    public final static String UPLOAD_RESULTS = "vulas.patchEval.uploadResults";
    public final static String ADD_RESULTS = "vulas.patchEval.onlyAddNewResults";
   
    //Comma separated list of gavs to be analysed
    public final static String GAV = "vulas.patchEval.gav"; 
  //Comma separated list of ga to be analysed
    public final static String GA = "vulas.patchEval.ga"; 
  //Comma separated list of group to be analysed
    public final static String GROUP = "vulas.patchEval.g"; 
    
    //Comma separated list of gavs to be ignored
    public final static String GAV_EXCLUDED = "vulas.patchEval.excludedGav"; 
  //Comma separated list of ga to be ignored
    public final static String GA_EXCLUDED = "vulas.patchEval.excludedGa"; 
  //Comma separated list of group to be ignored
    public final static String GROUP_EXCLUDED = "vulas.patchEval.excludedG"; 
    
    
	public final static Path getBaseFolder() {
		return VulasConfiguration.getGlobal().getDir(BASEFOLDER);
	}
}
