package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;

public class PathNode implements Serializable {
	
	private ConstructId constructId;
	
	private Library lib;
	
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
