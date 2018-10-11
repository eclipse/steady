package com.sap.psr.vulas.shared.model;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.Constants;

public class ApplicationTest {

	@Test(expected=IllegalArgumentException.class)
	public void testArgumentNull() {
		new Application(null, "foo", "bar");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArgumentTooLong() {
		final StringBuilder g = new StringBuilder();
		for(int i=0; i<=Constants.MAX_LENGTH_GROUP+10; i++)
			g.append("a");
		new Application(g.toString(), "foo", "bar");
	}
}
