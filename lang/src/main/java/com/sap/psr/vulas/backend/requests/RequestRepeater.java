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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Used to repeat HTTP requests for a certain number of times.
 */
public class RequestRepeater {

	private static final Log log = LogFactory.getLog(RequestRepeater.class);

	private long failCount = 0;
	private long max = 50;
	private long waitMilli = 60000;

	/**
	 * <p>Constructor for RequestRepeater.</p>
	 *
	 * @param _max a long.
	 * @param _milli a long.
	 */
	public RequestRepeater(long _max, long _milli) {
		this.max = _max;
		this.waitMilli = _milli;
	}

	/**
	 * Returns true (and waits {@link RequestRepeater#waitMilli} milliseconds) if the response code of the previous HTTP call was 503
	 * and the number of maximum retries has not been reached, false otherwise.
	 *
	 * @param _503 a boolean.
	 * @return a boolean.
	 */
	public boolean repeat(boolean _503) {

		// Don't wait and repeat if the HTTP response code was NE 503
		if(!_503)
			return false;
		// Else check whether we reached the max. no. of retries
		else {
			this.failCount++;
			if(this.failCount<this.max) {
				log.info("Retry [" + this.failCount + "/" + this.max + "] in [" + this.waitMilli/1000 + "] seconds");
				try {
					Thread.sleep(this.waitMilli);
				} catch (InterruptedException e) {
					log.error("Interrupted: " + e.getMessage());
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}
