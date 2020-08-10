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
 * <p>AffectedVersionSource class.</p>
 *
 */
public enum AffectedVersionSource {
	MANUAL,				// created by frontend-bugs
	CHECK_VERSION , 	// deprecated, was used by CHECKVER goal which is now deprecated
	AST_EQUALITY, 		// created by patch-lib-analyzer
	MAJOR_EQUALITY, 	// created by patch-lib-analyzer
	MINOR_EQUALITY, 	// created by patch-lib-analyzer
	INTERSECTION, 		// created by patch-lib-analyzer
	GREATER_RELEASE, 	// created by patch-lib-analyzer when a release root (X.0) has been created temporally after the latest first fixed version 
	TO_REVIEW,			// created by patch-lib-analyzer 
	PROPAGATE_MANUAL,	// created by patch-lib-analyzer when propagating manual assessments to greater versions for cases where no other sources concluded (i.e. all existing assessments are MANUAL)
	CHECK_CODE;			// created by CHECKCODE goal and digestAnalyzer
}
