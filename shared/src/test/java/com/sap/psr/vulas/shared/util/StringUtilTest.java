package com.sap.psr.vulas.shared.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class StringUtilTest {

  @Test
  public void testJoin() {
    final String[] a = new String[] {"a", "b"};
    final String j1 = StringUtil.join(a, ", ");
    assertEquals("a, b", j1);

    final List<String> s = new ArrayList<String>();
    s.add("1");
    s.add("2");
    final String j2 = StringUtil.join(s, ", ");
    assertEquals("1, 2", j2);
  }

  @Test
  public void testIsEmptyOrContainsEmptyString() {
    assertEquals(true, StringUtil.isEmptyOrContainsEmptyString(null));
    assertEquals(true, StringUtil.isEmptyOrContainsEmptyString(new String[] {}));
    assertEquals(true, StringUtil.isEmptyOrContainsEmptyString(new String[] {""}));
    assertEquals(false, StringUtil.isEmptyOrContainsEmptyString(new String[] {"foo"}));
    assertEquals(false, StringUtil.isEmptyOrContainsEmptyString(new String[] {"foo", "bar"}));
  }

  @Test
  public void testPadLeft() {
    assertEquals("  1", StringUtil.padLeft(1, 3)); // Padding of a shorter string representation
    assertEquals(
        "11111",
        StringUtil.padLeft(11111, 3)); // Padding of a longer string representation must not cut
  }
}
