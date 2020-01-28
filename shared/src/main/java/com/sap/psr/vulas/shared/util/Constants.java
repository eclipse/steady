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

/**
 * <p>Constants interface.</p>
 *
 */
public interface Constants {

	/** Constant <code>HTTP_TENANT_HEADER="X-Vulas-Tenant"</code> */
	public static final String HTTP_TENANT_HEADER = "X-Vulas-Tenant";
	
	/** Constant <code>HTTP_SPACE_HEADER="X-Vulas-Space"</code> */
	public static final String HTTP_SPACE_HEADER = "X-Vulas-Space";
	
	// Other headers
	/** Constant <code>HTTP_VERSION_HEADER="X-Vulas-Version"</code> */
	public static final String HTTP_VERSION_HEADER   = "X-Vulas-Version";
	/** Constant <code>HTTP_COMPONENT_HEADER="X-Vulas-Component"</code> */
	public static final String HTTP_COMPONENT_HEADER = "X-Vulas-Component";
	public enum VulasComponent { client, appfrontend, patcheval, bugfrontend };
	
	// Length restrictions
	/** Constant <code>MAX_LENGTH_GROUP=128</code> */
	public static final int MAX_LENGTH_GROUP    = 128;
	/** Constant <code>MAX_LENGTH_ARTIFACT=128</code> */
	public static final int MAX_LENGTH_ARTIFACT = 128;
	/** Constant <code>MAX_LENGTH_VERSION=96</code> */
	public static final int MAX_LENGTH_VERSION  = 96;
}
