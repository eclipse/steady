package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Space;

public interface SpaceRepositoryCustom {
	
	
	public Space customSave(Space _lib);
	
	/**
	 * Returns the default space for the given tenant token (default tenant used if null).
	 * @return
	 */
	public Space getDefaultSpace(String _tenantToken);
	
	
	/**
	 * Returns the space for the given space token (default space of default tenant used if null).
	 * @return
	 */
	public Space getSpace(String _spaceToken);

}
