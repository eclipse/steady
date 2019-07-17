package com.sap.psr.vulas.shared.util;

import java.util.Set;

/**
 * <p>CollectionUtil class.</p>
 *
 */
public class CollectionUtil<T> {
	/**
	 * <p>haveIntersection.</p>
	 *
	 * @param _s1 a {@link java.util.Set} object.
	 * @param _s2 a {@link java.util.Set} object.
	 * @return a boolean.
	 */
	public boolean haveIntersection(Set<T> _s1, Set<T> _s2) {
		for(T o1: _s1)
			for(T o2: _s2)
				if(o1.equals(o2))
					return true;
		return false;
	}
}
