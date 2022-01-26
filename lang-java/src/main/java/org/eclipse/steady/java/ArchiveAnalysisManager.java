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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Dependency;
import org.eclipse.steady.shared.util.FileSearch;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StopWatch;

/**
 * Parallelizes the analysis of Java archives.
 * The number of parallel {@link Thread}s as well as a timeout is passed as argument to the constructor.
 */
public class ArchiveAnalysisManager {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private ExecutorService pool;

  private boolean instrument = false;
  private Application ctx = null;
  private Path workDir = null;
  private Path libDir = null;
  private Path inclDir = null;
  private Map<Path, JarAnalyzer> analyzers = new HashMap<Path, JarAnalyzer>();

  /** Maps {@link JarAnalyzer}s, which implement {@link Callable}, to their respective {@link Future}. */
  private Map<JarAnalyzer, Future<FileAnalyzer>> futures =
      new HashMap<JarAnalyzer, Future<FileAnalyzer>>();

  private boolean rename = false;

  private Map<Path, Dependency> knownDependencies = null;

  private long analysisTimeout = -1;

  /**
   * <p>Constructor for ArchiveAnalysisManager.</p>
   *
   * @param _pool_size the number of parallel analysis threads
   * @param _timeout the timeout in milleseconds to wait for the completion of all analysis tasks (-1 means no timeout)
   * @param _instr whether or not the Java archives shall be instrumented
   * @param _ctx the application context in which the analysis takes place (if any)
   */
  public ArchiveAnalysisManager(int _pool_size, long _timeout, boolean _instr, Application _ctx) {
    this.pool = Executors.newFixedThreadPool(_pool_size);
    this.analysisTimeout = _timeout;
    this.instrument = _instr;
    this.ctx = _ctx;
  }

  /**
   * <p>Setter for the field <code>instrument</code>.</p>
   *
   * @param _instr a boolean.
   */
  public void setInstrument(boolean _instr) {
    this.instrument = _instr;
  }

  /**
   * <p>Setter for the field <code>workDir</code>.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  public void setWorkDir(Path _p) {
    this.setWorkDir(_p, false);
  }

  /**
   * <p>Setter for the field <code>workDir</code>.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   * @param _create a boolean.
   */
  public void setWorkDir(Path _p, boolean _create) {
    this.workDir = _p;
    if (_create) {
      try {
        Files.createDirectories(_p);
      } catch (IOException e) {
        ArchiveAnalysisManager.log.error(
            "Error while creating dir [" + _p + "]: " + e.getMessage());
      }
    }
    ArchiveAnalysisManager.log.info("Work dir set to [" + _p + "]");
  }

  /**
   * <p>Setter for the field <code>libDir</code>.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  public void setLibDir(Path _p) {
    this.libDir = _p;
    ArchiveAnalysisManager.log.info("Lib dir set to [" + _p + "]");
  }

  /**
   * <p>setIncludeDir.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   */
  public void setIncludeDir(Path _p) {
    this.inclDir = _p;
    ArchiveAnalysisManager.log.info("Include dir set to [" + _p + "]");
  }

  /**
   * <p>getSupportedFileExtensions.</p>
   *
   * @return an array of {@link java.lang.String} objects.
   */
  public static String[] getSupportedFileExtensions() {
    return new String[] {"jar", "war", "aar"};
  }

  /**
   * <p>canAnalyze.</p>
   *
   * @param _file a {@link java.io.File} object.
   * @return a boolean.
   */
  public static final boolean canAnalyze(File _file) {
    final String ext = FileUtil.getFileExtension(_file);
    if (ext == null || ext.equals("")) return false;
    for (String supported_ext : getSupportedFileExtensions()) {
      if (supported_ext.equalsIgnoreCase(ext)) return true;
    }
    return false;
  }

  /**
   * Determines whether the instrumented JAR is renamed or not. If yes, the new file name follows the following format:
   * - If app context is provided: [originalJarName]-vulas-[appGroupId]-[appArtifactId]-[appVersion].jar
   * - Otherwise: [originalJarName]-vulas.jar
   *
   * @param _b a boolean.
   */
  public void setRename(boolean _b) {
    this.rename = _b;
  }

  /**
   * Takes a map of file system paths to {@link Dependency}s.
   * Entries will be used when instantiating {@link JarAnalyzer}s in {@link #startAnalysis(Set, JarAnalyzer)}.
   *
   * @param _deps a {@link java.util.Map} object.
   */
  public void setKnownDependencies(Map<Path, Dependency> _deps) {
    this.knownDependencies = _deps;
  }

