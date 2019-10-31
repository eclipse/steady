package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.shared.enums.GoalType;

/** TestGoal class. */
public class TestGoal extends AbstractAppGoal {

  /** Constructor for TestGoal. */
  public TestGoal() {
    super(GoalType.TEST);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Empty implementation.
   */
  @Override
  protected void executeTasks() throws Exception {}
}
