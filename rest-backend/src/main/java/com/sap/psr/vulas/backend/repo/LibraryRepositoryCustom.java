package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Library;

/**
 * <p>LibraryRepositoryCustom interface.</p>
 *
 */
public interface LibraryRepositoryCustom {

	/**
	 * <p>customSave.</p>
	 *
	 * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public Library customSave(Library _lib);
	
	/**
	 * <p>saveIncomplete.</p>
	 *
	 * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public Library saveIncomplete(Library _lib);
}
