package com.sap.psr.vulas.malice;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Loops over all existing implementations of {@link MaliciousnessAnalyzer} and returns a set of all findings (positive and negative).
 */
public class MaliciousnessAnalyzerLoop {
	
	final ServiceLoader<MaliciousnessAnalyzer> loader = ServiceLoader.load(MaliciousnessAnalyzer.class);

	/**
	 * Checks whether the given {@link File} is malicious or not.
	 *
	 * @param _file a {@link java.io.File} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<MaliciousnessAnalysisResult> isMalicious(File _file) {
		final Set<MaliciousnessAnalysisResult> results = new HashSet<MaliciousnessAnalysisResult>();
		for(MaliciousnessAnalyzer a: loader) {
			results.add(a.isMalicious(_file));
		}
		return results;
	}

	/**
	 * Checks whether the given {@link File} is malicious or not.
	 *
	 * @param _is a {@link java.io.InputStream} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<MaliciousnessAnalysisResult> isMalicious(InputStream _is) {
		final Set<MaliciousnessAnalysisResult> results = new HashSet<MaliciousnessAnalysisResult>();
		for(MaliciousnessAnalyzer a: loader) {
			results.add(a.isMalicious(_is, true));
		}
		return results;
	}
}
