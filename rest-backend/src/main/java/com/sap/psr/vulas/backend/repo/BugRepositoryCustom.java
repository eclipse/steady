package com.sap.psr.vulas.backend.repo;

import javax.persistence.PersistenceException;

import org.springframework.data.repository.CrudRepository;

import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructId;

/**
 * Specifies additional methods of the {@link BugRepository}.
 *
 */
public interface BugRepositoryCustom {

	/**
	 * Saves the given {@link Bug} together with all the nested {@link ConstructId}s.
	 * This method has to be used in favor of the save method provided by the
	 * {@link CrudRepository}.
	 * @param bug
	 * @return
	 */
	public Bug customSave(Bug bug, Boolean _considerCC) throws PersistenceException;
	
	/**
	 * Checks whether the given {@link Bug} needs CVE data and, if yes, updates its description and CVSS information.
	 * @return true if the bug was updated, false otherwise
	 */
	public boolean updateCachedCveData(Bug _b, boolean _force);
}
