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
package org.eclipse.steady.goals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.enums.GoalClient;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.JsonBuilder;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.Space;
import org.eclipse.steady.shared.json.model.Tenant;
import org.eclipse.steady.shared.util.MemoryMonitor;
import org.eclipse.steady.shared.util.StopWatch;
import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.StringUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.shared.util.StringList.CaseSensitivity;
import org.eclipse.steady.shared.util.StringList.ComparisonMode;

/**
 * Represents the execution of a goal, which is triggered by client components such as the CLI and the Maven plugin.
 * There exist different types of goals, e.g., {@link GoalType#CLEAN} or {@link GoalType#A2C}.
 *
 * Goal executions can be started and stopped manually using the methods {@link AbstractGoal#start()},
 * {@link AbstractGoal#stop()}, {@link AbstractGoal#stop(Exception)} and {@link AbstractGoal#upload(boolean)}.
 *
 * Goal executions can also be executed automatically using the methods
 * {@link AbstractGoal#executeSync()} or {@link AbstractGoal#executeAsync()}, which results in the sequential
 * execution of the above-mentioned methods {@link AbstractGoal#start()}, etc.
 *
 * Subclasses typically override the methods prepareExecution, executeTasks and cleanAfterExecution.
 *
 * See VULAS-204 in case we run into problems related to special characters in paths.
 */
public abstract class AbstractGoal implements Runnable {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** Constant <code>CLASS_EXT</code> */
  protected static final String[] CLASS_EXT = new String[] {"CLASS"};
  /** Constant <code>JAR_EXT</code> */
  protected static final String[] JAR_EXT = new String[] {"jar"};
  /** Constant <code>WAR_EXT</code> */
  protected static final String[] WAR_EXT = new String[] {"war"};
  /** Constant <code>JAR_WAR_EXT</code> */
  protected static final String[] JAR_WAR_EXT = new String[] {"jar", "war"};

  private GoalClient client = null;

  /**
   * Random identifier composed of the goal, current time millis and a random number (8 digits).
   */
  private String id = null;

  private final long createdAt = System.currentTimeMillis();

  /**
   * The goal-specific configuration.
   */
  private VulasConfiguration configuration;

  /**
   * The context in which the goal is going to be executed.
   */
  private GoalContext goalContext = null;

  /**
   * The goal executed.
   */
  private GoalType goalType = null;

  /**
   * The exception that terminated the goal execution (if any).
   * @see #stop(Exception)
   */
  private Exception exception = null;

  // Stop watch to determine runtime
  private StopWatch stopWatch = null;

  // Memory monitoring
  private MemoryMonitor memoThread = null;

  // System information (CPU, OS, JVM, etc.)
  private Map<String, String> systemInfo = new HashMap<String, String>();

  // Goal-specific stats (set from outside)
  private Map<String, Double> goalStats = new HashMap<String, Double>();

  private ExecutionObserver observer = null;

  /** Determines whether goal execution info will be uploaded. */
  private boolean goalUploadEnabled = true;

  /*
   * Creates a new goal execution.
   * @param _app the context of this execution (can be null, i.e., unknown, in case of TEST)
   * @param _goal the goal of this execution
   * @see
   */
  /**
   * <p>Constructor for AbstractGoal.</p>
   *
   * @param _goal a {@link org.eclipse.steady.shared.enums.GoalType} object.
   */
  protected AbstractGoal(GoalType _goal) {
    this(_goal, true);
  }

  /**
   * <p>Constructor for AbstractGoal.</p>
   *
   * @param _goal a {@link org.eclipse.steady.shared.enums.GoalType} object.
   * @param _monitor_mem a boolean.
   */
  protected AbstractGoal(GoalType _goal, boolean _monitor_mem) {
    this.goalType = _goal;

    // Create memory monitor (if requested)
    if (_monitor_mem) this.memoThread = new MemoryMonitor();

    // Number of processors
    this.systemInfo.put(
        "runtime.availableProcessors",
        Integer.toString(Runtime.getRuntime().availableProcessors()));
  }

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public synchronized String getId() {
    if (this.id == null) {
      if (this.client != null)
        this.id =
            this.client
                + "-"
                + this.goalType
                + "-"
                + this.createdAt
                + "-"
                + (int) Math.abs(Math.random() * 100000000);
      else
        this.id =
            this.goalType + "-" + this.createdAt + "-" + (int) Math.abs(Math.random() * 100000000);
    }
    return this.id;
  }