  /**
   * Returns the {@link Dependency} for a given archive, or null if no such archive is known.
   * The underlying map is built during the execution of the Maven plugin.
   *
   * @param _p a {@link java.nio.file.Path} object.
   * @return a {@link org.eclipse.steady.shared.json.model.Dependency} object.
   */
  public Dependency getKnownDependency(Path _p) {
    if (this.knownDependencies != null) return this.knownDependencies.get(_p);
    else return null;
  }

  /**
   * Starts the analysis for all {@link Path}s of the given {@link Map} that have a null value.
   * As such, it can be used to avoid analyzing archives multiple times.
   *
   * @param _paths a {@link java.util.Map} object.
   * @param _parent a {@link org.eclipse.steady.java.JarAnalyzer} object.
   */
  public void startAnalysis(@NotNull Map<Path, JarAnalyzer> _paths, JarAnalyzer _parent) {

    // Split those that have been analyzed already and those that need analysis
    final Set<Path> not_yet_analyzed = new HashSet<Path>();
    for (Map.Entry<Path, JarAnalyzer> entry : _paths.entrySet()) {
      if (entry.getValue() == null) not_yet_analyzed.add(entry.getKey());
      else this.analyzers.put(entry.getKey(), entry.getValue());
    }

    // Analyze if necessary
    if (!not_yet_analyzed.isEmpty()) {
      log.info(
          "["
              + this.analyzers.size()
              + "/"
              + _paths.size()
              + "] archives already analyzed, the remaining ["
              + not_yet_analyzed.size()
              + "] will be analyzed now ...");
      this.startAnalysis(not_yet_analyzed, _parent);
    } else {
      log.info(
          "All ["
              + this.analyzers.size()
              + "/"
              + _paths.size()
              + "] archives have been analyzed already");
    }
  }

  /**
   * Starts the analysis for all the given {@link Path}s, which must point to either JAR or WAR archives.
   *
   * @param _paths a {@link java.util.Set} object.
   * @param parent a {@link org.eclipse.steady.java.JarAnalyzer} object.
   */
  public void startAnalysis(@NotNull Set<Path> _paths, JarAnalyzer parent) {

    // Set the several static attributes of the JarAnalyzer, for all the instances created later
    JarAnalyzer.setAppContext(this.ctx);

    // Add all the paths to the classpath, so that the compilation works (if instrumentation is
    // requested).
    for (Path p : _paths) {
      try {
        JarAnalyzer.insertClasspath(p.toString());
      } catch (Exception e) {
        // No problem at all if instrumentation is not requested.
        // If instrumentation is requested, however, some classes may not compile
        ArchiveAnalysisManager.log.error("Error while updating the classpath: " + e.getMessage());
      }
    }

    // Add additional JARs into the classpath (if any)
    if (this.libDir != null && (this.libDir.toFile().exists())) {
      final FileSearch vis = new FileSearch(new String[] {"jar"});
      final Set<Path> libs = vis.search(this.libDir);
      for (Path p : libs) {
        try {
          JarAnalyzer.insertClasspath(p.toString());
        } catch (Exception e) {
          // No problem at all if instrumentation is not requested.
          // If instrumentation is requested, however, some classes may not compile
          ArchiveAnalysisManager.log.error(
              "Error while updating the classpath from lib ["
                  + this.libDir
                  + "]: "
                  + e.getMessage());
        }
      }
    }

    // Create temp directory for storing the modified JARs (if any)
    if (this.instrument && this.workDir == null) {
      try {
        workDir = java.nio.file.Files.createTempDirectory("jar_analysis_");
      } catch (IOException e) {
        throw new IllegalStateException("Unable to create work directory", e);
      }
    }

    // Create a JarAnalyzer for all paths, and ask the thread pool to start them
    for (Path p : _paths) {
      try {
        JarAnalyzer ja = null;
        if (p.toString().endsWith("jar")) {
          final JarWriter jw = new JarWriter(p);
          if (jw.hasEntry("BOOT-INF/")) {
            ja = new SpringBootAnalyzer();
          }
          else {
            ja = new JarAnalyzer();
          }
        } else if (p.toString().endsWith("war")) {
          ja = new WarAnalyzer();
          ((WarAnalyzer) ja).setIncludeDir(this.inclDir);
        } else if (p.toString().endsWith("aar")) {
          ja = new AarAnalyzer();
        } else {
          ArchiveAnalysisManager.log.warn(
              "File extension not supported (only JAR, WAR, AAR): " + p);
          continue;
        }

        if (parent != null) ja.setParent(parent);

        ja.setRename(this.rename);
        ja.setWorkDir(this.workDir);

        if (this.getKnownDependency(p) != null)
          ja.setLibraryId(this.getKnownDependency(p).getLib().getLibraryId());

        ja.analyze(p.toFile());
        ja.setInstrument(
            this.instrument); // To be called after analyze, since instrument uses the URL member

        this.analyzers.put(p, ja);

        // Execute the analyzer
        final Future<FileAnalyzer> future = this.pool.submit(ja);
        this.futures.put(ja, future);
      } catch (Exception e) {
        ArchiveAnalysisManager.log.error(
            "Error while analyzing path [" + p + "]: " + e.getMessage());
      }
    }

    final StopWatch sw =
        new StopWatch("Analysis of [" + futures.size() + "] Java archives")
            .setTotal(futures.size())
            .start();

    this.pool.shutdown(); // Don't accept new tasks, wait for the completion of existing ones

    try {
      while (!this.pool.awaitTermination(10, TimeUnit.SECONDS)) {
        final Map<JarAnalyzer, Future<FileAnalyzer>> open_tasks = this.getOpenTasks();
        final long sw_runtime = sw.getRuntimeMillis();

        // There are remaining tasks, and a timeout has been configured and reached
        if (!open_tasks.isEmpty()
            && this.analysisTimeout != -1
            && sw_runtime > this.analysisTimeout) {
          ArchiveAnalysisManager.log.warn(
              "Timeout of ["
                  + this.analysisTimeout
                  + "ms] reached, the following ["
                  + open_tasks.size()
                  + "] non-completed analysis tasks will be canceled:");
          for (Map.Entry<JarAnalyzer, Future<FileAnalyzer>> e : open_tasks.entrySet()) {
            ArchiveAnalysisManager.log.info("    " + e.getKey());
            e.getValue().cancel(true);
          }
          throw new JarAnalysisException(
              "Timeout of ["
                  + this.analysisTimeout
                  + "ms] reached, ["
                  + open_tasks.size()
                  + "] have been canceled");
        }
        // There are remaining tasks, but no timeout is set or reached
        else if (!open_tasks.isEmpty()) {
          ArchiveAnalysisManager.log.info(
              "Waiting for the completion of [" + open_tasks.size() + "] analysis tasks");
          for (JarAnalyzer ja : open_tasks.keySet()) {
            ArchiveAnalysisManager.log.debug("    " + ja);
          }
        }
      }

      sw.stop();
    } catch (JarAnalysisException jae) {
      sw.stop(jae);
    } catch (InterruptedException e) {
      sw.stop(e);
    }
  }

