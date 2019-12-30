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

/**
 * <p>ReportException class.</p>
 *
 */
public class ReportException extends GoalExecutionException {
	private String longMessage = null;
	/**
	 * <p>Constructor for ReportException.</p>
	 *
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ReportException(Throwable _cause) {
		super(_cause);
	}
	/**
	 * <p>Constructor for ReportException.</p>
	 *
	 * @param _msg a {@link java.lang.String} object.
	 * @param _cause a {@link java.lang.Throwable} object.
	 */
	public ReportException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
	/**
	 * <p>Getter for the field <code>longMessage</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLongMessage() {
		return longMessage;
	}
	/**
	 * <p>Setter for the field <code>longMessage</code>.</p>
	 *
	 * @param longMessage a {@link java.lang.String} object.
	 */
	public void setLongMessage(String longMessage) {
		this.longMessage = longMessage;
	}
}
