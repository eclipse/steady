package com.sap.psr.vulas.backend.repo;

import java.util.List;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Path;

/**
 * Specifies additional methods of the {@link PathRepository}.
 *
 */
public interface PathRepositoryCustom {

	/**
	 * 
	 * @param traces
	 * @return
	 */
	public List<Path> customSave(Application _app, Path[] _paths);
}
