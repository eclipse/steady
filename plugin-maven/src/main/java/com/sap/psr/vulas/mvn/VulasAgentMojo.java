package com.sap.psr.vulas.mvn;


import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Mojo(name = "prepare-vulas-agent", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class VulasAgentMojo extends AbstractVulasMojo {


    @Parameter(property = "vulas.agent.propertyName")
    private String propertyName;

    @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
    private Map<String, Artifact> pluginArtifactMap;


    private static final String ECLIPSE_TEST_PLUGIN = "eclipse-test-plugin";


    private static final String VULAS_AGENT_ARTIFACT_NAME = "com.sap.research.security.vulas:lang-java";
    private static final String VULAS_AGENT_ARTIFACT_CLASSIFIER = "jar-with-dependencies";

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
         * Creates the options for Vuals' Java Agent and initializes them with reasonable defaults.
         */
        public VulasAgentOptions() {

            //prepare vulas configuration
            try {
                VulasAgentMojo.this.prepareConfiguration();
                VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.MONI_PERIODIC_UPL_ENABLED, false);
                VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_WRITE_CODE, false);
                VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_MAX_STACKTRACES, 10);
                VulasConfiguration.getGlobal().setPropertyIfEmpty(CoreConfiguration.INSTR_CHOOSEN_INSTR, "com.sap.psr.vulas.monitor.trace.SingleStackTraceInstrumentor");

            } catch (Exception e) {
                e.printStackTrace();
            }

            Configuration configuration = VulasConfiguration.getGlobal().getConfiguration();

            //shared properties
            setTmpDir(configuration.getString(VulasConfiguration.TMP_DIR));

            //backend
            setBackendURL(VulasConfiguration.getGlobal().getServiceUrl(Service.BACKEND));
            setBackendConnect(CoreConfiguration.ConnectType.READ_ONLY);

            //core properties
            setVulasDummy("dummy");
            setUploadDir(configuration.getString(CoreConfiguration.UPLOAD_DIR));
            setPeriodicUpload(configuration.getBoolean(CoreConfiguration.MONI_PERIODIC_UPL_ENABLED));

            //space token
            setSpaceToken(configuration.getString(CoreConfiguration.SPACE_TOKEN));

            //app agentOptions
            setAppCtxGroup(configuration.getString(CoreConfiguration.APP_CTX_GROUP));
            setAppCtxArtifact(configuration.getString(CoreConfiguration.APP_CTX_ARTIF));
            setAppCtxVersion(configuration.getString(CoreConfiguration.APP_CTX_VERSI));
            //instrumentation config
            setWriteCode(configuration.getBoolean(CoreConfiguration.INSTR_WRITE_CODE));
            setMaxStackTraces(configuration.getInt(CoreConfiguration.INSTR_MAX_STACKTRACES));
            setInstrumentors(configuration.getStringArray(CoreConfiguration.INSTR_CHOOSEN_INSTR));

        }

        void setVulasDummy(String dummy) {
            this.agentOptions.put("vulas.core.dummy", dummy);
        }

        void setSpaceToken(String spaceToken) {
            this.agentOptions.put(CoreConfiguration.SPACE_TOKEN, spaceToken);
        }

        void setBackendURL(String backendURL) {
            this.agentOptions.put("vulas.shared.backend.serviceUrl", backendURL);
        }

        void setBackendConnect(CoreConfiguration.ConnectType connectType) {
            this.agentOptions.put(CoreConfiguration.BACKEND_CONNECT, connectType.toString());
        }

        void setAppCtxGroup(String appCtxGroup) {
            this.agentOptions.put(CoreConfiguration.APP_CTX_GROUP, appCtxGroup);
        }

        void setAppCtxArtifact(String appCtxArtifact) {
            this.agentOptions.put(CoreConfiguration.APP_CTX_ARTIF, appCtxArtifact);
        }

        void setAppCtxVersion(String appCtxVersion) {
            this.agentOptions.put(CoreConfiguration.APP_CTX_VERSI, appCtxVersion);
        }

        void setPeriodicUpload(boolean enabled) {
            this.agentOptions.put(CoreConfiguration.MONI_PERIODIC_UPL_ENABLED, String.valueOf(enabled));
        }


        void setUploadDir(String uploadDir) {
            this.agentOptions.put(CoreConfiguration.UPLOAD_DIR, uploadDir);
        }

        void setTmpDir(String tmpDir) {
            this.agentOptions.put(VulasConfiguration.TMP_DIR, tmpDir);
        }

        void setWriteCode(boolean enable) {
            this.agentOptions.put(CoreConfiguration.INSTR_WRITE_CODE, String.valueOf(enable));
        }

        void setMaxStackTraces(int maxNumberOfStackTraces) {
            this.agentOptions.put(CoreConfiguration.INSTR_MAX_STACKTRACES, String.valueOf(maxNumberOfStackTraces));
        }

        void setInstrumentors(String[] vulasInstr) {
            String selectedInstr = StringUtil.join(vulasInstr, ",");
            this.agentOptions.put(CoreConfiguration.INSTR_CHOOSEN_INSTR, selectedInstr);
        }


        public String prependVMArguments(final String arguments, final File agentJarFile) {

            CommandlineJava commandlineJava = new CommandlineJava() {
                @Override
                public void setVm(String vm) {
                    //do not set "**/java" as the first command
                }
            };

            //add the javaagent
            commandlineJava.createVmArgument().setLine(format("-javaagent:%s", agentJarFile));

            //remove any javaagent with the same file name from the arguments
            final String[] args = Commandline.translateCommandline(arguments);
            // the new javaagent, as used by the prepare-vulas-agent goal
            final String regexForCurrentVulasAgent = format("-javaagent:(\"?)%s(\"?)", Pattern.quote(agentJarFile.toString()));
            // the default name of the original javaagent
            final String regexForOldVulasAgent = format("-javaagent:(\"?).*%s(\"?)", Pattern.quote("/vulas/lib/vulas-core-latest-jar-with-dependencies.jar"));

            ArrayList<String> patterns = new ArrayList<>();
            patterns.add(regexForCurrentVulasAgent);
            patterns.add(regexForOldVulasAgent);
            // go through the arguments to check for existing javaagents
            argprocess: for (String arg : args) {

                // check if one of vulas's agents is already defined as an arg
                // if yes ignore the arg
                for (String regExExp : patterns) {
                    if (arg.matches(regExExp)) {
                        continue argprocess;
                    }

                }
                commandlineJava.createArgument().setLine(arg);

            }
            //add my properties
            for (final String key : this.agentOptions.keySet()) {
                final String value = this.agentOptions.get(key);
                if (value != null && !value.isEmpty()) {
                    Environment.Variable variable = new Environment.Variable();
                    variable.setKey(key);
                    variable.setValue(value);
                    commandlineJava.addSysproperty(variable);
                }
            }

            //add -noverify
            commandlineJava.createVmArgument().setValue("-noverify");

            return commandlineJava.toString();
        }


    }

    @Override
    public void execute() throws MojoExecutionException {

        final String name = getEffectivePropertyName();
        final Properties projectProperties = this.project.getProperties();
        final String oldValue = projectProperties.getProperty(name);

        VulasAgentOptions vulasAgentOptions = createVulasAgentConfig();
        final String newValue = vulasAgentOptions.prependVMArguments(
                oldValue, getAgentJarFile());
        getLog().info(name + " set to " + newValue);
        projectProperties.setProperty(name, newValue);
    }


    File getAgentJarFile() throws MojoExecutionException {
        final Artifact vulasAgentArtifact = pluginArtifactMap
                .get(VULAS_AGENT_ARTIFACT_NAME);
        if (vulasAgentArtifact == null || !vulasAgentArtifact.hasClassifier() || !vulasAgentArtifact.getClassifier().equals(VULAS_AGENT_ARTIFACT_CLASSIFIER)) {
            throw new MojoExecutionException("Could not found " + VULAS_AGENT_ARTIFACT_NAME + ":" + VULAS_AGENT_ARTIFACT_CLASSIFIER);
        }
        return vulasAgentArtifact.getFile();
    }


    @Override
    protected void createGoal() {
        throw new RuntimeException("Create Goal not valid for Mojo: " + this.getClass().getName());

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
        if (isPropertyNameSpecified()) {
            return propertyName;
        }
        if (isEclipseTestPluginPackaging()) {
            return TYCHO_ARG_LINE;
        }
        return SUREFIRE_ARG_LINE;
    }

    private boolean isPropertyNameSpecified() {
        return propertyName != null && !propertyName.isEmpty();
    }

    private boolean isEclipseTestPluginPackaging() {
        return ECLIPSE_TEST_PLUGIN.equals(project.getPackaging());
    }
}
