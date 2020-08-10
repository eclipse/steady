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
package com.sap.psr.vulas.shared.json.model.view;


/**
 * Json Views used to filter the entity fields to be serialized based on the API used.
 *
 * Default: Used across all controllers for APIs that requires a minimal set of information for each entity (as it will exclude all fields with a different view annotated).
 * LibDetails: Used in LibraryController for API that includes details about the Library (including its counters extending the CountDetails view)
 * CountDetails: Used in Application and Library for counter fields
 */
public class Views {
    public interface Default {}

    public interface LibDetails extends CountDetails {}

    public interface CountDetails {}
}
