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
package com.sap.psr.vulas.backend.requests;

import com.sap.psr.vulas.backend.HttpResponse;

/**
 * <p>StatusCondition class.</p>
 *
 */
public class StatusCondition implements ResponseCondition {

	private int status;
	
	/**
	 * <p>Constructor for StatusCondition.</p>
	 *
	 * @param _status a int.
	 */
	public StatusCondition(int _status) { this.status = _status; }
	
	/**
	 * {@inheritDoc}
	 *
	 * Returns true if the HTTP status of the given {@link HttpResponse} equals the status of the condition, false otherwise.
	 */
	@Override
	public boolean meetsCondition(HttpResponse _response) { return (_response!=null && _response.getStatus()==this.status); }

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() { return "[HTTP RC==" + this.status + "]"; }
}
