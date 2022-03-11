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
package org.eclipse.steady.java.mvn;

import static java.lang.String.format;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.goals.GoalExecutionException;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.model.ExemptionBug;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

/**
 * <p>VulasAgentMojo class.</p>
 */
@Mojo(
    name = "prepare-agent",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true)
public class VulasAgentMojo extends AbstractVulasMojo {

  @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
  private Map<String, Artifact> pluginArtifactMap;

  private static final String PROPERTY_NAME = "vulas.maven.agent.propertyName";

  private static final String ECLIPSE_TEST_PLUGIN = "eclipse-test-plugin";

  /**
   * Name of the property used in maven-osgi-test-plugin.
   */
  private static final String TYCHO_ARG_LINE = "tycho.testArgLine";

  /**
   * Name of the property used in maven-surefire-plugin.
   */
  private static final String SUREFIRE_ARG_LINE = "argLine";

  protected final class VulasAgentOptions {

    private final HashMap<String, String> agentOptions = new HashMap<>();

    /**
     * Creates the options for Steady's Java agent and populates them using
     * some settings from {@link VulasConfiguration}, which is created in
     * {@link AbstractVulasMojo#prepareConfiguration}.
     */
    public VulasAgentOptions() {
      final Configuration configuration = vulasConfiguration.getConfiguration();
      getLog()
          .info(
              "The configuration settings starting with \"vulas.core.*\" or \"vulas.shared.*\" are"
                  + " taken from the composite configuration:");
      final Iterator<String> iter = configuration.getKeys();
      while (iter.hasNext()) {
        final String key = iter.next();
        final Object val = configuration.getProperty(key);
        String val_str = null;
        if (key.startsWith("vulas.core.") || key.startsWith("vulas.shared.")) {
          if (val instanceof String[]) {
            val_str = StringUtil.join((String[]) val, ",");
          } else if (val instanceof ArrayList<?>) {
            val_str = StringUtil.join((ArrayList<String>) val, ",");
          } else {
            val_str = val.toString();
          }

          // Do not include exemptions, as too many would result in error "The
          // command line is too long."
          if (key.startsWith(ExemptionBug.CFG_PREFIX)
              || key.startsWith(ExemptionBug.DEPRECATED_CFG_PREFIX)) {
            getLog().warn("  Ignoring  [" + key + "=...]");
          } else {
            this.agentOptions.put(key, val_str);
            getLog().info("    [" + key + "=" + val + "]");
          }
        }
      }

      // Always READ_ONLY so that traces, paths, etc. will be written to disk
      this.agentOptions.put(
          CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.READ_ONLY.toString());
      getLog()
          .info(
              "Setting ["
                  + CoreConfiguration.BACKEND_CONNECT
                  + "] set to ["
                  + CoreConfiguration.ConnectType.READ_ONLY
                  + "] (hard-coded, no matter the configured value)");
    }

    public String prependVMArguments(final String arguments, final File agentJarFile) {
      CommandlineJava commandlineJava =
          new CommandlineJava() {
            @Override
            public void setVm(String vm) {
              // do not set "**/java" as the first command
            }
          };

      // add the javaagent
      commandlineJava.createVmArgument().setLine(format("-javaagent:%s", agentJarFile));

      // remove any javaagent with the same file name from the arguments
      final String[] args = Commandline.translateCommandline(arguments);
      // the new javaagent, as used by the prepare-agent goal
      final String regexForCurrentVulasAgent =
          format("-javaagent:(\"?)%s(\"?)", Pattern.quote(agentJarFile.toString()));
      // the default name of the legacy JAR
      final String regexForOldVulasAgent =
          format(
              "-javaagent:(\"?).*%s(\"?)",
              Pattern.quote("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar"));

      ArrayList<String> patterns = new ArrayList<>();
      patterns.add(regexForCurrentVulasAgent);
      patterns.add(regexForOldVulasAgent);
      // go through the arguments to check for existing javaagents
      argprocess:
      for (String arg : args) {

        // check if one of vulas's agents is already defined as an arg
        // if yes ignore the arg
        for (String regExExp : patterns) {
          if (arg.matches(regExExp)) {
            continue argprocess;
          }
        }
        commandlineJava.createArgument().setLine(arg);
      }

      // add my properties
      for (Map.Entry<String, String> e : this.agentOptions.entrySet()) {
        final String value = e.getValue();
        if (value != null && !value.isEmpty()) {
          Environment.Variable variable = new Environment.Variable();
          variable.setKey(e.getKey());
          variable.setValue(value);
          commandlineJava.addSysproperty(variable);
        }
      }

      // add -noverify
      commandlineJava.createVmArgument().setValue("-noverify");

      return commandlineJava.toString();
    }
  }

  /**
   * {@inheritDoc}
   *
   * Has to override {@link AbstractVulasMojo#execute} as there is no dedicated {@link GoalType} for the agent preparation.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      this.prepareConfiguration();

      // >>> Changed compared to AbstractVulasMojo#execute
      final boolean do_process = this.isPassingFilter(this.project);
      if (do_process) {

        final String name = getEffectivePropertyName();
        final Properties projectProperties = this.project.getProperties();
        final String old_value = projectProperties.getProperty(name);

        VulasAgentOptions vulasAgentOptions = createVulasAgentConfig();
        final String new_value = vulasAgentOptions.prependVMArguments(old_value, getAgentJarFile());

        projectProperties.setProperty(name, new_value);
        getLog().info(name + " set to " + new_value);
      }
      // <<<
    }
    // Expected problems will be passed on as is
    catch (MojoFailureException mfe) {
      throw mfe;
    }
    // Unexpected problems (the goal execution terminates abnormally/unexpectedly)
    catch (GoalExecutionException gee) {
      throw new MojoExecutionException(gee.getMessage(), gee);
    }
    // Every other exception results in a MojoExecutionException (= unexpected)
    catch (Exception e) {
      throw new MojoExecutionException("Error during agent preparation: ", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void createGoal() {
    throw new RuntimeException(
        "Create goal not valid for Mojo [" + this.getClass().getName() + "]");
  }

  private VulasAgentOptions createVulasAgentConfig() {
    return new VulasAgentOptions();
  }

  /**
   * Same as done in jacoco
   * checks which property should be modified
   *
   * @return the name of the property to modify
   */
  private String getEffectivePropertyName() {
    if (!this.vulasConfiguration.isEmpty(PROPERTY_NAME)) {
      return this.vulasConfiguration.getConfiguration().getString(PROPERTY_NAME);
    } else if (isEclipseTestPluginPackaging()) {
      return TYCHO_ARG_LINE;
    }
    return SUREFIRE_ARG_LINE;
  }

  private boolean isEclipseTestPluginPackaging() {
    return ECLIPSE_TEST_PLUGIN.equals(project.getPackaging());
  }
}
