package com.sap.psr.vulas.backend.repo;

import java.util.List;
import java.util.TreeSet;

import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.VulnerableDependency;

/**
 * Specifies additional methods of the {@link AffectedLibraryRepository}.
 *
 */
public interface AffectedLibraryRepositoryCustom {

	/**
	 * 
	 * @param bug
	 * @return
	 */
	public List<AffectedLibrary> customSave(Bug _bug, AffectedLibrary[] _aff_libs);
	
	public void computeAffectedLib(TreeSet<VulnerableDependency> _vdList);
	
	public void computeAffectedLib(VulnerableDependency _vd);
}
