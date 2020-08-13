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
package com.sap.psr.vulas.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.goals.GoalFactory;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Space;

/**
 * Command line interface (CLI) to execute goals related to {@link Application}s or {@link Space}s.
 * Mandatory configuration parameters such as the backend URL or the workspace token have
 * to be specified as system properties (-D) or using a configuration file "vulas-&lt;foo&gt;.properties".
 */
public class VulasCli {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static GoalType goalType = null;

  private static AbstractGoal goal = null;

  /**
   * <p>main.</p>
   *
   * @param _args an array of {@link java.lang.String} objects.
   * @throws com.sap.psr.vulas.goals.GoalConfigurationException if any.
   * @throws com.sap.psr.vulas.goals.GoalExecutionException if any.
   */
  public static void main(String[] _args)
      throws GoalConfigurationException, GoalExecutionException {

    // Prepare parsing of cmd line arguments
    final Options options = new Options();
    options.addOption(
        "goal",
        "goal",
        true,
        "Goal to be executed (clean, cleanSpace, app, a2c, t2c, instr, report, upload)");

    // Get the goal to be executed
    try {
      final CommandLineParser parser = new DefaultParser();
      final CommandLine cmd = parser.parse(options, _args);
      String g = cmd.getOptionValue("goal", null);

      // Ugly workaround to correct the non-intuitive goal type SPACECLEAN and SPACEDEL
      // TODO: Change GoalType.SPACECLEAN and GoalType.SPACEDEL
      if ("cleanSpace".equals(g)) g = "spaceClean";
      else if ("cleanSpace".equals(g)) g = "spaceDel";

      goalType = GoalType.parseGoal(g);
    }
    // Happens for unknown/invalid options
    catch (ParseException pe) {
      log.error(pe.getMessage());
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Vulas Command Line Interface", options);
      return;
    }
    // Happens during the parsing of the goal
    catch (IllegalArgumentException iae) {
      log.error(iae.getMessage());
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Vulas Command Line Interface", options);
      return;
    }

    // Create and execute the goal
    try {
      goal = GoalFactory.create(goalType, GoalClient.CLI);
      goal.executeSync();
    } catch (IllegalStateException ise) {
      log.error(ise.getMessage());
    } catch (IllegalArgumentException iae) {
      log.error(iae.getMessage());
    }
  }
}
