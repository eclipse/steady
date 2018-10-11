package com.sap.psr.vulas.shared.json.model.diff;

import java.util.Collection;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.ConstructId;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassDiffResult {

	@JsonProperty("class")
	private ConstructId clazz = null;

	private Collection<ConstructId> removedConstructors = null;
	private Collection<ConstructId> removedMethods = null;

	private Collection<ConstructId> deprecatedConstructors = null;
	private Collection<ConstructId> deprecatedMethods = null;

	private Collection<ClassModification> modifiedConstructors = null;
	private Collection<ClassModification> modifiedMethods = null;

	private Collection<ConstructId> undeprecatedConstructors = null;
	private Collection<ConstructId> undeprecatedMethods = null;

	private Collection<ConstructId> newConstructors = null;
	private Collection<ConstructId> newMethods = null;

	public ConstructId getClazz() {
		return this.clazz;
	}
	public void setClazz(ConstructId clazz) {
		this.clazz = clazz;
	}

	public Collection<ConstructId> getRemovedConstructors() {
		return removedConstructors;
	}
	public void addRemovedContructor(ConstructId _c) {
		if(this.removedConstructors==null) this.removedConstructors = new TreeSet<ConstructId>();
		this.removedConstructors.add(_c);
	}
	public void setRemovedConstructors(Collection<ConstructId> removedConstructors) {
		this.removedConstructors = removedConstructors;
	}
	public void addRemovedMethod(ConstructId _m) {
		if(this.removedMethods==null) this.removedMethods = new TreeSet<ConstructId>();
		this.removedMethods.add(_m);
	}
	public Collection<ConstructId> getRemovedMethods() {
		return removedMethods;
	}
	public void setRemovedMethods(Collection<ConstructId> removedMethods) {
		this.removedMethods = removedMethods;
	}
	public Collection<ConstructId> getDeprecatedConstructors() {
		return deprecatedConstructors;
	}
	public void addDeprecatedContructor(ConstructId _c) {
		if(this.deprecatedConstructors==null) this.deprecatedConstructors = new TreeSet<ConstructId>();
		this.deprecatedConstructors.add(_c);
	}
	public void setDeprecatedConstructors(Collection<ConstructId> deprecatedConstructors) {
		this.deprecatedConstructors = deprecatedConstructors;
	}
	public Collection<ConstructId> getDeprecatedMethods() {
		return deprecatedMethods;
	}
	public void addDeprecatedMethod(ConstructId _m) {
		if(this.deprecatedMethods==null) this.deprecatedMethods = new TreeSet<ConstructId>();
		this.deprecatedMethods.add(_m);
	}
	public void setDeprecatedMethods(Collection<ConstructId> deprecatedMethods) {
		this.deprecatedMethods = deprecatedMethods;
	}
	public Collection<ClassModification> getModifiedConstructors() {
		return modifiedConstructors;
	}
	public void addModifiedContructor(ClassModification _c) {
		if(this.modifiedConstructors==null) this.modifiedConstructors = new TreeSet<ClassModification>();
		this.modifiedConstructors.add(_c);
	}
	public void setModifiedConstructors(Collection<ClassModification> modifiedConstructors) {
		this.modifiedConstructors = modifiedConstructors;
	}
	public Collection<ClassModification> getModifiedMethods() {
		return modifiedMethods;
	}
	public void addModifiedMethod(ClassModification _m) {
		if(this.modifiedMethods==null) this.modifiedMethods = new TreeSet<ClassModification>();
		this.modifiedMethods.add(_m);
	}
	public void setModifiedMethods(Collection<ClassModification> modifiedMethods) {
		this.modifiedMethods = modifiedMethods;
	}
	public Collection<ConstructId> getUndeprecatedConstructors() {
		return undeprecatedConstructors;
	}
	public void addUndeprecatedContructor(ConstructId _c) {
		if(this.undeprecatedConstructors==null) this.undeprecatedConstructors = new TreeSet<ConstructId>();
		this.undeprecatedConstructors.add(_c);
	}
	public void setUndeprecatedConstructors(Collection<ConstructId> undeprecatedConstructors) {
		this.undeprecatedConstructors = undeprecatedConstructors;
	}
	public Collection<ConstructId> getUndeprecatedMethods() {
		return undeprecatedMethods;
	}
	public void addUndeprecatedMethod(ConstructId _m) {
		if(this.undeprecatedMethods==null) this.undeprecatedMethods = new TreeSet<ConstructId>();
		this.undeprecatedMethods.add(_m);
	}
	public void setUndeprecatedMethods(Collection<ConstructId> undeprecatedMethods) {
		this.undeprecatedMethods = undeprecatedMethods;
	}
	public Collection<ConstructId> getNewConstructors() {
		return newConstructors;
	}
	public void addNewContructor(ConstructId _c) {
		if(this.newConstructors==null) this.newConstructors = new TreeSet<ConstructId>();
		this.newConstructors.add(_c);
	}
	public void setNewConstructors(Collection<ConstructId> newConstructors) {
		this.newConstructors = newConstructors;
	}
	public Collection<ConstructId> getNewMethods() {
		return newMethods;
	}
	public void addNewMethod(ConstructId _m) {
		if(this.newMethods==null) this.newMethods = new TreeSet<ConstructId>();
		this.newMethods.add(_m);
	}
	public void setNewMethods(Collection<ConstructId> newMethods) {
		this.newMethods = newMethods;
	}

	/**
	 * Returns true if the given {@link ConstructId} is deleted.
	 */
	@JsonIgnore
	public boolean isDeleted(@NotNull ConstructId _cid) {
		return (_cid.getType()==ConstructType.CONS && this.removedConstructors!=null && this.removedConstructors.contains(_cid)) ||
				(_cid.getType()==ConstructType.METH && this.removedMethods!=null && this.removedMethods.contains(_cid));
	}

	/**
	 * Returns true if the given {@link ConstructId} is modified.
	 */
	@JsonIgnore
	public boolean isBodyChanged(@NotNull ConstructId _cid) {
		boolean mod = false;
		if(_cid.getType()==ConstructType.CONS && this.modifiedConstructors!=null) {
			for(ClassModification m: this.modifiedConstructors) {
				if(m.getNewConstruct().getQname().equals(_cid.getQname()) && m.isBodyChanged()) {
					mod = true;
					break;
				}
			}
		}
		else if(_cid.getType()==ConstructType.METH && this.modifiedMethods!=null) {
			for(ClassModification m: this.modifiedMethods) {			
				if(m.getNewConstruct().getQname().equals(_cid.getQname()) && m.isBodyChanged()) {
					mod = true;
					break;
				}
			}
		}
		return mod;
	}
}
