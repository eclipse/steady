package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Space;

/** SpaceRepositoryCustom interface. */
public interface SpaceRepositoryCustom {

  /**
   * customSave.
   *
   * @param _lib a {@link com.sap.psr.vulas.backend.model.Space} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public Space customSave(Space _lib);

  /**
   * Returns the default space for the given tenant token (default tenant used if null).
   *
   * @param _tenantToken a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public Space getDefaultSpace(String _tenantToken);

  /**
   * Returns the space for the given space token (default space of default tenant used if null).
   *
   * @param _spaceToken a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public Space getSpace(String _spaceToken);
}
