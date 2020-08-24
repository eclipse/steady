/**
 * This file is part of Eclipse Steady.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>SPDX-License-Identifier: Apache-2.0
 *
 * <p>Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.python.pip;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.python.ProcessWrapperException;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

/** PyWrapper class. */
public class PyWrapper {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private Path pathToPython = null;

  private Path logDir = null;

  /**
   * Assumes that the Python executable is part of the PATH environment variable.
   *
   * @throws com.sap.psr.vulas.python.ProcessWrapperException if any.
   */
  public PyWrapper() throws ProcessWrapperException {
    this(Paths.get("python"), null);
  }

  /**
   * Creates a new wrapper for the python executable at the given path.
   *
   * @param _path_to_python a {@link java.nio.file.Path} object.
   * @param _log_dir a {@link java.nio.file.Path} object.
   * @throws com.sap.psr.vulas.python.ProcessWrapperException if any.
   */
  public PyWrapper(Path _path_to_python, Path _log_dir) throws ProcessWrapperException {
    this.pathToPython = _path_to_python;
    if (_log_dir != null) {
      this.logDir = _log_dir;
    } else {
      try {
        FileUtil.createTmpDir("vulas-pip-");
      } catch (IOException e) {
        throw new ProcessWrapperException("Cannot create tmp directory: " + e.getMessage());
      }
    }
  }

  /**
   * isAvailable.
   *
   * @return a boolean.
   */
  public boolean isAvailable() {
    boolean exists = false;
    try {
      final Process p = new ProcessBuilder(this.pathToPython.toString()).start();
      final int exit_code = p.waitFor();
      exists = exit_code == 0;
    } catch (IOException ioe) {
      log.error("Error calling [python]: " + ioe.getMessage(), ioe);
    } catch (InterruptedException ie) {
      log.error("Error calling [python]: " + ie.getMessage(), ie);
    }
    return exists;
  }

  /**
<<<<<<< HEAD
   * <p>Runs a Python script.</p>
=======
   * Runs a Python script.
>>>>>>> master
   *
   * @param _script a {@link java.nio.file.Path} object.
   * @param _args a {@link java.util.List} object.
   * @return an int representing the exit code.
   */
  public int runScript(Path _script, List<String> _args) {
    int exit_code = -1;

    // Complete command line call
    final List<String> list = new ArrayList<String>();
    list.add(this.pathToPython.toString());
    list.add(_script.toString());
    list.addAll(_args);

    final String script_name = _script.getFileName().toString();

    try {
      // Perform call
      final ProcessBuilder pb = new ProcessBuilder(list);

      // Create temp. directory for out and err streams
      final Path out = Paths.get(logDir.toString(), "python-" + script_name + "-out.txt");
      final Path err = Paths.get(logDir.toString(), "python-" + script_name + "-err.txt");

      // Redirect out and err
      pb.redirectOutput(out.toFile());
      pb.redirectError(err.toFile());

      // Start and wait
      final Process process = pb.start();
      exit_code = process.waitFor();

      // Success: Parse output and call pip show <package>
      if (exit_code != 0) {
        final String error_msg = FileUtil.readFile(err);
        log.error("Error calling [python " + StringUtil.join(list, " ") + "]: " + error_msg);
      }
    } catch (IOException ioe) {
      log.error("Error calling [python " + StringUtil.join(list, " ") + "]: " + ioe.getMessage());
    } catch (InterruptedException ie) {
      log.error("Error calling [python " + StringUtil.join(list, " ") + "]: " + ie.getMessage());
    }
    return exit_code;
  }
}
