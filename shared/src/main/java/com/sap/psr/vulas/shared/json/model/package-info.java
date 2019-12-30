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
/**
 * This package contains a large share of the tool's data model.
 * 
 * They facilitate data
 * sharing between different modules, either by explicit (de)serialization through
 * {@link com.sap.psr.vulas.shared.json.JacksonUtil} or implicit through the use
 * of Spring Rest.
 * 
 * Many of those classes also exist as JPA entities in module vulas-backend. While their
 * JSON representation MUST be compatible, they implement individual helper methods for
 * their respective context.
 * 
 */
package com.sap.psr.vulas.shared.json.model;