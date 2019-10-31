package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Library;

/** LibraryRepositoryCustom interface. */
public interface LibraryRepositoryCustom {

  /**
   * customSave.
   *
   * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public Library customSave(Library _lib);

  /**
   * saveIncomplete.
   *
   * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
   */
  public Library saveIncomplete(Library _lib);
}
