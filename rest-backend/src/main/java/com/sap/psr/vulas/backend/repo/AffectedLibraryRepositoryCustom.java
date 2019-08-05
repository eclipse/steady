package com.sap.psr.vulas.backend.repo;

import java.util.List;
import java.util.TreeSet;

import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.VulnerableDependency;

/**
 * Specifies additional methods of the {@link AffectedLibraryRepository}.
 */
public interface AffectedLibraryRepositoryCustom {

	/**
	 * <p>customSave.</p>
	 *
	 * @param _bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 * @param _aff_libs an array of {@link com.sap.psr.vulas.backend.model.AffectedLibrary} objects.
	 * @return a {@link java.util.List} object.
	 */
	public List<AffectedLibrary> customSave(Bug _bug, AffectedLibrary[] _aff_libs);
	
	/**
	 * <p>computeAffectedLib.</p>
	 *
	 * @param _vdList a {@link java.util.TreeSet} object.
	 */
	public void computeAffectedLib(TreeSet<VulnerableDependency> _vdList);
	
	/**
	 * <p>computeAffectedLib.</p>
	 *
	 * @param _vd a {@link com.sap.psr.vulas.backend.model.VulnerableDependency} object.
	 * @param _lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public void computeAffectedLib(VulnerableDependency _vd, Library _lib);
}
