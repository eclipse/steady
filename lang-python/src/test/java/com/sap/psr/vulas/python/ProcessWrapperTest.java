package com.sap.psr.vulas.python;

import java.nio.file.Paths;

import org.junit.Test;

public class ProcessWrapperTest {

	@Test(expected=com.sap.psr.vulas.python.ProcessWrapperException.class)
	public void testIllegalChar() throws ProcessWrapperException {
		ProcessWrapper pw = new ProcessWrapper();
		pw.setCommand(Paths.get("pip"), "foo", "---..--", "\\asas");
	}
	
	@Test//(expected=com.sap.psr.vulas.python.ProcessWrapperException.class)
	public void testLegalChar() throws ProcessWrapperException {
		ProcessWrapper pw = new ProcessWrapper();
		pw.setCommand(Paths.get("pip"), "bar", "---..--");
	}
}
