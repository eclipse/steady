package com.sap.psr.vulas.shared.json.model.diff;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.json.model.ConstructId;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassModification implements Comparable {

	private ConstructId oldConstruct = null;
	private ConstructId newConstruct = null;
	private boolean declarationChanged;
	private boolean bodyChanged;
	
	public ConstructId getOldConstruct() {
		return oldConstruct;
	}
	public void setOldConstruct(ConstructId oldConstruct) {
		this.oldConstruct = oldConstruct;
	}
	public ConstructId getNewConstruct() {
		return newConstruct;
	}
	public void setNewConstruct(ConstructId newConstruct) {
		this.newConstruct = newConstruct;
	}
	public boolean isDeclarationChanged() {
		return declarationChanged;
	}
	public void setDeclarationChanged(boolean declarationChanged) {
		this.declarationChanged = declarationChanged;
	}
	public boolean isBodyChanged() {
		return bodyChanged;
	}
	public void setBodyChanged(boolean bodyChanged) {
		this.bodyChanged = bodyChanged;
	}
	
	public int compareTo(Object _other) {
		if(_other instanceof ClassModification)
			return this.getNewConstruct().getQname().compareTo(((ClassModification)_other).getNewConstruct().getQname());
		else
			throw new IllegalArgumentException("Expected object of type [" + ClassModification.class.getName() + "], got " + _other.getClass().getName());
	}
}
