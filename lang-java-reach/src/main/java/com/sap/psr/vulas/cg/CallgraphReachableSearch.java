package com.sap.psr.vulas.cg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.wala.util.graph.Graph;
import com.sap.psr.vulas.shared.util.StopWatch;

public class CallgraphReachableSearch implements Runnable {

	private static final Log log = LogFactory.getLog(CallgraphReachableSearch.class);

	private Callgraph graph = null;

	private boolean findTouchPoints = true;

	private StopWatch sw = null;

	private ReachabilityAnalyzer analyzer = null;

	private Set<com.sap.psr.vulas.shared.json.model.ConstructId> appConstructs = null;

	private int min = -1;

	private int max = -1;

	/**
	 * Library touch points (stored as a set of linked lists with two elements each) per library (SHA1).
	 */
	private Map<String, Set<List<NodeMetaInformation>>> touchPoints = new HashMap<String, Set<List<NodeMetaInformation>>>();

	/**
	 * Reachable constructs per library (SHA1).
	 */
	private Map<String, Set<NodeMetaInformation>> reachableConstructs = new HashMap<String, Set<NodeMetaInformation>>();

	public CallgraphReachableSearch setCallgraph(Callgraph _g) {
		this.graph = _g;
		return this;
	}

	public CallgraphReachableSearch setFindTouchPoints(boolean _ft) {
		this.findTouchPoints = _ft;
		return this;
	}

	public CallgraphReachableSearch setCallback(ReachabilityAnalyzer _analyzer) {
		this.analyzer = _analyzer;
		return this;
	}

	public CallgraphReachableSearch setMinMax(int _min, int _max) {
		this.min = _min;
		this.max = _max;
		return this;
	}

	public CallgraphReachableSearch setAppConstructs(Set<com.sap.psr.vulas.shared.json.model.ConstructId> _app_constructs) {
		this.appConstructs = _app_constructs;
		return this;
	}

	/**
	 * Adds a given node to the set of reachable constructs for a given library (dependency).
	 * @param _node
	 */
	private void addReachableNode(NodeMetaInformation _node) {
		final String sha1 = _node.getArchiveId(); 
		if(sha1!=null) {
			if(!this.reachableConstructs.containsKey(sha1)) {
				this.reachableConstructs.put(sha1, new HashSet<NodeMetaInformation>());
			}
			final Set<NodeMetaInformation> nodes = this.reachableConstructs.get(sha1);
			nodes.add(_node);
		}
	}

	/**
	 * Adds a given node to the set of touch points for a given library (dependency).
	 * @param _node
	 */
	private void addTouchPoint(NodeMetaInformation _from, NodeMetaInformation _to) {
		final String sha1 = _to.getArchiveId(); 
		if(sha1!=null) {
			if(!this.touchPoints.containsKey(sha1)) {
				this.touchPoints.put(sha1, new HashSet<List<NodeMetaInformation>>());
			}
			final Set<List<NodeMetaInformation>> touch_points = this.touchPoints.get(sha1);
			final List<NodeMetaInformation> touch_point = new ArrayList<NodeMetaInformation>();
			touch_point.add(_from);
			touch_point.add(_to);
			touch_points.add(touch_point);
		}
	}

	@Override
	public void run() {
		if(this.graph!=null && this.graph.getGraph()!=null) {
			StopWatch sw = null;
			try {				
				// Loop over all nodes
				final Graph<Integer> wala_graph = this.graph.getGraph();
				final Iterator<Integer> wala_graph_iterator = wala_graph.iterator();

				if(wala_graph_iterator==null) {
					log.error("No iterator for callgraph [" + wala_graph + "]");
				}
				else {
					// Loop variables
					Integer successor_node = null;
					Iterator<Integer> successor_nodes_iterator = null;
					NodeMetaInformation current_node_metainf = null, successor_node_metainf = null;
					String current_node_qname = null;

					sw = new StopWatch("Collect touch points and reachable constructs per library, nodes [" + this.min + " - " + this.max + "]").setTotal(max-min).start();

					for(int current_node=this.min; current_node<this.max; current_node++) {
						try {
							// Current node and its meta information
							current_node_metainf = this.graph.getInformationForId(current_node);
							if(current_node_metainf!=null) {
								current_node_qname = current_node_metainf.getConstructId().getQname();

								// Current node is part of library --> reachable constructs
								if(MethodNameFilter.getInstance().isLibraryMethod(this.appConstructs, current_node_qname)) {
									if(!MethodNameFilter.getInstance().isBlackListed(current_node_qname))
										this.addReachableNode(current_node_metainf);
								}
								// Current node is part of app --> touch points
								else {
									// Collection enabled?
									if(this.findTouchPoints) {
										// Loop all successors of the current node
										successor_nodes_iterator = wala_graph.getSuccNodes(current_node);
										while(successor_nodes_iterator.hasNext()){

											// Successor node of the current node
											successor_node = successor_nodes_iterator.next();
											successor_node_metainf = this.graph.getInformationForId(successor_node);

											final String succ_node_qname = successor_node_metainf.getConstructId().getQname();
											if(MethodNameFilter.getInstance().isLibraryMethod(this.appConstructs, succ_node_qname) && !MethodNameFilter.getInstance().isBlackListed(succ_node_qname)){
												//current_node_metainf.addToList(successor_node_metainf);
												this.addTouchPoint(current_node_metainf, successor_node_metainf);
											}
										}
									}
								}
							}
							sw.progress();

							//TODO Davide: Also collect touchpoints from Library to Application (L2A)
						} catch(Exception e) {
							log.error(e.getClass().getSimpleName() + " occured when looping callgraph node " + current_node_metainf + ": " + e.getMessage());
						}
					}
					sw.stop();
				}
			}
			catch(NullPointerException npe) {
				sw.stop(npe);
				log.error(npe.getClass().getSimpleName() + " occured when looping callgraph", npe);
			}
			catch(Exception e) {
				sw.stop(e);
				log.error(e.getClass().getSimpleName() + " occured when looping callgraph: " + e.getMessage());
			}
		}	
	}

	public Map<String, Set<List<NodeMetaInformation>>> getTouchPoints() {
		return touchPoints;
	}

	public Map<String, Set<NodeMetaInformation>> getReachableConstructs() {
		return reachableConstructs;
	}	
}
