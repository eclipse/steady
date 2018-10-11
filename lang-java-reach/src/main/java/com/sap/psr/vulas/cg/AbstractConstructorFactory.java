package com.sap.psr.vulas.cg;

import com.sap.psr.vulas.cg.wala.WalaFactory;
import com.sap.psr.vulas.shared.json.model.Application;

/**
 * Design pattern: abstract factory
 * Create a callgraph constructor: either using soot framework or wala framework; hide the concrete framework-specified stuff from end-users
 *
 */
public abstract class AbstractConstructorFactory {
	
	public abstract ICallgraphConstructor createConstructor(Application _ctx);
	
	/**
	 * Choose framework between soot and wala
	 * @param _choice
	 * @return
	 */
	public static AbstractConstructorFactory getFactory (String _choice) {
		AbstractConstructorFactory f = null;
		f = new WalaFactory();
		return f;
	}
}
