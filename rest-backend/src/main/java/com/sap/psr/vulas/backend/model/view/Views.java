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
package com.sap.psr.vulas.backend.model.view;


/**
 * Json Views used to filter the entity fields to be serialized based on the API used.
 *
 * Default: Used across all controllers for APIs that requires a minimal set of information for each entity (as it will exclude all fields with a different view annotated).
 * BugDetails: Used in the BugController for API that includes details about the bug(i.e., constructChanges, and constructChanges counter)
 * BugAffLibs: Used in the BugController for API that includes details about the affectedLibrary (i.e., libraryId and affected construct changes)
 * LibDetails: Used in LibraryController for API that includes details about the Library (including its counters extending the CountDetails view)
 * CountDetails: Used in Application and Library for counter fields
 * DepDetails: Used in ApplicationController for API that includes dependencies to include reachableConstructIds and touchPoints on top of libDetails such as the properties
 * AppDepDetails: Used in the LIbraryController to return the dependency of applications based on a given digest (otherwise the dependency is never serialized when returning applications)
 * GoalDetails: Used in ApplicationController for the goal details
 * VulnDepDetails: Used in ApplicationController to get details for vulnerable dependencies (i.e., constructList)
 * Never: Used in Entity fields that will never be serialized (thus never used in the controllers)
 * LibraryIdDetails: Used in the LibraryIdController to include infomation about affected LibIds when getting all versions of a Maven artifact
 */
public class Views {

  public interface Default {}

  public interface BugDetails {}

  public interface BugAffLibs {}

  public interface LibDetails extends CountDetails {}

  public interface CountDetails {}

  public interface DepDetails extends LibDetails {}

  public interface AppDepDetails {}

  public interface GoalDetails {}

  public interface VulnDepDetails {}

  public interface Never {}

  public interface LibraryIdDetails {}

  public interface BugAffLibsDetails extends BugAffLibs {}

  public interface Overview {}
}
