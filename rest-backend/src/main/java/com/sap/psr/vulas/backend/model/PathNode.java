package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

@Embeddable
public class PathNode implements Serializable {
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "constructId",  referencedColumnName = "id")
	private ConstructId constructId;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "lib",  referencedColumnName = "digest")
	private Library lib;
	
	@Transient
	private Dependency dep;
	
	public PathNode() { super(); }
	
	public PathNode(ConstructId _cid) {
		this(_cid, null);
	}
	
	public PathNode(ConstructId _cid, Library _lib) {
		super();
		this.constructId = _cid;
		this.lib = _lib;
	}
	
	public ConstructId getConstructId() { return constructId; }
	public void setConstructId(ConstructId constructId) { this.constructId = constructId; }
	
	public Library getLib() { return lib; }
	public void setLib(Library lib) { this.lib = lib; }

	public Dependency getDep() { return dep; }
	public void setDep(Dependency dep) { this.dep = dep; }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
		result = prime * result + ((lib == null) ? 0 : lib.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathNode other = (PathNode) obj;
		if (constructId == null) {
			if (other.constructId != null)
				return false;
		} else if (!constructId.equals(other.constructId))
			return false;
		if (lib == null) {
			if (other.lib != null)
				return false;
		} else if (!lib.equals(other.lib))
			return false;
		return true;
	}
}
