package com.sap.psr.vulas.shared.categories;

import org.junit.Test;

/**
 * Indicates that a given {@link Test} is an integration test, thus, requires the availability of other networked services.
 *
 */
public interface Integrated extends RequiresNetwork {}
