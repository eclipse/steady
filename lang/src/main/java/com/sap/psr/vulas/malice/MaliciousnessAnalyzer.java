package com.sap.psr.vulas.malice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public interface MaliciousnessAnalyzer {
	
	/**
	 * Checks whether the given {@link File} is malicious or not. A value of 0 means it is not malicious, a value of 1 means that it is malicious, and every value in between expresses the confidence (or probability) of the file being malicious.
	 * 
	 * @param _file
	 * @return
	 */
	public MaliciousnessAnalysisResult isMalicious(File _file);

	/**
	 * Checks whether the given {@link File} is malicious or not. A value of 0 means it is not malicious, a value of 1 means that it is malicious, and every value in between expresses the confidence (or probability) of the file being malicious.
	 * 
	 * @param _is
	 * @return
	 */
	public MaliciousnessAnalysisResult isMalicious(InputStream _is, boolean _log);

}
