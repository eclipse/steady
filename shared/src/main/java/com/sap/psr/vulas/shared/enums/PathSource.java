package com.sap.psr.vulas.shared.enums;

/**
 * Indicates how a given path has been found, e.g., through test execution ({@link PathSource#X2C}) or via
 * static call graph analysis starting from application methods (@link {@link PathSource#A2C}).
 */
public enum PathSource {
	 A2C, // From app constructs to change list elements (found by call graph analysis)
	 X2C, // From somewhere x to change list elements  (observed during test)
	 C2A, // From change list elements to app constructs (found by call graph analysis)
	 T2C; // From traces to change list elements (found by call graph analysis)
}
