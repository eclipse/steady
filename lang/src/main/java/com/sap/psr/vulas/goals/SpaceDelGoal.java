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
package com.sap.psr.vulas.goals;

import org.apache.logging.log4j.Logger;


import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Space;

/**
 * <p>SpaceDelGoal class.</p>
 *
 */
public class SpaceDelGoal extends AbstractSpaceGoal {
	
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SpaceDelGoal.class);

	/**
	 * <p>Constructor for SpaceDelGoal.</p>
	 */
	public SpaceDelGoal() { super(GoalType.SPACEDEL); }

	/** {@inheritDoc} */
	@Override
	protected void executeTasks() throws Exception {
		final Space s = this.getGoalContext().getSpace();

		final BackendConnector bc = BackendConnector.getInstance();

		// Check that space exists
		if(!bc.isSpaceExisting(this.getGoalContext(), s))
			throw new GoalExecutionException("Space with token [" + s.getSpaceToken() + "] does not exist", null);

		bc.deleteSpace(this.getGoalContext(), s);
	}
}
