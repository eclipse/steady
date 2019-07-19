package com.sap.psr.vulas.cg.spi;

import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.ibm.wala.util.graph.Graph;
import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.cg.CallgraphConstructException;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * A call graph constructor builds a call graph starting from a given set of entry points. Entry points can be, for instance,
 * all {@link Construct}s of an application, or all {@link Construct}s that have been traced during tests.
 */
public interface ICallgraphConstructor {


    /**
     * The name of the analysis framework, used to register this callgraph constructor
     *
     * @return the framework's name
     */
    public String getFramework();

    /**
     * Sets the application for call graph construction.
     *
     * @param _ctx a {@link com.sap.psr.vulas.shared.json.model.Application} object.
     */
    public void setAppContext(Application _ctx);

    /**
     * <p>setVulasConfiguration.</p>
     *
     * @param _cfg a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
     */
    public void setVulasConfiguration(VulasConfiguration _cfg);
    
    /**
     * Sets the class path with directories and/or JAR files for the application. Individual entries must be separated by the system-specific path separator.
     *
     * @param _cp a {@link java.lang.String} object.
     */
    public void setAppClasspath(String _cp);

    /**
     * Sets the class path with directories and/or JAR files for the dependencies. Individual entries must be separated by the system-specific path separator.
     *
     * @param _dependencyClasspath a {@link java.lang.String} object.
     */
    public void setDepClasspath(String _dependencyClasspath);

    /**
     * Sets the entry points for call graph construction. These are the starting point of the call graph construction.
     *
     * @param _constructs a {@link java.util.Set} object.
     * @throws com.sap.psr.vulas.cg.CallgraphConstructException
     */
    public void setEntrypoints(Set<com.sap.psr.vulas.shared.json.model.ConstructId> _constructs) throws CallgraphConstructException;

    /**
     * Excludes certain packages from the analysis, i.e., potential invocations happening in {@link Construct}s of those packages will
     * be ignored.
     *
     * @param _packages a {@link java.lang.String} object.
     */
    public void setExcludePackages(String _packages);

    /**
     * Returns the effectively used entry points. {@link Construct}s of type class or package, for instance, will be filtered.
     * Also, it may happen that some of the specified entry points cannot be found, e.g., due to an incomplete class path.
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<com.sap.psr.vulas.shared.json.model.ConstructId> getEntrypoints();

    /**
     * Builds the call graph. If the argument is set to true, the construction will be aborted if
     * not all of the specified entry points could be used.
     *
     * @throws com.sap.psr.vulas.cg.CallgraphConstructException
     * @param _policy a boolean.
     */
    public void buildCallgraph(boolean _policy) throws CallgraphConstructException;

    /**
     * Returns the time required for building the call graph (in nanoseconds), or -1 if the construction did not finish.
     *
     * @return the time required for building the call graph (in nanoseconds)
     */
    public long getConstructionTime();

    /**
     * Returns the constructor's specific configuration settings.
     *
     * @return a {@link org.apache.commons.configuration.Configuration} object.
     */
    public Configuration getConstructorConfiguration();

    /**
     * Returns the call graph.
     *
     * @return a {@link com.ibm.wala.util.graph.Graph} object.
     */
    public Graph<com.sap.psr.vulas.shared.json.model.ConstructId> getCallgraph();


}
