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
package org.eclipse.steady.backend.component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.steady.backend.model.Application;
import org.eclipse.steady.backend.model.GoalExecution;
import org.eclipse.steady.backend.model.VulnerableDependency;
import org.eclipse.steady.backend.model.view.Views;
import org.eclipse.steady.backend.repo.ApplicationRepository;
import org.eclipse.steady.backend.repo.GoalExecutionRepository;
import org.eclipse.steady.shared.enums.ExportFormat;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.JsonBuilder;
import org.eclipse.steady.shared.util.StopWatch;
import org.eclipse.steady.shared.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>ApplicationExporterThread class.</p>
 */
@Component(value = "csvProducerThread")
@Scope("prototype")
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class ApplicationExporterThread implements Runnable {

  private static Logger log = LoggerFactory.getLogger(ApplicationExporterThread.class);

  private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

  private static final String lb = System.getProperty("line.separator");

  @Autowired GoalExecutionRepository gexeRepository;

  @Autowired ApplicationRepository appRepository;

  private String separator = ";";

  private String[] includeSpaceProperties = null;

  private String[] includeGoalConfiguration = null;

  private String[] includeGoalSystemInfo = null;

  private String[] bugs = null;

  private StringBuffer buffer = new StringBuffer();

  private HashMap<Long, HashMap<String, Boolean>> affectedApps = null;

  private List<Application> apps = null;

  private ExportFormat format = null;

  private boolean includeAllBugs = false;

  private boolean includeExemptions = false;

  /**
   * <p>Setter for the field <code>separator</code>.</p>
   *
   * @param separator a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setSeparator(String separator) {
    this.separator = separator;
    return this;
  }

  /**
   * <p>Setter for the field <code>includeSpaceProperties</code>.</p>
   *
   * @param includeSpaceProperties an array of {@link java.lang.String} objects.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setIncludeSpaceProperties(String[] includeSpaceProperties) {
    this.includeSpaceProperties =
        includeSpaceProperties == null ? null : includeSpaceProperties.clone();
    return this;
  }

  /**
   * <p>Setter for the field <code>includeGoalConfiguration</code>.</p>
   *
   * @param includeGoalConfiguration an array of {@link java.lang.String} objects.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setIncludeGoalConfiguration(String[] includeGoalConfiguration) {
    this.includeGoalConfiguration =
        includeGoalConfiguration == null ? null : includeGoalConfiguration.clone();
    return this;
  }

  /**
   * <p>Setter for the field <code>includeGoalSystemInfo</code>.</p>
   *
   * @param includeGoalSystemInfo an array of {@link java.lang.String} objects.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setIncludeGoalSystemInfo(String[] includeGoalSystemInfo) {
    this.includeGoalSystemInfo =
        includeGoalSystemInfo == null ? null : includeGoalSystemInfo.clone();
    return this;
  }

  /**
   * <p>Setter for the field <code>apps</code>.</p>
   *
   * @param apps a {@link java.util.List} object.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setApps(List<Application> apps) {
    this.apps = apps;
    return this;
  }

  /**
   * <p>Setter for the field <code>bugs</code>.</p>
   *
   * @param bugs an array of {@link java.lang.String} objects.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setBugs(String[] bugs) {
    this.bugs = bugs == null ? null : bugs.clone();
    return this;
  }

  /**
   * <p>Getter for the field <code>affectedApps</code>.</p>
   *
   * @return a {@link java.util.HashMap} object.
   */
  public HashMap<Long, HashMap<String, Boolean>> getAffectedApps() {
    return affectedApps;
  }

  /**
   * <p>Setter for the field <code>affectedApps</code>.</p>
   *
   * @param affectedApps a {@link java.util.HashMap} object.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setAffectedApps(
      HashMap<Long, HashMap<String, Boolean>> affectedApps) {
    this.affectedApps = affectedApps;
    return this;
  }

  /**
   * <p>Getter for the field <code>format</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ExportFormat} object.
   */
  public ExportFormat getFormat() {
    return format;
  }

  /**
   * <p>Setter for the field <code>format</code>.</p>
   *
   * @param format a {@link org.eclipse.steady.shared.enums.ExportFormat} object.
   */
  public void setFormat(ExportFormat format) {
    this.format = format;
  }

  /**
   * <p>Getter for the field <code>buffer</code>.</p>
   *
   * @return a {@link java.lang.StringBuffer} object.
   */
  public StringBuffer getBuffer() {
    return buffer;
  }

  /**
   * <p>isIncludeAllBugs.</p>
   *
   * @return a boolean.
   */
  public boolean isIncludeAllBugs() {
    return includeAllBugs;
  }

  /**
   * <p>Setter for the field <code>includeAllBugs</code>.</p>
   *
   * @param includeAllBugs a boolean.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setIncludeAllBugs(boolean includeAllBugs) {
    this.includeAllBugs = includeAllBugs;
    return this;
  }

  /**
   * <p>isIncludeExemptions.</p>
   *
   * @return a boolean.
   */
  public boolean isIncludeExemptions() {
    return includeExemptions;
  }

  /**
   * <p>Setter for the field <code>includeExemptions</code>.</p>
   *
   * @param includeExemptions a boolean.
   * @return a {@link org.eclipse.steady.backend.component.ApplicationExporterThread} object.
   */
  public ApplicationExporterThread setIncludeExemptions(boolean includeExemptions) {
    this.includeExemptions = includeExemptions;
    return this;
  }

  /**
   * <p>run.</p>
   */
  @Transactional(
      readOnly = true,
      propagation =
          Propagation.REQUIRED) // Needed in order to lazy load properties when called async
  public void run() {
    // Show progress
    final StopWatch sw =
        new StopWatch(
            "Worker thread: Produce [" + this.format + "] for [" + apps.size() + "] apps");
    sw.setTotal(this.apps.size());
    sw.start();

    // Separate emails by ; unless it is the CSV column separator
    final String email_separator = separator.equals(";") ? "," : ";";

    for (Application a : this.apps) {

      // Always produce both and decide at the end
      final StringBuffer csv = new StringBuffer();
      final JsonBuilder json = new JsonBuilder().startObject();

      try {
        // Space
        csv.append(a.getSpace().getSpaceToken())
            .append(separator)
            .append(a.getSpace().getSpaceName())
            .append(separator);
        json.startObjectProperty("workspace");
        json.appendObjectProperty("token", a.getSpace().getSpaceToken())
            .appendObjectProperty("name", a.getSpace().getSpaceName());
        json.startArrayProperty("owners");
        if (a.getSpace().getSpaceOwners() != null && a.getSpace().getSpaceOwners().size() > 0) {
          int i = 0;
          for (String o : a.getSpace().getSpaceOwners()) {
            json.appendToArray(o);
            // Email addresses in CSV (comma or semicolon-separated)
            if (i++ > 0) csv.append(email_separator);
            csv.append(o);
          }
        }
        json.endArray();
        csv.append(separator);

        if (includeSpaceProperties != null && includeSpaceProperties.length > 0) {
          for (String p : includeSpaceProperties) {
            final String value = a.getSpace().getPropertyValue(p);
            csv.append(value == null ? "" : value).append(separator);
            json.appendObjectProperty(p, value);
          }
        }

        json.endObject();

        // Application
        csv.append(a.getId())
            .append(separator)
            .append(a.getMvnGroup())
            .append(separator)
            .append(a.getArtifact())
            .append(separator)
            .append(a.getVersion())
            .append(separator);
        csv.append(this.dateFormat.format(a.getCreatedAt().getTime()));
        // entry.append(separator).append(a.countDependencies()).append(separator).append(a.countConstructs());
        json.startObjectProperty("app");
        json.appendObjectProperty("id", a.getId().toString())
            .appendObjectProperty("group", a.getMvnGroup())
            .appendObjectProperty("artifact", a.getArtifact())
            .appendObjectProperty("version", a.getVersion());
        if (a.getCreatedAt() != null)
          json.appendObjectProperty(
              "createdAt", this.dateFormat.format(a.getCreatedAt().getTime()));
        else json.appendObjectProperty("createdAt", (String) null);
        json.endObject();

        // Is the current app affected by the given bugs
        if (!StringUtil.isEmptyOrContainsEmptyString(this.bugs) && this.affectedApps != null) {
          json.startObjectProperty("vulns");
          final HashMap<String, Boolean> affected_app = this.affectedApps.get(a.getId());
          for (String b : this.bugs) {
            Boolean affected = false;
            if (affected_app != null && affected_app.containsKey(b)) {
              affected = affected_app.get(b); // Can be true or null
            }
            if (affected == null) {
              csv.append(separator).append("unconfirmed");
              json.appendObjectProperty(b, "unconfirmed");
            } else if (affected == true) {
              csv.append(separator).append("affected");
              json.appendObjectProperty(b, "affected");
            } else {
              csv.append(separator).append("not affected");
              json.appendObjectProperty(b, "not affected");
            }
          }
          json.endObject();
        }

        // Include all vulnerable dependencies of the current app (JSON format only)
        if (ExportFormat.JSON.equals(this.getFormat()) && this.includeAllBugs) {
          json.startArrayProperty("vulnerableDependencies");
          final TreeSet<VulnerableDependency> vd_all =
              this.appRepository.findAppVulnerableDependencies(a, this.includeExemptions, false);
          for (VulnerableDependency vd : vd_all) {
            json.appendJsonToArray(JacksonUtil.asJsonString(vd, null, Views.Default.class));
          }
          json.endArray();
        }

        // Stuff from goal execution
        final GoalExecution latest_goal_exe = gexeRepository.findLatestGoalExecution(a, null);
        if (latest_goal_exe != null) {
          csv.append(separator)
              .append(this.dateFormat.format(latest_goal_exe.getCreatedAt().getTime()))
              .append(separator)
              .append(latest_goal_exe.getClientVersion());
          json.startObjectProperty("lastGoalExecution");
          json.appendObjectProperty(
                  "timestamp", this.dateFormat.format(latest_goal_exe.getCreatedAt().getTime()))
              .appendObjectProperty("client", latest_goal_exe.getClientVersion());

          // Goal config
          if (!StringUtil.isEmptyOrContainsEmptyString(this.includeGoalConfiguration)) {
            // log.info("Get goal configuration for " + latest_goal_exe); //TODO: Delete
            for (String p : includeGoalConfiguration) {
              final String prop = latest_goal_exe.getConfiguration(p);
              csv.append(separator).append(prop == null ? "" : prop);
              json.appendObjectProperty(p, prop);
            }
          }

          // Sys info
          if (!StringUtil.isEmptyOrContainsEmptyString(includeGoalSystemInfo)) {
            // log.info("Get system info for " + latest_goal_exe); //TODO: Delete
            for (String p : includeGoalSystemInfo) {
              final String prop = latest_goal_exe.getSystemInfo(p);
              csv.append(separator).append(prop == null ? "" : prop);
              json.appendObjectProperty(p, prop);
            }
          }
          json.endObject();
        } else {
          csv.append(separator)
              .append("")
              .append(separator)
              .append("")
              .append(separator)
              .append("");
          json.appendObjectProperty("lastGoalExecution", (String) null);

          // Goal config
          if (!StringUtil.isEmptyOrContainsEmptyString(this.includeGoalConfiguration))
            for (String p : includeGoalConfiguration) csv.append(separator).append("");

          // Sys info
          if (!StringUtil.isEmptyOrContainsEmptyString(includeGoalSystemInfo))
            for (String p : includeGoalSystemInfo) csv.append(separator).append("");
        }

        csv.append(lb);
        json.endObject();

        // Append to buffer according to format
        if (ExportFormat.CSV.equals(this.format)) buffer.append(csv);
        else buffer.append(buffer.length() == 0 ? "" : ",").append(json.toString());

        sw.progress();
      } catch (Exception e) {
        log.error(
            "["
                + e.getClass().getName()
                + "] while appending data for app "
                + a
                + ", entry ["
                + csv.toString()
                + "] will not be appended to ["
                + this.format
                + "]: "
                + e.getMessage());
      }
    }
    sw.stop();
  }
}
