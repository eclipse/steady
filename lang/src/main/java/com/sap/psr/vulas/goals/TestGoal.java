package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.shared.enums.GoalType;

/**
 * <p>TestGoal class.</p>
 *
 */
public class TestGoal extends AbstractAppGoal {

	/**
	 * <p>Constructor for TestGoal.</p>
	 */
	public TestGoal() { super(GoalType.TEST); }

	/**
	 * {@inheritDoc}
	 *
	 * Empty implementation.
	 */
	@Override
	protected void executeTasks() throws Exception {}
}
