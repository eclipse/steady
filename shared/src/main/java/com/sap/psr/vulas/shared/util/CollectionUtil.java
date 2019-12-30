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
package com.sap.psr.vulas.shared.util;

import java.util.Set;

/**
 * <p>CollectionUtil class.</p>
 *
 */
public class CollectionUtil<T> {
	/**
	 * <p>haveIntersection.</p>
	 *
	 * @param _s1 a {@link java.util.Set} object.
	 * @param _s2 a {@link java.util.Set} object.
	 * @return a boolean.
	 */
	public boolean haveIntersection(Set<T> _s1, Set<T> _s2) {
		for(T o1: _s1)
			for(T o2: _s2)
				if(o1.equals(o2))
					return true;
		return false;
	}
}
