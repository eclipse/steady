package com.sap.psr.vulas.cg.wala;

import com.sap.psr.vulas.cg.AbstractConstructorFactory;
import com.sap.psr.vulas.cg.ICallgraphConstructor;
import com.sap.psr.vulas.shared.json.model.Application;

/**
 * The concrete WalaFactory inheriting AbstractConstructorFactory
 *
 */
public class WalaFactory extends AbstractConstructorFactory {

	// Return a new wala callgraph constructor
	public ICallgraphConstructor createConstructor(Application _ctx) {
		return new WalaCallgraphConstructor(_ctx);
	}

}
