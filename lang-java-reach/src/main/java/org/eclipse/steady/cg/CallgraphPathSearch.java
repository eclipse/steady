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
package org.eclipse.steady.cg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.util.StopWatch;

/**
 * <p>CallgraphPathSearch class.</p>
 *
 */
public class CallgraphPathSearch implements Runnable {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private Callgraph graph = null;

  private boolean shortestPaths = true;

  private Set<org.eclipse.steady.shared.json.model.ConstructId> entrypoints;

  private Set<org.eclipse.steady.shared.json.model.ConstructId> targetpoints;

  private ArrayList<List<org.eclipse.steady.shared.json.model.ConstructId>> paths =
      new ArrayList<List<org.eclipse.steady.shared.json.model.ConstructId>>();

  private String label = null;

  private StopWatch sw = null;

  private ReachabilityAnalyzer analyzer = null;

  /**
   * <p>setCallgraph.</p>
   *
   * @param _g a {@link org.eclipse.steady.cg.Callgraph} object.
   * @return a {@link org.eclipse.steady.cg.CallgraphPathSearch} object.
   */
  public CallgraphPathSearch setCallgraph(Callgraph _g) {
    this.graph = _g;
    return this;
  }

  /**
   * <p>Getter for the field <code>label</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * <p>Setter for the field <code>label</code>.</p>
   *
   * @param _label a {@link java.lang.String} object.
   * @return a {@link org.eclipse.steady.cg.CallgraphPathSearch} object.
   */
  public CallgraphPathSearch setLabel(String _label) {
    this.label = _label;
    return this;
  }

  /**
   * <p>Setter for the field <code>shortestPaths</code>.</p>
   *
   * @param shortestPaths a boolean.
   * @return a {@link org.eclipse.steady.cg.CallgraphPathSearch} object.
   */
  public CallgraphPathSearch setShortestPaths(boolean shortestPaths) {
    this.shortestPaths = shortestPaths;
    return this;
  }

  /**
   * <p>Setter for the field <code>entrypoints</code>.</p>
   *
   * @param entrypoints a {@link java.util.Set} object.
   * @return a {@link org.eclipse.steady.cg.CallgraphPathSearch} object.
   */
  public CallgraphPathSearch setEntrypoints(
      Set<org.eclipse.steady.shared.json.model.ConstructId> entrypoints) {
    this.entrypoints = entrypoints;
    return this;
  }

  /**
   * <p>Setter for the field <code>targetpoints</code>.</p>
   *
   * @param targetpoints a {@link java.util.Set} object.
   * @return a {@link org.eclipse.steady.cg.CallgraphPathSearch} object.
   */
  public CallgraphPathSearch setTargetpoints(
      Set<org.eclipse.steady.shared.json.model.ConstructId> targetpoints) {
    this.targetpoints = targetpoints;
    return this;
  }

