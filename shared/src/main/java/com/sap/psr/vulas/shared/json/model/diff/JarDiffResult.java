package com.sap.psr.vulas.shared.json.model.diff;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.LibraryId;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JarDiffResult {

	private LibraryId oldLibId;
	private LibraryId newLibId;

	private Set<ConstructId> newPackages;
	private Set<ConstructId> deletedPackages;

	private Set<ConstructId> newClasses;
	private Set<ClassDiffResult> modifiedClasses;
	private Set<ConstructId> deletedClasses;
	private Set<ConstructId> deprecatedClasses;
	private Set<ConstructId> undeprecatedClasses;

	private Set<ConstructId> newInterfaces;
	private Set<ClassDiffResult> modifiedInterfaces;
	private Set<ConstructId> deletedInterfaces;
	private Set<ConstructId> deprecatedInterfaces;
	private Set<ConstructId> undeprecatedInterfaces;

	public LibraryId getOldLibId() {
		return oldLibId;
	}
	public void setOldLibId(LibraryId oldLibId) {
		this.oldLibId = oldLibId;
	}

	public LibraryId getNewLibId() {
		return newLibId;
	}
	public void setNewLibId(LibraryId newLibId) {
		this.newLibId = newLibId;
	}

	public Set<ConstructId> getNewPackages() {
		return newPackages;
	}
	public void addNewPackage(ConstructId _p) {
		if(this.newPackages==null) this.newPackages = new HashSet<ConstructId>();
		this.newPackages.add(_p);
	}
	public void setNewPackages(Set<ConstructId> newPackages) {
		this.newPackages = newPackages;
	}

	public Set<ConstructId> getDeletedPackages() {
		return deletedPackages;
	}
	public void addDeletedPackage(ConstructId _p) {
		if(this.deletedPackages==null) this.deletedPackages = new HashSet<ConstructId>();
		this.deletedPackages.add(_p);
	}
	public void setDeletedPackages(Set<ConstructId> deletedPackages) {
		this.deletedPackages = deletedPackages;
	}

	public Set<ConstructId> getNewClasses() {
		return newClasses;
	}
	public void addNewClass(ConstructId _class) {
		if(this.newClasses==null) this.newClasses = new HashSet<ConstructId>();
		this.newClasses.add(_class);
	}
	public void setNewClasses(Set<ConstructId> newClasses) {
		this.newClasses = newClasses;
	}

	public Set<ClassDiffResult> getModifiedClasses() {
		return modifiedClasses;
	}
	public void addModifiedClass(ClassDiffResult _class) {
		if(this.modifiedClasses==null) this.modifiedClasses = new HashSet<ClassDiffResult>();
		this.modifiedClasses.add(_class);
	}
	public void setModifiedClasses(Set<ClassDiffResult> modifiedClasses) {
		this.modifiedClasses = modifiedClasses;
	}

	public Set<ConstructId> getDeletedClasses() {
		return deletedClasses;
	}
	public void addDeletedClass(ConstructId _class) {
		if(this.deletedClasses==null) this.deletedClasses = new HashSet<ConstructId>();
		this.deletedClasses.add(_class);
	}
	public void setDeletedClasses(Set<ConstructId> deletedClasses) {
		this.deletedClasses = deletedClasses;
	}

	public Set<ConstructId> getDeprecatedClasses() {
		return deprecatedClasses;
	}
	public void addDeprecatedClass(ConstructId _class) {
		if(this.deprecatedClasses==null) this.deprecatedClasses = new HashSet<ConstructId>();
		this.deprecatedClasses.add(_class);
	}
	public void setDeprecatedClasses(Set<ConstructId> deprecatedClasses) {
		this.deprecatedClasses = deprecatedClasses;
	}

	public Set<ConstructId> getUndeprecatedClasses() {
		return undeprecatedClasses;
	}
	public void addUndeprecatedClass(ConstructId _class) {
		if(this.undeprecatedClasses==null) this.undeprecatedClasses = new HashSet<ConstructId>();
		this.undeprecatedClasses.add(_class);
	}
	public void setUndeprecatedClasses(Set<ConstructId> undeprecatedClasses) {
		this.undeprecatedClasses = undeprecatedClasses;
	}

	public Set<ConstructId> getNewInterfaces() {
		return newInterfaces;
	}
	public void addNewInterface(ConstructId _class) {
		if(this.newInterfaces==null) this.newInterfaces = new HashSet<ConstructId>();
		this.newInterfaces.add(_class);
	}
	public void setNewInterfaces(Set<ConstructId> newInterfaces) {
		this.newInterfaces = newInterfaces;
	}

	public Set<ClassDiffResult> getModifiedInterfaces() {
		return modifiedInterfaces;
	}
	public void addModifiedInterface(ClassDiffResult _class) {
		if(this.modifiedInterfaces==null) this.modifiedInterfaces = new HashSet<ClassDiffResult>();
		this.modifiedInterfaces.add(_class);
	}
	public void setModifiedInterfaces(Set<ClassDiffResult> modifiedInterfaces) {
		this.modifiedInterfaces = modifiedInterfaces;
	}

	public Set<ConstructId> getDeletedInterfaces() {
		return deletedInterfaces;
	}
	public void addDeletedInterface(ConstructId _class) {
		if(this.deletedInterfaces==null) this.deletedInterfaces = new HashSet<ConstructId>();
		this.deletedInterfaces.add(_class);
	}
	public void setDeletedInterfaces(Set<ConstructId> deletedInterfaces) {
		this.deletedInterfaces = deletedInterfaces;
	}

	public Set<ConstructId> getDeprecatedInterfaces() {
		return deprecatedInterfaces;
	}
	public void addDeprecatedInterface(ConstructId _class) {
		if(this.deprecatedInterfaces==null) this.deprecatedInterfaces = new HashSet<ConstructId>();
		this.deprecatedInterfaces.add(_class);
	}
	public void setDeprecatedInterfaces(Set<ConstructId> deprecatedInterfaces) {
		this.deprecatedInterfaces = deprecatedInterfaces;
	}

	public Set<ConstructId> getUndeprecatedInterfaces() {
		return undeprecatedInterfaces;
	}
	public void addUndeprecatedInterface(ConstructId _class) {
		if(this.undeprecatedInterfaces==null) this.undeprecatedInterfaces = new HashSet<ConstructId>();
		this.undeprecatedInterfaces.add(_class);
	}
	public void setUndeprecatedInterfaces(Set<ConstructId> undeprecatedInterfaces) {
		this.undeprecatedInterfaces = undeprecatedInterfaces;
	}

	/**
	 * Returns true if the given construct is deleted from the class, the entire class is deleted or the package is deleted.
	 * from the container class.
	 */
	@JsonIgnore
	public boolean isDeleted(@NotNull ConstructId _cid) {
		// Package deleted
		final ConstructId pack = JarDiffResult.getPackageOf(_cid);
		boolean del = this.deletedPackages!=null && pack!=null &&  this.deletedPackages.contains(pack);
		
		if(_cid.getType()!= ConstructType.PACK){
			// Class deleted
			if(!del) {
				final ConstructId clas = JarDiffResult.getClassOf(_cid);
				del = this.deletedClasses!=null && this.deletedClasses.contains(clas);
			}
			
			// Construct deleted
			if(!del && this.modifiedClasses!=null) {
				for(ClassDiffResult cdr: this.modifiedClasses) {
					if(cdr.isDeleted(_cid)) {
						del = true;
						break;
					}
				}
			}
		}
		return del;
	}

	/**
	 * Returns true if ...
	 * from the container class.
	 */
	@JsonIgnore
	public boolean isBodyChanged(@NotNull ConstructId _cid) {
		boolean mod = false;
		if(this.modifiedClasses!=null) {
			for(ClassDiffResult cdr: this.modifiedClasses) {
				if(cdr.isBodyChanged(_cid)) {
					mod = true;
					break;
				}
			}
		}
		return mod;
	}

	/**
	 * Returns a {@link ConstructId} of type {@link ConstructType#PACK} in which the given construct ID has been declared.
	 * @param _cid
	 * @return
	 */
	public static ConstructId getPackageOf(ConstructId _cid) {
		ConstructId ccid = null;

		if(_cid.getType()==ConstructType.PACK)
			ccid = _cid;

		else if(_cid.getType()==ConstructType.CLAS) {
			int idx = _cid.getQname().lastIndexOf(".");
			//final String ctx = _cid.getQname().substring(0, idx);
			if(idx==-1)
				ccid = null; //throw new IllegalArgumentException("Cannot determine . in qname [" + _cid.getQname() + "]");
			else
				ccid = new ConstructId(_cid.getLang(), ConstructType.PACK, _cid.getQname().substring(0, idx));
		}
		else if(_cid.getType()==ConstructType.INIT || _cid.getType()==ConstructType.METH)
			ccid = JarDiffResult.getPackageOf(JarDiffResult.getClassOf(_cid));		
		else if(_cid.getType()==ConstructType.CONS)
			ccid = JarDiffResult.getPackageOf(JarDiffResult.getClassOf(_cid));
		return ccid;
	}
	
	/**
	 * Returns a {@link ConstructId} of type {@link ConstructType#CLAS} in which the given construct ID has been declared.
	 * @param _cid
	 * @return
	 */
	public static ConstructId getClassOf(ConstructId _cid) {
		ConstructId ccid = null;

		if(_cid.getType()==ConstructType.PACK)
			throw new IllegalArgumentException("Cannot determine class of a package");

		else if(_cid.getType()==ConstructType.CLAS)
			ccid = _cid;

		else if(_cid.getType()==ConstructType.INIT || _cid.getType()==ConstructType.METH) {
			int idx = _cid.getQname().lastIndexOf(".");
			//final String ctx = _cid.getQname().substring(0, idx);
			if(idx==-1)
				throw new IllegalArgumentException("Cannot determine . in qname [" + _cid.getQname() + "]");
			else
				ccid = new ConstructId(_cid.getLang(), ConstructType.CLAS, _cid.getQname().substring(0, idx));
		}		
		else if(_cid.getType()==ConstructType.CONS) {
			final int idx = _cid.getQname().lastIndexOf("(");
			if(idx==-1)
				throw new IllegalArgumentException("Cannot determine . in qname [" + _cid.getQname() + "]");
			else
				ccid = new ConstructId(_cid.getLang(), ConstructType.CLAS, _cid.getQname().substring(0, idx));
		}
		return ccid;
	}
}
