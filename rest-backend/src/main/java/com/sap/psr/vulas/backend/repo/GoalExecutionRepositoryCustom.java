package com.sap.psr.vulas.backend.repo;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.cache.annotation.Cacheable;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.model.Property;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.Scope;

/**
 * <p>GoalExecutionRepositoryCustom interface.</p>
 *
 */
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
	 * Returns the latest {@link GoalExecution} for the given application, or null if no executions exist.
	 *
	 * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param _type TODO
	 * @return a {@link com.sap.psr.vulas.backend.model.GoalExecution} object.
	 */
	public GoalExecution findLatestGoalExecution(Application _app, GoalType _type);
}
