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
package com.sap.psr.vulas.shared.enums;

/**
 * Indicates how a given path has been found, e.g., through test execution ({@link PathSource#X2C}) or via
 * static call graph analysis starting from application methods (@link {@link PathSource#A2C}).
 */
public enum PathSource {
	 A2C, // From app constructs to change list elements (found by call graph analysis)
	 X2C, // From somewhere x to change list elements  (observed during test)
	 C2A, // From change list elements to app constructs (found by call graph analysis)
	 T2C; // From traces to change list elements (found by call graph analysis)
}
