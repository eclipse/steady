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

import java.util.Collection;
import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.steady.kb.exception.CommandLineParserException;

/**
 * command parser
 */
public class CommandParser {
  /**
   * parse a command
   *
   * @param _args array of {@link java.lang.String}
   * @param options a {@link org.apache.commons.cli.Options} object
   * @throws org.eclipse.steady.kb.exception.CommandLineParserException
   * @return a {@link java.util.HashMap} object
   */
  public static HashMap<String, Object> parse(String[] _args, Options options)
      throws CommandLineParserException {
    final CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, _args, true);
    } catch (ParseException e) {
      throw new CommandLineParserException(e.getMessage());
    }

    HashMap<String, Object> mapOptionValues = new HashMap<>();
    Collection<Option> optionCollection = options.getOptions();
    for (Option option : optionCollection) {
      if (option.hasArg()) {
        mapOptionValues.put(option.getOpt(), cmd.getOptionValue(option.getOpt()));
      } else {
        if (cmd.hasOption(option.getOpt()) || cmd.hasOption(option.getLongOpt())) {
          mapOptionValues.put(option.getOpt(), true);
        } else {
          mapOptionValues.put(option.getOpt(), false);
        }
      }
    }

    return mapOptionValues;
  }
}