  /**
   * <p>Setter for the field <code>observer</code>.</p>
   *
   * @param observer a {@link org.eclipse.steady.goals.ExecutionObserver} object.
   */
  public void setObserver(ExecutionObserver observer) {
    this.observer = observer;
  }
  /**
   * <p>Getter for the field <code>goalType</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.GoalType} object.
   */
  public GoalType getGoalType() {
    return this.goalType;
  }
  /**
   * <p>getGoalClient.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.GoalClient} object.
   */
  public GoalClient getGoalClient() {
    return this.client;
  }
  /**
   * <p>setGoalClient.</p>
   *
   * @param _client a {@link org.eclipse.steady.shared.enums.GoalClient} object.
   */
  public void setGoalClient(GoalClient _client) {
    this.client = _client;
  }

  /**
   * Returns true if this {@link AbstractGoal} is executed in the given {@link GoalClient}, false otherwise.
   *
   * @param _client a {@link org.eclipse.steady.shared.enums.GoalClient} object.
   * @return a boolean.
   */
  public boolean runsIn(GoalClient _client) {
    return this.client != null && _client != null && _client.equals(this.client);
  }

  // TODO (17/05/2017): Add callback parameter?
  /**
   * <p>executeAsync.</p>
   */
  public final void executeAsync() {
    final Thread t = new Thread(this, "vulas-" + this.goalType.toString().toLowerCase());
    t.start();
  }

  /**
   * <p>run.</p>
   */
  public final void run() {
    try {
      this.execute();
    } catch (GoalConfigurationException e) {
      log.error("Error while configuring " + this + ": " + e.getMessage(), e);
    } catch (GoalExecutionException e) {
      log.error("Error while executing " + this + ": " + e.getMessage(), e);
    }
  }

  /**
   * <p>executeSync.</p>
   *
   * @throws org.eclipse.steady.goals.GoalConfigurationException if any.
   * @throws org.eclipse.steady.goals.GoalExecutionException if any.
   */
  public final void executeSync() throws GoalConfigurationException, GoalExecutionException {
    this.execute();
  }

  private final void execute() throws GoalConfigurationException, GoalExecutionException {
    // Execute the goal and measure execution time
    try {
      this.start();
      this.executeTasks();
      this.stop();
    }
    // Throw as is
    catch (GoalConfigurationException gce) {
      this.stop(gce);
      this.skipGoalUpload(); // Do not upload in case of configuration problems
      throw gce;
    }
    // Throw as is
    catch (GoalExecutionException gee) {
      this.stop(gee);
      throw gee;
    }
    // Embedd in GoalExecutionException
    catch (Exception e) {
      this.stop(e);
      throw new GoalExecutionException(e);
    } finally {
      if (this.goalUploadEnabled) this.upload(false);
    }
  }

  /**
   * Returns the configuration of this goal execution. If the configuration has not been set before, a new instance of
   * {@link VulasConfiguration} is created and returned. As such, the configuration settings of different goal executions
   * can be isolated.
   *
   * @return a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   */
  protected final synchronized VulasConfiguration getConfiguration() {
    if (this.configuration == null) this.configuration = new VulasConfiguration();
    return this.configuration;
  }

  /**
   * <p>Setter for the field <code>configuration</code>.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @return a {@link org.eclipse.steady.goals.AbstractGoal} object.
   */
  public final synchronized AbstractGoal setConfiguration(VulasConfiguration _c) {
    this.configuration = _c;
    return this;
  }

  /**
   * Returns the context of this goal execution. If the context has not been set before, it is constructed
   * by reading tenant, space and app information from the configuration obtained from {@link AbstractGoal#getConfiguration()}.
   *
   * @return a {@link org.eclipse.steady.goals.GoalContext} object.
   */
  public final synchronized GoalContext getGoalContext() {
    if (this.goalContext == null) {
      final Configuration c = this.getConfiguration().getConfiguration();

      this.goalContext = new GoalContext();

      // Configuration
      this.goalContext.setVulasConfiguration(this.getConfiguration());

      // Tenant
      if (!this.getConfiguration().isEmpty(CoreConfiguration.TENANT_TOKEN))
        this.goalContext.setTenant(new Tenant(c.getString(CoreConfiguration.TENANT_TOKEN)));

      // Space
      if (!this.getConfiguration().isEmpty(CoreConfiguration.SPACE_TOKEN)) {
        final Space space = new Space();
        space.setSpaceToken(c.getString(CoreConfiguration.SPACE_TOKEN));
        this.goalContext.setSpace(space);
      }

      // App
      if (Application.canBuildApplication(
          c.getString(CoreConfiguration.APP_CTX_GROUP),
          c.getString(CoreConfiguration.APP_CTX_ARTIF),
          c.getString(CoreConfiguration.APP_CTX_VERSI))) {
        final Application a =
            new Application(
                c.getString(CoreConfiguration.APP_CTX_GROUP),
                c.getString(CoreConfiguration.APP_CTX_ARTIF),
                c.getString(CoreConfiguration.APP_CTX_VERSI));
        if (a.isComplete()) this.goalContext.setApplication(a);
        else log.warn("Incomplete application context: " + a.toString());
      }
    }
    return this.goalContext;
  }

