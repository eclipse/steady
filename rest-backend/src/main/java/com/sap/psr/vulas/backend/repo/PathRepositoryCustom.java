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
package com.sap.psr.vulas.backend.repo;

import java.util.List;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Path;

/**
 * Specifies additional methods of the {@link PathRepository}.
 */
public interface PathRepositoryCustom {

    /**
     * <p>customSave.</p>
     *
     * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
     * @param _paths an array of {@link com.sap.psr.vulas.backend.model.Path} objects.
     * @return a {@link java.util.List} object.
     */
    public List<Path> customSave(Application _app, Path[] _paths);
}
