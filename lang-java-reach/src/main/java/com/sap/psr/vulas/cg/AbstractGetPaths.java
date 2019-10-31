package com.sap.psr.vulas.cg;

import com.ibm.wala.util.graph.Graph;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/** To implement different algorithms of computing all paths */
public abstract class AbstractGetPaths {

  protected Graph<Integer> graph = null;
  protected ArrayList<ConstructId> nodeId = null;

  /**
   * Constructor for AbstractGetPaths.
   *
   * @param _graph a {@link com.ibm.wala.util.graph.Graph} object.
   * @param _nodeid a {@link java.util.ArrayList} object.
   */
  public AbstractGetPaths(Graph<Integer> _graph, ArrayList<ConstructId> _nodeid) {
    this.graph = _graph;
    this.nodeId = _nodeid;
  }

  /**
   * getAllPaths.
   *
   * @param _src a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @param _tgt a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @return a {@link java.util.HashSet} object.
   */
  public abstract HashSet<LinkedList<ConstructId>> getAllPaths(ConstructId _src, ConstructId _tgt);
}
