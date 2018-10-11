package com.sap.psr.vulas.sign;

import java.util.Set;

/**
 * A signature change represents a set of modifications that must be applied in order to transform one signature into another one.
 * @see SignatureComparator
 */
public interface SignatureChange {

	/**
	 * Returns a set of modifications required to transform one signature into another one.
	 * @return
	 */
	public Set<Object> getModifications();

	public String toJSON();

	/**
	 * Returns true if the signature's structure was changed, e.g., if statements have been moved, inserted or deleted.
	 * Returns false otherwise.  
	 * @return
	 */
	public boolean isStructuralChange();
	
	public boolean isEmpty();
}
