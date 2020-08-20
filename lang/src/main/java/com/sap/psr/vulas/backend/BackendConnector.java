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
package com.sap.psr.vulas.backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;

import org.apache.http.HttpStatus;

import com.sap.psr.vulas.backend.requests.BasicHttpRequest;
import com.sap.psr.vulas.backend.requests.ConditionalHttpRequest;
import com.sap.psr.vulas.backend.requests.HttpRequest;
import com.sap.psr.vulas.backend.requests.HttpRequestList;
import com.sap.psr.vulas.backend.requests.PutLibraryCondition;
import com.sap.psr.vulas.backend.requests.StatusCondition;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.AffectedLibrary;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.Bug;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.json.model.view.Views;
import com.sap.psr.vulas.shared.util.FileSearch;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;

/**
 * To be used for interacting with the RESTful backend API.
 */
public class BackendConnector {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /* Singleton instance. */
  private static BackendConnector instance = null;

  /**
   * Cache of change lists for bugs, relevant for given applications.
   * The map is populated in method {@link BackendConnector#getAppBugs(Application)}.
   */
  private Map<Application, Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>>>
      cacheBugChangeLists = new HashMap<Application, Map<String, Set<ConstructId>>>();

  /**
   * Cache the presence of the space in the backend.
   */
  private Map<Space, Boolean> cacheSpaceExistanceCheck = new HashMap<Space, Boolean>();

  /**
   * Cache the presence of the application in the backend.
   */
  private Map<Application, Boolean> cacheAppExistanceCheck = new HashMap<Application, Boolean>();

  /**
   * Cache app dependencies.
   */
  private Map<Application, Set<Dependency>> cacheAppDependencies =
      new HashMap<Application, Set<Dependency>>();

  /**
   * Cache app constructs.
   */
  private Map<Application, Set<ConstructId>> cacheAppConstructs =
      new HashMap<Application, Set<ConstructId>>();

