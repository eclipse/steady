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
package com.sap.psr.vulas.malice;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Loops over all existing implementations of {@link MaliciousnessAnalyzer} and returns a set of all findings (positive and negative).
 */
public class MaliciousnessAnalyzerLoop {

    final ServiceLoader<MaliciousnessAnalyzer> loader =
            ServiceLoader.load(MaliciousnessAnalyzer.class);

    /**
     * Checks whether the given {@link File} is malicious or not.
     *
     * @param _file a {@link java.io.File} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<MaliciousnessAnalysisResult> isMalicious(File _file) {
        final Set<MaliciousnessAnalysisResult> results = new HashSet<MaliciousnessAnalysisResult>();
        for (MaliciousnessAnalyzer a : loader) {
            results.add(a.isMalicious(_file));
        }
        return results;
    }

    /**
     * Checks whether the given {@link File} is malicious or not.
     *
     * @param _is a {@link java.io.InputStream} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<MaliciousnessAnalysisResult> isMalicious(InputStream _is) {
        final Set<MaliciousnessAnalysisResult> results = new HashSet<MaliciousnessAnalysisResult>();
        for (MaliciousnessAnalyzer a : loader) {
            results.add(a.isMalicious(_is, true));
        }
        return results;
    }
}
