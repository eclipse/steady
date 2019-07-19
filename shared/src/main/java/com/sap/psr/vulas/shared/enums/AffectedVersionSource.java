package com.sap.psr.vulas.shared.enums;

/**
 * <p>AffectedVersionSource class.</p>
 *
 */
public enum AffectedVersionSource {
	MANUAL,
	PRE_COMMIT_POM,
	LINE_ADD,
	CHECK_VERSION ,
	AST_EQUALITY,
	MAJOR_EQUALITY,
	MINOR_EQUALITY,
	INTERSECTION,
	GREATER_RELEASE,
	TO_REVIEW,
	PROPAGATE_MANUAL;
}
