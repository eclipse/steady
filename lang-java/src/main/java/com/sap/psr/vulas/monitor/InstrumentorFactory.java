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
package com.sap.psr.vulas.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Creates implementations of {@link IInstrumentor} for all classes specified via the
 * configuration option {@link CoreConfiguration#INSTR_CHOOSEN_INSTR}.
 */
public class InstrumentorFactory {

	private static final Log log = LogFactory.getLog(InstrumentorFactory.class);

	private static List<IInstrumentor> instrumentors = null;

	/**
	 * Creates and returns implementations of {@link IInstrumentor} for all classes specified via the
	 * configuration option {@link CoreConfiguration#INSTR_CHOOSEN_INSTR}.
	 * Those will be looped during static and dynamic instrumentation, e.g., in the classes
	 * {@link ExecutionMonitor} and {@link ClassVisitor}.
	 *
	 * @return a {@link java.util.List} object.
	 */
	public static synchronized List<IInstrumentor> getInstrumentors() {
		if(instrumentors==null) {
			instrumentors = new ArrayList<IInstrumentor>();
			final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();
			final String[] instrumentors = cfg.getStringArray(CoreConfiguration.INSTR_CHOOSEN_INSTR);
			for(String name: instrumentors) {
				final AbstractInstrumentor i = InstrumentorFactory.getInstrumentor(name);
				if(i!=null) InstrumentorFactory.instrumentors.add(i);
			}
		}
		return instrumentors;
	}

	private static AbstractInstrumentor getInstrumentor(String _name) {
		AbstractInstrumentor i = null;
		try {
			final Class cls = Class.forName(_name);
			i = (AbstractInstrumentor)cls.newInstance();
		}
		catch (Throwable e) {
			InstrumentorFactory.log.error("Error while creating instrumentor of class [" + _name + "]: " + e.getMessage(), e);
		}
		return i;
	}
}
