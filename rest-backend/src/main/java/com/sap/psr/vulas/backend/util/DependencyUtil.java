package com.sap.psr.vulas.backend.util;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.Library;


public class DependencyUtil {
	

	private static final Log log = LogFactory.getLog(DependencyUtil.class);
	
	/**
	 * Returns a set of dependencies such that every {@link Dependency} points to a different {@link Library}.
	 * This is needed because {@link Dependency#equals(Object)} considers all kinds of members of {@link Dependency}, while
	 * the relational database table storing dependencies does not.
	 * 
	 * @param _deps
	 * @param _lib
	 * @return
	 */
	public static Set<Dependency> removeDuplicateLibraryDependencies(Collection<Dependency> _deps) {
		final Set<Dependency> clean_set = new HashSet<Dependency>();
		if(_deps!=null) {
			for(Dependency d: _deps) {
				final Dependency existing_dep = DependencyUtil.getLibraryDependency(clean_set, d.getLib());
				if(existing_dep==null) {
					clean_set.add(d);
				}
				else {
					log.warn("Dependency " + d + " removed from set, one on the same library already exists: " + existing_dep);
				}
			}
		}
		return clean_set;
	}	
	
	/**
	 * Returns true of the given set of dependencies already contains a {@link Dependency} for the given {@link Library}, false otherwise.
	 * 
	 * @param _deps
	 * @param _lib
	 * @return
	 */
	public static boolean containsLibraryDependency(Set<Dependency> _deps, Library _lib) {
		return DependencyUtil.getLibraryDependency(_deps, _lib)!=null;
	}
	
	/**
	 * Returns true of the given set of dependencies already contains a {@link Dependency} with the same library' digest, parent and relativePath, false otherwise.
	 * 
	 * @param _deps
	 * @param _dep
	 * @return
	 */
	public static Dependency getDependency(Set<Dependency> _deps, Dependency _dep) {
		for(Dependency d: _deps) {
			if(d.getLib().equals(_dep.getLib()) && d.getParent().equalLibParentRelPath(_dep.getParent()) && d.getRelativePath().equals(_dep.getRelativePath())) {
				return d;
			}
		}
		return null;
	}
	
	/**
	 * Returns the {@link Dependency} for the given {@link Library}, null if no such dependency exists.
	 * 
	 * @param _deps
	 * @param _lib
	 * @return
	 */
	public static Dependency getLibraryDependency(Set<Dependency> _deps, Library _lib) {
		for(Dependency d: _deps) {
			if(d.getLib().equals(_lib)) {
				return d;
			}
		}
		return null;
	}	
	
	/**
	 * Checks whether the set of dependencies is valid: 
	 * - every {@link Dependency} tuple (sha1, parent and relativePath) appears only once. 
	 * - every {@link Dependency} appearing as parent also appear in the _deps set
	 * 
	 * @param _deps
	 * @return
	 */
	public static boolean isValidDependencyCollection(Application _app) {
		Collection<Dependency> _deps = _app.getDependencies();
		final Set<Dependency> main_set = new HashSet<Dependency>();
		final Set<Dependency> parent_set = new HashSet<Dependency>();
		if(_deps!=null) {
			for(Dependency d: _deps) {
				d.setAppRecursively(_app);
				final Dependency existing_dep = DependencyUtil.getDependency(main_set, d);
				if(existing_dep==null) {
					main_set.add(d);
					if(d.getParent()!=null)
						parent_set.add(d.getParent());
				}
				else {
					log.error("Dependency " + d + " occurs multiple times in the set, one on the same library already exists: " + existing_dep);
					return false;
				}
			}
			for(Dependency d: parent_set){
				if(!main_set.contains(d)) {
					log.error("Dependency parent " + d + " is not declared as dependency itself");
					return false;
				}
			}
		}	
		return true;
	}
}
