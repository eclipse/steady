package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.shared.enums.GoalType;

/** GoalExecutionRepositoryCustom interface. */
public interface GoalExecutionRepositoryCustom {

  /**
   * Updates the identifiers of independent JPE entities and saves the given {@link Application}.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param _goal_execution a {@link com.sap.psr.vulas.backend.model.GoalExecution} object.
   * @return a {@link com.sap.psr.vulas.backend.model.GoalExecution} object.
   */
  public GoalExecution customSave(Application _app, GoalExecution _goal_execution);

  /**
   * Returns the latest {@link GoalExecution} for the given application, or null if no executions
   * exist.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param _type TODO
   * @return a {@link com.sap.psr.vulas.backend.model.GoalExecution} object.
   */
  public GoalExecution findLatestGoalExecution(Application _app, GoalType _type);
}
