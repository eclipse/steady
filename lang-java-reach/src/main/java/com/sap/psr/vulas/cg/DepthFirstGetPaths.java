package com.sap.psr.vulas.cg;

import com.ibm.wala.util.graph.Graph;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** DepthFirstGetPaths class. */
public class DepthFirstGetPaths extends AbstractGetPaths {

  private static final Log log = LogFactory.getLog(DepthFirstGetPaths.class);

  private long start_millis = System.currentTimeMillis(), end_millis = System.currentTimeMillis();

  /**
   * Constructor for DepthFirstGetPaths.
   *
   * @param _graph a {@link com.ibm.wala.util.graph.Graph} object.
   * @param _nodeid a {@link java.util.ArrayList} object.
   */
  public DepthFirstGetPaths(Graph<Integer> _graph, ArrayList<ConstructId> _nodeid) {
    super(_graph, _nodeid);
  }

  /** {@inheritDoc} */
  public HashSet<LinkedList<ConstructId>> getAllPaths(ConstructId _src, ConstructId _tgt) {
    this.start_millis = System.currentTimeMillis();
    HashSet<LinkedList<Integer>> paths = new HashSet<LinkedList<Integer>>();
    Integer currentNode = this.nodeId.indexOf(_src);
    LinkedList<Integer> visited = new LinkedList<Integer>();
    visited.add(currentNode);
    findAllPaths(visited, paths, currentNode, this.nodeId.indexOf(_tgt));
    HashSet<LinkedList<ConstructId>> result = new HashSet<LinkedList<ConstructId>>();
    for (LinkedList<Integer> p : paths) {
      LinkedList<ConstructId> newp = new LinkedList<ConstructId>();
      for (Integer i : p) newp.add(this.nodeId.get(i));
      result.add(newp);
    }
    return result;
  }

  private void findAllPaths(
      LinkedList<Integer> visited,
      HashSet<LinkedList<Integer>> paths,
      Integer currentNode,
      Integer _tgt) {
    if (currentNode == _tgt) {
      LinkedList<Integer> newpath = new LinkedList<Integer>();
      newpath.addAll(visited);
      paths.add(newpath);
      if ((!paths.isEmpty()) && paths.size() % 5 == 0) {
        this.end_millis = System.currentTimeMillis();
        DepthFirstGetPaths.log.info(
            "Found 5 more paths in [ " + (this.end_millis - this.start_millis) + " millisecs ]");
      }
      return;
    } else {
      int succnode = -1;
      Iterator<Integer> nodes = this.graph.getSuccNodes(currentNode);
      while (nodes.hasNext()) {
        succnode = nodes.next();
        if (visited.contains(succnode)) {
          continue;
        }
        LinkedList<Integer> temp = new LinkedList<Integer>();
        temp.addAll(visited);
        temp.add(succnode);
        findAllPaths(temp, paths, succnode, _tgt);
      }
    }
  }
}
