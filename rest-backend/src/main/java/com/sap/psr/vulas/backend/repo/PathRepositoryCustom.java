package com.sap.psr.vulas.backend.repo;

import java.util.List;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Path;

/**
 * Specifies additional methods of the {@link PathRepository}.
 */
public interface PathRepositoryCustom {

	/**
	 * <p>customSave.</p>
	 *
	 * @param _app a {@link com.sap.psr.vulas.backend.model.Application} object.
	 * @param _paths an array of {@link com.sap.psr.vulas.backend.model.Path} objects.
	 * @return a {@link java.util.List} object.
	 */
	public List<Path> customSave(Application _app, Path[] _paths);
}
