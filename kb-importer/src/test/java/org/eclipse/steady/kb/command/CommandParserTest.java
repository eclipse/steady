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
package org.eclipse.steady.kb.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import org.apache.commons.cli.Options;
import org.eclipse.steady.kb.exception.CommandLineParserException;
import org.junit.Test;

public class CommandParserTest {

  private static final String UPLOAD_CONSTRUCT_OPTION = "u";
  private static final String DIRECTORY_OPTION = "d";
  private static final String OVERWRITE_OPTION = "o";
  private static final String VERBOSE_OPTION = "v";

  private static final String UPLOAD_LONG_OPTION = "upload";
  private static final String VERBOSE_LONG_OPTION = "verbose";
  private static final String OVERWRITE_LONG_OPTION = "overwrite";
  private static final String DIRECTORY_LONG_OPTION = "directory";

  @Test
  public void testParse() throws CommandLineParserException {
    Options options = new Options();
    options.addRequiredOption(
        DIRECTORY_OPTION,
        DIRECTORY_LONG_OPTION,
        true,
        "directory containing mutiple commit folders with meta files");
    options.addOption(
        OVERWRITE_OPTION,
        OVERWRITE_LONG_OPTION,
        false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, VERBOSE_LONG_OPTION, false, "Verbose mode");
    options.addOption(
        UPLOAD_CONSTRUCT_OPTION, UPLOAD_LONG_OPTION, false, "Upload construct changes");

    String _args = "-d test -u -v";
    HashMap<String, Object> parsedCommands = CommandParser.parse(_args.split(" "), options);
    assertEquals("test", parsedCommands.get(DIRECTORY_OPTION));
    assertTrue((boolean) parsedCommands.get(VERBOSE_OPTION));
    assertTrue((boolean) parsedCommands.get(UPLOAD_CONSTRUCT_OPTION));
    assertFalse((boolean) parsedCommands.get(OVERWRITE_OPTION));
  }

  @Test(expected = CommandLineParserException.class)
  public void testRequiredOptions() throws CommandLineParserException {
    Options options = new Options();
    options.addRequiredOption(
        DIRECTORY_OPTION,
        DIRECTORY_LONG_OPTION,
        true,
        "directory containing mutiple commit folders with meta files");
    options.addOption(
        OVERWRITE_OPTION,
        OVERWRITE_LONG_OPTION,
        false,
        "overwrite the bug if it already exists in the backend");
    options.addOption(VERBOSE_OPTION, VERBOSE_LONG_OPTION, false, "Verbose mode");
    options.addOption(
        UPLOAD_CONSTRUCT_OPTION, UPLOAD_LONG_OPTION, false, "Upload construct changes");

    String _args = "-u -v";
    CommandParser.parse(_args.split(" "), options);
  }
}
