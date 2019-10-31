package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.TouchPoint;
import java.util.Set;

/** Specifies additional methods of the {@link PathRepository}. */
public interface DependencyRepositoryCustom {

  /**
   * saveReachableConstructIds.
   *
   * @param _dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _construct_ids an array of {@link com.sap.psr.vulas.backend.model.ConstructId} objects.
   * @return a {@link java.util.Set} object.
   */
  public Set<ConstructId> saveReachableConstructIds(Dependency _dep, ConstructId[] _construct_ids);

  /**
   * saveTouchPoints.
   *
   * @param _dep a {@link com.sap.psr.vulas.backend.model.Dependency} object.
   * @param _touch_points an array of {@link com.sap.psr.vulas.backend.model.TouchPoint} objects.
   * @return a {@link java.util.Set} object.
   */
  public Set<TouchPoint> saveTouchPoints(Dependency _dep, TouchPoint[] _touch_points);
}
