package com.sap.psr.vulas.backend.repo;

import java.util.List;

import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.TouchPoint;
import java.util.Set;

/**
 * Specifies additional methods of the {@link PathRepository}.
 *
 */
public interface DependencyRepositoryCustom {

	/**
	 * 
	 * @param traces
	 * @return
	 */
	public Set<ConstructId> saveReachableConstructIds(Dependency _dep, ConstructId[] _construct_ids);
	
	public Set<TouchPoint> saveTouchPoints(Dependency _dep, TouchPoint[] _touch_points);
}
