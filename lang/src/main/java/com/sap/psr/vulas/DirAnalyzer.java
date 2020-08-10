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
package com.sap.psr.vulas;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Analyzes all files below a given directory, thereby using other implementations of {@link FileAnalyzer}.
 */
public class DirAnalyzer implements FileAnalyzer {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    /** The dir to be analyzed. */
    private File dir = null;

    /** All Java constructs found in the given class file. */
    private Map<ConstructId, Construct> constructs = null;

    private Set<FileAnalyzer> analyzers = new HashSet<FileAnalyzer>();

    private String[] extensionFilter = null;

    /**
     * <p>Setter for the field <code>extensionFilter</code>.</p>
     *
     * @param _exts an array of {@link java.lang.String} objects.
     */
    public void setExtensionFilter(String[] _exts) {
        this.extensionFilter = _exts.clone();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getSupportedFileExtensions() {
        return new String[] {};
    }

    /** {@inheritDoc} */
    @Override
    public boolean canAnalyze(File _file) {
        return FileUtil.isAccessibleDirectory(_file);
    }

    /** {@inheritDoc} */
    @Override
    public void analyze(final File _file) throws FileAnalysisException {
        if (!FileUtil.isAccessibleDirectory(_file))
            throw new IllegalArgumentException("Expected a directory but got [" + _file + "]");
        this.dir = _file;

        // Statistics
        final Map<String, Integer> count_ext = new HashMap<String, Integer>();
        int total = 0, err = 0;

        // Search for files
        Set<Path> files = null;
        if (this.extensionFilter != null)
            files = new FileSearch(this.extensionFilter).search(this.dir.toPath().normalize());
        else
            files =
                    new FileSearch(FileAnalyzerFactory.getSupportedFileExtensions())
                            .search(this.dir.toPath().normalize());

        // Create corresponding file analyzers
        FileAnalyzer fa = null;
        for (Path file : files) {
            try {
                // Stats
                total++;
                final String ext = FileUtil.getFileExtension(file.toFile());

                // Increment ext counter
                Integer curr = count_ext.get(ext);
                if (curr == null) curr = Integer.valueOf(0);
                count_ext.put(ext, Integer.valueOf(curr + 1));
                // Build the analyzer
                fa = FileAnalyzerFactory.buildFileAnalyzer(file.toFile());
                if (fa != null) this.analyzers.add(fa);
            } catch (RuntimeException e) {
                err++;
                DirAnalyzer.log.error(
                        "Error while analyzing file [" + file.toAbsolutePath() + "]: " + e, e);
            }
        }
        final StringBuffer b = new StringBuffer();
        b.append("File analyzers created: [" + total + " total, " + err + " with error]");
        for (Map.Entry<String, Integer> entry : count_ext.entrySet())
            b.append(", [" + entry.getValue() + " " + entry.getKey() + "]");
        DirAnalyzer.log.info(b.toString());
    }

    /**
     * {@inheritDoc}
     *
     * Returns the union of constructs of all {@link FileAnalyzer}s created when searching recursivly in the directory.
     */
    @Override
    public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
        if (this.constructs == null) {
            this.constructs = new TreeMap<ConstructId, Construct>();
            for (FileAnalyzer fa : this.analyzers) {
                try {
                    this.constructs.putAll(fa.getConstructs());
                } catch (RuntimeException e) {
                    DirAnalyzer.log.error("Error getting constructs from [" + fa + "]: " + e, e);
                }
            }
            DirAnalyzer.log.info("Constructs found: [" + this.constructs.size() + "]");
        }
        return this.constructs;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
        return this.getConstructs().containsKey(_id);
    }

    /** {@inheritDoc} */
    @Override
    public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
        return this.getConstructs().get(_id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChilds() {
        return this.analyzers != null && !this.analyzers.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Set<FileAnalyzer> getChilds(boolean _recursive) {
        final Set<FileAnalyzer> nested_fa = new HashSet<FileAnalyzer>();
        if (!_recursive) {
            nested_fa.addAll(this.analyzers);
        } else {
            for (FileAnalyzer fa : this.analyzers) {
                nested_fa.add(fa);
                final Set<FileAnalyzer> nfas = fa.getChilds(true);
                if (nfas != null && !nfas.isEmpty()) nested_fa.addAll(nfas);
            }
        }
        return nested_fa;
    }

    /**
     * The given {@link InputStream} has been created from an archive entry with the given name.
     * The entry is extracted below the temporary directory, and a {@link FileAnalyzer} is created for it.
     *
     * @param _is a {@link java.io.InputStream} object.
     * @param _entry a {@link java.lang.String} object.
     * @return a {@link com.sap.psr.vulas.FileAnalyzer} object.
     */
    public static synchronized FileAnalyzer createAnalyzerForArchiveEntry(
            InputStream _is, String _entry) {
        final Path tmp_dir = VulasConfiguration.getGlobal().getTmpDir();
        FileAnalyzer fa = null;

        // ZipSlip: Do not extract
        if (!DirUtil.isBelowDestinationPath(tmp_dir, _entry)) {
            log.warn(
                    "Entry ["
                            + _entry
                            + "] will not be extracted, as it would be outside of destination"
                            + " directory");
        }

        // Extract to temp file and create nested PythonArchiveAnalyzer
        else {
            final File file = new File(tmp_dir.toFile(), _entry);
            if (file.exists()) {
                log.info(
                        "Exists already: Entry ["
                                + _entry
                                + "] corresponds to ["
                                + file.toPath().toAbsolutePath()
                                + "]");
                fa = FileAnalyzerFactory.buildFileAnalyzer(file);
            } else {
                boolean dir_exists = file.getParentFile().exists();

                // Create parent dir if not existing
                if (!dir_exists) {
                    dir_exists = file.getParentFile().mkdirs();
                    if (!dir_exists)
                        log.error("Could not create directory [" + file.getParentFile() + "]");
                }

                if (dir_exists) {
                    try (final FileOutputStream fos = new FileOutputStream(file)) {
                        final InputStream is2 = new BufferedInputStream(_is);
                        int cc = -1;
                        while ((cc = is2.read()) >= 0) fos.write(cc);
                        fos.flush();
                        log.info(
                                "Extracted entry ["
                                        + _entry
                                        + "] to ["
                                        + file.toPath().toAbsolutePath()
                                        + "]");
                        fa = FileAnalyzerFactory.buildFileAnalyzer(file);
                    } catch (IOException ioe) {
                        log.error(
                                "Error when extracting entry to ["
                                        + file.toPath().toAbsolutePath()
                                        + "]: "
                                        + ioe.getMessage());
                    }
                }
            }
        }

        return fa;
    }
}
