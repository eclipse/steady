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
package com.sap.psr.vulas.python.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.python.ProcessWrapperException;
import com.sap.psr.vulas.python.PythonArchiveAnalyzer;
import com.sap.psr.vulas.python.pip.PipInstalledPackage;
import com.sap.psr.vulas.python.pip.PipWrapper;
import com.sap.psr.vulas.python.utils.PythonConfiguration;
import com.sap.psr.vulas.python.virtualenv.VirtualenvWrapper;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.util.DependencyUtil;
import com.sap.psr.vulas.shared.util.DirWithFileSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.tasks.AbstractBomTask;

/**
 * <p>PythonBomTask class.</p>
 *
 */
public class PythonBomTask extends AbstractBomTask {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    private static final String[] EXT_FILTER = new String[] {"whl", "egg", "py"};

    /** {@inheritDoc} */
    @Override
    public Set<ProgrammingLanguage> getLanguage() {
        return new HashSet<ProgrammingLanguage>(
                Arrays.asList(new ProgrammingLanguage[] {ProgrammingLanguage.PY}));
    }

    /** {@inheritDoc} */
    @Override
    public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
        super.configure(_cfg);
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws GoalExecutionException {

        // The app to be completed
        final Application a = this.getApplication();

        // 1) App dependencies
        final Set<PipInstalledPackage> app_pip_packs = new HashSet<PipInstalledPackage>();
        // final Set<Dependency> app_deps = new HashSet<Dependency>();

        // No pip installation path provided: Search for setup.py
        if (this.vulasConfiguration.isEmpty(PythonConfiguration.PY_PIP_PATH)) {
            log.info(
                    "Determine app dependencies by finding setup.py files below the search"
                        + " path(s), and installing them in virtual environments");

            // Find all dirs with setup.py
            final Set<Path> prj_paths = new HashSet<Path>();
            final DirWithFileSearch search = new DirWithFileSearch("setup.py");
            for (Path p : this.getSearchPath()) {
                log.info("Searching for Python projects in search path [" + p + "]");
                search.clear();
                prj_paths.addAll(search.search(p));
            }
            log.info("Found [" + prj_paths.size() + "] Python projects in search path(s)");

            // There are Python projects to analyze
            if (!prj_paths.isEmpty()) {
                // Create virtualenv for every project path
                for (Path p : prj_paths) {
                    log.info("Analyzing Python project in [" + p + "]");
                    try {
                        final VirtualenvWrapper vew = new VirtualenvWrapper(p);
                        app_pip_packs.addAll(vew.getInstalledPackages());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        // Pip installation path provided: Call pip to get installed packages
        else {
            try {
                final String pip_path =
                        this.vulasConfiguration
                                .getConfiguration()
                                .getString(PythonConfiguration.PY_PIP_PATH);
                log.info("Determine app dependencies using [" + pip_path + "]");
                final PipWrapper pip = new PipWrapper(Paths.get(pip_path), (Path) null);
                app_pip_packs.addAll(pip.getFreezePackages());
            } catch (ProcessWrapperException e) {
                throw new GoalExecutionException(
                        "Error creating pip wrapper: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new GoalExecutionException(
                        "Error creating pip wrapper: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                throw new GoalExecutionException(
                        "Error creating pip wrapper: " + e.getMessage(), e);
            }
        }

        if (app_pip_packs.size() == 0) log.warn("No dependencies found");
        a.addDependencies(this.toDependencies(app_pip_packs));

        // 2) App constructs
        final Set<ConstructId> app_constructs = new HashSet<ConstructId>();
        for (Path p : this.getSearchPath()) {
            try {
                // Make sure to not accidently add other than Python constructs
                if (FileUtil.isAccessibleDirectory(p) || FileUtil.hasFileExtension(p, EXT_FILTER)) {
                    log.info(
                            "Searching for Python constructs in search path ["
                                    + p
                                    + "] with filter ["
                                    + StringUtil.join(EXT_FILTER, ", ")
                                    + "]");
                    final FileAnalyzer da =
                            FileAnalyzerFactory.buildFileAnalyzer(p.toFile(), EXT_FILTER);
                    app_constructs.addAll(da.getConstructs().keySet());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        a.addConstructs(ConstructId.getSharedType(app_constructs));

        // Set the one to be returned
        this.setCompletedApplication(a);
    }

    /**
     * Creates {@link Dependency}s for the given {@link PipInstalledPackage}s.
     * @param _packs
     * @return
     */
    private Set<Dependency> toDependencies(Set<PipInstalledPackage> _packs) {
        // Get the installed package that corresponds to the project (if any)
        PipInstalledPackage prj_package = null;
        for (PipInstalledPackage pack : _packs) {
            if (pack.getStandardDistributionName()
                    .equals(
                            PipInstalledPackage.getStandardDistributionName(
                                    this.getApplication().getMvnGroup()))) {
                prj_package = pack;
                break;
            }
        }

        // Create deps for pip packages
        final Set<Dependency> deps = new HashSet<Dependency>();
        for (PipInstalledPackage pack : _packs) {
            try {
                // Do not add the project package itself as dependency
                if ((prj_package == null || !prj_package.equals(pack))
                        && pack.getLibrary().hasValidDigest()) {

                    final Dependency dep = new Dependency();
                    dep.setLib(pack.getLibrary());
                    dep.setApp(this.getApplication());
                    final Path download_path = pack.getDownloadPath();
                    if (download_path != null) {
                        dep.setFilename(download_path.getFileName().toString());
                        dep.setPath(download_path.toString());
                    }
                    dep.setDeclared(true);
                    dep.setScope(Scope.RUNTIME);
                    dep.setTransitive(
                            (prj_package != null && prj_package.requires(pack) ? false : true));

                    deps.add(dep);
                }
            } catch (FileAnalysisException e) {
                log.error(e.getMessage(), e);
            }
        }

        // Create deps for nested Python archives
        for (PipInstalledPackage pack : _packs) {
            final Set<FileAnalyzer> nested_fas = pack.getNestedArchives();
            if (nested_fas != null) {
                for (FileAnalyzer nested_fa : nested_fas) {
                    if (nested_fa instanceof PythonArchiveAnalyzer) {
                        try {
                            final PythonArchiveAnalyzer paa = (PythonArchiveAnalyzer) nested_fa;
                            final Library nested_lib = paa.getLibrary();

                            if (DependencyUtil.containsLibraryDependency(deps, nested_lib)) {
                                log.warn(
                                        "Dependency for library "
                                                + nested_lib
                                                + " already exists, will not be duplicated for the"
                                                + " nested library with path ["
                                                + paa.getArchivePath()
                                                + "]");
                            } else {
                                final Dependency dep = new Dependency();
                                dep.setLib(nested_lib);
                                dep.setApp(this.getApplication());
                                final Path archive_path = paa.getArchivePath();
                                if (archive_path != null) {
                                    dep.setFilename(archive_path.getFileName().toString());
                                    dep.setPath(archive_path.toString());
                                }
                                dep.setDeclared(false); // Nested in one of the above packages
                                dep.setScope(Scope.RUNTIME);
                                dep.setTransitive(true);

                                deps.add(dep);
                            }
                        } catch (FileAnalysisException e) {
                            log.error(e.getMessage(), e);
                        }
                    } else {
                        log.warn(
                                "Nested analyzer of unexpected type ["
                                        + nested_fa.getClass().getSimpleName()
                                        + "]");
                    }
                }
            }
        }

        return deps;
    }
}