  /**
   * Returns all analysis {@link Future}s that are not (yet) done.
   * @see Future#isDone()
   * @return
   */
  private final Map<JarAnalyzer, Future<FileAnalyzer>> getOpenTasks() {
    final Map<JarAnalyzer, Future<FileAnalyzer>> open_tasks =
        new HashMap<JarAnalyzer, Future<FileAnalyzer>>();
    for (Map.Entry<JarAnalyzer, Future<FileAnalyzer>> e : this.futures.entrySet()) {
      if (!e.getValue().isDone()) {
        open_tasks.put(e.getKey(), e.getValue());
      }
    }
    return open_tasks;
  }

  /**
   * <p>Getter for the field <code>analyzers</code>.</p>
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<JarAnalyzer> getAnalyzers() {
    final HashSet<JarAnalyzer> analyzers = new HashSet<JarAnalyzer>();
    for (JarAnalyzer ja : this.analyzers.values()) {
      analyzers.add(ja);
      if (ja.hasChilds()) {
        final Set<FileAnalyzer> fas = ja.getChilds(true);
        for (FileAnalyzer fa : fas) if (fa instanceof JarAnalyzer) analyzers.add((JarAnalyzer) fa);
      }
    }
    return analyzers;
  }

  /**
   * Returns the analyzer used to analyze a Java archive whose path ends with the given sub-path _p (the first match is taken), null if no such analyzer can be found.
   *
   * @param _p a {@link java.nio.file.Path} object.
   * @return a {@link org.eclipse.steady.java.JarAnalyzer} object.
   */
  public JarAnalyzer getAnalyzerForSubpath(Path _p) {
    JarAnalyzer ja = null;
    for (Map.Entry<Path, JarAnalyzer> e : this.analyzers.entrySet()) {
      if (e.getKey().endsWith(_p)) {
        ja = e.getValue();
        break;
      }
    }
    return ja;
  }
}
