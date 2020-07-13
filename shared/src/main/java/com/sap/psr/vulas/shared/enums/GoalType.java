/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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
	CHECKCODE,
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
