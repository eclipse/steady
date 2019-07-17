package com.sap.psr.vulas.sign;

import java.util.Set;

/**
 * A signature change represents a set of modifications that must be applied in order to transform one signature into another one.
 *
 * @see SignatureComparator
 */
public interface SignatureChange {

	/**
	 * Returns a set of modifications required to transform one signature into another one.
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Object> getModifications();

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toJSON();

	/**
	 * Returns true if the signature's structure was changed, e.g., if statements have been moved, inserted or deleted.
	 * Returns false otherwise.
	 *
	 * @return a boolean.
	 */
	public boolean isStructuralChange();
	
	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty();
}
