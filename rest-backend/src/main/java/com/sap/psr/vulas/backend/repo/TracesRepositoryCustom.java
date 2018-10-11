package com.sap.psr.vulas.backend.repo;

import java.util.List;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Trace;

/**
 * Specifies additional methods of the {@link TracesRepository}.
 *
 */
public interface TracesRepositoryCustom {

	/**
	 * 
	 * @param traces
	 * @return
	 */
	public List<Trace> customSave(Application _app, Trace[] _traces);
}
