package com.sap.psr.vulas.monitor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.sap.psr.vulas.monitor.trace.PathNode;
import com.sap.psr.vulas.monitor.trace.StackTraceUtil;

public class ConstructTransformerTest {
	
    @Test
	public void testLoaderHierarchy() {
		final LoaderHierarchy h = new LoaderHierarchy();
		final Loader l = h.add(this.getClass().getClassLoader());
		assertEquals(l.isLeaf(), true);
		assertEquals(l.isRoot(), false);
	}
    
    /**
     * This method overloads the other method having the same name, which allows testing whether StackTraceUtil can successfully identify the right method based on line numbers.
     * @param _i
     */
    public Throwable stacktraceTest(int _i) {
    	return new Throwable();
    }
    
    @Test
    public void stacktraceTest() {
    	StackTraceUtil util = new StackTraceUtil();

    	util.setStopAtJUnit(true);
    	List<PathNode> path = util.transformStackTrace(this.stacktraceTest(1).getStackTrace(), null);
    	assertEquals(true, path.size()==2);
    	
    	util.setStopAtJUnit(false);
    	path = util.transformStackTrace(this.stacktraceTest(1).getStackTrace(), null);
    	assertEquals(true, path.size()>1);
    }
}