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
package com.sap.psr.vulas.java.goals;

import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.bytecode.BytecodeComparator;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractAppGoal;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;

/**
 * <p>
 * InstrGoal class.
 * </p>
 *
 */
public class CheckBytecodeGoal extends AbstractAppGoal {

	private static final Log log = LogFactory.getLog(CheckBytecodeGoal.class);


	/**
	 * <p>
	 * Constructor for CheckBytecodeGoal.
	 * </p>
	 */
	public CheckBytecodeGoal() {
		super(GoalType.CHECKCODE);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * Evaluates the configuration setting {@link CoreConfiguration#APP_PREFIXES}.
	 */
	@Override
	protected void prepareExecution() throws GoalConfigurationException {
		super.prepareExecution();
	}
	

	/** {@inheritDoc} */
	@Override
	protected void executeTasks() throws Exception {
		final Application app = this.getApplicationContext();

		
		// get the list of vulnerable dependencies that are NOT assessed (orange
		// hourglasses)
		final Set<VulnerableDependency> vulndeps = BackendConnector.getInstance().getAppVulnDeps(this.getGoalContext(),
				app, false, false, true);
		
		BytecodeComparator comparator = new BytecodeComparator();
		

		for (VulnerableDependency vulndep : vulndeps) {
			if(vulndep.getAffectedVersionConfirmed()==0) //redundant check due to the flags used in the GET request
				comparator.compareLibForBug(vulndep.getDep().getLib(), vulndep.getBug().getBugId(), Paths.get(vulndep.getDep().getPath()));
		}

	}
}
