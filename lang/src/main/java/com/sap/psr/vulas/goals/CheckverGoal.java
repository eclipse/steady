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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.sign.SignatureAnalysis;

/**
 * <p>CheckverGoal class.</p>
 *
 */
public class CheckverGoal extends AbstractAppGoal {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    private StringList bugsWhitelist = new StringList();

    /**
     * <p>Constructor for CheckverGoal.</p>
     */
    public CheckverGoal() {
        super(GoalType.CHECKVER);
    }

    /**
     * Used to specify the bugs for which the analysis will be done.
     *
     * @param _bugs a {@link java.lang.String} object.
     */
    public void addToBugsWhitelist(String _bugs) {
        if (_bugs != null && !_bugs.equals("")) this.bugsWhitelist.addAll(_bugs, ",", true);
    }

    private URLClassLoader getClassLoader() {
        final List<URL> urls = new ArrayList<URL>();
        Set<Path> dep_jars = this.getKnownDependencies().keySet();
        for (Path d : dep_jars) {
            try {
                urls.add(d.toFile().toURI().toURL());
            } catch (MalformedURLException e) {
                log.error("No URL for dependency [" + d + "]");
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    /** {@inheritDoc} */
    @Override
    protected void executeTasks() throws Exception {
        SignatureAnalysis signatureAnalysis = SignatureAnalysis.getInstance();
        signatureAnalysis.setUrlClassLoader(this.getClassLoader());
        signatureAnalysis.setIsCli(true);
        // signatureAnalysis.setPath(dep_path);
        signatureAnalysis.setApp(this.getApplicationContext());
        signatureAnalysis.setBugs(this.bugsWhitelist);
        signatureAnalysis.execute();
    }
}
