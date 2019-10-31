package com.sap.psr.vulas.mvn;

import static java.lang.String.format;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
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

/** VulasAgentMojo class. */
@Mojo(
    name = "prepare-vulas-agent",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true)
public class VulasAgentMojo extends AbstractVulasMojo {

  @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
  private Map<String, Artifact> pluginArtifactMap;

  private static final String PROPERTY_NAME = "vulas.maven.agent.propertyName";

  private static final String ECLIPSE_TEST_PLUGIN = "eclipse-test-plugin";

  private static final String VULAS_AGENT_ARTIFACT_NAME =
      "com.sap.research.security.vulas:lang-java";
  private static final String VULAS_AGENT_ARTIFACT_CLASSIFIER = "jar-with-dependencies";

  /** Name of the property used in maven-osgi-test-plugin. */
  private static final String TYCHO_ARG_LINE = "tycho.testArgLine";

  /** Name of the property used in maven-surefire-plugin. */
  private static final String SUREFIRE_ARG_LINE = "argLine";

  protected final class VulasAgentOptions {

    private final HashMap<String, String> agentOptions = new HashMap<>();

    /** Creates the options for Vulas' Java Agent and initializes them with reasonable defaults. */
    public VulasAgentOptions() {
      // prepare vulas configuration
      /*try {
          VulasAgentMojo.this.prepareConfiguration();
          VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.MONI_PERIODIC_UPL_ENABLED, false);
          VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_WRITE_CODE, false);
          VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_MAX_STACKTRACES, 10);
          VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_CHOOSEN_INSTR, "com.sap.psr.vulas.monitor.trace.SingleStackTraceInstrumentor");

      } catch (Exception e) {
          e.printStackTrace();
      }*/

      // Add settings from plugin configuration
      Configuration configuration = vulasConfiguration.getConfigurationLayer(PLUGIN_CFG_LAYER);
      if (configuration != null) {
        getLog().info("The following settings are taken from layer [" + PLUGIN_CFG_LAYER + "]:");
        final Iterator<String> iter = configuration.getKeys();
        while (iter.hasNext()) {
          final String key = iter.next();
          final Object val = configuration.getProperty(key);
          String val_str = null;
          if (val instanceof String[]) {
            val_str = StringUtil.join((String[]) val, ",");
          } else if (val instanceof ArrayList<?>) {
            val_str = StringUtil.join((ArrayList<String>) val, ",");
          } else {
            val_str = val.toString();
          }
          this.agentOptions.put(key, val_str);
          getLog().info("    [" + key + "=" + val + "]");
        }
      }

      // Add settings from sys properties
      configuration =
          vulasConfiguration.getConfigurationLayer(VulasConfiguration.SYS_PROP_CFG_LAYER);
      if (configuration != null) {
        getLog()
            .info(
                "The following settings are taken from layer ["
                    + VulasConfiguration.SYS_PROP_CFG_LAYER
                    + "]:");
        final Iterator<String> iter = configuration.getKeys();
        while (iter.hasNext()) {
          final String key = iter.next();
          final Object val = configuration.getProperty(key);
          String val_str = null;
          if (key.startsWith("vulas.")) {
            if (val instanceof String[]) {
              val_str = StringUtil.join((String[]) val, ",");
            } else if (val instanceof ArrayList<?>) {
              val_str = StringUtil.join((ArrayList<String>) val, ",");
            } else {
              val_str = val.toString();
            }
            this.agentOptions.put(key, val_str);
            getLog().info("    [" + key + "=" + val + "]");
          }
        }
      }

      // If not yet present, e.g., because no plugin configuration is present, add GAV from pom.xml
      if (this.agentOptions.get(CoreConfiguration.APP_CTX_GROUP) == null
          || this.agentOptions.get(CoreConfiguration.APP_CTX_ARTIF) == null
          || this.agentOptions.get(CoreConfiguration.APP_CTX_VERSI) == null) {
        getLog().info("The following settings are taken from the project's [pom.xml]:");
        if (this.agentOptions.get(CoreConfiguration.APP_CTX_GROUP) == null) {
          this.agentOptions.put(CoreConfiguration.APP_CTX_GROUP, project.getGroupId());
          getLog()
              .info("    [" + CoreConfiguration.APP_CTX_GROUP + "=" + project.getGroupId() + "]");
        }
        if (this.agentOptions.get(CoreConfiguration.APP_CTX_ARTIF) == null) {
          this.agentOptions.put(CoreConfiguration.APP_CTX_ARTIF, project.getArtifactId());
          getLog()
              .info(
                  "    [" + CoreConfiguration.APP_CTX_ARTIF + "=" + project.getArtifactId() + "]");
        }
        if (this.agentOptions.get(CoreConfiguration.APP_CTX_VERSI) == null) {
          this.agentOptions.put(CoreConfiguration.APP_CTX_VERSI, project.getVersion());
          getLog()
              .info("    [" + CoreConfiguration.APP_CTX_VERSI + "=" + project.getVersion() + "]");
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
      // the new javaagent, as used by the prepare-vulas-agent goal
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
      for (final String key : this.agentOptions.keySet()) {
        final String value = this.agentOptions.get(key);
        if (value != null && !value.isEmpty()) {
          Environment.Variable variable = new Environment.Variable();
          variable.setKey(key);
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
   * <p>Has to override {@link AbstractVulasMojo#execute} as there is no dedicated {@link GoalType}
   * for the agent preparation.
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
      throw new MojoExecutionException("Error during Vulas agent preparation: ", e);
    }
  }

  File getAgentJarFile() throws MojoExecutionException {
    final Artifact vulasAgentArtifact = pluginArtifactMap.get(VULAS_AGENT_ARTIFACT_NAME);
    if (vulasAgentArtifact == null
        || !vulasAgentArtifact.hasClassifier()
        || !vulasAgentArtifact.getClassifier().equals(VULAS_AGENT_ARTIFACT_CLASSIFIER)) {
      throw new MojoExecutionException(
          "Could not found " + VULAS_AGENT_ARTIFACT_NAME + ":" + VULAS_AGENT_ARTIFACT_CLASSIFIER);
    }
    return vulasAgentArtifact.getFile();
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
   * Same as done in jacoco checks which property should be modified
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
