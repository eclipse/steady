package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;

public class StopWatchTest {

	@Test
    public void testGetRuntimeMillis() {
    	final StopWatch sw = new StopWatch("foo").start();
    	sw.lap("bar", false); // Should not be printed due to threshold
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
    	sw.lap("baz", true);
    	sw.stop();    	
    	final long rt = sw.getRuntimeMillis();
    	assertTrue(rt < 50000);
    }
	
	@Test
    public void testProgressTracker() {
		int total = 67;
    	final StopWatch sw = new StopWatch("foo").setTotal(total).start();
    	for(int i=0; i<total; i++) {
	    	try {
				Thread.sleep(Math.round(Math.random()*178));
				sw.progress(); // log messages every 5%
			} catch (InterruptedException e) {}
    	}
    	sw.stop();
    	System.out.println("Avg lap time: " + StringUtil.nanoToFlexDurationString(sw.getAvgLapTime()) + ", max lap time: " + StringUtil.nanoToFlexDurationString(sw.getMaxLapTime()));
    }
}