  /**
   * <p>setCallback.</p>
   *
   * @param _analyzer a {@link org.eclipse.steady.cg.ReachabilityAnalyzer} object.
   * @return a {@link org.eclipse.steady.cg.CallgraphPathSearch} object.
   */
  public CallgraphPathSearch setCallback(ReachabilityAnalyzer _analyzer) {
    this.analyzer = _analyzer;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public void run() {
    sw =
        new StopWatch(
            this.label
                + ": Search for paths from ["
                + this.entrypoints.size()
                + "] entry points to ["
                + this.targetpoints.size()
                + "] target points");
    sw.start();

    final HashSet<org.eclipse.steady.shared.json.model.ConstructId> reachable_target_points =
        new HashSet<org.eclipse.steady.shared.json.model.ConstructId>();

    // Loop all target points
    for (org.eclipse.steady.shared.json.model.ConstructId cid : this.targetpoints) {

      // Check whether the target construct is actually part of the callgraph
      if (this.graph.existsInCallgraph(cid)) {

        log.info("Target construct [" + cid.getQname() + "] is part of the callgraph");

        // Compute the distances from all nodes to cid
        final Map<org.eclipse.steady.shared.json.model.ConstructId, Integer> distance =
            this.graph.getDist(cid);
        for (org.eclipse.steady.shared.json.model.ConstructId ep : this.entrypoints) {
          // get the distance from entry points to changed construct; if it's not -1, then this
          // changed construct is reachable from entrypoints
          if (this.graph.existsInCallgraph(ep)) {
            if (distance.get(ep) != Integer.MAX_VALUE) {
              reachable_target_points.add(cid);
              // At this point in time, we should be able to exit the loop over all entry points,
              // added break
              break;
            }
          }
        }

        // Only if the construct is reachable, compute the paths
        if (reachable_target_points.contains(cid)) {

          // Path(s) found in the call graph
          Map<org.eclipse.steady.shared.json.model.ConstructId, LinkedList<Integer>>
              searched_paths = null;

          // Search shortest or first path to cid
          if (this.shortestPaths) {
            log.info("Searching the shortest path(s) to [" + cid.getQname() + "]");
            searched_paths = this.graph.getShortestPath(cid, null);
          } else {
            log.info("Searching a path to [" + cid.getQname() + "], stopping after first hit");
            searched_paths = this.graph.getShortestPath(cid, this.entrypoints);
          }

          // For all entry points, check whether there is a path
          // TODO: Check if the loop can be exited depending on the search_shortest option
          for (org.eclipse.steady.shared.json.model.ConstructId ep : this.entrypoints) {
            if (this.graph.existsInCallgraph(ep)) {

              // Linked list of integers, each one being the unique ID given to constructs in the
              // callgraph
              final LinkedList<Integer> spath = searched_paths.get(ep);
              if (spath != null) {
                log.info(
                    "Shortest path from entrypoint [ "
                        + ep.getQname()
                        + " ] with distance "
                        + spath.size());

                // Create a new path (linked list of constructs), thereby resolving the unique
                // integer IDs used in the callgraph class
                final LinkedList<org.eclipse.steady.shared.json.model.ConstructId> newpath =
                    new LinkedList<org.eclipse.steady.shared.json.model.ConstructId>();
                newpath.add(ep);
                for (int n = (spath.size() - 1); n >= 0; n--) {
                  log.info(
                      "        		===>   " + this.graph.getConstructForId(spath.get(n)).getQname());
                  newpath.add(this.graph.getConstructForId(spath.get(n)));
                }
                this.paths.add(newpath);
              }
            }
          }

          // Performance killer, commented out!
          /*
          ReachabilityAnalyzer.log.info("Starting to Compute all paths");
          AbstractGetPaths getpaths = new DepthFirstGetPaths(this.callgraph.getGraph());
          HashSet<LinkedList<ConstructId>> paths = new HashSet<LinkedList<ConstructId>>();
          for (ConstructId ep : _entrypoints) {
          	if (this.callgraph.nodeId.contains(ep)) {
          		paths = getpaths.getAllPaths(ep, cid);
          	}
          }*/
        }
      } else {
        log.info("Target construct [" + cid.getQname() + "] is NOT part of the callgraph");
      }
    }

    log.info(
        "Search paths for ["
            + label
            + "] completed: ["
            + reachable_target_points.size()
            + "/"
            + this.targetpoints.size()
            + "] constructs are reachable");
    sw.stop();

    if (this.analyzer != null) {
      this.analyzer.callback(this);
    }
  }

  /**
   * <p>Getter for the field <code>paths</code>.</p>
   *
   * @return a {@link java.util.ArrayList} object.
   */
  public ArrayList<List<org.eclipse.steady.shared.json.model.ConstructId>> getPaths() {
    return paths;
  }

  /**
   * <p>getStopWatch.</p>
   *
   * @return a {@link org.eclipse.steady.shared.util.StopWatch} object.
   */
  public StopWatch getStopWatch() {
    return this.sw;
  }
}