  /**
   * <p>Setter for the field <code>goalContext</code>.</p>
   *
   * @param _ctx a {@link org.eclipse.steady.goals.GoalContext} object.
   */
  public final synchronized void setGoalContext(GoalContext _ctx) {
    this.goalContext = _ctx;
  }

  // >>>>> Methods that can/must be implemented by specific goals

  /**
   * Cleans the cache of the {@link BackendConnector}.
   * CAN be overridden in subclasses to perform additional, goal-specific checks and preparations.
   *
   * @throws org.eclipse.steady.goals.GoalConfigurationException if any.
   */
  protected void prepareExecution() throws GoalConfigurationException {
    BackendConnector.getInstance().cleanCache();
    try {
      this.getConfiguration().checkSettings();
    } catch (ConfigurationException e) {
      throw new GoalConfigurationException(e);
    }
  }

  /**
   * CAN be overridden in subclasses.
   *
   * @throws org.eclipse.steady.goals.GoalConfigurationException if any.
   */
  protected void checkPreconditions() throws GoalConfigurationException {}

  /**
   * MUST be overridden in subclasses to implement the goal-specific tasks.
   *
   * @throws java.lang.Exception if any.
   */
  protected abstract void executeTasks() throws Exception;

  /**
   * Empty implementation.
   *
   * CAN be overridden in subclasses in order to perform goal-specific clean-up.
   */
  protected void cleanAfterExecution() {}

  /**
   * Empty implementation.
   *
   * @return a {@link java.lang.Object} object.
   */
  protected Object getResultObject() {
    return null;
  }

  // <<<<< Methods that can/must be implemented by specific goals

  /**
   * Starts the goal execution.
   *
   * @throws org.eclipse.steady.goals.GoalConfigurationException if any.
   */
  public void start() throws GoalConfigurationException {
    // Start time taking
    this.stopWatch = new StopWatch(this.toString()).start();

    // Monitor mem consumption?
    if (this.memoThread != null) {
      final Thread t = new Thread(this.memoThread, "vulas-memo");
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    }

    // Prepare the execution
    this.prepareExecution();

    // Check whether all conditions to execute the tasks are met
    this.checkPreconditions();

    this.stopWatch.lap("Completed goal preparation", false);
  }

  /**
   * Stops the goal execution, i.e., takes the time and stops the monitoring thread (if any).
   */
  public void stop() {
    // Already stopped?
    if (!this.stopWatch.isRunning())
      throw new IllegalStateException("Goal execution already finished");

    // Stop!
    else {
      this.stopMemo();
      this.stopWatch.lap("Completed execution", true);

      this.cleanAfterExecution();
      this.stopWatch.lap("Completed clean-up", false);

      this.stopWatch.stop();

      this.notifyObserver();
    }
  }

  /**
   * Stops the goal execution in response to the provided exception.
   *
   * @param _e a {@link java.lang.Exception} object.
   * @see #stop()
   */
  public void stop(Exception _e) {
    // Already stopped?
    if (!this.stopWatch.isRunning())
      throw new IllegalStateException("Goal execution already finished");

    // Stop!
    else {
      this.stopMemo();

      this.exception = _e;
      this.stopWatch.stop(_e);

      this.notifyObserver();
    }
  }

  private void notifyObserver() {
    if (this.observer != null) this.observer.callback(this);
  }

  private final void stopMemo() {
    if (this.memoThread != null) this.memoThread.stop();
  }

  /**
   * <p>addGoalStats.</p>
   *
   * @param _prefix a {@link java.lang.String} object.
   * @param _stats a {@link java.util.Map} object.
   */
  public void addGoalStats(String _prefix, Map<String, Long> _stats) {
    for (Map.Entry<String, Long> entry : _stats.entrySet()) {
      this.addGoalStats(
          (_prefix == null || _prefix.equals("") ? entry.getKey() : _prefix + "." + entry.getKey()),
          entry.getValue());
    }
  }

  /**
   * <p>addGoalStats.</p>
   *
   * @param _key a {@link java.lang.String} object.
   * @param _val a long.
   */
  public void addGoalStats(String _key, long _val) {
    this.addGoalStats(_key, (double) _val);
  }

  /**
   * <p>addGoalStats.</p>
   *
   * @param _key a {@link java.lang.String} object.
   * @param _val a int.
   */
  public void addGoalStats(String _key, int _val) {
    this.addGoalStats(_key, (double) _val);
  }

  /**
   * <p>addGoalStats.</p>
   *
   * @param _key a {@link java.lang.String} object.
   * @param _val a double.
   */
  public void addGoalStats(String _key, double _val) {
    this.goalStats.put(_key, new Double(_val));
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append("Goal [id=").append(this.getId()).append(", type=").append(this.getGoalType());
    if (this.goalContext != null) b.append(", ctx=").append(this.goalContext.toString());
    b.append("]");
    return b.toString();
  }

