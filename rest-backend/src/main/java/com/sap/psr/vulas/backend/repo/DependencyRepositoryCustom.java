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
package com.sap.psr.vulas.backend.repo;

import java.util.List;

import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.TouchPoint;
import java.util.Set;

/**
 * Specifies additional methods of the {@link PathRepository}.
 */
public interface DependencyRepositoryCustom {

	/**
	 * <p>saveReachableConstructIds.</p>
	 *
	 * @param _dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 * @param _construct_ids an array of {@link com.sap.psr.vulas.backend.model.ConstructId} objects.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<ConstructId> saveReachableConstructIds(Dependency _dep, ConstructId[] _construct_ids);
	
	/**
	 * <p>saveTouchPoints.</p>
	 *
	 * @param _dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
	 * @param _touch_points an array of {@link com.sap.psr.vulas.backend.model.TouchPoint} objects.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<TouchPoint> saveTouchPoints(Dependency _dep, TouchPoint[] _touch_points);
}
