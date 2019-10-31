package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Path;
import java.util.List;

/** Specifies additional methods of the {@link PathRepository}. */
public interface PathRepositoryCustom {

  /**
   * customSave.
   *
   * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param _paths an array of {@link com.sap.psr.vulas.backend.model.Path} objects.
   * @return a {@link java.util.List} object.
   */
  public List<Path> customSave(Application _app, Path[] _paths);
}
