package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;

public class StringFilterTest {

	/**
	 * Tests the use of {@link StringList} for blacklists and whitelists.
	 */
	@Test
    public void testStringFilter() {
    	final String[] itemlist = new String[] { "foo.bar" };
    	
    	// Use as blacklist for classnames
    	final StringList blacklist = new StringList(itemlist);
    	assertTrue(  blacklist.contains("foo.bar"));
    	assertTrue( !blacklist.contains("FOO.bar"));
    	assertTrue(  blacklist.contains("FOO.bar", ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE));
    	assertTrue( !blacklist.contains("top.down"));
    	
    	// Use as whitelist for packages
    	final StringList whitelist = new StringList(itemlist);
    	assertTrue(  blacklist.contains("foo.bar.Test",  ComparisonMode.STARTSWITH, CaseSensitivity.CASE_SENSITIVE));
    	assertTrue(  blacklist.contains("foo.BAR.Test",  ComparisonMode.STARTSWITH, CaseSensitivity.CASE_INSENSITIVE));
    	assertTrue( !blacklist.contains("foo.BAR.Test",  ComparisonMode.STARTSWITH, CaseSensitivity.CASE_SENSITIVE));
    	assertTrue( !blacklist.contains("top.down.Test", ComparisonMode.STARTSWITH, CaseSensitivity.CASE_SENSITIVE));
    	
    	// Use as blacklist for JAR file names
    	final StringList jar_bl = new StringList(new String[] { "vulas-core-.*\\.jar" });
    	assertTrue(  jar_bl.contains("vulas-core-1.1.0-SNAPSHOT-jar-with-dependencies.jar",  ComparisonMode.PATTERN, CaseSensitivity.CASE_INSENSITIVE));
    	assertTrue( !jar_bl.contains("commons-fileupload-1.2.1.jar",  ComparisonMode.PATTERN, CaseSensitivity.CASE_INSENSITIVE));
    }
}
