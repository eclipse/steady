package com.sap.psr.vulas.shared.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ScopeTest {

	@Test
	public void testFromStringArray() {
		final String[] scope_strings = new String[] { "teST", "compiLE" };
		final Set<Scope> scopes = Scope.fromStringArray(scope_strings);
		assertTrue(scopes.contains(Scope.TEST));
		assertTrue(scopes.contains(Scope.COMPILE));
		assertEquals(2, scopes.size());
	}
	
	@Test
	public void testFromString() {
		assertEquals(Scope.TEST, Scope.fromString("teST", null));
		assertEquals(Scope.TEST, Scope.fromString("123", Scope.TEST));
		assertEquals(null, Scope.fromString("123", null));
	}
}
