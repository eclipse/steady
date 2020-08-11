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
package com.sap.psr.vulas.sign;

import java.util.Set;

/**
 * A signature change represents a set of modifications that must be applied in order to transform one signature into another one.
 *
 * @see SignatureComparator
 */
public interface SignatureChange {

	/**
	 * Returns a set of modifications required to transform one signature into another one.
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Object> getModifications();

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toJSON();
	
	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty();
}