  /**
   * <p>Constructor for BackendConnector.</p>
   */
  protected BackendConnector() {
    super();
  }

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.BackendConnector} object.
   */
  public static synchronized BackendConnector getInstance() {
    if (instance == null) instance = new BackendConnector();
    return instance;
  }

  // TODO: Make all caches dependent on space and/or app!
  /**
   * <p>cleanCache.</p>
   */
  public void cleanCache() {
    // if(!this.cacheBugChangeLists.isEmpty() || !this.cacheAppExistanceCheck.isEmpty()) {
    BackendConnector.log.info(
        "Deleting cache: ["
            + this.cacheBugChangeLists.size()
            + "] bug change lists, ["
            + this.cacheAppExistanceCheck.size()
            + "] app existance");
    this.cacheBugChangeLists = new HashMap<Application, Map<String, Set<ConstructId>>>();
    this.cacheSpaceExistanceCheck = new HashMap<Space, Boolean>();
    this.cacheAppExistanceCheck = new HashMap<Application, Boolean>();
    this.cacheAppDependencies = new HashMap<Application, Set<Dependency>>();
    this.cacheAppConstructs = new HashMap<Application, Set<ConstructId>>();
    // }
  }

  // ---------------------------------- SPACE-RELATED CALLS

  /**
   * Returns true if the given {@link Space} exists in the backend, false otherwise.
   * If the client is {@link CoreConfiguration.ConnectType#OFFLINE}, the check is skipped and true is returned.
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _space a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException
   * @return a boolean.
   */
  public boolean isSpaceExisting(GoalContext _goal_context, Space _space)
      throws BackendConnectionException {
    Boolean exists = false;
    if (!cacheSpaceExistanceCheck.containsKey(_space)) {

      // Don't check if client is OFFLINE
      if (CoreConfiguration.isBackendOffline(_goal_context.getVulasConfiguration())) {
        exists = true;
      }
      // Check whether workspace exists in backend
      else {
        final BasicHttpRequest request =
            new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.space(_space), null);
        request.setGoalContext(_goal_context);
        final HttpResponse response = request.send();
        exists = response != null && response.isOk();
      }
      cacheSpaceExistanceCheck.put(_space, exists);
    }
    return cacheSpaceExistanceCheck.get(_space);
  }

  /**
   * <p>createSpace.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _space a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   * @return a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Space createSpace(GoalContext _goal_context, Space _space)
      throws BackendConnectionException {
    final BasicHttpRequest r = new BasicHttpRequest(HttpMethod.POST, PathBuilder.spaces(), null);
    r.setGoalContext(_goal_context);
    r.setPayload(JacksonUtil.asJsonString(_space), null, true);
    final HttpResponse response = r.send();

    // Read and return the response to the caller (including the server-side generated space token)
    Space created_space = null;
    if (response != null && response.isCreated()) {
      try {
        created_space = (Space) JacksonUtil.asObject(response.getBody(), Space.class);
      } catch (Exception e) {
        throw new BackendConnectionException(
            "Cannot deseriale the newly created space: " + e.getMessage(), e);
      }
    }
    return created_space;
  }

  /**
   * <p>modifySpace.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _space a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void modifySpace(GoalContext _goal_context, Space _space)
      throws BackendConnectionException {
    final BasicHttpRequest r =
        new BasicHttpRequest(HttpMethod.PUT, PathBuilder.space(_space), null);
    r.setGoalContext(_goal_context);
    r.setPayload(JacksonUtil.asJsonString(_space), null, true);
    r.send();
  }

  /**
   * <p>cleanSpace.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _space a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void cleanSpace(GoalContext _goal_context, Space _space)
      throws BackendConnectionException {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("clean", "true");
    final BasicHttpRequest r =
        new BasicHttpRequest(HttpMethod.POST, PathBuilder.space(_space), params);
    r.setGoalContext(_goal_context);
    r.send();
  }

  /**
   * <p>deleteSpace.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _space a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void deleteSpace(GoalContext _goal_context, Space _space)
      throws BackendConnectionException {
    final BasicHttpRequest r =
        new BasicHttpRequest(HttpMethod.DELETE, PathBuilder.space(_space), null);
    r.setGoalContext(_goal_context);
    r.send();
  }

  // ---------------------------------- APP-RELATED CALLS

  /**
   * <p>isAppExisting.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @return a boolean.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public boolean isAppExisting(GoalContext _goal_context, Application _app)
      throws BackendConnectionException {
    Boolean exists = false;
    if (!cacheAppExistanceCheck.containsKey(_app)) {
      final BasicHttpRequest r =
          new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.app(_app), null);
      r.setGoalContext(_goal_context);
      final HttpResponse response = r.send();
      exists = response != null && response.isOk();
      cacheAppExistanceCheck.put(_app, exists);
    }
    return cacheAppExistanceCheck.get(_app);
  }

  /**
   * <p>cleanApp.</p>
   *
   * @param _goal_context TODO
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _clean_history a boolean.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException
   */
  public void cleanApp(GoalContext _goal_context, Application _app, boolean _clean_history)
      throws BackendConnectionException {
    if (this.isAppExisting(_goal_context, _app)) {
      final Map<String, String> params = new HashMap<String, String>();
      params.put("clean", "true");
      params.put("cleanGoalHistory", Boolean.toString(_clean_history));
      final BasicHttpRequest req =
          new BasicHttpRequest(HttpMethod.POST, PathBuilder.app(_app), params);
      req.setGoalContext(_goal_context);
      req.send();
    }
  }

  /**
   * <p>purgeAppVersions.</p>
   *
   * @param _goal_context TODO
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _keep a int.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void purgeAppVersions(GoalContext _goal_context, Application _app, int _keep)
      throws BackendConnectionException {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("keep", Integer.toString(_keep));
    params.put("mode", "VERSIONS"); // Mode DAYS is not yet support on client-side
    final BasicHttpRequest req =
        new BasicHttpRequest(HttpMethod.DELETE, PathBuilder.artifact(_app), params);
    req.setGoalContext(_goal_context);
    req.send();
  }

  /**
   * <p>uploadApp.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadApp(GoalContext _goal_context, Application _app)
      throws BackendConnectionException {
    final String json = JacksonUtil.asJsonString(_app, null, Views.Default.class);

    // The request depending on whose result either POST or PUT will be called
    final BasicHttpRequest cond_req =
        new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.app(_app), null);
    cond_req.setGoalContext(_goal_context);

    final HttpRequestList req_list = new HttpRequestList();
    final Map<String, String> params = new HashMap<String, String>();
    params.put("skipResponseBody", "true");
    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.POST, PathBuilder.apps(), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND))
            .setPayload(json, null, true)
            .setGoalContext(_goal_context));
    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.PUT, PathBuilder.app(_app), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_OK))
            .setPayload(json, null, true)
            .setGoalContext(_goal_context));
    req_list.send();

    // Clean app existance cache
    this.cleanCache();
  }

  /**
   * <p>uploadReachableConstructs.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _lib_digest a {@link java.lang.String} object.
   * @param _json a {@link java.lang.String} object.
   * @return a boolean.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public boolean uploadReachableConstructs(
      GoalContext _goal_context, Application _app, String _lib_digest, String _json)
      throws BackendConnectionException {
    if (this.isAppExisting(_goal_context, _app)) {
      final BasicHttpRequest req =
          new BasicHttpRequest(
              HttpMethod.POST, PathBuilder.appReachableConstructs(_app, _lib_digest), null);
      req.setGoalContext(_goal_context);
      req.setPayload(_json, null, true);
      final HttpResponse response = req.send();
      return response == null || response.isOk();
    } else {
      return false;
    }
  }

  /**
   * <p>uploadTouchPoints.</p>
   *
   * @param _goal_context a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _lib_digest a {@link java.lang.String} object.
   * @param _json a {@link java.lang.String} object.
   * @return a boolean.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public boolean uploadTouchPoints(
      GoalContext _goal_context, Application _app, String _lib_digest, String _json)
      throws BackendConnectionException {
    if (this.isAppExisting(_goal_context, _app)) {
      final Map<String, String> params = new HashMap<String, String>();
      params.put("skipResponseBody", "true");
      final BasicHttpRequest req =
          new BasicHttpRequest(
              HttpMethod.POST, PathBuilder.appTouchPoints(_app, _lib_digest), params);
      req.setGoalContext(_goal_context);
      req.setPayload(_json, null, true);
      final HttpResponse response = req.send();
      return response == null || response.isOk();
    } else {
      return false;
    }
  }

  /**
   * <p>getAppConstructIds.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @return a {@link java.util.Set} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Set<ConstructId> getAppConstructIds(GoalContext _ctx, Application _app)
      throws BackendConnectionException {
    if (!cacheAppConstructs.containsKey(_app)) {
      final boolean app_exists = this.isAppExisting(_ctx, _app);
      final Set<ConstructId> constructs = new HashSet<ConstructId>();
      if (app_exists) {
        final BasicHttpRequest r =
            new BasicHttpRequest(HttpMethod.GET, PathBuilder.appConstructIds(_app), null);
        r.setGoalContext(_ctx);
        final String json = r.send().getBody();
        final ConstructId[] backend_app_construct_ids =
            (ConstructId[]) JacksonUtil.asObject(json, ConstructId[].class);
        for (ConstructId backend_app_construct_id : backend_app_construct_ids) {
          try {
            constructs.add(backend_app_construct_id);
          } catch (IllegalArgumentException e) {
            BackendConnector.log.error(
                "Error while transforming backend to client entity: " + e.getMessage(), e);
          }
        }
        cacheAppConstructs.put(_app, constructs);
        BackendConnector.log.info(
            "["
                + backend_app_construct_ids.length
                + "] app constructs received from backend, ["
                + constructs.size()
                + "] transformed to client representation");
      }
    }
    return cacheAppConstructs.get(_app);
  }

  /**
   * Retrieves the change lists of all bugs relevant for the given application from the backend.
   *
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @return a {@link java.util.Map} object.
   */
  public Map<String, Set<ConstructId>> getAppBugs(GoalContext _ctx, Application _app)
      throws BackendConnectionException {
    // Make request and put in cache
    if (!this.cacheBugChangeLists.containsKey(_app)) {

      boolean app_exists = this.isAppExisting(_ctx, _app);
      final Map<String, Set<ConstructId>> changes = new HashMap<String, Set<ConstructId>>();
      int construct_count = 0;
      final Map<String, String> params = new HashMap<String, String>();
      params.put("historical", "false");
      if (app_exists) {
        final BasicHttpRequest r =
            new BasicHttpRequest(HttpMethod.GET, PathBuilder.appBugs(_app), params);
        r.setGoalContext(_ctx);
        final String json = r.send().getBody();
        final Bug[] bugs = (Bug[]) JacksonUtil.asObject(json, Bug[].class);
        Set<ConstructId> changes_set = null;
        ConstructId json_cid = null;
        for (Bug b : bugs) {

          // Get the change list for the current bug id (or create it)
          changes_set = changes.get(b.getBugId());
          if (changes_set == null) {
            changes_set = new HashSet<ConstructId>();
            changes.put(b.getBugId(), changes_set);
          }

          // Add constructs
          for (ConstructChange cc : b.getConstructChanges()) {
            json_cid = cc.getConstructId();
            if (json_cid.getLang().equals(ProgrammingLanguage.JAVA)
                && !cc.getConstructChangeType().equals(ConstructChangeType.ADD)) {
              changes_set.add(json_cid);
            }
          }

          // Total number of constructs received from the backend
          construct_count += changes_set.size();
        }
      }
      BackendConnector.log.info(
          "["
              + construct_count
              + "] constructs for ["
              + changes.keySet().size()
              + "] bugs received from backend");

      // Put in cache
      this.cacheBugChangeLists.put(_app, changes);
    }

    // Return from cache
    return this.cacheBugChangeLists.get(_app);
  }

  /**
   * Retrieves the change lists of the given bug(s) from the backend. Note that only bugs relevant for the given application
   * are included.
   *
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _filter Comma-separated list of bug identifiers
   * @throws com.sap.psr.vulas.backend.BackendConnectionException
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @return a {@link java.util.Map} object.
   */
  public Map<String, Set<ConstructId>> getAppBugs(
      GoalContext _ctx, Application _app, String _filter) throws BackendConnectionException {
    // Return all change lists
    if (_filter == null || _filter.equals("")) {
      return this.getAppBugs(_ctx, _app);
    }
    // Filter
    else {
      final Map<String, Set<ConstructId>> all_change_lists = this.getAppBugs(_ctx, _app);
      final Map<String, Set<ConstructId>> filtered_change_list =
          new HashMap<String, Set<ConstructId>>();

      // Build filter
      final StringList filter = new StringList();
      filter.addAll(_filter, ",", true);

      // Filter
      for (Map.Entry<String, Set<ConstructId>> entry : all_change_lists.entrySet())
        if (filter.contains(
            entry.getKey(), ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE))
          filtered_change_list.put(entry.getKey(), entry.getValue());

      return filtered_change_list;
    }
  }

  /*public VulnerableDependency[] getVulnerableAppArchiveConstructs(Application _app) throws BackendConnectionException {
  	final BasicHttpRequest req = new BasicHttpRequest(HttpMethod.GET, PathBuilder.vulnArchiveConstructs(_app), null);
  	final VulnerableDependency[] vulndeps = (VulnerableDependency[])JacksonUtil.asObject(req.send().getBody(), VulnerableDependency[].class);
  	return vulndeps;
  }*/

  /*public VulnerableDependency[] getVulnerableDependencies(Application _app) throws BackendConnectionException {
  	final BasicHttpRequest req = new BasicHttpRequest(HttpMethod.GET, PathBuilder.vulnArchiveConstructs(_app), null);
  	final VulnerableDependency[] vulndeps = (VulnerableDependency[])JacksonUtil.asObject(req.send().getBody(), VulnerableDependency[].class);
  	return vulndeps;
  }*/

  /*public VulnerableDependency getVulnerableAppArchiveDependencyConstructs(Application _app, String _sha1, String _bugId) throws BackendConnectionException{
  	final BasicHttpRequest req = new BasicHttpRequest(HttpMethod.GET,PathBuilder.vulnerableDependencyConstructs(_app, _sha1, _bugId), null);
  	VulnerableDependency vd = (VulnerableDependency)JacksonUtil.asObject(req.send().getBody(), VulnerableDependency.class);
  	return vd;
  }*/

  /**
   * <p>getAppDeps.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @return a {@link java.util.Set} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Set<Dependency> getAppDeps(GoalContext _ctx, Application _app)
      throws BackendConnectionException {
    if (!cacheAppDependencies.containsKey(_app)) {
      final Set<Dependency> deps = new HashSet<Dependency>();
      final boolean app_exists = this.isAppExisting(_ctx, _app);
      if (app_exists) {
        final String json =
            new BasicHttpRequest(HttpMethod.GET, PathBuilder.appDeps(_app), null)
                .setGoalContext(_ctx)
                .send()
                .getBody();
        final Dependency[] deps_array =
            (Dependency[]) JacksonUtil.asObject(json, Dependency[].class);
        deps.addAll(Arrays.asList(deps_array));
      }
      cacheAppDependencies.put(_app, deps);
      BackendConnector.log.info(
          "[" + deps.size() + "] dependencies for app " + _app + " received from backend");
    }
    return cacheAppDependencies.get(_app);
  }

  /**
   * <p>getAppVulnDeps.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _include_historical a boolean.
   * @param _include_affected a boolean.
   * @param _include_affected_unconfirmed a boolean.
   * @return a {@link java.util.Set} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Set<VulnerableDependency> getAppVulnDeps(
      GoalContext _ctx,
      Application _app,
      boolean _include_historical,
      boolean _include_affected,
      boolean _include_affected_unconfirmed)
      throws BackendConnectionException {
    final Set<VulnerableDependency> vuln_deps = new HashSet<VulnerableDependency>();
    final boolean app_exists = this.isAppExisting(_ctx, _app);
    if (app_exists) {
      final String json =
          new BasicHttpRequest(
                  HttpMethod.GET,
                  PathBuilder.appVulnDeps(
                      _app, _include_historical, _include_affected, _include_affected_unconfirmed),
                  null)
              .setGoalContext(_ctx)
              .send()
              .getBody();
      final VulnerableDependency[] vuln_deps_array =
          (VulnerableDependency[]) JacksonUtil.asObject(json, VulnerableDependency[].class);
      vuln_deps.addAll(Arrays.asList(vuln_deps_array));
    }
    BackendConnector.log.info(
        "["
            + vuln_deps.size()
            + "] vulnerable dependencies for app "
            + _app
            + " received from backend");
    return vuln_deps;
  }

  /**
   * <p>getVulnDeps.</p>
   *
   * @param unconfirmedOnly a {@link java.lang.Boolean} object.
   * @return an array of {@link com.sap.psr.vulas.shared.json.model.VulnerableDependency} objects.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public VulnerableDependency[] getVulnDeps(Boolean unconfirmedOnly)
      throws BackendConnectionException {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("unconfirmedOnly", unconfirmedOnly.toString());
    final BasicHttpRequest req =
        new BasicHttpRequest(HttpMethod.GET, PathBuilder.vulnDeps(), params);
    final VulnerableDependency[] vulndeps =
        (VulnerableDependency[])
            JacksonUtil.asObject(req.send().getBody(), VulnerableDependency[].class);
    return vulndeps;
  }

  /**
   * Returns all {@link ConstructId}s that have been previously traced for the given {@link Application}.
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @return a {@link java.util.Set} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Set<ConstructId> getAppTraces(GoalContext _ctx, @NotNull Application _app)
      throws BackendConnectionException {
    boolean app_exists = this.isAppExisting(_ctx, _app);
    final Set<ConstructId> constructs = new HashSet<ConstructId>();
    if (app_exists) {
      final String json =
          new BasicHttpRequest(HttpMethod.GET, PathBuilder.appTraces(_app), null)
              .setGoalContext(_ctx)
              .send()
              .getBody();
      final com.sap.psr.vulas.shared.json.model.Trace[] backend_traces =
          (com.sap.psr.vulas.shared.json.model.Trace[])
              JacksonUtil.asObject(json, com.sap.psr.vulas.shared.json.model.Trace[].class);
      for (com.sap.psr.vulas.shared.json.model.Trace backend_trace : backend_traces) {
        try {
          constructs.add(backend_trace.getConstructId());
        } catch (IllegalArgumentException e) {
          BackendConnector.log.error(
              "Error while transforming backend to client entity: " + e.getMessage(), e);
        }
      }
      BackendConnector.log.info(
          "["
              + backend_traces.length
              + "] traces received from backend, ["
              + constructs.size()
              + "] transformed to client representation");
    }
    return constructs;
  }

  /**
   * Returns all {@link Dependency}s of the given {@link Application} including their reachable {@link ConstructId}s.
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @return a {@link java.util.Set} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Set<Dependency> getAppDependencies(GoalContext _ctx, @NotNull Application _app)
      throws BackendConnectionException {
    boolean app_exists = this.isAppExisting(_ctx, _app);
    final Set<Dependency> deps = new HashSet<Dependency>();
    if (app_exists) {
      final String json =
          new BasicHttpRequest(HttpMethod.GET, PathBuilder.appReachableConstructIds(_app), null)
              .setGoalContext(_ctx)
              .send()
              .getBody();
      com.sap.psr.vulas.shared.json.model.Dependency[] backend_deps = null;
      if (json != null)
        backend_deps =
            (com.sap.psr.vulas.shared.json.model.Dependency[])
                JacksonUtil.asObject(json, com.sap.psr.vulas.shared.json.model.Dependency[].class);
      else backend_deps = new com.sap.psr.vulas.shared.json.model.Dependency[] {};
      deps.addAll(Arrays.asList(backend_deps));
      BackendConnector.log.info("[" + deps.size() + "] dependencies received from backend");
    }
    return deps;
  }

  private static final Pattern pattern = Pattern.compile("\\\"countTotal\\\"\\s*:\\s*([\\d]*)");

  // ---------------------------------- LIB-RELATED CALLS

  /**
   * <p>getLibrary.</p>
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.EntityNotFoundInBackendException if any.
   */
  public String getLibrary(String _sha1) throws EntityNotFoundInBackendException {
    HttpResponse response = null;
    try {
      response = new BasicHttpRequest(HttpMethod.GET, PathBuilder.lib(_sha1), null).send();
      if (response.isNotFound())
        throw new EntityNotFoundInBackendException(
            "Library with SHA1 [" + _sha1 + "] not found in backend");
      else return response.getBody();
    } catch (BackendConnectionException e) {
      throw new EntityNotFoundInBackendException(
          "Library with SHA1 [" + _sha1 + "] not found in backend");
    }
  }

  /**
   * <p>countLibraryConstructs.</p>
   *
   * @param _ja a {@link java.lang.String} object.
   * @return a int.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public int countLibraryConstructs(String _ja) throws BackendConnectionException {
    int count_existing = -1;
    String http_response = null;
    try {
      http_response = this.getLibrary(_ja);

      // Use pattern matching to read number of constructs (rather than using JayWay JSonPath)
      final Matcher m = pattern.matcher(http_response);
      if (m.find()) count_existing = Integer.parseInt(m.group(1));
    } catch (NumberFormatException e) {
      final BackendConnectionException bce =
          new BackendConnectionException("Expected number at JSON property $.countTotal", e);
      bce.setHttpResponseBody(http_response);
      throw bce;
    } catch (EntityNotFoundInBackendException e) {
      count_existing = -1;
    }
    return count_existing;
  }

  /**
   * <p>uploadLibrary.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _lib a {@link com.sap.psr.vulas.shared.json.model.Library} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public synchronized void uploadLibrary(GoalContext _ctx, Library _lib)
      throws BackendConnectionException {
    final String sha1 = _lib.getDigest();
    final String json = JacksonUtil.asJsonString(_lib, null, Views.LibDetails.class);
    // Override setting
    final boolean override =
        _ctx.getVulasConfiguration()
            .getConfiguration()
            .getBoolean("collector.overrideArchive", false);

    final HttpRequestList req_list = new HttpRequestList();
    final BasicHttpRequest cond_req =
        new BasicHttpRequest(HttpMethod.GET, PathBuilder.lib(sha1), null);
    cond_req.setGoalContext(_ctx);

    final Map<String, String> params = new HashMap<String, String>();
    params.put("skipResponseBody", "true");

    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.POST, PathBuilder.libs(), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND))
            .setPayload(json, null, false)
            .setGoalContext(_ctx));
    if (override) {
      BackendConnector.log.info("collector.overrideArchive is enabled");
      req_list.addRequest(
          new ConditionalHttpRequest(HttpMethod.PUT, PathBuilder.lib(sha1), params)
              .setConditionRequest(cond_req)
              .addCondition(new StatusCondition(HttpURLConnection.HTTP_OK))
              .setPayload(json, null, false)
              .setGoalContext(_ctx));
    } else if (!_ctx.getVulasConfiguration()
        .getConfiguration()
        .getBoolean("skipKnownArchive", false)) {
      req_list.addRequest(
          new ConditionalHttpRequest(HttpMethod.PUT, PathBuilder.lib(sha1), params)
              .setConditionRequest(cond_req)
              .addCondition(new StatusCondition(HttpURLConnection.HTTP_OK))
              .addCondition(new PutLibraryCondition(_lib))
              .setPayload(json, null, false)
              .setGoalContext(_ctx));
    }

    req_list.send();
  }

  /**
   * <p>uploadLibraryFile.</p>
   *
   * @param _sha1 a {@link java.lang.String} object.
   * @param _file a {@link java.nio.file.Path} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadLibraryFile(String _sha1, Path _file) throws BackendConnectionException {
    try (final FileInputStream inputStream = new FileInputStream(_file.toFile())) {
      final HttpRequestList req_list = new HttpRequestList();
      final BasicHttpRequest cond_req =
          new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.libupload(_sha1));
      final Map<String, String> params = new HashMap<String, String>();
      params.put("skipResponseBody", "true");
      req_list.addRequest(
          new ConditionalHttpRequest(HttpMethod.POST, PathBuilder.libupload(_sha1), params)
              .setConditionRequest(cond_req)
              .addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND))
              //	.addCondition(new ContentCondition("\\\"wellknownSha1\\\"\\s*:\\s*([a-zA-Z]*)",
              // ContentCondition.Mode.EQ_STRING, "true"))
              .setBinPayload(inputStream, "application/octet-stream"));
      req_list.send();
    } catch (FileNotFoundException e) {
      BackendConnector.log.error(
          "Cannot find ["
              + _file.toString()
              + "]: Check if unknown to Maven and upload will be skipped");
    } catch (IOException e) {
      BackendConnector.log.error(
          "Exception when uploading [" + _file.toString() + "]: " + e.getMessage());
    }
  }

  // ==================== Others

  /**
   * Returns true if the upload succeeded or the upload cannot be performed (because the application does not exist), false otherwise.
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException
   * @param _gexe a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
   * @param _before a boolean.
   * @return a boolean.
   */
  public boolean uploadGoalExecution(GoalContext _ctx, AbstractGoal _gexe, boolean _before)
      throws BackendConnectionException {
    boolean ret = false;

    // Application goal
    if (_ctx.getApplication() != null) {

      // Make sure the app exists in the backend
      final Application app = _ctx.getApplication();
      if (this.isAppExisting(_ctx, app)) {

        // The request depending on whose result either POST or PUT will be called
        final BasicHttpRequest cond_req =
            new BasicHttpRequest(
                HttpMethod.OPTIONS,
                PathBuilder.goalExcecution(null, _ctx.getSpace(), app, _gexe.getId()),
                null);
        cond_req.setGoalContext(_ctx);

        // Create conditional requests for POST or PUT
        final Map<String, String> params = new HashMap<String, String>();
        params.put("skipResponseBody", "true");

        final ConditionalHttpRequest post =
            new ConditionalHttpRequest(
                HttpMethod.POST, PathBuilder.goalExcecutions(null, _ctx.getSpace(), app), params);
        post.setConditionRequest(cond_req);
        post.addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND));
        post.setPayload(_gexe.toJson(), null, false);
        post.setGoalContext(_ctx);

        final ConditionalHttpRequest put =
            new ConditionalHttpRequest(
                HttpMethod.PUT,
                PathBuilder.goalExcecution(null, _ctx.getSpace(), app, _gexe.getId()),
                params);
        put.setConditionRequest(cond_req);
        put.addCondition(new StatusCondition(HttpURLConnection.HTTP_OK));
        put.setPayload(_gexe.toJson(), null, false);
        put.setGoalContext(_ctx);

        // Order them depending whether the upload is called before or after the actual goal
        // execution (relevant for serialization)
        final HttpRequestList req_list = new HttpRequestList();
        if (_before) {
          req_list.addRequest(post);
          req_list.addRequest(put);
        } else {
          req_list.addRequest(put);
          req_list.addRequest(post);
        }

        // Send and check response code
        final HttpResponse response = req_list.send();
        if (CoreConfiguration.isBackendReadWrite(_ctx.getVulasConfiguration()))
          ret = response != null && (response.isCreated() || response.isOk());
        else ret = true;
      } else {
        BackendConnector.log.info(
            "App "
                + _ctx.getApplication()
                + " does not exist in backend, upload of goal execution ["
                + _gexe.getId()
                + "] skipped");
        ret = true;
      }
    }
    // Space goal
    else {
      // TODO: Allow saving of workspace-specific goal executions (e.g., cleanspace)
      BackendConnector.log.warn("Upload of space goals not yet implemented");
      ret = false;
    }
    return ret;
  }

  /**
   * <p>uploadTraces.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _json a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadTraces(GoalContext _ctx, Application _app, String _json)
      throws BackendConnectionException {
    if (this.isAppExisting(_ctx, _app)) {
      final Map<String, String> params = new HashMap<String, String>();
      params.put("skipResponseBody", "true");
      final BasicHttpRequest req =
          new BasicHttpRequest(HttpMethod.POST, PathBuilder.appTraces(_app), params);
      req.setGoalContext(_ctx);
      req.setPayload(_json, null, false);
      req.send();
    } else {
      BackendConnector.log.warn("App " + _app + " does not exist in backend, trace upload skipped");
    }
  }

  /**
   * <p>uploadPaths.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   * @param _json a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadPaths(GoalContext _ctx, Application _app, String _json)
      throws BackendConnectionException {
    if (this.isAppExisting(_ctx, _app)) {
      final Map<String, String> params = new HashMap<String, String>();
      params.put("skipResponseBody", "true");
      final BasicHttpRequest req =
          new BasicHttpRequest(HttpMethod.POST, PathBuilder.appPaths(_app), params);
      req.setGoalContext(_ctx);
      req.setPayload(_json, null, false);
      req.send();
    } else {
      BackendConnector.log.warn("App " + _app + " does not exist in backend, path upload skipped");
    }
  }

  /**
   * <p>isBugExisting.</p>
   *
   * @param _bug a {@link java.lang.String} object.
   * @return a boolean.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public boolean isBugExisting(String _bug) throws BackendConnectionException {
    final HttpResponse response =
        new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.bug(_bug), null).send();
    return response.isOk();
  }

  /**
   * <p>uploadChangeList.</p>
   *
   * @param _bug a {@link java.lang.String} object.
   * @param _json a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadChangeList(String _bug, String _json) throws BackendConnectionException {

    // The request depending on whose result either POST or PUT will be called
    final BasicHttpRequest cond_req =
        new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.bug(_bug), null);

    final HttpRequestList req_list = new HttpRequestList();
    final Map<String, String> params = new HashMap<String, String>();
    params.put("skipResponseBody", "true");
    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.POST, PathBuilder.bugs(null), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND))
            .setPayload(_json, null, false));
    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.PUT, PathBuilder.bug(_bug), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_OK))
            .setPayload(_json, null, false));
    req_list.send();
  }

  /**
   * <p>uploadCheckVersionResults.</p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @param _json a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadCheckVersionResults(String _bugId, String _json)
      throws BackendConnectionException {
    final HashMap<String, String> params = new HashMap<String, String>();
    params.put("source", "CHECK_VERSION");
    final HttpRequestList req_list = new HttpRequestList();
    final BasicHttpRequest cond_req =
        new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.bugAffectedLibs(_bugId), params);
    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.POST, PathBuilder.bugAffectedLibs(_bugId), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND))
            .setPayload(_json, null, false));

    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.PUT, PathBuilder.bugAffectedLibs(_bugId), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_OK))
            .setPayload(_json, null, false));

    req_list.send();
  }

  /**
   * <p>getBugAffectedLibraries.</p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @param _source a {@link java.lang.String} object.
   * @return an array of {@link com.sap.psr.vulas.shared.json.model.AffectedLibrary} objects.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public AffectedLibrary[] getBugAffectedLibraries(
      GoalContext _g, String _bugId, String _source, Boolean _onlyWellKnown)
      throws BackendConnectionException {
    final HashMap<String, String> params = new HashMap<String, String>();
    params.put("source", _source);
    params.put("onlyWellKnown", _onlyWellKnown.toString());
    BasicHttpRequest request =
        new BasicHttpRequest(HttpMethod.GET, PathBuilder.bugAffectedLibs(_bugId), params);
    if (_g != null) request.setGoalContext(_g);
    final String json = request.send().getBody();
    return (AffectedLibrary[]) JacksonUtil.asObject(json, AffectedLibrary[].class);
  }

  /**
   * <p>deletePatchEvalResults.</p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @param _source a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void deletePatchEvalResults(String _bugId, AffectedVersionSource _source)
      throws BackendConnectionException {
    final HashMap<String, String> params = new HashMap<String, String>();
    params.put("source", _source.toString());
    final BasicHttpRequest del_req =
        new BasicHttpRequest(HttpMethod.DELETE, PathBuilder.bugAffectedLibs(_bugId), params);
    // payload cannot be empty otherwise request doesn t work
    del_req.setPayload("[]", "application/json", true);
    del_req.send();
  }

  /**
   * <p>uploadPatchEvalResults.</p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @param _json a {@link java.lang.String} object.
   * @param _source a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public void uploadBugAffectedLibraries(
      GoalContext _g, String _bugId, String _json, AffectedVersionSource _source)
      throws BackendConnectionException {
    final HashMap<String, String> params = new HashMap<String, String>();
    params.put("source", _source.toString());

    final HttpRequestList req_list = new HttpRequestList();

    final BasicHttpRequest cond_req =
        new BasicHttpRequest(HttpMethod.OPTIONS, PathBuilder.bugAffectedLibs(_bugId), params);
    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.POST, PathBuilder.bugAffectedLibs(_bugId), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_NOT_FOUND))
            .setPayload(_json, null, false));

    req_list.addRequest(
        new ConditionalHttpRequest(HttpMethod.PUT, PathBuilder.bugAffectedLibs(_bugId), params)
            .setConditionRequest(cond_req)
            .addCondition(new StatusCondition(HttpURLConnection.HTTP_OK))
            .setPayload(_json, null, false));

    if (_g != null) {
      req_list.setGoalContext(_g);
    }

    req_list.send();
  }

  /**
   * Loads all upload requests form the upload folder and sends them to the backend
   * (as long as they do not have a payload, or the payload does not exceed the maximum length {@link CoreConfiguration#UPLOAD_MAX_SIZE}).
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   */
  public void batchUpload(GoalContext _ctx) {
    final long max_size =
        _ctx.getVulasConfiguration()
            .getConfiguration()
            .getLong(CoreConfiguration.UPLOAD_MAX_SIZE, -1); // -1 means no size limit
    final FileSearch fs =
        new FileSearch(
            new String[] {
              "obj"
            }); // size limit not used here, as it would only cover obj files (but not the payload)
    final Set<Path> objs =
        fs.search(_ctx.getVulasConfiguration().getDir(CoreConfiguration.UPLOAD_DIR));
    for (Path obj : objs) {
      HttpRequest ur = null;
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(obj.toFile()))) {
        ur = (HttpRequest) ois.readObject();
        ur.setGoalContext(
            _ctx); // configuration is not serialized to disk (cf. GoalContext), hence, has to be
        // set again
        if (max_size > 0 && ur instanceof BasicHttpRequest) {
          final long payload_size = ((BasicHttpRequest) ur).getPayloadSize();
          if (payload_size > max_size) {
            log.warn(
                "File ["
                    + obj
                    + "] ignored because it exceeds the maximum accepted size ["
                    + payload_size
                    + " > "
                    + max_size
                    + "] bytes");
            continue;
          }
        }
        ur.send();
      } catch (Exception e) {
        BackendConnector.log.error(
            "Exception during batch upload of [" + obj + "] to [" + ur + "]: " + e.getMessage());
      }
    }
  }

  /*public Signature getConstructSignature(Application _lib, ConstructId _cid) {
  	Signature ast = null;
  	try {
  		final HttpResponse response = new BasicHttpRequest(Service.CIA, HttpMethod.GET, PathBuilder.constructSignature(_lib, JavaId.toSharedType(_cid)), null).send();
  		if(response.isOk()) {
  			final String json = response.getBody();
  			final Gson gson = GsonHelper.getCustomGsonBuilder().create();
  			ast = gson.fromJson(json, ASTConstructBodySignature.class);
  		} else {
  			BackendConnector.log.error("HTTP response status [" + response.getStatus() + "], no AST for GAV [" + _lib + "] and construct " + _cid + " found");
  		}
  	} catch (BackendConnectionException e1) {
  		BackendConnector.log.error("Error while retrieving AST for GAV [" + _lib + "] and construct " + _cid + ": " + e1.getMessage(), e1);
  	}
  	return ast;
  }

  public Signature getConstructSignature(String _sha1, ConstructId _cid) {
  	Signature ast = null;
  	try {
  		final HttpResponse response = new BasicHttpRequest(Service.CIA, HttpMethod.GET, PathBuilder.constructSignature(_sha1, JavaId.toSharedType(_cid)), null).send();
  		if(response.isOk()) {
  			final String json = response.getBody();
  			final Gson gson = GsonHelper.getCustomGsonBuilder().create();
  			ast = gson.fromJson(json, ASTConstructBodySignature.class);
  		} else {
  			BackendConnector.log.error("HTTP response status [" + response.getStatus() + "], no AST for SHA1 [" + _sha1 + "] and construct " + _cid + " found");
  		}
  	} catch (BackendConnectionException e1) {
  		BackendConnector.log.error("Error while retrieving AST for SHA1 [" + _sha1 + "] and construct " + _cid + ": " + e1.getMessage(), e1);
  	}
  	return ast;
  }*/

  /**
   * <p>getBug.</p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException
   * @return a {@link com.sap.psr.vulas.shared.json.model.Bug} object.
   */
  public Bug getBug(GoalContext _g, String _bugId) throws BackendConnectionException {
    BasicHttpRequest request = new BasicHttpRequest(HttpMethod.GET, PathBuilder.bug(_bugId), null);
    if (_g != null) request.setGoalContext(_g);
    HttpResponse r = request.send();
    final String json = r.getBody();
    if (r.getStatus() == 200) {
      final Bug bugChangeList = (Bug) JacksonUtil.asObject(json, Bug.class);
      BackendConnector.log.info("bug change list for bug " + _bugId + " received from backend");
      return bugChangeList;
    } else return null;
  }

  /**
   * <p>getClassLibraryIds.</p>
   *
   * @param _className a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String getClassLibraryIds(String _className) {
    String json = null;
    try {
      final HttpResponse response =
          new BasicHttpRequest(
                  Service.CIA, HttpMethod.GET, PathBuilder.classesLibraryIds(_className), null)
              .send();
      if (response.isOk()) {
        json = response.getBody();
      } else {
        log.info(String.valueOf(response.getStatus()));
      }
    } catch (BackendConnectionException ex) {
      log.info(ex);
    }
    return json;
  }

  /**
   * <p>getAstForQnameInLib.</p>
   * @param _gc TODO
   * @param _qname a {@link java.lang.String} object.
   * @param _sources a {@link java.lang.Boolean} object.
   * @param _lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   *
   * @return a {@link java.lang.String} object.
   */
  public synchronized String getAstForQnameInLib(
      GoalContext _gc, String _qname, Boolean _sources, ProgrammingLanguage _lang) {
    String json = null;
    try {
      final BasicHttpRequest request =
          new BasicHttpRequest(
              Service.CIA,
              HttpMethod.GET,
              PathBuilder.astForQnameInLib(_qname, _sources, _lang),
              null);
      if (_gc != null) request.setGoalContext(_gc);
      final HttpResponse response = request.send();
      if (response.isOk()) {
        json = response.getBody();
      } else {
        log.info(String.valueOf(response.getStatus()));
      }
    } catch (BackendConnectionException ex) {
      log.info(ex);
    }
    return json;
  }

  /**
   * <p>getSourcesForQnameInLib.</p>
   *
   * @param qString a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public synchronized String getSourcesForQnameInLib(String qString) {
    String json = null;
    try {
      final HttpResponse response =
          new BasicHttpRequest(
                  Service.CIA, HttpMethod.GET, PathBuilder.sourcesForQnameInLib(qString), null)
              .send();
      if (response.isOk()) {
        json = response.getBody();
      }
    } catch (BackendConnectionException ex) {
      log.info(ex);
    }
    return json;
  }

  /**
   * <p>getArtifactBugConstructsIntersection.</p>
   *
   * @param _qString a {@link java.lang.String} object.
   * @param c a {@link java.util.List} object.
   * @param packaging a {@link java.lang.String} object.
   * @param lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   * @return an array of {@link com.sap.psr.vulas.shared.json.model.ConstructId} objects.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public synchronized ConstructId[] getArtifactBugConstructsIntersection(
      String _qString, List<ConstructId> c, String packaging, ProgrammingLanguage lang)
      throws BackendConnectionException {
    String json = null;
    BasicHttpRequest bhr =
        new BasicHttpRequest(
            Service.CIA,
            HttpMethod.POST,
            PathBuilder.libConstructIdsIntersect(_qString, packaging, lang),
            null);
    bhr.setPayload(JacksonUtil.asJsonString(c), "application/json", false);
    final HttpResponse response = bhr.send();
    ConstructId[] intersection = null;
    if (response.isOk()) {
      json = response.getBody();
      intersection = (ConstructId[]) JacksonUtil.asObject(json, ConstructId[].class);
    }
    return intersection;
  }

  /**
   * <p>getJarConstructs.</p>
   *
   * @param qString a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public String getJarConstructs(String qString) {
    String json = null;
    try {
      final HttpResponse response =
          new BasicHttpRequest(
                  Service.CIA, HttpMethod.GET, PathBuilder.sourcesForQnameInLib(qString), null)
              .send();
      if (response.isOk()) {
        json = response.getBody();
      }
    } catch (BackendConnectionException ex) {
      log.info(ex);
    }
    return json;
  }

  /**
   * <p>getAstDiff.</p>
   *
   * @param _json a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public synchronized String getAstDiff(GoalContext _g, String _json) {
    String json = null;
    // String ast =null;
    try {
      final BasicHttpRequest bhr =
          new BasicHttpRequest(Service.CIA, HttpMethod.POST, PathBuilder.constructsDiff(), null);
      bhr.setPayload(_json, "application/json", false);
      if (_g != null) bhr.setGoalContext(_g);
      final HttpResponse response = bhr.send();

      if (response.isOk()) {
        json = response.getBody();
        // ast = (String)JacksonUtil.asObject(json, String.class);
      }
    } catch (BackendConnectionException ex) {
      log.error(ex);
    }
    return json;
  }

  /**
   * <p>getBugLibraries.</p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @return an array of {@link com.sap.psr.vulas.shared.json.model.Library} objects.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Library[] getBugLibraries(String _bugId) throws BackendConnectionException {
    final String json =
        new BasicHttpRequest(HttpMethod.GET, PathBuilder.bugLibraryVersions(_bugId), null)
            .send()
            .getBody();
    final Library[] libs = (Library[]) JacksonUtil.asObject(json, Library[].class);
    BackendConnector.log.info("Libraries for bug " + _bugId + " received from backend");
    return libs;
  }

  /**
   * <p>getAllArtifactsGroupArtifact.</p>
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @return an array of {@link com.sap.psr.vulas.shared.json.model.Artifact} objects.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Artifact[] getAllArtifactsGroupArtifact(String _g, String _a)
      throws BackendConnectionException {
    String json = null;
    Artifact[] result = null;

    json =
        new BasicHttpRequest(
                Service.CIA, HttpMethod.GET, PathBuilder.artifactsGroupVersion(_g, _a), null)
            .send()
            .getBody();
    BackendConnector.log.info("artifacts for  " + _g + ":" + _a + " received from backend");
    if (json != null) result = (Artifact[]) JacksonUtil.asObject(json, Artifact[].class);

    return result;
  }

  /**
   * <p>getArtifact.</p>
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public Artifact getArtifact(String _g, String _a, String _v) throws BackendConnectionException {
    String json = null;
    Artifact result = null;

    json =
        new BasicHttpRequest(
                Service.CIA, HttpMethod.GET, PathBuilder.artifactsGAV(_g, _a, _v), null)
            .send()
            .getBody();
    if (json != null) result = (Artifact) JacksonUtil.asObject(json, Artifact.class);

    return result;
  }

  /**
   * <p>getArtifactConstructs.</p>
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @return an array of {@link com.sap.psr.vulas.shared.json.model.ConstructId} objects.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public synchronized ConstructId[] getArtifactConstructs(String _g, String _a, String _v)
      throws BackendConnectionException {
    final String json =
        new BasicHttpRequest(
                Service.CIA, HttpMethod.GET, PathBuilder.artifactsConstruct(_g, _a, _v), null)
            .send()
            .getBody();
    return (ConstructId[]) JacksonUtil.asObject(json, ConstructId[].class);
    // return (ConstructId[])JacksonUtil.asObject(new BasicHttpRequest(Service.CIA, HttpMethod.GET,
    // PathBuilder.artifactsConstruct(_g,_a,_v), null).send().getBody(), ConstructId[].class);
  }

  /**
   * <p>doesArtifactExist.</p>
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @param _sources a {@link java.lang.Boolean} object.
   * @param packaging a {@link java.lang.String} object.
   * @return a boolean.
   * @throws java.lang.InterruptedException if any.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public synchronized boolean doesArtifactExist(
      String _g, String _a, String _v, Boolean _sources, String packaging)
      throws InterruptedException, BackendConnectionException {
    final Map<String, String> params = new HashMap<String, String>();

    if (_sources != null && _sources) params.put("classifier", "sources");
    params.put("packaging", packaging);

    params.put("skipResponseBody", "true");

    final HttpResponse r =
        new BasicHttpRequest(
                Service.CIA, HttpMethod.GET, PathBuilder.artifactsGAV(_g, _a, _v), params)
            .send();
    if (r.getStatus() == HttpStatus.SC_OK) return true;
    else if (r.getStatus() == HttpURLConnection.HTTP_NOT_FOUND) return false;
    else {
      log.warn(
          "Got status code ["
              + r.getStatus()
              + "], artifact ["
              + _g
              + ":"
              + _a
              + ":"
              + _v
              + "] is considered not being available");
      return false;
    }
  }

  /**
   * <p>getBugsList.</p>
   *
   * @param _l a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   * @return a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public String getBugsList(ProgrammingLanguage _l) throws BackendConnectionException {
    final String json =
        new BasicHttpRequest(Service.BACKEND, HttpMethod.GET, PathBuilder.bugs(_l), null)
            .send()
            .getBody();
    return json;
  }

  /**
   * <p>getJarForLib.</p>
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @param _s a {@link java.lang.Boolean} object.
   * @param _d a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.HttpResponse} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public HttpResponse getJarForLib(String _g, String _a, String _v, Boolean _s, String _d)
      throws BackendConnectionException {
    BasicHttpRequest b =
        new BasicHttpRequest(
            Service.CIA, HttpMethod.GET, PathBuilder.downloadArtifactJars(_g, _a, _v, _s), null);
    b.setDir(_d);
    return b.send();
  }

  /**
   * <p>getBugsForLib.</p>
   *
   * @param _digest a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public String getBugsForLib(String _digest) throws BackendConnectionException {
    final String json =
        new BasicHttpRequest(Service.BACKEND, HttpMethod.GET, PathBuilder.libbugs(_digest), null)
            .send()
            .getBody();
    return json;
  }

  /**
   * <p>
   * getBugAffectedLibrary for a GAV. and source
   * </p>
   *
   * @param _bugId a {@link java.lang.String} object.
   * @param _group a {@link java.lang.String} object.
   * @param _artifact a {@link java.lang.String} object.
   * @param _version a {@link java.lang.String} object.
   * @param _source a {@link java.lang.String} object.
   * @return {@link com.sap.psr.vulas.shared.json.model.AffectedLibrary} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public AffectedLibrary[] getBugAffectedLibraries(
      String _bugId,
      String _group,
      String _artifact,
      String _version,
      AffectedVersionSource _source)
      throws BackendConnectionException {
    final HashMap<String, String> params = new HashMap<String, String>();
    if (params != null) params.put("source", _source.toString());
    BasicHttpRequest request =
        new BasicHttpRequest(
            HttpMethod.GET, PathBuilder.affectedLibs(_bugId, _group, _artifact, _version), params);
    String json = request.send().getBody();
    if (json == null) {
      json = "[]";
    }
    return (AffectedLibrary[]) JacksonUtil.asObject(json, AffectedLibrary[].class);
  }

  /**
   * <p>
   * get CVE description
   * </p>
   *
   * @param _bugId
   * @return String
   * @throws BackendConnectionException
   */
  public String getCVE(String _bugId) throws BackendConnectionException {
    final String json =
        new BasicHttpRequest(HttpMethod.GET, PathBuilder.nvdRest(_bugId), null).send().getBody();
    return json;
  }
}
