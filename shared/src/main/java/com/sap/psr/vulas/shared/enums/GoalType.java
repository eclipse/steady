package com.sap.psr.vulas.shared.enums;

import com.sap.psr.vulas.shared.json.model.Application;

/**
 * Goal types for a given {@link Application} or workspace.
 */
public enum GoalType {

	// Application goals
	CLEAN,
	APP,
	CHECKVER, // Deprecated, related coding requires refactoring/update
	A2C,
	TEST,
	INSTR,
	T2C,
	REPORT,
	UPLOAD,

	// Workspace goals
	SPACENEW,
	SPACEMOD,
	SPACEDEL, // Available in CLI (but not documented), not exposed as Maven plugin goal
	SPACECLEAN,

	// Sequence of zero, one or more single goals
	SEQUENCE;

	/**
	 * <p>parseGoal.</p>
	 *
	 * @param _goal a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public static GoalType parseGoal(String _goal) throws IllegalArgumentException {
		if(_goal==null || _goal.equals(""))
			throw new IllegalArgumentException("No goal specified");
		for (GoalType t : GoalType.values())
			if (t.name().equalsIgnoreCase(_goal))
				return t;
		throw new IllegalArgumentException("Invalid goal [" + _goal + "]");	
	}
}