  /**
   * Creates a JSON string representing this goal execution.
   *
   * @throws java.lang.IllegalStateException
   * @return a {@link java.lang.String} object.
   */
  public String toJson() throws IllegalStateException {
    final StringBuilder b = new StringBuilder();
    b.append("{\"executionId\":\"").append(this.getId()).append("\"");
    b.append(",\"goal\":\"").append(this.goalType).append("\"");
    b.append(",\"startedAtClient\":\"")
        .append(StringUtil.formatDate(this.stopWatch.getStartMillis()))
        .append("\"");
    b.append(",\"clientVersion\":").append(JsonBuilder.escape(CoreConfiguration.getVulasRelease()));

    // Exception (if any) and stacktrace
    if (exception != null && exception.getMessage() != null)
      b.append(",\"exception\":")
          .append(
              JsonBuilder.escape(
                  this.exception
                      .getMessage()
                      .substring(0, Math.min(this.exception.getMessage().length(), 255))));

    // Runtime in nano secs
    if (this.stopWatch.isRunning()) b.append(",\"runtimeNano\":-1");
    else b.append(",\"runtimeNano\":").append(this.stopWatch.getRuntime());

    // Memory info (can be -1 if not monitored)
    if (this.memoThread != null) {
      b.append(",\"memMax\":").append(this.memoThread.getJvmMax());
      b.append(",\"memUsedMax\":").append(this.memoThread.getMaxUsed());
      b.append(",\"memUsedAvg\":").append(this.memoThread.getAvgUsed());
    }

    // Goal configuration
    b.append(",\"configuration\":[");
    int c = 0;
    final Iterator<String> iter = this.getConfiguration().getConfiguration().getKeys("vulas");
    while (iter.hasNext()) {
      final String key = iter.next();
      final String[] value = this.getConfiguration().getConfiguration().getStringArray(key);
      if (c++ > 0) b.append(",");
      b.append("{\"source\":\"GOAL_CONFIG\",\"name\":")
          .append(JsonBuilder.escape(key))
          .append(",\"value\":")
          .append(JsonBuilder.escape(StringUtil.join(value, ",")))
          .append("}");
    }
    b.append("]");

    // Goal statistics
    b.append(",\"statistics\":{");
    c = 0;
    for (Map.Entry<String, Double> entry : this.goalStats.entrySet()) {
      if (c++ > 0) b.append(",");
      b.append(JsonBuilder.escape(entry.getKey())).append(":").append(entry.getValue());
    }
    b.append("}");

    // System info
    final StringList env_whitelist =
        this.getConfiguration()
            .getStringList(VulasConfiguration.ENV_VARS, VulasConfiguration.ENV_VARS_CUSTOM);
    final StringList sys_whitelist =
        this.getConfiguration()
            .getStringList(VulasConfiguration.SYS_PROPS, VulasConfiguration.SYS_PROPS_CUSTOM);

    // A subset of environment variables
    this.systemInfo.putAll(
        env_whitelist.filter(
            System.getenv(), true, ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE));

    // A subset of system properties
    for (Object key : System.getProperties().keySet()) {
      final String key_string = (String) key;
      if (sys_whitelist.contains(
          key_string, ComparisonMode.STARTSWITH, CaseSensitivity.CASE_INSENSITIVE))
        this.systemInfo.put(key_string, System.getProperty(key_string));
    }

    b.append(",\"systemInfo\":[");
    c = 0;
    for (Map.Entry<String, String> entry : this.systemInfo.entrySet()) {
      if (c++ > 0) b.append(",");
      b.append("{\"source\":\"SYSTEM_INFO\",\"name\":")
          .append(JsonBuilder.escape(entry.getKey()))
          .append(",\"value\":")
          .append(JsonBuilder.escape(entry.getValue()))
          .append("}");
    }
    b.append("]}");
    return b.toString();
  }

  /**
   * <p>skipGoalUpload.</p>
   */
  protected final void skipGoalUpload() {
    this.goalUploadEnabled = false;
  }

  /**
   * Uploads the JSON presentation of this goal execution to the Vulas backend.
   * Returns true of everything went fine (upload succeeded or is not necessary), false otherwise.
   *
   * @param _before a boolean.
   * @return a boolean.
   */
  public boolean upload(boolean _before) {
    boolean ret = false;
    try {
      AbstractGoal.log.info("Uploading goal execution info ...");
      ret =
          BackendConnector.getInstance().uploadGoalExecution(this.getGoalContext(), this, _before);
      AbstractGoal.log.info("Uploaded goal execution info");
    } catch (Exception e) {
      AbstractGoal.log.error("Error while uploading goal execution info: " + e.getMessage());
    }
    return ret;
  }
}
