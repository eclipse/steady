package com.sap.psr.vulas.java.sign;

import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Required to invoke the Eclipse JDT plugin.
 *
 */
public class DistillerUtil {

	protected static final Injector mInjector = Guice.createInjector(new JavaChangeDistillerModule());
	protected static final StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "", 0);
	protected static final Distiller mDistiller = mInjector.getInstance(DistillerFactory.class).create(structureEntity);

}
