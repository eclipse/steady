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
package com.sap.psr.vulas.cg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;


/**
 * <p>PathSimilarity class.</p>
 *
 */
public class PathSimilarity {
	
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger( PathSimilarity.class );

	//ArrayList: every path has a unique index
	ArrayList<LinkedList<String>> paths = null;
	
	//group all paths based on the node joint, the map key is the qname of the node joint, the value are all paths id in this group, id is read from ArrayList paths
	HashMap<String, HashSet<Integer>> groupedPaths = null;
	
	/**
	 * <p>Constructor for PathSimilarity.</p>
	 *
	 * @param _p a {@link java.util.ArrayList} object.
	 */
	public PathSimilarity(ArrayList<LinkedList<String>> _p) {
		this.paths = _p;
	}
	
	/**
	 * <p>addPath.</p>
	 *
	 * @param _p a {@link java.util.LinkedList} object.
	 */
	public void addPath (LinkedList<String> _p) {
		this.paths.add(_p);
	}
	
	/**
	 * <p>groupPathsByJointNode.</p>
	 */
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
	/**
	 * <p>pathSimilarity.</p>
	 *
	 * @param _path1 a {@link java.util.LinkedList} object.
	 * @param _path2 a {@link java.util.LinkedList} object.
	 */
	public void pathSimilarity (LinkedList<String> _path1, LinkedList<String> _path2) {
		int count = 0;
		HashSet<List<String>> overlaps = overlapPath(_path1, _path2);
		for(List<String> sp : overlaps) count += sp.size();
		PathSimilarity.log.info("The lengths of two paths are " + _path1.size() + " and " + _path2.size() 
				+ " separately; overlapping [ " + count + " nodes ] distributed in [ " + overlaps.size() + " subpaths ]");
	}

}
