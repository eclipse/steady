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
package com.sap.psr.vulas.python;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

/**
 * <p>ProcessWrapper class.</p>
 *
 */
public class ProcessWrapper implements Runnable {

  private static Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static final Pattern ALLOWED = Pattern.compile("[\\.\\-\\w=]+");

  private String id = null;

  private Path exe = null;

  private String[] args = null;

  private Path outPath = null;

  private Path outFile = null;

  private Path errFile;

  private int exitCode = -1;

  /**
   * <p>Constructor for ProcessWrapper.</p>
   */
  public ProcessWrapper() {
    this.id = StringUtil.getRandonString(10);
  }

  /**
   * <p>Constructor for ProcessWrapper.</p>
   *
   * @param _id a {@link java.lang.String} object.
   */
  public ProcessWrapper(String _id) {
    this.id = _id;
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getId() {
    return this.id;
  }

  /**
   * <p>setCommand.</p>
   *
   * @param _executable a {@link java.nio.file.Path} object.
   * @param _args a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.python.ProcessWrapper} object.
   * @throws com.sap.psr.vulas.python.ProcessWrapperException if any.
   */
  public ProcessWrapper setCommand(Path _executable, String... _args)
      throws ProcessWrapperException {
    // if(_executable==null || FileUtil.isAccessibleFile(_executable))
    //	throw new ProcessWrapperException("Illegal executable [" + _executable + "]");

    for (int i = 0; i < _args.length; i++) {
      final Matcher m = ALLOWED.matcher(_args[i]);
      if (!m.matches()
          && !FileUtil.isAccessibleFile(_args[i])
          && !FileUtil.isAccessibleDirectory(_args[i]))
        throw new ProcessWrapperException(
            "Illegal characters in argument [" + i + "], allowed are: a-zA-Z_0-9-.=");
    }

    this.exe = _executable;
    this.args = _args;
    return this;
  }

  /**
   * <p>setPath.</p>
   *
   * @param _p a {@link java.nio.file.Path} object.
   * @return a {@link com.sap.psr.vulas.python.ProcessWrapper} object.
   */
  public ProcessWrapper setPath(Path _p) {
    this.outPath = _p;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public void run() {
    String name = null;
    if (FileUtil.isAccessibleFile(this.exe)) name = this.exe.getFileName().toString();
    else if (this.exe.toString().indexOf(System.getProperty("file.separator")) != -1)
      name =
          this.exe
              .toString()
              .substring(this.exe.toString().lastIndexOf(System.getProperty("file.separator")) + 1);
    else name = this.exe.toString();
    final String rnd = StringUtil.getRandonString(6);
    final String out_name = name + "-" + this.getId() + "-" + rnd + "-out.txt";
    final String err_name = name + "-" + this.getId() + "-" + rnd + "-err.txt";

    // Create temp. directory for out and err streams
    this.outFile = Paths.get(this.outPath.toString(), out_name);
    this.errFile = Paths.get(this.outPath.toString(), err_name);

    try {
      final ArrayList<String> cmd = new ArrayList<String>();
      cmd.add(this.exe.toString());
      cmd.addAll(Arrays.asList(this.args));
      final ProcessBuilder pb = new ProcessBuilder(cmd);

      // Redirect out and err
      pb.redirectOutput(this.outFile.toFile());
      pb.redirectError(this.errFile.toFile());

      // Start and wait
      final Process process = pb.start();
      this.exitCode = process.waitFor();

      if (this.exitCode != 0) {
        final String error_msg = FileUtil.readFile(this.errFile);
        log.error("Error running [" + this.getCommand() + "]: " + error_msg);
      }

    } catch (IOException ioe) {
      log.error("Error running [" + this.getCommand() + "]: " + ioe.getMessage());
    } catch (InterruptedException ie) {
      log.error("Error running [" + this.getCommand() + "]: " + ie.getMessage());
    }
  }

  /**
   * <p>Getter for the field <code>outFile</code>.</p>
   *
   * @return a {@link java.nio.file.Path} object.
   */
  public Path getOutFile() {
    return outFile;
  }

  /**
   * <p>Getter for the field <code>errFile</code>.</p>
   *
   * @return a {@link java.nio.file.Path} object.
   */
  public Path getErrFile() {
    return errFile;
  }

  /**
   * <p>Getter for the field <code>exitCode</code>.</p>
   *
   * @return a int.
   */
  public int getExitCode() {
    return exitCode;
  }

  /**
   * <p>terminatedWithSuccess.</p>
   *
   * @return a boolean.
   */
  public boolean terminatedWithSuccess() {
    return this.exitCode == 0;
  }

  /**
   * <p>getCommand.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getCommand() {
    final ArrayList<String> cmd = new ArrayList<String>();
    cmd.add(this.exe.toString());
    cmd.addAll(Arrays.asList(this.args));
    return StringUtil.join(cmd, " ");
  }
}
