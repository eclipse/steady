package com.sap.psr.vulas.cg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PathSimilarity {
	
	private static final Log log = LogFactory.getLog( PathSimilarity.class );

	//ArrayList: every path has a unique index
	ArrayList<LinkedList<String>> paths = null;
	
	//group all paths based on the node joint, the map key is the qname of the node joint, the value are all paths id in this group, id is read from ArrayList paths
	HashMap<String, HashSet<Integer>> groupedPaths = null;
	
	public PathSimilarity(ArrayList<LinkedList<String>> _p) {
		this.paths = _p;
	}
	
	public void addPath (LinkedList<String> _p) {
		this.paths.add(_p);
	}
	
	public void groupPathsByJointNode () {
		this.groupedPaths = new HashMap<String, HashSet<Integer>>();
		for (int i = 0; i < this.paths.size(); i++) {
			LinkedList<String> p = this.paths.get(i);
			for(String s : p) {
				if(!this.groupedPaths.containsKey(s)) {
					HashSet<Integer> tmp = new HashSet<Integer>();
					tmp.add(i);
					this.groupedPaths.put(s, tmp);
				} else {
					this.groupedPaths.get(s).add(i);
				}
			}
		}
	}

	//return all the overlapping paths (linked edges)
	private HashSet<List<String>> overlapPath (LinkedList<String> _path1, LinkedList<String> _path2) {
		LinkedList<String> shortpath = new LinkedList<String>();
		LinkedList<String> longpath = new LinkedList<String>();
		if(_path1.size() >= _path2.size()) {
			shortpath.addAll(_path2);
			longpath.addAll(_path1);
		} else {
			shortpath.addAll(_path1);
			longpath.addAll(_path2);
		}
		HashSet<List<String>> results = new HashSet<List<String>>();
		List<String> overlap = new LinkedList<String>();
		int length = 1, pointer = 0;
		//complexity: looping only once by increasing / decreasing i and j
		for(int i = 0, j= 0; i < shortpath.size() && j < longpath.size();) {
			String node = shortpath.get(i);
			if(node.equals(longpath.get(j))) {
				overlap.add(node);
				i++;
				j++;
			}
			else {
				if(!overlap.isEmpty()) {
					List<String> newoverlap = new LinkedList<String>();
					newoverlap.addAll(overlap);
					if(newoverlap.size() > length) length = newoverlap.size();
					results.add(newoverlap);
					overlap.clear();	
					i = pointer;
				} else {			
					if( i!=(shortpath.size()-1) && j == (longpath.size()-1) ) {
						i = i + length;
						pointer = i;
						j = 0;
						length = 1;
					}
					else j++;
				}
			}
		}
		if(!overlap.isEmpty()) results.add(overlap);
		PathSimilarity.log.info("Results: " + results);
		return results;
	}
	
	//one way to compute the similarity between two paths
	public void pathSimilarity (LinkedList<String> _path1, LinkedList<String> _path2) {
		int count = 0;
		HashSet<List<String>> overlaps = overlapPath(_path1, _path2);
		for(List<String> sp : overlaps) count += sp.size();
		PathSimilarity.log.info("The lengths of two paths are " + _path1.size() + " and " + _path2.size() 
				+ " separately; overlapping [ " + count + " nodes ] distributed in [ " + overlaps.size() + " subpaths ]");
	}

}
