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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Checks whether archives are subject to the ZipSlip vulnerability.
 * Supports the following archive types:
 * - ZIP and derivates JAR, WAR, EAR, AAR
 * - TAR and TAR.GZ
 *
 * See https://github.com/snyk/zip-slip-vulnerability
 */
public class ZipSlipAnalyzer implements MaliciousnessAnalyzer {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    private Path destinationPath =
            VulasConfiguration.getGlobal()
                    .getTmpDir()
                    .toAbsolutePath()
                    .resolve(StringUtil.getRandonString(10));

    /** {@inheritDoc} */
    @Override
    public MaliciousnessAnalysisResult isMalicious(File _file) {
        MaliciousnessAnalysisResult mal = null;
        final String name = _file.getName();

        try (final InputStream fis = new FileInputStream(_file)) {
            // Analyze
            if (name.endsWith(".zip")
                    || name.endsWith(".jar")
                    || name.endsWith(".war")
                    || name.endsWith(".ear")
                    || name.endsWith(".aar")) {
                mal = this.isMalicious(new ZipInputStream(fis), false);
            } else if (name.endsWith(".tar")) {
                mal = this.isMalicious(new TarArchiveInputStream(fis), false);
            } else if (name.endsWith(".tar.gz")) {
                mal =
                        this.isMalicious(
                                new TarArchiveInputStream(
                                        new GzipCompressorInputStream(
                                                new BufferedInputStream(fis))),
                                false);
            } else {
                log.warn("Cannot analyze [" + _file.toPath().toAbsolutePath() + "]");
            }

            // Log
            if (mal != null) {
                mal.setReason("[" + _file.toPath().toAbsolutePath() + "] " + mal.getReason());
                if (mal.isMalicious()) log.warn(mal.getReason());
                else log.info(mal.getReason());
            }
        } catch (FileNotFoundException fnfe) {
            log.error("File [" + _file.getAbsolutePath() + "] cannot be found");
        } catch (IOException ioe) {
            log.error(
                    ioe.getClass().getSimpleName()
                            + " when opening ["
                            + _file.getAbsolutePath()
                            + "]: "
                            + ioe.getMessage(),
                    ioe);
        }
        return mal;
    }

    /** {@inheritDoc} */
    @Override
    public MaliciousnessAnalysisResult isMalicious(InputStream _is, boolean _log) {
        final StringBuffer buffer = new StringBuffer();
        int count = 0;

        final MaliciousnessAnalysisResult mal = new MaliciousnessAnalysisResult();
        mal.setAnalyzer(this.getClass().getName());

        if (_is instanceof ZipInputStream) {
            while (true) {
                try {
                    final ZipEntry e = ((ZipInputStream) _is).getNextEntry();
                    if (e == null) break;
                    count++;
                    final boolean is_below =
                            DirUtil.isBelowDestinationPath(this.destinationPath, e.getName());
                    if (!is_below) {
                        mal.setResult(1);
                        if (buffer.length() > 0) buffer.append(", ");
                        buffer.append(e.getName());
                    }
                } catch (IOException ioe) {
                    log.error(
                            ioe.getClass().getSimpleName()
                                    + " when looping archive entries: "
                                    + ioe.getMessage(),
                            ioe);
                }
            }
        } else if (_is instanceof ArchiveInputStream) {
            while (true) {
                try {
                    final ArchiveEntry e = ((ArchiveInputStream) _is).getNextEntry();
                    if (e == null) break;
                    count++;
                    final boolean is_below =
                            DirUtil.isBelowDestinationPath(this.destinationPath, e.getName());
                    if (!is_below) {
                        mal.setResult(1);
                        if (buffer.length() > 0) buffer.append(", ");
                        buffer.append(e.getName());
                    }
                } catch (IOException ioe) {
                    log.error(
                            ioe.getClass().getSimpleName()
                                    + " when looping archive entries: "
                                    + ioe.getMessage(),
                            ioe);
                }
            }
        }

        // Log
        if (mal.isMalicious()) {
            mal.setReason(
                    "Archive is subject to ZipSlip vulnerability, the following file(s) would be"
                            + " extracted outside of an intended target folder:");
            mal.appendReason(buffer.toString(), " ");
            if (_log) log.warn(mal.getReason());
        } else {
            mal.setReason(
                    "Archive is NOT subject to ZipSlip vulnerability, all ["
                            + count
                            + "] archive entries would be extracted inside or below an intended"
                            + " target folder");
            if (_log) log.debug(mal.getReason());
        }

        return mal;
    }
}
