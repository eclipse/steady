package com.sap.psr.vulas.cg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.ibm.wala.util.graph.Graph;
import com.sap.psr.vulas.shared.json.model.ConstructId;

/**
 * To implement different algorithms of computing all paths
 *
 */

public abstract class AbstractGetPaths {
	
	protected Graph<Integer> graph = null;
	protected ArrayList<ConstructId> nodeId = null;
	
	public AbstractGetPaths(Graph<Integer> _graph, ArrayList<ConstructId> _nodeid) {
		this.graph = _graph;
		this.nodeId = _nodeid;
	}
	
	public abstract HashSet<LinkedList<ConstructId>> getAllPaths (ConstructId _src, ConstructId _tgt);

}
